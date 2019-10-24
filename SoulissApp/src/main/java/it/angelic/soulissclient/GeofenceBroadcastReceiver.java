package it.angelic.soulissclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.LinkedList;
import java.util.List;

import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.util.NotificationStaticUtil;

import static it.angelic.soulissclient.Constants.TAG;

/**
 * Created by shine@angelic.it on 23/10/2019.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private String getGeofenceTransitionDetails(Context geofenceBroadcastReceiver, int geofenceTransition, List<Geofence> triggeringGeofences) {
        return triggeringGeofences.get(0).getRequestId() + triggeringGeofences.get(0).toString();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SoulissDBHelper database = new SoulissDBHelper(context);
        SoulissDBHelper.open();
        LinkedList<SoulissCommand> progs = database.getPositionalPrograms();

        Log.w(Constants.TAG, "RECEIVED GEOFENCE INTENT " + intent.getAction());
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(context,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    context,
                    geofenceTransition,
                    triggeringGeofences
            );
            Log.i(TAG, "GEOFENCE EVENT " + geofenceTransitionDetails);
            for (SoulissCommand soulissCommand : progs) {
                if ((soulissCommand.getCommandId() + soulissCommand.getName()).equals(triggeringGeofences.get(0).getRequestId())) {
                    Log.i(TAG, "TRIGGER GEOFENCE EVENT " + geofenceTransitionDetails);
                    soulissCommand.execute();
                    NotificationStaticUtil.sendProgramNotification(context, context.getString(R.string.positional_executed),
                            soulissCommand.toString() + " " + soulissCommand.getParentTypical() != null ? soulissCommand.getParentTypical().getNiceName() : "", R.drawable.exit1, soulissCommand);
                }
            }

            // Send notification and log the transition details.
            // sendNotification(geofenceTransitionDetails);

        } else {
            // Log the error.
            Log.e(TAG, "GEOFENCE ERR:" + geofenceTransition);
        }
    }

    private static class GeofenceErrorMessages {
        public static String getErrorString(Context geofenceBroadcastReceiver, int errorCode) {
            return "ERROR MSG TODO: " + errorCode;
        }
    }
}
