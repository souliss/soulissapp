package it.angelic.soulissclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import it.angelic.soulissclient.model.SoulissCommand;

public class SendCommandActivityNoDisplay extends Activity {


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(Constants.TAG, "onCreate WrapperActivity");

        final SoulissCommand toExec = (SoulissCommand) getIntent().getSerializableExtra("COMMAND");
        if (toExec == null) {
            Log.e(Constants.TAG, "Null command, aborting");
            //TODO return errror
            finishActivity(-1);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                toExec.execute();
                // UDPHelper.issueSoulissCommand("" + toExec.getNodeId(), "" + toExec.getSlot(), SoulissApp.getOpzioni(), toExec.toString());
                Log.i(Constants.TAG, "Voice Command SENT: " + toExec.getName());
            }
        }).start();
        finish();
    }
}