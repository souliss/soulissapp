package it.angelic.soulissclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class SoulissMainActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_souliss_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                displaySpeechRecognizer();
            }
        });
    }


    private static final int SPEECH_REQUEST_CODE = 9876;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            final String spokenText = results.get(0);
            // Do something with spokenText
            Log.i("SoulissWear", "Command received: " + spokenText);
            mTextView.setText(spokenText);
            Intent intentFireAction = new Intent();
            String[] bar = results.toArray(new String[results.size()]);
            intentFireAction.putExtra( RecognizerIntent.EXTRA_RESULTS ,  bar);
            intentFireAction.setAction("it.angelic.soulissclient.WEAR_VOICE_COMMAND");
            Log.i("SoulissWear", "intent SENT: " + intentFireAction.getAction());
            sendBroadcast(intentFireAction);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
