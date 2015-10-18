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
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import junit.framework.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendSoulissCommandIntentService extends IntentService {
    private static final String TAG = SendSoulissCommandIntentService.class.getSimpleName();
    private final Handler mHandler;

    public SendSoulissCommandIntentService() {
        super(TAG);
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        Log.w(TAG, "onHandleIntent action: " + action);
        String outputPath = null;
        byte[] dataToSend = null;
        switch (action) {//decidi cosa fare
            case Constants.ACTION_OPEN_SOULISS:
                outputPath = "notification/open";
                break;
            case Constants.ACTION_SEND_SOULISS_COMMAND:
                String voice = null;
                try {
                    voice = intent.getExtras().getString("THEVOICE");
                    Log.w(TAG, "onHandleIntent: " + voice);
                    dataToSend = voice.getBytes();
                    outputPath = "notification/send";
                } catch (Exception ree) {
                    Bundle inputResults = RemoteInput.getResultsFromIntent(intent);
                    CharSequence replyText = inputResults.getCharSequence("reply");
                    if (replyText != null && replyText.length() > 0) {
                        voice = replyText.toString();
                       dataToSend = voice.getBytes();
                       Log.w(TAG, "replaced Extra with getResultsFromIntent: " + voice);
                        outputPath = "notification/send";
                    }
                }
                Assert.assertTrue(voice != null);
                break;
            default:
                break;
        }
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (outputPath == null){
            Log.e(TAG, "Path not Found. Not implemented?");

            // Show the open on phone animation
            mHandler.post(new DisplayToast(this, "Missing path error"));
            return;
        }
        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            // Show the open on phone animation
            mHandler.post(new DisplayToast(this, "Google API error"));
            return;
        }
        //Set<Node> nodes =  Wearable.CapabilityApi.getCapability(googleApiClient, "activate_souliss",
        //       CapabilityApi.FILTER_REACHABLE).await()
        //       .getCapability().getNodes();
        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient)
                .await().getNodes();
        Log.w(TAG, "onHandleIntent is sending messages. getNodes().size: " + nodes.size());
        if (!nodes.isEmpty()) {


            // Show the open on phone animation
           /* Intent confirmationIntent = new Intent(this, ConfirmationActivity.class);
            confirmationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            confirmationIntent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                    getString(R.string.command_sent));

            confirmationIntent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.SUCCESS_ANIMATION);
            startActivity(confirmationIntent);*/


            // Clear the notification
            // NotificationManager notificationManager = (NotificationManager)
            //        getSystemService(NOTIFICATION_SERVICE);
            //notificationManager.cancel(Constants.NOTIFICATION_ID);
            // Send the message to the phone to send souliss command
            for (Node node : nodes) {
                Wearable.MessageApi.sendMessage(googleApiClient, node.getId(),
                        outputPath, dataToSend).await();
                Log.e(TAG, "Message sent");
            }
        }
        googleApiClient.disconnect();
    }
}
