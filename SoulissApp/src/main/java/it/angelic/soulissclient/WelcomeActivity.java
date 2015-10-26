package it.angelic.soulissclient;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.Eula;
import it.angelic.soulissclient.helpers.Utils;
import it.angelic.soulissclient.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class WelcomeActivity extends FragmentActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

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
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_welcome);

        // final TextView welcomeSkipText = (TextView) findViewById(R.id.welcome_skip_text);
        final Button welcomeTourButton = (Button) findViewById(R.id.welcome_tour_button);
        final CheckBox welcomeEnableCheckBox = (CheckBox) findViewById(R.id.welcome_enable_checkbox);
        final FrameLayout welcomeContainer = (FrameLayout) findViewById(R.id.frame_welcome_container);
        welcomeContainer.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        welcomeContainer.getBackground().setDither(true);

        welcomeEnableCheckBox.setChecked(SoulissApp.isWelcomeDisabled());
        welcomeEnableCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoulissApp.saveWelcomeDisabledPreference(welcomeEnableCheckBox.isChecked());
            }
        });
        final Spinner confSpinner = (Spinner) findViewById(R.id.configSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        confSpinner.setAdapter(spinnerAdapter);
        Set<String> configs = SoulissApp.getConfigurations();
        //quelli statici
        String[] statiche = getResources().getStringArray(R.array.configChooserArray);
        int selectedIdx = 0;
        int i = 0;
        for (String nconf : statiche) {
            if (SoulissApp.getCurrentConfig().equalsIgnoreCase(nconf))
                selectedIdx = i;
            i++;
            spinnerAdapter.add(nconf);
        }
        //quelli dinamici
        for (String nconf : configs) {
            if (SoulissApp.getCurrentConfig().equalsIgnoreCase(nconf))
                selectedIdx = i;
            i++;
            spinnerAdapter.add(nconf);
        }
        spinnerAdapter.notifyDataSetChanged();
        confSpinner.setSelection(selectedIdx, true);

        confSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.w(Constants.TAG, "Config spinner selected val:" + confSpinner.getSelectedItem());
                String previousConfig = SoulissApp.getCurrentConfig();
                final File importDir = new File(Environment.getExternalStorageDirectory(), "//Souliss");
                SoulissApp.setCurrentConfig(confSpinner.getSelectedItem().toString());
                //SAVE PREVIOUS if old one is not "create new" or "import"
                if (!previousConfig.equals("") && !(previousConfig.equals(getResources().getStringArray(R.array.configChooserArray)[1]))
                        && !(previousConfig.equals(getResources().getStringArray(R.array.configChooserArray)[2]))) {
                    //save Old DB and config
                    File filePrefs = new File(importDir, previousConfig + "_SoulissApp.prefs");
                    Utils.saveSharedPreferencesToFile(WelcomeActivity.this, filePrefs);
                    //locateDB
                    SoulissDBHelper db = new SoulissDBHelper(WelcomeActivity.this);
                    SoulissDBHelper.open();
                    String DbPath = SoulissDBHelper.getDatabase().getPath();
                    File oldDb = new File(DbPath);
                    File bckDb = new File(importDir, previousConfig + "_" + SoulissDB.DATABASE_NAME);
                    Log.w(Constants.TAG, "Saving old DB: " + DbPath + " to: " + bckDb.getPath());
                    try {
                        Utils.fileCopy(oldDb, bckDb);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String selVal = confSpinner.getSelectedItem().toString();
                //Adesso carico la nuova
                if (selVal.equals(getResources().getStringArray(R.array.configChooserArray)[0])) {

                    File filePrefs;
                    try {
                        filePrefs = new File(importDir, selVal + "_SoulissApp.prefs");
                        if (!filePrefs.exists())
                            throw new Resources.NotFoundException();
                    } catch (Resources.NotFoundException e) {
                        //MAI creato prima, inizializza
                        filePrefs = new File(importDir, selVal + "_SoulissDB.csv.prefs");
                        try {
                            //se non esiste la demo, Crea & salva
                            filePrefs.createNewFile();
                            SharedPreferences newDefault = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                            SharedPreferences.Editor demo = newDefault.edit();
                            demo.putString("edittext_IP_pubb", "demo.souliss.net");
                            demo.putString("edittext_IP", "10.14.10.77");
                            demo.commit();
                            Utils.saveSharedPreferencesToFile(WelcomeActivity.this, filePrefs);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        Log.e(Constants.TAG, "Errore import prefs", e);
                    }
                    Utils.loadSharedPreferencesFromFile(WelcomeActivity.this, filePrefs);
                    Log.w(Constants.TAG, "DEMO prefs loaded");
                    loadSoulissDbFromFile(selVal, importDir);


                } else if (confSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.configChooserArray)[1])) {
                    Log.i(Constants.TAG, "TODO? forse non c'e da fare nulla qui...(crea nuovo)");
                } else if (confSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.configChooserArray)[2])) {
                    Log.i(Constants.TAG, "TODO? forse non c'e da fare nulla qui...(importa)");
                } else {
                    //caso dinamico
                    File filePrefs;
                    try {
                        filePrefs = new File(importDir, selVal + "_SoulissApp.prefs");
                        if (!filePrefs.exists())
                            throw new Resources.NotFoundException();
                        Utils.loadSharedPreferencesFromFile(WelcomeActivity.this, filePrefs);
                        Log.w(Constants.TAG, selVal + " prefs loaded");
                    } catch (Resources.NotFoundException e) {
                        //MAI creato prima? WTF
                        Log.e(Constants.TAG, "Errore import config " + selVal, e);
                    }
                    loadSoulissDbFromFile(selVal, importDir);
                }

                //dopo aver caricato opzioni, richiedo ping
                SoulissApp.getOpzioni().setBestAddress();

                //https://github.com/ribico/souliss_demo/blob/master/souliss_demo.ino
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        welcomeTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here we've already chosen config and loaded right files
                if (confSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.configChooserArray)[1])) {
                    Intent createNewConfig = new Intent(WelcomeActivity.this, WelcomeCreateConfigActivity.class);
                    startActivity(createNewConfig);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    supportFinishAfterTransition();
                } else
                    startSoulissMainActivity();
            }
        });
        /*welcomeSkipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSoulissMainActivity();
            }
        });

*/
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        /* show EULA if not accepted */
        Eula.show(this);
    }

    private void loadSoulissDbFromFile(String config, File importDir) {
        try {
            File bckDb = new File(importDir, config + "_" + SoulissDB.DATABASE_NAME);
            SoulissDBHelper db = new SoulissDBHelper(WelcomeActivity.this);
            SoulissDBHelper.open();
            String DbPath = SoulissDBHelper.getDatabase().getPath();
            db.close();
            File newDb = new File(DbPath);
            Utils.fileCopy(bckDb, newDb);
            Log.w(Constants.TAG, config + " DB loaded");
        } catch (Exception demex) {
            Log.e(Constants.TAG, "Can't load DB " + config + demex.getMessage());
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* check for first time run */
        welcomeEnabledCheck();
    }


    private void startSoulissMainActivity() {
        Intent myIntent = new Intent(WelcomeActivity.this, LauncherActivity.class);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        supportFinishAfterTransition();
    }

    private void welcomeEnabledCheck() {
        boolean firstTimeRun = SoulissApp.isWelcomeDisabled();
        //boolean firstTimeRun = true;
        if (firstTimeRun) {
            startSoulissMainActivity();

        } else {
            //let the user choose config
        }
    }
}
