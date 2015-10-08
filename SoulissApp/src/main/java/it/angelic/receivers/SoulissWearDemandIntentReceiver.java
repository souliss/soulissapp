package it.angelic.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import java.util.ArrayList;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.VoiceCommandActivityNoDisplay;

public class SoulissWearDemandIntentReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.w("MyTag", "Action message from intent = " + intent.getAction());
    if (intent.getAction().equals("it.angelic.soulissclient.WEAR_VOICE_COMMAND")) {
      /*String message =
              intent.getStringExtra(Handheld.EXTRA_MESSAGE);
      Log.v("MyTag", "Extra message from intent = " + message);
      Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
      CharSequence reply = remoteInput.getCharSequence(Handheld.EXTRA_VOICE_REPLY);
      Log.v("MyTag", "User reply from wearable: " + reply);
*/

      ArrayList<String> thingsYouSaid = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      // ((TextView)findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
      final String yesMan = thingsYouSaid.get(0).toLowerCase();
      Log.i(Constants.TAG, "SoulissWearDemandIntentReceiver.onReceive, searching command: " + yesMan);
      //Invia comando
      VoiceCommandActivityNoDisplay.interpretCommand(context, yesMan);



      }
    }
  }