package it.angelic.soulissclient;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

public class SoulissAutomateReceiver extends BroadcastReceiver {

    private static final String TAG = "SoulissWidget";

    @Override
    public void onReceive(@NonNull final Context context, final Intent intent) {

        Log.w(TAG, "SoulissAutomateReceiver onReceive intent: " + intent.getAction());
        Log.w(TAG, "SoulissAutomateReceiver onReceive URI: " + intent.getData());
        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        ArrayList<String> thingsYouSaid = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (thingsYouSaid != null) {
            Log.w(TAG, "Automate command from id:" + thingsYouSaid.get(0));
        }

    }


}
