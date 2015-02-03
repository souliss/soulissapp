package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.net.UDPRunnable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SoulissDataService extends Service implements LocationListener {
    // LOGGA a parte
    private static final String TAG = "SoulissDataService";

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    private LocationManager locationManager;
    private Calendar lastupd = Calendar.getInstance();
    private Handler mHandler = new Handler();

    // private Timer timer = new Timer();
    private SoulissPreferenceHelper opts;
    // private long uir;
    private SoulissDBHelper db;
    private String provider;
    private float homeDist = 0;
    NotificationManager nm;
    private String cached;
    private Thread udpThread;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "service onCreate()");
        opts = SoulissClient.getOpzioni();
        // subito
        startUDPListener();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria crit = new Criteria();
        crit.setPowerRequirement(Criteria.POWER_LOW);
        // riporta exec precedenti, non usare ora attuale
        lastupd.setTimeInMillis(opts.getServiceLastrun());
        provider = locationManager.getBestProvider(crit, false);
        db = new SoulissDBHelper(this);


        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "Service Destroy!! " + opts.getBackedOffServiceInterval());
        mHandler.removeCallbacks(mUpdateSoulissRunnable);
        db.close();
        locationManager.removeUpdates(SoulissDataService.this);
        if (udpThread != null && udpThread.isAlive())
            udpThread.interrupt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        opts = SoulissClient.getOpzioni();
        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.setTimeInMillis(opts.getNextServiceRun());
        requestBackedOffLocationUpdates();
        // uir = opts.getDataServiceInterval();
        Log.i(TAG, "Service onStartCommand()");
        // delle opzioni
        if (!next.before(now)) {
            Log.w(TAG, "Service next sched outdated, sched NOW");
            reschedule(true);
        }
        startUDPListener();
        return START_STICKY;
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
            Log.i(TAG, "UDP thread started" + opts.getBackedOffServiceInterval());
        }
    }

    /**
     * Schedule a new execution of the srvice via mHandler.postDelayed
     */
    public void reschedule(boolean immediate) {

        opts.initializePrefs();// reload interval
        mHandler.removeCallbacks(mUpdateSoulissRunnable);// the first removes
        // the others
        // reschedule self
        if (immediate) {
            Log.i(TAG, "Reschedule immediate");
            mHandler.post(mUpdateSoulissRunnable);
        } else {
            Log.i(TAG, "Regular mode, rescheduling self every " + opts.getDataServiceIntervalMsec() / 1000 + " seconds");
            // mHandler.postDelayed(mUpdateSoulissRunnable,
            // opts.getDataServiceIntervalMsec());
            /* One of the two should get it */
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent secureShot = PendingIntent.getService(this, 0, new Intent(this, SoulissDataService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MILLISECOND, opts.getDataServiceIntervalMsec());
            opts.setNextServiceRun(calendar.getTimeInMillis());
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), secureShot);

        }
        startUDPListener();
    }

    public Calendar getLastupd() {
        return lastupd;
    }

    public void setLastupd(Calendar lastupd) {
        this.lastupd = lastupd;
        opts.setLastServiceRun(lastupd);
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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "Low memory, schedule a reserve task");
        mHandler.postDelayed(mUpdateSoulissRunnable, opts.getDataServiceIntervalMsec() + 1000000);
    }

    private Runnable mUpdateSoulissRunnable = new Runnable() {

        public void run() {
            // locationManager.requestSingleUpdate(provider, null);
            Log.v(TAG,
                    "Service run " + SoulissDataService.this.hashCode() + " backedoffInterval="
                            + opts.getBackedOffServiceInterval());
            opts = SoulissClient.getOpzioni();
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
                        db.open();
                        LinkedList<SoulissCommand> unexecuted = db.getUnexecutedCommands(SoulissDataService.this);
                        Log.i(TAG, "checked unexecuted commands: " + unexecuted.size());
                        for (SoulissCommand unexnex : unexecuted) {
                            Calendar now = Calendar.getInstance();
                            if (unexnex.getType() == Constants.COMMAND_TIMED
                                    && now.after(unexnex.getCommandDTO().getScheduledTime())) {
                                // esegui comando
                                Log.w(TAG, "issuing command: " + unexnex.toString());
                                unexnex.execute();
                                unexnex.getCommandDTO().persistCommand(db);
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
                                    nc.getCommandDTO().persistCommand(db);
                                    Log.w(TAG, "recreate recursive command");
                                }
                                sendNotification(SoulissDataService.this, "Souliss Timed Program Executed",
                                        unexnex.toString() + " " + unexnex.getParentTypical().toString(),
                                        R.drawable.clock);
                            }
                        }
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
                                db.open();

                                Map<Short, SoulissNode> refreshedNodes = new HashMap<Short, SoulissNode>();

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
                Intent i = new Intent();
                i.setAction(Constants.CUSTOM_INTENT);
                setLastupd(Calendar.getInstance());
                getApplicationContext().sendBroadcast(i);
                reschedule(false);
            }
        }

    };


    private void logThings(Map<Short, SoulissNode> refreshedNodes) {
        Log.i(Constants.TAG, "logging sensors for " + refreshedNodes.size() + " nodes");
        for (Short index : refreshedNodes.keySet()) {
            SoulissNode pirt = refreshedNodes.get(index);
            List<SoulissTypical> tips = pirt.getTypicals();
            for (SoulissTypical soulissTypical : tips) {
                if (soulissTypical.isSensor()) {
                    soulissTypical.getTypicalDTO().logTypical();
                }
            }
        }
    }

    public static void sendNotification(Context ctx, String desc, String longdesc, int icon) {

        Intent notificationIntent = new Intent(ctx, ProgramListActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

        builder.setContentIntent(contentIntent).setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setLargeIcon(BitmapFactory.decodeResource(res, icon)).setTicker("Souliss program activated")
                .setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(desc).setContentText(longdesc);
        Notification n = builder.build();
        nm.notify(665, n);
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
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Service location status Provider changed to: " + status);
    }

    @Override
    public void onLocationChanged(Location location) {
        opts = SoulissClient.getOpzioni();
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
            db.open();
            final LinkedList<SoulissCommand> unexecuted = db.getPositionalPrograms(SoulissDataService.this);
            Log.i(TAG, "activating positional programs: " + unexecuted.size());
            // tornato a casa
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (SoulissCommand soulissCommand : unexecuted) {
                        if (soulissCommand.getType() == Constants.COMMAND_COMEBACK_CODE) {
                            Log.w(TAG, "issuing COMEBACK command: " + soulissCommand.toString());
                            soulissCommand.execute();
                            soulissCommand.getCommandDTO().persistCommand(db);
                            sendNotification(SoulissDataService.this, "Souliss Positional Program Executed",
                                    soulissCommand.toString() + " " + soulissCommand.getParentTypical().toString(), R.drawable.exit);
                        }
                    }
                }
            }).start();
            opts.setPrevDistance(homeDist);
        } else if (homeDistPrev < (opts.getHomeThresholdDistance() + opts.getHomeThresholdDistance() / 10)
                && homeDist > (opts.getHomeThresholdDistance() + opts.getHomeThresholdDistance() / 10)) {
            db.open();
            final LinkedList<SoulissCommand> unexecuted = db.getPositionalPrograms(SoulissDataService.this);
            Log.i(TAG, "activating positional programs: " + unexecuted.size());
            // uscito di casa
            new Thread(new Runnable() {
                @Override
                public void run() {
            for (SoulissCommand soulissCommand : unexecuted) {
                final SoulissCommand cmd = soulissCommand;
                if (soulissCommand.getType() == Constants.COMMAND_GOAWAY_CODE) {
                    Log.w(TAG, "issuing AWAY command: " + soulissCommand.toString());

                            cmd.execute();
                            cmd.getCommandDTO().persistCommand(db);
                            sendNotification(SoulissDataService.this, "Souliss Positional Program Executed",
                                    cmd.toString() + " " + cmd.getParentTypical().toString(), R.drawable.exit);
                        }

                }
            }}).start();
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
        } catch (Exception e) {
            Log.e(TAG, "location manager updates request FAIL", e);
        }
    }
}
