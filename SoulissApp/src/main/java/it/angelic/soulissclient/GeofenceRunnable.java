package it.angelic.soulissclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.db.SoulissDBHelper;

/**
 * Created by shine@angelic.it on 23/10/2019.
 */
class GeofenceRunnable implements Runnable {
    private final SoulissPreferenceHelper opzioni;
    Activity parent;

    private PendingIntent geofencePendingIntent;
    private GeofencingClient geofencingClient;

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(Context ctx) {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(ctx, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public GeofenceRunnable(Activity context) {
        this.parent = context;
        geofencingClient = LocationServices.getGeofencingClient(context);

        opzioni = new SoulissPreferenceHelper(context);
        Log.w(Constants.TAG, "Created GEOFENCE Runnable");
    }

    @Override
    public void run() {
        SoulissDBHelper database = new SoulissDBHelper(parent);
        LinkedList<SoulissCommand> comandi = database.getPositionalPrograms();

        ArrayList<Geofence> geofenceList = new ArrayList<>();
        for (SoulissCommand programmaPos : comandi) {
            Log.w(Constants.TAG, "Adding GEOFENCE: " + programmaPos.getCommandId() + programmaPos.getName());
            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(programmaPos.getCommandId() + programmaPos.getName())

                    .setCircularRegion(
                            opzioni.getHomeLatitude(),
                            opzioni.getHomeLongitude(),
                            opzioni.getHomeThresholdDistance()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(programmaPos.getType() == Constants.COMMAND_COMEBACK_CODE ? Geofence.GEOFENCE_TRANSITION_ENTER :
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        if (geofenceList.size() > 0) {
            // inserimento nuovo geofence
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), getGeofencePendingIntent(parent))
                    .addOnSuccessListener(parent, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(Constants.TAG, "Registered GEOFENCE ");
                        }
                    })
                    .addOnFailureListener(parent, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(Constants.TAG, "Registered GEOFENCE FAIL ");
                        }
                    });
        }
    }
}
