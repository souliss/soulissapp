package it.angelic.soulissclient;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.net.UDPRunnable;

import static it.angelic.soulissclient.Constants.TAG;
import static it.angelic.soulissclient.util.NotificationStaticUtil.sendTooLongWarnNotification;

/**
 * TEST with
 * adb shell dumpsys activity service GcmService  | findstr it.angelic
 */

public class WorkerZombieRestore extends Worker {
    private final SoulissDBHelper db;
    private Context context;
    private SoulissPreferenceHelper opts;
    private Thread udpThread;

    public WorkerZombieRestore(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
        context = appContext;
        opts = SoulissApp.getOpzioni();
        db = new SoulissDBHelper(context);
        startUDPListener();//avra` vita breve
    }

    @Override
    public ListenableWorker.Result doWork() {
        // Do your work here.
        Data input = getInputData();
        Log.e(TAG, "WORK SCHEDULER GO, T alive? " + udpThread.isAlive());

        // locationManager.requestSingleUpdate(provider, null);
        Log.d(TAG,
                "Service doWork id " + this.getId() + " backedoffInterval="
                        + opts.getBackedOffServiceIntervalMsec());

        if (!opts.isDbConfigured()) {
            Log.w(TAG, "Database empty, closing service");
            //lastupd = (Calendar.getInstance());
            // mHandler.removeCallbacks(mUpdateSoulissRunnable);
            //reschedule(false);
            // SoulissDataService.this.stopSelf();
            return Result.failure();
        }
        if (!opts.getCustomPref().contains("numNodi")) {
            Log.w(TAG, "Souliss didn't answer yet, rescheduling");
            //lastupd = (Calendar.getInstance());
            // mHandler.removeCallbacks(mUpdateSoulissRunnable);
            //reschedule(false);
            return Result.failure();
        }

        /*
         * if (uir != opts.getDataServiceInterval()) { uir =
         * opts.getDataServiceInterval(); Log.w(TAG, "pace changed: " +
         * opts.getDataServiceInterval()); }
         */
        String cached = opts.getAndSetCachedAddress();

        final byte nodesNum = (byte) opts.getCustomPref().getInt("numNodi", 0);

        if (cached != null) {
            if (cached.compareTo("") == 0
                    || cached.compareTo(context.getString(R.string.unavailable)) == 0) {
                Log.e(TAG, "Souliss Unavailable, rescheduling");
                //DONT REFRESH last exec. We want a re-schedule soon
                //setLastupd(Calendar.getInstance());
                //reschedule(false);
                // SoulissDataService.this.stopSelf();
                return Result.failure();
            }
            // db.open();
            //float homeDistPrev = opts.getPrevDistance();
            //Log.i(TAG, "Previous distance " + homeDistPrev + " current: TODO");

            // Check for too long ON status

            Log.d(TAG, "Checking warning for long turned-on typicals");
            SoulissDBHelper.open();
            int checkd = 0;
            List<SoulissNode> nodes = db.getAllNodes();
            Calendar now = Calendar.getInstance();
            for (SoulissNode piter : nodes) {
                List<SoulissTypical> slots = piter.getActiveTypicals();
                for (SoulissTypical tipico : slots) {
                    Date when = tipico.getTypicalDTO().getLastStatusChange();
                    if (when != null && (tipico.getOutput() != Constants.Typicals.Souliss_T1n_OffCoil || tipico.getOutput() != Constants.Typicals.Souliss_T1n_OffCoil_Auto)
                            && tipico.getTypicalDTO().getWarnDelayMsec() > 0
                            && now.getTime().getTime() - when.getTime() > tipico.getTypicalDTO().getWarnDelayMsec()) {

                        Log.w(TAG, String.format(context.getString(R.string.hasbeenturnedontoolong), tipico.getNiceName()));
                        sendTooLongWarnNotification(context, context.getString(R.string.timed_warning)
                                , tipico);
                        checkd++;
                    }

                }
            }
            Log.i(TAG, "checked timed on  warnings: " + checkd);


            /* SENSORS REFRESH THREAD */

            // spostato per consentire comandi manuali
            if (!opts.isDataServiceEnabled()) {
                Log.w(TAG, "Worker disabled, is not going to be re-scheduled");
                //lastupd = (Calendar.getInstance());
                // mHandler.removeCallbacks(mUpdateSoulissRunnable);
                // SoulissDataService.this.stopSelf();
                return Result.failure();
            } else {
                // refresh della subscription
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "issuing pollRequest, numnodes=" + nodesNum);
                        UDPHelper.pollRequest(opts, nodesNum, 0);
                    }
                }).start();

                try {
                    // ritarda il logging
                    Thread.sleep(3000);
                    SoulissDBHelper.open();

                    Map<Short, SoulissNode> refreshedNodes = new HashMap<>();

                    List<SoulissNode> ref = db.getAllNodes();
                    for (SoulissNode soulissNode : ref) {
                        refreshedNodes.put(soulissNode.getNodeId(), soulissNode);
                    }
                    Log.v(TAG, "logging nodes:" + nodesNum);
                    // issueRefreshSensors(ref, refreshedNodes);
                    logThings(refreshedNodes);

                    // try a local reach, just in case ..
                    //UDPHelper.checkSoulissUdp(2000, opts, opts.getPrefIPAddress());

                } catch (Exception e) {
                    Log.e(TAG, "Worker error, scheduling again ", e);
                }

                Log.i(TAG, "Service end run, id: " + WorkerZombieRestore.this.getId());
                //lastupd = (Calendar.getInstance());
                // reschedule(false);
            }


        } else {// NO CONNECTION, NO NODES!!
            Log.w(TAG, "Service end but NOTHING DONE, no connection");
            return Result.failure();
        }

        opts.setLastServiceRun(Calendar.getInstance());
        // Return a ListenableWorker.Result
        Data outputData = new Data.Builder()
                .putString("processed", "DX")
                .build();
        return Result.success(outputData);
    }

    private void logThings(Map<Short, SoulissNode> refreshedNodes) {
        Log.i(Constants.TAG, "logging sensors for " + refreshedNodes.size() + " nodes");
        for (SoulissNode pirt : refreshedNodes.values()) {
            //SoulissNode pirt = refreshedNodes.get(index);
            List<SoulissTypical> tips = pirt.getTypicals();
            for (SoulissTypical soulissTypical : tips) {
                if (soulissTypical.isSensor()) {
                    soulissTypical.logTypical();
                }
            }
        }
    }

    @Override
    public void onStopped() {
        // Cleanup because you are being stopped.
        if (udpThread != null && udpThread.isAlive()) {
            udpThread.interrupt();
            Log.w(TAG, "UDP Interrupt");
        }
    }

    private void startUDPListener() {
        if (udpThread == null || !udpThread.isAlive() || udpThread.isInterrupted()) {
            // Create the object with the run() method
            Runnable runnable = new UDPRunnable(opts);
            // Create the udpThread supplying it with the runnable
            // object
            udpThread = new Thread(runnable);
            // Start the udpThread
            udpThread.start();
            Log.i(TAG, "UDP SERVICE thread started" + opts.getBackedOffServiceIntervalMsec());
        }
    }
}