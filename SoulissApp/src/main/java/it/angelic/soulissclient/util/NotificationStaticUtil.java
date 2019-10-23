package it.angelic.soulissclient.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import it.angelic.soulissclient.AddProgramActivity;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SendCommandActivityNoDisplay;
import it.angelic.soulissclient.T4nFragWrapper;
import it.angelic.soulissclient.TypicalDetailFragWrapper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Created by shine@angelic.it on 23/10/2019.
 */
public class NotificationStaticUtil {

    private final static String channelId = "SoulissNotifications";

    public static void sendProgramNotification(Context ctx, String desc, String longdesc, int icon, @Nullable SoulissCommand ppr) {

        Intent notificationIntent = new Intent(ctx, AddProgramActivity.class);
        if (ppr != null)
            notificationIntent.putExtra("PROG", ppr);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId);

        builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_notification_souliss)
                .setLargeIcon(BitmapFactory.decodeResource(res, icon))
                .setTicker("Souliss program activated")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(desc)
                .setContentText(longdesc);
        Notification n = builder.build();
        nm.notify(665, n);
    }

    public static void sendTooLongWarnNotification(Context ctx, String desc, String longdesc, @NonNull SoulissTypical ppr) {
        Intent notificationIntent = new Intent(ctx, TypicalDetailFragWrapper.class);
        notificationIntent.putExtra("TIPICO", ppr);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId);

        SoulissCommand shutoff = new SoulissCommand(ppr);
        shutoff.setCommand(Constants.Typicals.Souliss_T1n_OffCmd);

        Intent mapIntent = new Intent(ctx, SendCommandActivityNoDisplay.class);
        mapIntent.putExtra("COMMAND", shutoff);

        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(ctx, 0, mapIntent, 0);


        builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_notification_souliss)
                .setLargeIcon(BitmapFactory.decodeResource(res, ppr.getIconResourceId()))
                .setTicker("Turned on warning")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(desc)

                .addAction(R.drawable.ic_cancel_24dp,
                        ctx.getString(R.string.scene_turnoff_lights), mapPendingIntent)
                .setContentText(longdesc);

        Notification n = builder.build();
        nm.notify(664, n);
    }

    /**
     * TODO Should be moved. Produces Android notification
     *
     * @param ctx
     * @param desc
     * @param longdesc
     * @param icon
     * @param ty
     */
    public static void sendAntiTheftNotification(Context ctx, String desc, String longdesc, int icon, SoulissTypical ty) {

        Intent notificationIntent = new Intent(ctx, T4nFragWrapper.class);
        notificationIntent.putExtra("TIPICO", ty);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId);

        builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_notification_souliss)
                .setLargeIcon(BitmapFactory.decodeResource(res, icon)).setTicker(desc)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(desc).setContentText(longdesc);
        Notification n = builder.build();
        nm.notify(663, n);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(ctx, notification);
            r.play();
        } catch (Exception e) {
            Log.e(Constants.Net.TAG, "Unable to play sounds:" + e.getMessage());
        }
    }
}
