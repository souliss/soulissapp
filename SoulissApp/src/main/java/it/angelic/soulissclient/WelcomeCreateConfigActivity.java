package it.angelic.soulissclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;

import it.angelic.soulissclient.helpers.SoulissGlobalPreferenceHelper;
import it.angelic.soulissclient.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class WelcomeCreateConfigActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoulissGlobalPreferenceHelper gbPref = new SoulissGlobalPreferenceHelper(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome_createconf);

        // final TextView welcomeSkipText = (TextView) findViewById(R.id.welcome_skip_text);
        final Button welcomeTourButton = (Button) findViewById(R.id.welcome_tour_button);
        final EditText configName = (EditText) findViewById(R.id.config_name);
        // final EditText initialIp = (EditText) findViewById(R.id.config_ip);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, gbPref.getIpDictionary());
        final AutoCompleteTextView initialIp = (AutoCompleteTextView)
                findViewById(R.id.config_ip);
        initialIp.setAdapter(adapter);

        welcomeTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here we've already chosen config and loaded right
                if (configName.getText() == null ||
                        configName.getText().toString().length() <= 0) {
                    //Toast & exit
                    Toast.makeText(WelcomeCreateConfigActivity.this, R.string.config_mandatory, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (initialIp.getText() != null &&
                        initialIp.getText().toString().length() > 0) {
                    new Thread() {
                        public void run() {
                            Looper.prepare();
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
                    }.start();
                }
                String adding = configName.getText().toString();
                Log.w(Constants.TAG, "Saving new Config:" + adding);
                SoulissApp.setCurrentConfig(adding);
                SoulissApp.addConfiguration(adding);
                //TODO Ask DB Struct
                startSoulissMainActivity();
                //close and don't go back here
                supportFinishAfterTransition();
            }
        });
        /*welcomeSkipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSoulissMainActivity();
            }
        });*/
    }


    private void startSoulissMainActivity() {
        Intent myIntent = new Intent(WelcomeCreateConfigActivity.this, LauncherActivity.class);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }


}
