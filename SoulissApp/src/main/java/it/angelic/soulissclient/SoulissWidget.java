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
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypicalSensor;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

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
        final int node = customSharedPreference.getInt(appWidgetId + "_NODE", -3);
        final int slot = customSharedPreference.getInt(appWidgetId + "_SLOT", -3);
        final long cmd = customSharedPreference.getLong(appWidgetId + "_CMD", -3);
        final String name = customSharedPreference.getString(appWidgetId + "_NAME", "");

        if (node == -3) {
            Log.e(TAG, "missing widget preferences, aborting");
            return;
        }
            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            db = new SoulissDBHelper(context);
            SoulissDBHelper.open();
            if (node > Constants.MASSIVE_NODE_ID) {
                try {
                    final SoulissTypical tgt = db.getTypical(node, (short) slot);
                    updateViews.setTextViewText(R.id.button1, tgt.getNiceName());
                    updateViews.setInt(R.id.button1, "setBackgroundResource", tgt.getIconResourceId());
                    if (tgt instanceof ISoulissTypicalSensor) {
                        updateViews.setTextViewText(R.id.wid_info,
                                tgt.getOutputDesc());
                    } else

                    if (tgt instanceof ISoulissTypicalSensor)
                        //TODO change to effective output
                        updateViews.setTextViewText(R.id.wid_info, ""+(((ISoulissTypicalSensor)tgt).getOutputFloat()));
                    else
                        updateViews.setTextViewText(R.id.wid_info, (tgt.getOutputDesc()));
                }catch (Exception ee) {
                    updateViews.setTextViewText(R.id.button1, name);
                    updateViews.setTextViewText(R.id.wid_info, context.getString(R.string.widget_cantsave));
                }

                updateViews.setTextViewText(R.id.wid_node, context.getString(R.string.node) + " " + node);
                updateViews.setTextViewText(R.id.wid_typical, context.getString(R.string.slot) + " " + slot);



                updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape);
            } else if (node == Constants.MASSIVE_NODE_ID) {
               //final SoulissTypical tgt = db.getTypical(node, (short) slot);
                updateViews.setTextViewText(R.id.wid_node, context.getString(R.string.allnodes));
                updateViews.setTextViewText(R.id.wid_typical, context.getString(R.string.typical) + " " + slot);
                updateViews.setTextViewText(R.id.wid_info, context.getString(R.string.scene_cmd_massive));
                updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape);
                updateViews.setTextViewText(R.id.button1, name);
            } else if (node == Constants.COMMAND_FAKE_SCENE) {
                final SoulissScene tgt = db.getScene( (short) slot);
                updateViews.setTextViewText(R.id.wid_node, context.getString(R.string.scene));
                updateViews.setTextViewText(R.id.wid_typical, "");
                updateViews.setTextViewText(R.id.wid_info,  context.getString(R.string.execute));
                updateViews.setInt(R.id.widgetcontainer, "setBackgroundResource", R.drawable.widget_shape_scene);
                if (!name.equals(""))
                    updateViews.setTextViewText(R.id.button1, name);
                else
                updateViews.setTextViewText(R.id.button1, tgt.getNiceName());
                updateViews.setInt(R.id.button1, "setBackgroundResource", tgt.getIconResourceId());
            }
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
            final short node = (short) customSharedPreference.getInt(got + "_NODE", -3);
            final short slot = (short) customSharedPreference.getInt(got + "_SLOT", -3);
            final long cmd = customSharedPreference.getLong(got + "_CMD", -3);
            final String name = customSharedPreference.getString(got + "_NAME", "");
            final RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            // sfondo rosso...
            if (cmd != -3) {
                updateViews.setTextViewText(R.id.button1, "Sending command...");
            }
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
                    if (node > Constants.MASSIVE_NODE_ID) {
                        final SoulissTypical tgt = db.getTypical(node, slot);
                        UDPHelper.pollRequest(opzioni, 1, tgt.getNodeId());
                        final SoulissCommand cmdd = new SoulissCommand(tgt);
                        cmdd.setCommand(cmd);
                        // se comando non vuoto
                        if (cmd != -3) {
                            cmdd.execute();
                        }
                    } else if (node == Constants.MASSIVE_NODE_ID) {
                        SoulissCommandDTO dto = new SoulissCommandDTO();
                        UDPHelper.pollRequest(opzioni, db.countNodes(), 0);
                        dto.setNodeId(node);
                        dto.setSlot(slot);
                        if (cmd != -3) {
                            dto.setCommand(cmd);
                            SoulissCommand cmdd = new SoulissCommand(dto);
                            cmdd.execute();
                        }
                    } else if (node == Constants.COMMAND_FAKE_SCENE) {
                        SoulissScene targrt = db.getScene( slot);
                        targrt.execute();
                        UDPHelper.pollRequest(opzioni, db.countNodes(), 0);
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
