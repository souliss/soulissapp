package it.angelic.soulissclient.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
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
    private static final int NOTIF_ID_COMEBACK = 665;
    private static final int NOTIF_ID_GOAWAY = 667;

    private static void createNotificationChannel(Context ctx) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channelId;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription("SoulissApp notification channel");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void sendProgramNotification(Context ctx, String desc, String longdesc, int icon, @NonNull SoulissCommand ppr) {
//It's safe to call this repeatedly because creating an existing notification channel performs no operation.
        createNotificationChannel(ctx);
        Intent notificationIntent = new Intent(ctx, AddProgramActivity.class);
            notificationIntent.putExtra("PROG", ppr);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_notification_souliss)
                .setTicker(desc)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), icon))
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(desc)
                .setContentText(longdesc)
                //.setStyle(new NotificationCompat.BigTextStyle()
                //        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification n = builder.build();
        nm.notify(ppr.getType() == Constants.COMMAND_COMEBACK_CODE ? NOTIF_ID_COMEBACK : NOTIF_ID_GOAWAY, n);
    }

    public static void sendTooLongWarnNotification(Context ctx, String desc, @NonNull SoulissTypical ppr) {
        //It's safe to call this repeatedly because creating an existing notification channel performs no operation.
        createNotificationChannel(ctx);

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
                .setContentText(String.format(ctx.getString(R.string.hasbeenturnedontoolong), ppr.getNiceName()));

        Notification n = builder.build();
        nm.notify(664, n);
    }

    /**
     * TODO Should be moved. Produces Android notification
     *
     * @param ctx
     * @param desc
     * @param icon
     * @param ty
     */
    public static void sendAntiTheftNotification(Context ctx, String desc, int icon, SoulissTypical ty) {
        //It's safe to call this repeatedly because creating an existing notification channel performs no operation.
        createNotificationChannel(ctx);

        Intent notificationIntent = new Intent(ctx, T4nFragWrapper.class);
        notificationIntent.putExtra("TIPICO", ty);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId);

        builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_notification_souliss)
                .setLargeIcon(BitmapFactory.decodeResource(res, icon)).setTicker(desc)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(desc).setContentText(ctx.getString(R.string.antitheft_notify_desc));
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
