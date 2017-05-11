package it.angelic.soulissclient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.Eula;
import it.angelic.soulissclient.model.db.SoulissDB;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.util.SoulissUtils;
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

    /**
     * Ritorna l'indice della conf selezionata
     * in base alla configurazione
     *
     * @param spinnerAdapter
     * @return
     */
    private int fillSpinnerConfig(ArrayAdapter<String> spinnerAdapter) {
        Set<String> configs = SoulissApp.getConfigurations();
        //popola config, quelli statici
        String[] statiche = getResources().getStringArray(R.array.configChooserArray);
        int selectedIdx = -1;
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
        return selectedIdx;
    }

    private void loadOrCreateDemoConfig(File importDir, String newConfig) {
        //carica DEMO
        File filePrefs;
        try {
            filePrefs = new File(importDir, newConfig + "_SoulissApp.prefs");
            if (!filePrefs.exists())
                throw new Resources.NotFoundException();
        } catch (Resources.NotFoundException e) {
            //MAI creato prima, inizializza
            filePrefs = new File(importDir, newConfig + "_SoulissApp.prefs");
            try {
                //se non esiste la demo, Crea & salva
                filePrefs.createNewFile();
                SharedPreferences newDefault = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                SharedPreferences.Editor demo = newDefault.edit();
                demo.clear();
                //95.241.222.134
                demo.putString("edittext_IP_pubb", Constants.DEMO_PUBLIC_IP);
                demo.putString("edittext_IP", Constants.DEMO_LOCAL_IP);
                demo.commit();
                Log.w(Constants.TAG, "new Demo prefs created to: " + filePrefs.getPath());
                SoulissUtils.saveSharedPreferencesToFile(newDefault, WelcomeActivity.this, filePrefs);
            } catch (IOException e1) {
                Log.e(Constants.TAG, "Errore GRAVE create DEMO prefs", e1);
            }

        }
        SoulissUtils.loadSharedPreferencesFromFile(WelcomeActivity.this, filePrefs);
        Log.w(Constants.TAG, "DEMO prefs loaded");
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //avoid black-text spinner
        //setTheme(R.style.DarkThemeSelector);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);

        // final TextView welcomeSkipText = (TextView) findViewById(R.id.welcome_skip_text);
        final Button welcomeTourButton = (Button) findViewById(R.id.welcome_tour_button);
        final CheckBox welcomeEnableCheckBox = (CheckBox) findViewById(R.id.welcome_enable_checkbox);
        final FrameLayout welcomeContainer = (FrameLayout) findViewById(R.id.frame_welcome_container);
        //welcomeContainer.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        //welcomeContainer.getBackground().setDither(true);
        Log.i(Constants.TAG, "onCreate: current config:" + SoulissApp.getCurrentConfig());
        welcomeEnableCheckBox.setChecked(SoulissApp.isWelcomeDisabled());

        final Spinner confSpinner = (Spinner) findViewById(R.id.configSpinner);
        final Button renameButton = (Button) findViewById(R.id.welcome_tour_rename);
        final Button deleteButton = (Button) findViewById(R.id.welcome_tour_delete);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int selectedIdx = fillSpinnerConfig(spinnerAdapter);
        confSpinner.setAdapter(spinnerAdapter);

        if (selectedIdx < 0) {
            //non c'e -> e` il default
            Log.w(Constants.TAG, "Warning: no config for spinner:" + SoulissApp.getCurrentConfig());
            SoulissApp.setCurrentConfig("default");
            SoulissApp.addConfiguration("default");
            spinnerAdapter.add("default");

            //we're loading from a old soulissapp
            try {
                ContextWrapper c = new ContextWrapper(WelcomeActivity.this);
                final File importDir = c.getFilesDir();
                File filePrefs = new File(importDir, "PORTING_SoulissApp.prefs");
                //salvataggio dei vecchi valori
                filePrefs.createNewFile();
                SharedPreferences customCachedPrefs = WelcomeActivity.this.getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
                SoulissUtils.saveSharedPreferencesToFile(customCachedPrefs, WelcomeActivity.this, filePrefs);
                //li includo nella conf corrente che verra` poi salvata in startup
                SoulissUtils.loadSharedPreferencesFromFile(WelcomeActivity.this, filePrefs);
            } catch (Exception ee) {
                Log.e(Constants.TAG, "Impossibile eseguire porting da files precedenti: " + ee.getMessage());
            }

            spinnerAdapter.notifyDataSetChanged();
            selectedIdx = spinnerAdapter.getCount() - 1;
        } else if (selectedIdx > 2) {//user defined
            renameButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
        confSpinner.setSelection(selectedIdx, true);


        //RENAME CONFIGURATION
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogHelper.renameConfigDialog(WelcomeActivity.this, confSpinner).create().show();
            }
        });

        //DELETE CONFIGURATION
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogHelper.deleteConfigDialog(WelcomeActivity.this, confSpinner).create().show();
            }
        });

        //DISABLE WELCOME
        welcomeEnableCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoulissApp.saveWelcomeDisabledPreference(welcomeEnableCheckBox.isChecked());
            }
        });

        confSpinner.setOnTouchListener(mDelayHideTouchListener);
        confSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.w(Constants.TAG, "Config spinner selected val:" + confSpinner.getSelectedItem());
                String newConfig = confSpinner.getSelectedItem().toString();
                String previousConfig = SoulissApp.getCurrentConfig();
                Log.w(Constants.TAG, "Current Config:" + previousConfig);
                if (newConfig.equals(getResources().getStringArray(R.array.configChooserArray)[0])) {
                    //non si cambia la DEMO
                    renameButton.setVisibility(View.INVISIBLE);
                    deleteButton.setVisibility(View.INVISIBLE);
                } else if (confSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.configChooserArray)[1])) {
                    renameButton.setVisibility(View.INVISIBLE);
                    deleteButton.setVisibility(View.INVISIBLE);
                    Log.i(Constants.TAG, "TODO? forse non c'e da fare nulla qui...(crea nuovo)");
                } else if (confSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.configChooserArray)[2])) {
                    renameButton.setVisibility(View.INVISIBLE);
                    deleteButton.setVisibility(View.INVISIBLE);
                    Log.i(Constants.TAG, "TODO? forse non c'e da fare nulla qui...(importa)");
                } else {
                    renameButton.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                }

                //https://github.com/ribico/souliss_demo/blob/master/souliss_demo.ino


                //clear cached address to put ofline&retriggger checks
                SoulissApp.getOpzioni().clearCachedAddress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        welcomeTourButton.setOnTouchListener(mDelayHideTouchListener);
        welcomeTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load current settings
                ContextWrapper c = new ContextWrapper(WelcomeActivity.this);
                final File importDir = c.getFilesDir();
                String previousConfig = SoulissApp.getCurrentConfig();
                //change config
                String newConfig = confSpinner.getSelectedItem().toString();
                SoulissApp.setCurrentConfig(newConfig);
                if (!(previousConfig.equalsIgnoreCase(newConfig))) {
                    //SAVE PREVIOUS if old one is not "create new" or "import"
                    if (!previousConfig.equals("") && !(previousConfig.equals(getResources().getStringArray(R.array.configChooserArray)[1]))
                            && !(previousConfig.equals(getResources().getStringArray(R.array.configChooserArray)[2]))) {
                        //save Old DB and config
                        File filePrefs = new File(importDir, previousConfig + "_SoulissApp.prefs");
                        Log.w(Constants.TAG, "Saving old Preferences to: " + filePrefs.getPath());
                        SoulissUtils.saveSharedPreferencesToFile(PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this), WelcomeActivity.this, filePrefs);

                        //locateDB
                        // SoulissDBHelper db = new SoulissDBHelper(WelcomeActivity.this);
                        try {
                            SoulissDBHelper present = new SoulissDBHelper(WelcomeActivity.this);
                            SoulissDBHelper.open();
                            Log.d(Constants.TAG, "going to backup DB (bytes): " + present.getSize());

                            String DbPath = SoulissDBHelper.getDatabase().getPath();
                            File oldDb = new File(DbPath);
                            File bckDb = new File(importDir, previousConfig + "_" + SoulissDB.DATABASE_NAME);
                            Log.w(Constants.TAG, "Saving old DB: " + DbPath + " to: " + bckDb.getPath());
                            SoulissUtils.fileCopy(oldDb, bckDb);

                            Log.w(Constants.TAG, "Deleting saved DB: " + oldDb.getPath());
                            present.truncateAll();
                        } catch (IOException e) {
                            Log.w(Constants.TAG, "ERROR Saving old DB to: " + previousConfig);
                        }
                    }

                    //Adesso carico la nuova
                    if (newConfig.equals(getResources().getStringArray(R.array.configChooserArray)[0])) {
                        //DEMO
                        loadOrCreateDemoConfig(importDir, newConfig);
                        try {
                            SoulissUtils.loadSoulissDbFromFile(WelcomeActivity.this, newConfig, importDir);
                        } catch (IOException e1) {
                            Log.w(Constants.TAG, "DB DEMO non disponibile: " + newConfig);
                            String DbPath = SoulissDBHelper.getDatabase().getPath();
                            SoulissDBHelper.getDatabase().close();
                            File newDb = new File(DbPath);
                            if (!newDb.exists())
                                try {
                                    newDb.createNewFile();
                                } catch (IOException e) {
                                    Log.e(Constants.TAG, "SERIO DB DEMO non generabile:" + newConfig);
                                }
                        }
                    } else if (newConfig.equals(getResources().getStringArray(R.array.configChooserArray)[1])) {
                        Log.i(Constants.TAG, "Nothing here");
                    } else if (newConfig.equals(getResources().getStringArray(R.array.configChooserArray)[2])) {
                        Log.i(Constants.TAG, "Nothing here");
                    } else { //caso dinamico
                        File filePrefs;
                        try {
                            filePrefs = new File(importDir, newConfig + "_SoulissApp.prefs");
                            if (!filePrefs.exists())
                                throw new Resources.NotFoundException();
                            SoulissUtils.loadSharedPreferencesFromFile(WelcomeActivity.this, filePrefs);
                            Log.w(Constants.TAG, newConfig + " prefs loaded");
                        } catch (Resources.NotFoundException e) {
                            //MAI creato prima? WTF
                            Log.e(Constants.TAG, "Errore import config " + newConfig, e);
                            //faccio default
                            try {
                                filePrefs = new File(importDir, newConfig + "_SoulissApp.prefs");
                                //se non esiste la demo, Crea & salva
                                filePrefs.createNewFile();
                                SharedPreferences newDefault = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                                SharedPreferences.Editor demo = newDefault.edit();
                                demo.clear();
                                demo.commit();
                                Log.w(Constants.TAG, "new EMPTY prefs created to: " + filePrefs.getPath());
                                SoulissUtils.saveSharedPreferencesToFile(newDefault, WelcomeActivity.this, filePrefs);
                            } catch (IOException e1) {
                                Log.e(Constants.TAG, "Errore GRAVE load prefs " + newConfig + e1.getMessage());
                            }
                        }
                        try {
                            SoulissUtils.loadSoulissDbFromFile(WelcomeActivity.this, newConfig, importDir);
                        } catch (Exception te) {
                            //MAI creato prima? WTF
                            Log.e(Constants.TAG, "Errore loadSoulissDbFromFile " + newConfig, te);
                        }

                    }
                }

                //dopo aver caricato opzioni, richiedo ping
                SoulissApp.getOpzioni().setBestAddress();

                //here we've already chosen config and loaded right files
                if (confSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.configChooserArray)[1])) {
                    Intent createNewConfig = new Intent(WelcomeActivity.this, WelcomeCreateConfigActivity.class);
                    startActivity(createNewConfig);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    supportFinishAfterTransition();
                } else if (confSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.configChooserArray)[2])) {
                    Intent createNewConfig = new Intent(WelcomeActivity.this, WelcomeImportConfigActivity.class);
                    startActivity(createNewConfig);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    supportFinishAfterTransition();
                } else {
                    startSoulissMainActivity();
                }
            }
        });


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
        //trigger launcher build
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                SoulissDBLauncherHelper dbLauncher = new SoulissDBLauncherHelper(WelcomeActivity.this);
            }
        });


        /* check for first time run */
        welcomeEnabledCheck();
    }


    private void startSoulissMainActivity() {
        Intent myIntent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        supportFinishAfterTransition();
    }

    private void welcomeEnabledCheck() {
        boolean skipWelcome = SoulissApp.isWelcomeDisabled();
        //boolean firstTimeRun = true;
        if (skipWelcome)
            startSoulissMainActivity();
    }
}
