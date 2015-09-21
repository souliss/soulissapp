package it.angelic.soulissclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.ArrayList;

import it.angelic.soulissclient.model.SoulissScene;

public class WrapperActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(Constants.TAG, "onCreate WrapperActivity ");

        ArrayList<String> thingsYouSaid = getIntent().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (thingsYouSaid != null && thingsYouSaid.size() > 0){
            Log.w(Constants.TAG, "GOTIT!!! "+thingsYouSaid.get(0));
        }
    }

    @Override
    protected void onStart() {
        Log.w(Constants.TAG, "onStart WrapperActivity ");
        super.onStart();
    }
}