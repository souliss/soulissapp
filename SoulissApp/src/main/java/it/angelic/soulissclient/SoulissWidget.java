package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypicalSensor;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical52TemperatureSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical53HumiditySensor;
import it.angelic.soulissclient.net.UDPHelper;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

public class SoulissWidget extends AppWidgetProvider {

	private static final String TAG = "SoulissWidget";
	private static SoulissDBHelper db;
	private Handler handler;

    private SharedPreferences customSharedPreference;
    private SoulissPreferenceHelper opzioni;

    /**
     * Chiamato per refresh del widget, anche dalla rete (stateresponse e pollResponse)
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
	public static void forcedUpdate(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		SharedPreferences customSharedPreference = context.getSharedPreferences("SoulissWidgetPrefs",
				Activity.MODE_PRIVATE);
        Log.w(TAG, "forcedUpdate for widgetId:" + appWidgetId);
		final int node = customSharedPreference.getInt(appWidgetId + "_NODE", -1);
		final int slot = customSharedPreference.getInt(appWidgetId + "_SLOT", -1);
		final long cmd = customSharedPreference.getLong(appWidgetId + "_CMD", -1);
		final String name = customSharedPreference.getString(appWidgetId + "_NAME", "");

		if (node == -1) {
			Log.e(TAG, "missing widget preferences, aborting");
			return;
		}
		db = new SoulissDBHelper(context);
		db.open();
		final SoulissTypical tgt = db.getSoulissTypical(node, (short) slot);
		tgt.setCtx(context);
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		if (!name.equals(""))
			updateViews.setTextViewText(R.id.button1, name);
		else
			updateViews.setTextViewText(R.id.button1, tgt.getNiceName());
		// TODO edittext name
		updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape);
		updateViews.setInt(R.id.button1, "setBackgroundResource", tgt.getDefaultIconResourceId());
		updateViews.setTextViewText(R.id.wid_node, context.getString(R.string.node) + " " + node);
		updateViews.setTextViewText(R.id.wid_typical, context.getString(R.string.slot) + " " + slot);

        if (tgt instanceof ISoulissTypicalSensor) {
            if (tgt instanceof SoulissTypical53HumiditySensor)
                updateViews.setTextViewText(R.id.wid_info,
                        String.valueOf((((ISoulissTypicalSensor) tgt).getOutputFloat())) + "%");
            else if (tgt instanceof SoulissTypical52TemperatureSensor)
                updateViews.setTextViewText(R.id.wid_info,
                        String.valueOf((((ISoulissTypicalSensor) tgt).getOutputFloat())) + "%");
            else
                updateViews.setTextViewText(R.id.wid_info,
                        String.valueOf((((ISoulissTypicalSensor) tgt).getOutputFloat())));
        } else
            updateViews.setTextViewText(R.id.wid_info, (tgt.getOutputDesc()));
		
		// UPDATE SINCRONO
		Intent intent = new Intent(context, SoulissWidget.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		Uri data = Uri.withAppendedPath(Uri.parse("W://widget/id/"), String.valueOf(appWidgetId));
		intent.setData(data);
		intent.putExtra("_ID", appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.button1, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetId, updateViews);

		// Toast.makeText(context, "forcedUpdate(), node " +
		// String.valueOf(node), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onReceive(@NonNull final Context context, final Intent intent) {
		customSharedPreference = context.getSharedPreferences("SoulissWidgetPrefs", Activity.MODE_PRIVATE);
		opzioni = new SoulissPreferenceHelper(context);
		handler = new Handler();
		super.onReceive(context, intent);
		final AppWidgetManager awm = AppWidgetManager.getInstance(context);
		final int got = intent.getIntExtra("_ID", -1);

		Log.w(TAG, "widget command from id:" + got);
		if (got != -1) {
			Log.w("SoulissWidget", "PRESS");
			final int node = customSharedPreference.getInt(got + "_NODE", -1);
			final int slot = customSharedPreference.getInt(got + "_SLOT", -1);
			final long cmd = customSharedPreference.getLong(got + "_CMD", -1);
			final String name = customSharedPreference.getString(got + "_NAME", "");
			final RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			// sfondo rosso...
			if (cmd != -1) {
				updateViews.setTextViewText(R.id.button1, "Sending command...");
			}
			updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape_active);
			updateViews.setTextViewText(R.id.wid_node, context.getString(R.string.node) + " " + node);
			updateViews.setTextViewText(R.id.wid_typical, context.getString(R.string.slot) + " " + slot);

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
					db.open();
					final SoulissTypical tgt = db.getSoulissTypical(node, (short) slot);
					tgt.setCtx(context);

					UDPHelper.pollRequest(opzioni, 1, tgt.getTypicalDTO().getNodeId());

					final SoulissCommand cmdd = new SoulissCommand( tgt);
					cmdd.getCommandDTO().setCommand(cmd);
					// se comando non vuoto
					if (cmd != -1) {
						cmdd.execute();
					}
				}
			}).start();
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.w(TAG, "widget onUpdate for " + appWidgetIds.length + " widgets");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		ComponentName thisWidget = new ComponentName(context, SoulissWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			forcedUpdate(context, appWidgetManager, widgetId);

		}
	}

}
