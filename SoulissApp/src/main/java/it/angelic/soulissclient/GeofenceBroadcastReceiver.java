package it.angelic.soulissclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static it.angelic.soulissclient.Constants.TAG;

/**
 * Created by shine@angelic.it on 23/10/2019.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
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

            // Send notification and log the transition details.
            // sendNotification(geofenceTransitionDetails);
            Log.i(TAG, "GEOFENCE EVENT " + geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, "GEOFENCE ERR:" + geofenceTransition);
        }
    }

    private String getGeofenceTransitionDetails(Context geofenceBroadcastReceiver, int geofenceTransition, List<Geofence> triggeringGeofences) {
        return triggeringGeofences.get(0).getRequestId() + triggeringGeofences.get(0).toString();
    }

    private static class GeofenceErrorMessages {
        public static String getErrorString(Context geofenceBroadcastReceiver, int errorCode) {
            return "ERROR MSG TODO: " + errorCode;
        }
    }
}
