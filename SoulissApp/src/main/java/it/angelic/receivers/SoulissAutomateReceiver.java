package it.angelic.receivers;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import it.angelic.soulissclient.VoiceCommandActivityNoDisplay;

/**
 * This Activity can be used to send commands to Souliss framework, relaying
 * on Souliss voice API
 *
 * @see VoiceCommandActivityNoDisplay
 */
public class SoulissAutomateReceiver extends BroadcastReceiver {

    private static final String TAG = "SoulissWidget";

    @Override
    public void onReceive(@NonNull final Context context, final Intent intent) {

        Log.w(TAG, "SoulissAutomateReceiver onReceive intent action: " + intent.getAction());
        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        ArrayList<String> thingsYouSaid = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (intent.getAction() != null) {
            VoiceCommandActivityNoDisplay.interpretCommand(context, intent.getAction());
        } else {
            Log.w(TAG, "SoulissAutomateReceiver: empty Action Received");
        }
    }


}
