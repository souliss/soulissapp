package it.angelic.soulissclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;

import it.angelic.soulissclient.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class WelcomeCreateConfigActivity extends FragmentActivity {


    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    Handler mHideHandler = new Handler();
    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome_createconf);

        // final TextView welcomeSkipText = (TextView) findViewById(R.id.welcome_skip_text);
        final Button welcomeTourButton = (Button) findViewById(R.id.welcome_tour_button);
        final EditText configName = (EditText) findViewById(R.id.config_name);
        final EditText initialIp = (EditText) findViewById(R.id.config_ip);
        welcomeTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here we've already chosen config and loaded right
                if (configName.getText() == null ||
                        configName.getText().toString().length() <= 0) {
                    //Toast & exit
                    Toast.makeText(WelcomeCreateConfigActivity.this, "Configuration name is mandatory", Toast.LENGTH_SHORT);
                    return;
                }
                if (initialIp.getText() != null &&
                        initialIp.getText().toString().length() > 0) {
                    //Setta Ip
                    try {// sanity check
                        final InetAddress checkIPt = InetAddress.getByName(initialIp.getText().toString());
                        final String pars = " (" + checkIPt.getHostName() + ")";
                        Log.w(Constants.TAG, "Valid IP inserted");
                        SoulissApp.getOpzioni().setIPPreference(initialIp.getText().toString());

                    } catch (final Exception e) {
                        Log.e(Constants.TAG, "Error in address parsing: " + e.getMessage(), e);
                    }
                }
                String adding = configName.getText().toString();
                Log.w(Constants.TAG, "Saving new Config:" + adding);
                SoulissApp.setCurrentConfig(adding);
                SoulissApp.addConfiguration(adding);
                //TODO Ask DB Struct
                startSoulissMainActivity();
            }
        });
        /*welcomeSkipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSoulissMainActivity();
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        /* check for first time run */
    }

    private void startSoulissMainActivity() {
        Intent myIntent = new Intent(WelcomeCreateConfigActivity.this, LauncherActivity.class);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }


}
