package it.angelic.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Locale;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.VoiceCommandActivityNoDisplay;

/**
 * This Activity can be used to send commands to Souliss framework, relaying
 * on Souliss voice API
 *
 * @see VoiceCommandActivityNoDisplay
 */
public class AutomateReceiver extends BroadcastReceiver {

    private static final String TAG = "SoulissWidget";

    @Override
    public void onReceive(@NonNull final Context context, final Intent intent) {

        if (!Constants.ACTION_SEND_COMMAND.equals(intent.getAction())) {
            Log.e(Constants.TAG,
                    String.format(Locale.US, "Received unexpected Intent action %s", intent.getAction())); //$NON-NLS-1$
            return;
        }
        Log.d(TAG, "SoulissAutomateReceiver onReceive intent action: " + intent.getAction());
        //final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        // ArrayList<String> thingsYouSaid = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (intent.getData() != null) {
            Log.w(TAG, "SoulissAutomateReceiver: activating command: " + intent.getData().toString());

            VoiceCommandActivityNoDisplay.interpretCommand(context, intent.getData().toString());
        } else {
            Log.w(TAG, "SoulissAutomateReceiver: empty Action Received");
        }
    }


}
