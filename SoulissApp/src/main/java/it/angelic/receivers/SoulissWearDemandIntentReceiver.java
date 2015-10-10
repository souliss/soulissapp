package it.angelic.receivers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.VoiceCommandActivityNoDisplay;

public class SoulissWearDemandIntentReceiver extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.w(Constants.TAG , "Wear.onMessageReceived" + messageEvent);
        if (path.equals("notification/open")) {
            PackageManager packageManager = getPackageManager();
            String voice = new String(messageEvent.getData());
            Intent mainIntent = packageManager.getLaunchIntentForPackage(getPackageName());
            Log.w(Constants.TAG + ":WEAR", "Voice from Wear received:" + voice);
            VoiceCommandActivityNoDisplay.interpretCommand(getBaseContext(), voice);
            //startActivity(mainIntent);
        }
    }

//  @Override
//  public void onReceive(Context context, Intent intent) {
//    Log.w("MyTag", "Action message from intent = " + intent.getAction());
//    if (intent.getAction().equals("it.angelic.soulissclient.WEAR_VOICE_COMMAND")) {
//      /*String message =
//              intent.getStringExtra(Handheld.EXTRA_MESSAGE);
//      Log.v("MyTag", "Extra message from intent = " + message);
//      Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
//      CharSequence reply = remoteInput.getCharSequence(Handheld.EXTRA_VOICE_REPLY);
//      Log.v("MyTag", "User reply from wearable: " + reply);
//       */
//
//      ArrayList<String> thingsYouSaid = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//      // ((TextView)findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
//      final String yesMan = thingsYouSaid.get(0).toLowerCase();
//      Log.i(Constants.TAG, "SoulissWearDemandIntentReceiver.onReceive, searching command: " + yesMan);
//      //Invia comando
//      VoiceCommandActivityNoDisplay.interpretCommand(context, yesMan);
//
//      }
//    }
}