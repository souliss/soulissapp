/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.angelic.soulissclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.wearable.activity.ConfirmationActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;



import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActivateSoulissIntentService extends IntentService {
    private static final String TAG = ActivateSoulissIntentService.class.getSimpleName();
    private static final String ACTION_MARK_NOTIFICATION_READ =
            "it.angelic.soulissclient.WEAR_VOICE_COMMAND";



    public ActivateSoulissIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        String voice = intent.getExtras().getString("THEVOICE");
        Log.w(TAG, "onHandleIntent: "+voice);
        if (TextUtils.equals(action, ACTION_MARK_NOTIFICATION_READ)) {
            // Clear the notification
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(Constants.NOTIFICATION_ID);
            return;
        }
        // else -> Open on Phone action
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);
        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }
        //Set<Node> nodes =  Wearable.CapabilityApi.getCapability(googleApiClient, "activate_souliss",
         //       CapabilityApi.FILTER_REACHABLE).await()
         //       .getCapability().getNodes();
        List<Node> nodes =  Wearable.NodeApi.getConnectedNodes(googleApiClient)
                .await().getNodes();
        Log.w(TAG, "onHandleIntent.getNodes().size: "+nodes.size());
        if (!nodes.isEmpty()) {
            // Show the open on phone animation
            Intent openOnPhoneIntent = new Intent(this, ConfirmationActivity.class);
            openOnPhoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openOnPhoneIntent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
            startActivity(openOnPhoneIntent);
            // Clear the notification
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(Constants.NOTIFICATION_ID);
            // Send the message to the phone to open Muzei
            for (Node node : nodes) {
                Wearable.MessageApi.sendMessage(googleApiClient, node.getId(),
                        "notification/open", voice.getBytes()).await();
                Log.e(TAG, "Message sent");
            }
        }
        googleApiClient.disconnect();
    }
}
