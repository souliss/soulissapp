package it.angelic.soulissclient;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.net.UDPRunnable;

public class SoulissDataService extends Service implements LocationListener {
    // LOGGA a parte
    private static final String TAG = "SoulissDataService";

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    NotificationManager nm;
    private Intent cIntent;
    private String cached;
    // private long uir;
    private SoulissDBHelper db;
    private float homeDist = 0;
    private Calendar lastupd = Calendar.getInstance();
    private LocationManager locationManager;
    private Handler mHandler = new Handler();
    // private Timer timer = new Timer();
    private SoulissPreferenceHelper opts;
    private String provider;
    private Thread udpThread;
    private Runnable mUpdateSoulissRunnable = new Runnable() {

        public void run() {
            // locationManager.requestSingleUpdate(provider, null);
            Log.d(TAG,
                    "Service run " + SoulissDataService.this.hashCode() + " backedoffInterval="
                            + opts.getBackedOffServiceIntervalMsec());
            opts = SoulissApp.getOpzioni();
            if (!opts.isDbConfigured()) {
                Log.w(TAG, "Database empty, closing service");
                // mHandler.removeCallbacks(mUpdateSoulissRunnable);
                reschedule(false);
                // SoulissDataService.this.stopSelf();
                return;
            }
            if (!opts.getCustomPref().contains("numNodi")) {
                Log.w(TAG, "Souliss didn't answer yet, rescheduling");
                // mHandler.removeCallbacks(mUpdateSoulissRunnable);
                reschedule(false);
                return;
            }

			/*
             * if (uir != opts.getDataServiceInterval()) { uir =
			 * opts.getDataServiceInterval(); Log.w(TAG, "pace changed: " +
			 * opts.getDataServiceInterval()); }
			 */
            cached = opts.getAndSetCachedAddress();

            final byte nodesNum = (byte) opts.getCustomPref().getInt("numNodi", 0);

            if (opts.getCustomPref().contains("connection") && cached != null) {
                if (cached.compareTo("") == 0
                        || cached.compareTo(SoulissDataService.this.getResources().getString(R.string.unavailable)) == 0) {
                    Log.e(TAG, "Souliss Unavailable, rescheduling");
                    reschedule(false);
                    // SoulissDataService.this.stopSelf();
                    return;
                }
                // db.open();
                float homeDistPrev = opts.getPrevDistance();
                Log.i(TAG, "Previous distance " + homeDistPrev + " current: " + homeDist);
                // PROGRAMMI POSIZIONALI /
                if (homeDist != homeDistPrev) {
                    processPositionalPrograms(homeDistPrev);
                }

                // Timed commands
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SoulissDBHelper.open();
                        LinkedList<SoulissCommand> unexecuted = db.getUnexecutedCommands(SoulissDataService.this);
                        Log.i(TAG, String.format("checking %d unexecuted TIMED commands ", unexecuted.size()));
                        for (SoulissCommand unexnex : unexecuted) {
                            Calendar now = Calendar.getInstance();
                            if (unexnex.getType() == Constants.COMMAND_TIMED
                                    && now.after(unexnex.getCommandDTO().getScheduledTime())) {
                                // esegui comando
                                Log.w(TAG, "issuing command: " + unexnex.toString());
                                unexnex.execute();
                                unexnex.getCommandDTO().persistCommand();
                                // Se ricorsivo, ricrea
                                if (unexnex.getCommandDTO().getInterval() > 0) {

                                    SoulissCommand nc = new SoulissCommand(
                                            unexnex.getParentTypical());
                                    nc.getCommandDTO().setNodeId(unexnex.getCommandDTO().getNodeId());
                                    nc.getCommandDTO().setSlot(unexnex.getCommandDTO().getSlot());
                                    nc.getCommandDTO().setCommand(unexnex.getCommandDTO().getCommand());
                                    nc.getCommandDTO().setInterval(unexnex.getCommandDTO().getInterval());
                                    Calendar cop = Calendar.getInstance();
                                    cop.add(Calendar.SECOND, unexnex.getCommandDTO().getInterval());
                                    nc.getCommandDTO().setScheduledTime(cop);
                                    nc.getCommandDTO().setType(Constants.COMMAND_TIMED);
                                    nc.getCommandDTO().persistCommand();
                                    Log.w(TAG, "recreate recursive command");
                                }
                                sendProgramNotification(SoulissDataService.this, getString(R.string.timed_program_executed),
                                        unexnex.toString() + " " + unexnex.getParentTypical().toString(),
                                        R.drawable.clock1, unexnex);
                            } else if (unexnex.getType() != Constants.COMMAND_TIMED) {
                                //this is only a check
                                Log.e(TAG, "WTF? nt TIMED?? " + unexnex.getType());
                            }
                        }
                        // db.close();
                    }
                }).start();

