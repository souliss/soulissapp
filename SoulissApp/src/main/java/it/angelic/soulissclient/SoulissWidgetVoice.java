package it.angelic.soulissclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

public class SoulissWidgetVoice extends AppWidgetProvider {

    private static final String TAG = "SoulissWidget";
    private static SoulissDBHelper db;
    private Handler handler;

    private SoulissPreferenceHelper opzioni;

    /**
     * Chiamato per refresh del widget, anche dalla rete (stateresponse e pollResponse)
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    public static void forcedUpdate(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        Log.w(TAG, "forcedUpdate for widgetId:" + appWidgetId);
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_voice);



        // this intent points to activity that should handle results
        Intent activityIntent = new Intent( );
        activityIntent.setComponent(new ComponentName("it.angelic.soulissclient", "WrapperActivity"));
        // this intent wraps results activity intent
        PendingIntent resultsPendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

        // this intent calls the speech recognition
        Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        voiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT, resultsPendingIntent);

        Bundle fakeBun = new Bundle();
        fakeBun.putChar("fake", 'f');
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE, fakeBun);

        // this intent wraps voice recognition intent
        PendingIntent pendingInt = PendingIntent.getActivity(context, 0, voiceIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.button1, pendingInt);

        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        // Toast.makeText(context, "forcedUpdate(), node " +
        // String.valueOf(node), Toast.LENGTH_LONG).show();
    }



    @Override
    public void onReceive(@NonNull final Context context, final Intent intent) {
        opzioni = new SoulissPreferenceHelper(context);
        handler = new Handler();
        super.onReceive(context, intent);
        Log.w(TAG, "onReceive from intent: " + intent.getPackage());
        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        final int got = intent.getIntExtra("_ID", -1);
        ArrayList<String> thingsYouSaid = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (thingsYouSaid != null ) {
            Log.w(TAG, "widget VOICE command from id:" + thingsYouSaid.get(0));
        }
        if (got != -1) {
            Log.w("SoulissWidget", "PRESS");

            final RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape_active);
            //updateViews.setTextViewText(R.id.wid_node, context.getString(R.string.node) + " " + node);
            //updateViews.setTextViewText(R.id.wid_typical, context.getString(R.string.slot) + " " + slot);

            // UPDATE SINCRONO
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra("_ID", got);
            Uri data = Uri.withAppendedPath(Uri.parse("W://widget/id/"), String.valueOf(got));
            intent.setData(data);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.button1, pendingIntent);
            awm.updateAppWidget(got, updateViews);

            new Thread(new Runnable() {
                private SoulissDBHelper db;

                @Override
                public void run() {
                    Looper.prepare();

                    db = new SoulissDBHelper(context);
                    SoulissDBHelper.open();


                }
            }).start();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.w(TAG, "widget onUpdate for " + appWidgetIds.length + " widgets");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ComponentName thisWidget = new ComponentName(context, SoulissWidgetVoice.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            forcedUpdate(context, appWidgetManager, widgetId);

        }
    }

}
