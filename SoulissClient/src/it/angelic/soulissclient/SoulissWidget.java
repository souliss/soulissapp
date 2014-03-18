package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
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
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SoulissWidget extends AppWidgetProvider {

	private static final String TAG = "SoulissWidget";
	private Handler handler;

	public static void forcedUpdate(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		SharedPreferences customSharedPreference = context.getSharedPreferences("SoulissWidgetPrefs",
				Activity.MODE_PRIVATE);

		final int node = customSharedPreference.getInt(appWidgetId + "_NODE", -1);
		final int slot = customSharedPreference.getInt(appWidgetId + "_SLOT", -1);
		final long cmd = customSharedPreference.getLong(appWidgetId + "_CMD", -1);
		final String name = customSharedPreference.getString(appWidgetId + "_NAME", "");

		if (node == -1) {
			Log.e(TAG, "missing widget preferences, aborting");
			return;
		}
		SoulissDBHelper db = new SoulissDBHelper(context);
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
		// Register an onClickListener
		Intent intent = new Intent(context, SoulissWidget.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		Uri data = Uri.withAppendedPath(Uri.parse("W://widget/id/"), String.valueOf(appWidgetId));
		intent.setData(data);
		intent.putExtra("_ID", appWidgetId);
		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.button1, pendingIntent);

		appWidgetManager.updateAppWidget(appWidgetId, updateViews);

		// Toast.makeText(context, "forcedUpdate(), node " +
		// String.valueOf(node), Toast.LENGTH_LONG).show();
	}

	private SharedPreferences customSharedPreference;
	private SoulissPreferenceHelper opzioni;

	@Override
	public void onReceive(final Context context, final Intent intent) {
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
			updateViews.setTextViewText(R.id.button1, "Sending command...");
			updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape_active);
			updateViews.setTextViewText(R.id.wid_node, context.getString(R.string.node) + " " + node);
			updateViews.setTextViewText(R.id.wid_typical, context.getString(R.string.slot) + " " + slot);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra("_ID", got);
			Uri data = Uri.withAppendedPath(Uri.parse("W://widget/id/"), String.valueOf(got));
			intent.setData(data);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			updateViews.setOnClickPendingIntent(R.id.button1, pendingIntent);
			// refresh sincrono
			awm.updateAppWidget(got, updateViews);

			new Thread(new Runnable() {
				private SoulissDBHelper db;

				@Override
				public void run() {
					Looper.prepare();
					SoulissPreferenceHelper prefs = SoulissClient.getOpzioni();
					db = new SoulissDBHelper(context);
					db.open();
					final SoulissTypical tgt = db.getSoulissTypical(node, (short) slot);
					tgt.setCtx(context);
					final SoulissCommand cmdd = new SoulissCommand(context, tgt);
					cmdd.getCommandDTO().setCommand(cmd);
					// UDPHelper.issueSoulissCommand(cmdd, prefs);
					final String res = UDPHelper.issueSoulissCommand(cmdd, prefs);
					handler.post(new Runnable() {
						@Override
						public void run() {
							updateViews.setTextViewText(R.id.button1, cmdd.toString());
							updateViews.setTextViewText(R.id.wid_info, (tgt.getOutputDesc()));
							// TODO edittext name
							updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape);
							
							//Toast.makeText(context,
								//	"Command sent to node " + customSharedPreference.getInt(got + "_NODE", -1),
								//	Toast.LENGTH_LONG).show();

						}
					});
					UDPHelper.pollRequest(opzioni, 1, tgt.getTypicalDTO().getNodeId());
					
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO carica stato dal DB

							updateViews.setTextViewText(R.id.button1, cmdd.toString());
							updateViews.setTextViewText(R.id.wid_info, (tgt.getOutputDesc()));
							// edittext name
							updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape);

							Toast.makeText(context, res, Toast.LENGTH_LONG).show();
							// refresh asincrono x ripristino widget
							awm.updateAppWidget(got, updateViews);
						}
					}, 2000);

				}

			}).start();
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.w(TAG, "widget onUpdate from:" + appWidgetManager);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		ComponentName thisWidget = new ComponentName(context, SoulissWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			forcedUpdate(context, appWidgetManager, widgetId);

		}
	}

}
