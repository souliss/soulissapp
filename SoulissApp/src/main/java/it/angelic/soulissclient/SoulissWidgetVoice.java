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



        // this intent points to activity that should handle results, doesn't work
        Intent activityIntent = new Intent(SoulissApp.getAppContext(), WrapperActivity.class );
        activityIntent.setFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        //doesn't work as well
        //activityIntent.setComponent(new ComponentName("it.angelic.soulissclient", "it.angelic.soulissclient.WrapperActivity"));
        // this intent wraps results activity intent
        PendingIntent resultsPendingIntent = PendingIntent.getActivity(SoulissApp.getAppContext(), 0, activityIntent, 0);

        // this intent calls the speech recognition
        Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_command_help));
        voiceIntent.setFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT, resultsPendingIntent);

        Bundle fakeBun = new Bundle();
        fakeBun.putInt("widgetId", appWidgetId);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE, fakeBun);

        // this intent wraps voice recognition intent, works
        PendingIntent pendingInt = PendingIntent.getActivity(context, 0, voiceIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.button1, pendingInt);

        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        // Toast.makeText(context, "forcedUpdate(), node " +
        // String.valueOf(node), Toast.LENGTH_LONG).show();
    }



    @Override
    public void onReceive(@NonNull final Context context, final Intent intent) {

        super.onReceive(context, intent);
        Log.w(TAG, "onReceive from intent: " + intent.getPackage());
        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        final int got = intent.getIntExtra("_ID", -1);
        ArrayList<String> thingsYouSaid = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (thingsYouSaid != null ) {
            Log.w(TAG, "widget VOICE command from id:" + thingsYouSaid.get(0));
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
