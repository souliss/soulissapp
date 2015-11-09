package it.angelic.soulissclient;

import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
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

import java.io.File;
import java.net.URISyntaxException;

import it.angelic.soulissclient.helpers.Utils;
import it.angelic.soulissclient.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class WelcomeImportConfigActivity extends FragmentActivity {

    private static final String FTYPE = ".preferences";
    private static final int DIALOG_LOAD_FILE = 1000;
    private static final int FILE_SELECT_CODE = 0;
    private TextView initialIp;
    private String mChosenFile;
    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "//Souliss//");

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(Constants.TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = Utils.getPath(this, uri);
                        Log.d(Constants.TAG, "File Path: " + path);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    initialIp.setText(path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome_importconf);

        // final TextView welcomeSkipText = (TextView) findViewById(R.id.welcome_skip_text);
        final Button welcomeTourButton = (Button) findViewById(R.id.welcome_tour_button);
        final EditText configName = (EditText) findViewById(R.id.config_name);
        initialIp = (TextView) findViewById(R.id.config_ip);
        ContextWrapper c = new ContextWrapper(WelcomeImportConfigActivity.this);
        final File importDir = c.getFilesDir();
        initialIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This always works
                showFileChooser();
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
                    //Setta Prefs
                    try {
                        File prefs = new File(importDir, initialIp.getText().toString());
                        Utils.loadSharedPreferencesFromFile(WelcomeImportConfigActivity.this, prefs);
                        Log.w(Constants.TAG, "IMPORTED prefs: " + prefs.getPath());
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

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startSoulissMainActivity() {
        Intent myIntent = new Intent(WelcomeImportConfigActivity.this, LauncherActivity.class);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }


}
