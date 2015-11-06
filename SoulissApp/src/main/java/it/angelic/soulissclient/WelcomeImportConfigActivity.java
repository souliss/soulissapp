package it.angelic.soulissclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.net.InetAddress;

import it.angelic.soulissclient.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class WelcomeImportConfigActivity extends FragmentActivity {

    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "//Souliss//");
    private String mChosenFile;
    private static final String FTYPE = ".preferences";
    private static final int DIALOG_LOAD_FILE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome_importconf);

        // final TextView welcomeSkipText = (TextView) findViewById(R.id.welcome_skip_text);
        final Button welcomeTourButton = (Button) findViewById(R.id.welcome_tour_button);
        final EditText configName = (EditText) findViewById(R.id.config_name);
        final TextView initialIp = (TextView) findViewById(R.id.config_ip);

        initialIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This always works
                Intent i = new Intent(WelcomeImportConfigActivity.this, FilePickerActivity.class);
                // This works if you defined the intent filter
                // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                // Configure initial directory by specifying a String.
                // You could specify a String like "/storage/emulated/0/", but that can
                // dangerous. Always use Android's API calls to get paths to the SD-card or
                // internal memory.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, 66);
            }
        });
        welcomeTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here we've already chosen config and loaded right
                if (configName.getText() == null ||
                        configName.getText().toString().length() <= 0) {
                    //Toast & exit
                    Toast.makeText(WelcomeImportConfigActivity.this, R.string.config_mandatory, Toast.LENGTH_SHORT).show();
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
        Intent myIntent = new Intent(WelcomeImportConfigActivity.this, LauncherActivity.class);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }


}