                // Check for too long ON status
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Checking warning for long turned-on typicals");
                        SoulissDBHelper.open();
                        int checkd = 0;
                        List<SoulissNode> nodes = db.getAllNodes();
                        Calendar now = Calendar.getInstance();
                        for (SoulissNode piter : nodes) {
                            List<SoulissTypical> slots = piter.getActiveTypicals();
                            for (SoulissTypical tipico : slots) {
                                Date when = tipico.getTypicalDTO().getLastStatusChange();
                                if (when != null && tipico.getOutput() != Constants.Typicals.Souliss_T1n_OffCoil
                                        && tipico.getTypicalDTO().getWarnDelayMsec() > 0
                                        && now.getTime().getTime() - when.getTime() > tipico.getTypicalDTO().getWarnDelayMsec()) {

                                    Log.w(TAG, String.format(getString(R.string.hasbeenturnedontoolong), tipico.getNiceName()));
                                    sendTooLongWarnNotification(SoulissDataService.this, getString(R.string.timed_warning),
                                            String.format(getString(R.string.hasbeenturnedontoolong), tipico.getNiceName()), tipico);
                                    checkd++;
                                }

                            }
                        }
                        Log.i(TAG, "checked timed on  warnings: " + checkd);
                        // db.close();
                    }
                }).start();

				/* SENSORS REFRESH THREAD */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /*
                         * finally { db.close(); }
						 */

                        // spostato per consentire comandi manuali
                        if (!opts.isDataServiceEnabled()) {
                            Log.w(TAG, "Service disabled, is not going to be re-scheduled");
                            mHandler.removeCallbacks(mUpdateSoulissRunnable);
                            // SoulissDataService.this.stopSelf();
                            return;
                        } else {
                            // refresh della subscription
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "issuing subscribe, numnodes=" + nodesNum);
                                    UDPHelper.stateRequest(opts, nodesNum, 0);
                                }
                            }).start();

                            try {
                                // ritarda il logging
                                Thread.sleep(3000);
                                SoulissDBHelper.open();

                                Map<Short, SoulissNode> refreshedNodes = new HashMap<>();

                                List<SoulissNode> ref = db.getAllNodes();
                                for (SoulissNode soulissNode : ref) {
                                    refreshedNodes.put(soulissNode.getId(), soulissNode);
                                }
                                Log.v(TAG, "logging nodes:" + nodesNum);
                                // issueRefreshSensors(ref, refreshedNodes);
                                logThings(refreshedNodes);

                                // try a local reach, just in case ..
                                UDPHelper.checkSoulissUdp(2000, opts, opts.getPrefIPAddress());

                            } catch (Exception e) {
                                Log.e(TAG, "Service error, scheduling again ", e);
                            }

                            Log.i(TAG, "Service end run" + SoulissDataService.this.hashCode());
                            setLastupd(Calendar.getInstance());
                            reschedule(false);
                        }

                    }

                }).start();

            } else {// NO CONNECTION, NO NODES!!
                Log.w(TAG, "Service end but NOTHING DONE");
                Intent i = new Intent();
                i.setAction(Constants.CUSTOM_INTENT);
                setLastupd(Calendar.getInstance());
                getApplicationContext().sendBroadcast(i);
                reschedule(false);
            }
        }

    };

    public static void sendTooLongWarnNotification(Context ctx, String desc, String longdesc, @NonNull SoulissTypical ppr) {
        Intent notificationIntent = new Intent(ctx, TypicalDetailFragWrapper.class);
        notificationIntent.putExtra("TIPICO", ppr);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

        SoulissCommand shutoff = new SoulissCommand(ppr);
        shutoff.getCommandDTO().setCommand(Constants.Typicals.Souliss_T1n_OffCmd);

        Intent mapIntent = new Intent(ctx, SendCommandActivityNoDisplay.class);
        mapIntent.putExtra("COMMAND", shutoff);

        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(ctx, 0, mapIntent, 0);


        builder.setContentIntent(contentIntent).setSmallIcon(android.R.drawable.stat_sys_warning)
                .setLargeIcon(BitmapFactory.decodeResource(res, ppr.getIconResourceId()))
                .setTicker("Turned on warning")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(desc)

                .addAction(R.drawable.ic_cancel_24dp,
                        ctx.getString(R.string.scene_turnoff_lights), mapPendingIntent)
                .setContentText(longdesc);

        Notification n = builder.build();
        nm.notify(664, n);
    }

    public static void sendProgramNotification(Context ctx, String desc, String longdesc, int icon, @Nullable SoulissCommand ppr) {

        Intent notificationIntent = new Intent(ctx, AddProgramActivity.class);
        if (ppr != null)
            notificationIntent.putExtra("PROG", ppr);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

        builder.setContentIntent(contentIntent).setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setLargeIcon(BitmapFactory.decodeResource(res, icon))
                .setTicker("Souliss program activated")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(desc)
                .setContentText(longdesc);
        Notification n = builder.build();
        nm.notify(665, n);
    }

    public Calendar getLastupd() {
        return lastupd;
    }

    public void setLastupd(Calendar lastupd) {
        this.lastupd = lastupd;
        opts.setLastServiceRun(lastupd);
    }

    private void logThings(Map<Short, SoulissNode> refreshedNodes) {
        Log.i(Constants.TAG, "logging sensors for " + refreshedNodes.size() + " nodes");
        for (Short index : refreshedNodes.keySet()) {
            SoulissNode pirt = refreshedNodes.get(index);
            List<SoulissTypical> tips = pirt.getTypicals();
            for (SoulissTypical soulissTypical : tips) {
                if (soulissTypical.isSensor()) {
                    soulissTypical.logTypical();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "service onCreate()");
        opts = SoulissApp.getOpzioni();
        // subito
        startUDPListener();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria crit = new Criteria();
        crit.setPowerRequirement(Criteria.POWER_LOW);
        // riporta exec precedenti, non usare ora attuale
        lastupd.setTimeInMillis(opts.getServiceLastrun());
        provider = locationManager.getBestProvider(crit, true);
        db = new SoulissDBHelper(this);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        cIntent = new Intent(this, SoulissDataService.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "Service Destroy!! " + opts.getBackedOffServiceIntervalMsec());
        mHandler.removeCallbacks(mUpdateSoulissRunnable);
        //db.close();

        if (udpThread != null && udpThread.isAlive()) {
            udpThread.interrupt();
            Log.w(TAG, "UDP Interrupt");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(SoulissDataService.this);
    }

    @Override
    public void onLocationChanged(Location location) {
        opts = SoulissApp.getOpzioni();
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());

        float[] res = new float[3];
        try {
            Location.distanceBetween(lat, lng, opts.getHomeLatitude(), opts.getHomeLongitude(), res);
            Log.d(TAG, "Service received new Position. Home Distance:" + (int) res[0]);
            homeDist = res[0];
            if (opts.getPrevDistance() == 0) {
                Log.w(TAG, "Resetting prevdistance =>" + homeDist);
                opts.setPrevDistance(homeDist);
            } else {
                float homeDistPrev = opts.getPrevDistance();
                processPositionalPrograms(homeDistPrev);
            }

        } catch (Exception e) {// home note set
            homeDist = 0;
            Log.w(TAG, "can't compute home distance, home position not set");
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "Low memory, schedule a reserve task");
        mHandler.postDelayed(mUpdateSoulissRunnable, opts.getDataServiceIntervalMsec() + 1000000);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "Service received Provider Disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "Service received Provider ENABLED");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        opts = SoulissApp.getOpzioni();

        requestBackedOffLocationUpdates();
        // uir = opts.getDataServiceInterval();
        Log.i(TAG, "Service onStartCommand()");
        // delle opzioni

        reschedule(false);

        startUDPListener();

        return START_STICKY;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Service location status Provider changed to: " + status);
    }

    /**
     * Se la distanza attuale rispetto alla precedente supera la soglia,
     * scattano i programmi posizionali.
     * <p/>
     * Se viene rilevato un cambio fascia tra prev e attuale viene anche rischedulato
     * il locationManager
     *
     * @param homeDistPrev
     */
    private void processPositionalPrograms(float homeDistPrev) {
        float distPrevCache = opts.getPrevDistance();
        Log.d(TAG, "process positional programs, homedistanceprev=" + homeDistPrev + " homedist now is=" + homeDist);
        if (homeDistPrev > (opts.getHomeThresholdDistance() - opts.getHomeThresholdDistance() / 10)
                && homeDist < (opts.getHomeThresholdDistance() - opts.getHomeThresholdDistance() / 10)) {
            SoulissDBHelper.open();
            final LinkedList<SoulissCommand> unexecuted = db.getPositionalPrograms(SoulissDataService.this);
            Log.i(TAG, "processing positional programs: " + unexecuted.size());
            // tornato a casa
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (SoulissCommand soulissCommand : unexecuted) {
                        if (soulissCommand.getType() == Constants.COMMAND_COMEBACK_CODE) {
                            Log.w(TAG, "issuing COMEBACK command: " + soulissCommand.toString());
                            soulissCommand.execute();
                            soulissCommand.getCommandDTO().persistCommand();
                            sendProgramNotification(SoulissDataService.this, getString(R.string.positional_executed),
                                    soulissCommand.toString() + " " + soulissCommand.getParentTypical().getNiceName(), R.drawable.exit, soulissCommand);
                        }
                    }
                }
            }).start();
            opts.setPrevDistance(homeDist);
        } else if (homeDistPrev < (opts.getHomeThresholdDistance() + opts.getHomeThresholdDistance() / 10)
                && homeDist > (opts.getHomeThresholdDistance() + opts.getHomeThresholdDistance() / 10)) {
            SoulissDBHelper.open();
            final LinkedList<SoulissCommand> unexecuted = db.getPositionalPrograms(SoulissDataService.this);
            Log.i(TAG, "activating positional programs: " + unexecuted.size());
            // uscito di casa
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (SoulissCommand soulissCommand : unexecuted) {
                        if (soulissCommand.getType() == Constants.COMMAND_GOAWAY_CODE) {
                            Log.w(TAG, "issuing AWAY command: " + soulissCommand.toString());

                            soulissCommand.execute();
                            soulissCommand.getCommandDTO().persistCommand();
                            sendProgramNotification(SoulissDataService.this, getString(R.string.positional_executed),
                                    soulissCommand.getNiceName(), R.drawable.exit, soulissCommand);
                        }
                    }
                }
            }).start();
        } else {// gestione BACKOFF sse e` cambiata la fascia
            if ((homeDist > 25000 && distPrevCache <= 25000) || (homeDist < 25000 && homeDist > 5000 && distPrevCache >= 25000)) {
                Log.w(TAG, "FASCIA 25 " + homeDist);
                requestBackedOffLocationUpdates();
            } else if ((homeDist > 5000 && distPrevCache <= 5000) || (homeDist < 5000 && homeDist > 2000 && distPrevCache >= 5000)) {
                Log.w(TAG, "FASCIA 5 " + homeDist);
                requestBackedOffLocationUpdates();
            } else if ((homeDist > 2000 && distPrevCache <= 2000) || (homeDist < 2000 && distPrevCache >= 2000)) {
                Log.w(TAG, "FASCIA 2 " + homeDist);
                requestBackedOffLocationUpdates();
            }
        }
        //abbiamo processato, e` ora di resettare il prev
        opts.setPrevDistance(homeDist);
    }

    /**
     * Rischedula le richieste posizionali a intervali basati su fascie distanza
     */
    private void requestBackedOffLocationUpdates() {
        try {
            Log.w(TAG, "requesting updates at meters " + homeDist);
            locationManager.removeUpdates(SoulissDataService.this);
            if (homeDist > 25000) {
                locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL * 100,
                        Constants.POSITION_UPDATE_MIN_DIST * 10, SoulissDataService.this);
            } else if (homeDist > 5000) {
                locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL * 10,
                        Constants.POSITION_UPDATE_MIN_DIST * 4, SoulissDataService.this);
            } else if (homeDist > 2000) {
                locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL * 2,
                        Constants.POSITION_UPDATE_MIN_DIST * 2, SoulissDataService.this);
            } else {
                locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL,
                        Constants.POSITION_UPDATE_MIN_DIST, SoulissDataService.this);
            }
        } catch (SecurityException re) {
            Log.e(TAG, "NOT ALLOWED FROM USER PERMISSION", re);
        } catch (Exception e) {
            Log.e(TAG, "location manager updates request FAIL", e);
        }
    }

    /**
     * Schedule a new execution of the srvice via mHandler.postDelayed
     */
    public void reschedule(boolean immediate) {

        opts.initializePrefs();// reload interval

        // the others
        // reschedule self
        Calendar calendar = Calendar.getInstance();
        if (immediate) {
            mHandler.removeCallbacks(mUpdateSoulissRunnable);// the first removes
            Log.i(TAG, "Reschedule immediate");
            mHandler.post(mUpdateSoulissRunnable);
        } else {
            Log.i(TAG, "Regular mode, rescheduling self every " + opts.getDataServiceIntervalMsec() / 1000 + " seconds");

            if (getLastupd().getTime().getTime() + opts.getBackedOffServiceIntervalMsec() < Calendar.getInstance().getTime().getTime()) {
                Log.i(TAG, "DETECTED LATE SERVICE, LAST RUN: " + getLastupd().getTime());

                reschedule(true);
                return;
            }
            // mHandler.postDelayed(mUpdateSoulissRunnable,
            // opts.getDataServiceIntervalMsec());
            /* One of the two should get it */
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            PendingIntent secureShot = PendingIntent.getService(this, 0, cIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            calendar.setTimeInMillis(getLastupd().getTime().getTime());
            calendar.add(Calendar.MILLISECOND, opts.getBackedOffServiceIntervalMsec().intValue());
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), secureShot);
            //will call onStart(), detect late and schedule immediate
            Log.i(TAG, "DATASERVICE SCHEDULED ON: " + calendar.getTime());

        }
        startUDPListener();
    }

    private void startUDPListener() {
        if (udpThread == null || !udpThread.isAlive()) {
            // Create the object with the run() method
            Runnable runnable = new UDPRunnable(opts);
            // Create the udpThread supplying it with the runnable
            // object
            udpThread = new Thread(runnable);
            // Start the udpThread
            udpThread.start();
            Log.i(TAG, "UDP thread started" + opts.getBackedOffServiceIntervalMsec());
        }
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SoulissDataService getService() {
            return SoulissDataService.this;
        }
    }
}
