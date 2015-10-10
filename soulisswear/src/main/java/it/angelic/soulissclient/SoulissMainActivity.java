package it.angelic.soulissclient;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class SoulissMainActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_souliss_main);
        Log.i("SoulissWear", "onCreate");

        buildWearableOnlyNotification("Massimo", "casino", true);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                //displaySpeechRecognizer();
                //showNotificationAle(SoulissMainActivity.this);

            }
        });
    }

    /**
     * Builds a simple notification on the wearable.
     */
    private void buildWearableOnlyNotification(String title, String content,
                                               boolean withDismissal) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_phone_android_24dp)
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

    public static void showNotificationAle(Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        // Create the launch intent, in this case setting it as the content action
        Intent launchMuzeiIntent = new Intent(context,
                ActivateSoulissIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                launchMuzeiIntent, 0);
        Notification notif = new Notification.Builder(context)
                .extend(new Notification.WearableExtender()
                        .setDisplayIntent(pendingIntent)
                        .setCustomSizePreset(Notification.WearableExtender.SIZE_MEDIUM))
                .build();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(3113, notif);

    }

    public static void showNotification(Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        // Set up your notification as normal

        // Create the launch intent, in this case setting it as the content action
        Intent launchMuzeiIntent = new Intent(context,
                ActivateSoulissIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                launchMuzeiIntent, 0);
        builder.addAction(new Notification.Action.Builder(R.drawable.ic_phone_android_24dp,
                context.getString(R.string.common_open_on_phone), pendingIntent)
                .extend(new Notification.Action.WearableExtender()
                        .setAvailableOffline(false))
                .build());
        builder.extend(new Notification.WearableExtender()
                .setContentAction(0));
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("SMALL1");
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);

        Notification stacchio = builder.build();

        notificationManager.notify(3113, builder.build());
        Log.i("SoulissWear", "notified: "+builder.build().toString());
        // Send the notification with notificationManager.notify as usual
    }


    private static final int SPEECH_REQUEST_CODE = 9876;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            final String spokenText = results.get(0);
            // Do something with spokenText
            Log.i("SoulissWear", "Command received: " + spokenText);
            mTextView.setText(spokenText);
            Intent intentFireAction = new Intent();
            String[] bar = results.toArray(new String[results.size()]);
            intentFireAction.putExtra( RecognizerIntent.EXTRA_RESULTS ,  bar);
            intentFireAction.setAction("it.angelic.soulissclient.WEAR_VOICE_COMMAND");
            Log.i("SoulissWear", "intent SENT: " + intentFireAction.getAction());
            sendBroadcast(intentFireAction);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
