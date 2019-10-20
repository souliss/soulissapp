package it.angelic.soulissclient;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import static it.angelic.soulissclient.Constants.TAG;

/**
 * TEST with
 * adb shell dumpsys activity service GcmService  | findstr it.angelic
 */

public class SoulissZombieRestoreWorker extends Worker {

    public SoulissZombieRestoreWorker(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
    }

    @Override
    public ListenableWorker.Result doWork() {
        // Do your work here.
        Data input = getInputData();
        Log.e(TAG, "WORK SCHEDULER GO");

        // Return a ListenableWorker.Result
        Data outputData = new Data.Builder()
                .putString("A", "DX")
                .build();
        SoulissPreferenceHelper prefs = SoulissApp.getOpzioni();
        if (prefs.isDataServiceEnabled()) {
            Intent eventService = new Intent(SoulissApp.getAppContext(), SoulissDataService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SoulissApp.getAppContext().startForegroundService(eventService);
            } else {
                SoulissApp.getAppContext().startService(eventService);//sempre, ci pensa poi lui
            }
        }

        return Result.success(outputData);
    }

    @Override
    public void onStopped() {
        // Cleanup because you are being stopped.
    }
}