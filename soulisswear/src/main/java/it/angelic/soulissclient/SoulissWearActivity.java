package it.angelic.soulissclient;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class SoulissWearActivity extends Activity {

    private TextView mTextView;
    private NotificationPresets notfHelper;

    /**
     * Builds a simple notification on the wearable.
     */
    private void buildWearableOnlyNotification(String title, String content,
                                               boolean withDismissal) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_phone_android_32dp)
                .setContentTitle(title)
                .setContentText(content);

        if (withDismissal) {
            Intent dismissIntent = new Intent(Constants.ACTION_DISMISS);
            dismissIntent.putExtra(Constants.KEY_NOTIFICATION_ID, Constants.BOTH_ID);
            PendingIntent pendingIntent = PendingIntent
                    .getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setDeleteIntent(pendingIntent);
        }

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(Constants.WATCH_ONLY_ID, builder.build());
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, Constants.VOICE_REQUEST_OK);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == Constants.VOICE_REQUEST_OK && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            final String spokenText = results.get(0);
            // Do something with spokenText
            if (spokenText != null && spokenText.length() > 0) {
                Log.i("SoulissWear", "Command received: " + spokenText);
                mTextView.setText(spokenText);
                showNotification(SoulissWearActivity.this, spokenText);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_souliss_main);
        Log.i("SoulissWear", "onCreate");
        notfHelper = new NotificationPresets();
        // buildWearableOnlyNotification("Massimo", "casino", true);
        Notification brick = notfHelper.buildPagedNotification(this);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);


        notificationManager.notify(Constants.NOTIFICATION_ID, brick);
        displaySpeechRecognizer();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
       /* stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                //displaySpeechRecognizer();
                //displaySpeechRecognizer();
            }
        });*/
    }

    public void showNotification(Context context, String thevoice) {
        // Notification.Builder builder = new Notification.Builder(context);
        // Set up your notification as normal

        // Create the launch intent, in this case setting it as the content action
        Intent launchMuzeiIntent = new Intent(context,
                SendSoulissCommandIntentService.class);
        launchMuzeiIntent.putExtra("THEVOICE", thevoice);
        launchMuzeiIntent.setAction(Constants.ACTION_SEND_SOULISS_COMMAND);

        SoulissWearActivity.this.startService(launchMuzeiIntent);
        Log.i("SoulissWear", "startService: " + launchMuzeiIntent.toString());

    }
}
