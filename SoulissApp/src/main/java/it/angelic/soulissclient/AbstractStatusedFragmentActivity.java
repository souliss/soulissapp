package it.angelic.soulissclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.angelic.soulissclient.drawer.DrawerItemClickListener;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBOpenHelper;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;

import static it.angelic.soulissclient.Constants.TAG;


/**
 * Tutte le activity con l'icona stato online devono estendere questa
 *
 * @author Ale
 */
public abstract class AbstractStatusedFragmentActivity extends AppCompatActivity {

    SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
    //IL drawer ci piace qui
    LinearLayout mDrawerLinear;
    DrawerMenuHelper dmh;
    // private CharSequence mTitle;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;
    NavDrawerAdapter mDrawermAdapter;
    TextView actionTitleTextView;
    int numTries = 0;
    private Toolbar actionBar;
    private boolean hasPosted;
    Runnable timeExpired = new Runnable() {
        @Override
        public void run() {

            Log.w(Constants.TAG, "TIMEOUT!");
            if (numTries > 2) {
                opzioni.clearCachedAddress();
                refreshStatusIcon();//put offline
                numTries = 0;
            } else {
                //spara un ping
                opzioni.setBestAddress();
                numTries++;
                setSynching();//almost offline
            }
            hasPosted = false;

        }
    };
    private TextView info1;
    private TextView info2;
    private Handler timeoutHandler = new Handler();
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // opzioni.initializePrefs();
            // rimuove timeout
            Log.i(TAG, "TIMEOUT CANCELED. cachaddress: " + opzioni.getCachedAddress());
            timeoutHandler.removeCallbacks(timeExpired);
            hasPosted = false;
            refreshStatusIcon();
        }
    };
    // meccanismo per network detection
    private BroadcastReceiver packetSentNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            int delay = extras.getInt("REQUEST_TIMEOUT_MSEC");

            if (!hasPosted) {
                Log.w(Constants.TAG, "Something has been sent with timeout=" + delay);
                timeoutHandler.postDelayed(timeExpired, delay);
                hasPosted = true;
            }
            setSynching();//will last till timeout or online
        }
    };

    public TextView getActionTitleTextView() {
        return actionTitleTextView;
    }

    void initDrawer(final @NonNull Activity parentActivity, int activeSection) {

        // DRAWER
        dmh = new DrawerMenuHelper(parentActivity);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        info1 = findViewById(R.id.textViewDrawerInfo1);
        info2 = findViewById(R.id.textViewDrawerInfo2);
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.warn_wifi, /* "open drawer" description */
                R.string.warn_wifi /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                ActivityCompat.invalidateOptionsMenu(parentActivity);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                ActivityCompat.invalidateOptionsMenu(parentActivity);
                info2.setText(getString(R.string.souliss_app_name) + " " + (opzioni.isSoulissReachable() ? getString(R.string.Online) : getString(R.string.offline))
                        + " - " + getString(R.string.active_config) + ": " + SoulissApp.getCurrentConfig());
                info1.setText("Souliss is controlling " + opzioni
                        .getCustomPref().getInt("numTipici", 0) + " Things");
            }
        };
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLinear = findViewById(R.id.left_drawer_linear);
        FloatingActionButton mDrawerFloatButt = findViewById(R.id.fabSmall);
        SoulissDBTagHelper db = new SoulissDBTagHelper(parentActivity);
        SoulissDBHelper.open();
        if (db.countFavourites() > 0) {
            mDrawerFloatButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerList.setItemChecked(DrawerMenuHelper.TAGS, true);
                    // setTitle(mPlanetTitles[position]);
                    mDrawerLayout.closeDrawer(mDrawerLinear);
                    Intent myIntent = new Intent(AbstractStatusedFragmentActivity.this, TagDetailActivity.class);
                    //I preferiti son sempre quelli
                    myIntent.putExtra("TAG", SoulissDBOpenHelper.FAVOURITES_TAG_ID);

                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    AbstractStatusedFragmentActivity.this.startActivity(myIntent);
                }
            });
        } else {
            mDrawerFloatButt.setVisibility(View.INVISIBLE);
        }

        mDrawerList = findViewById(R.id.left_drawer);
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawermAdapter = new NavDrawerAdapter(parentActivity, R.layout.drawer_list_item, dmh.getStuff(), activeSection);
        mDrawerList.setAdapter(mDrawermAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(this, mDrawerList, mDrawerLayout, mDrawerLinear));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.VOICE_REQUEST_OK && resultCode == RESULT_OK) {

            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            final String yesMan = thingsYouSaid.get(0).toLowerCase();
            Log.i(Constants.TAG, "onActivityResult, searching command: " + yesMan);
            //Invia comando
            VoiceCommandActivityNoDisplay.interpretCommand(this, yesMan);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        timeoutHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (opzioni.isVoiceCommandEnabled()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.ctx_menu_voice, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.voiceCommand) {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "name one of your devices and a command, such as turn on/off, open, close");
            try {
                startActivityForResult(i, Constants.VOICE_REQUEST_OK);
            } catch (Exception e) {
                Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
            }
            return true;//cliccato sul drawer, non far altro
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(packetSentNotificationReceiver);
        unregisterReceiver(datareceiver);
        timeoutHandler.removeCallbacks(timeExpired);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // this is only used for refresh UI
        IntentFilter filtera = new IntentFilter();
        filtera.addAction(Constants.CUSTOM_INTENT_SOULISS_TIMEOUT);
        registerReceiver(packetSentNotificationReceiver, filtera);

        // IDEM, serve solo per reporting
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        registerReceiver(datareceiver, filtere);

        //DEVASTO TUTTO
        opzioni.setBestAddress();
    }

    @Override
    protected void onStart() {
        actionBar = findViewById(R.id.my_awesome_toolbar);
        //if (actionBar != null)
        setSupportActionBar(actionBar);
        super.onStart();

    }

    public void refreshStatusIcon() {
        try {
            View ds = actionBar.getRootView();
            if (ds != null) {
                ImageButton online = ds.findViewById(R.id.online_status_icon);
                TextView statusOnline = ds.findViewById(R.id.online_status);
                if (opzioni.isAnimationsEnabled()) {
                    final Animation animation = new AlphaAnimation(1, 0.2f); // Change alpha from fully visible to invisible
                    animation.setDuration(250);
                    animation.setInterpolator(new LinearOutSlowInInterpolator()); // do not alter animation rate
                    animation.setRepeatCount(1);
                    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
                    online.startAnimation(animation);
                }
                if (!opzioni.isSoulissReachable()) {
                    online.setBackgroundResource(R.drawable.red);
                    statusOnline.setTextColor(ContextCompat.getColor(this, R.color.std_red));
                    statusOnline.setText(R.string.offline);
                } else {
                    online.setBackgroundResource(R.drawable.green);
                    statusOnline.setTextColor(ContextCompat.getColor(this, R.color.std_green));
                    statusOnline.setText(R.string.Online);
                    final int numNodes = opzioni.getCustomPref().getInt("numNodi", 0);
                }
                statusOnline.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Siccome nel rientro da dettaglio nested a dettaglio
     * gli elementi non sono ancora presenti, si postpone la transazione per sbloccarla
     * poi con una chiamata a codesto metodo
     */
    protected void scheduleStartPostponedTransition(final View sharedElement) {
        Log.w(Constants.TAG, "SCHEDULE  ");
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        Log.w(Constants.TAG, "SCHEDULE StartPostponedTransition ");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        }
                        return true;
                    }
                });
    }

    public void setActionBarInfo(String title) {
        try {
            //actionBar = getSupportActionBar();
            View ds = actionBar.getRootView();

            actionTitleTextView = ds.findViewById(R.id.actionbar_title);
            actionTitleTextView.setText(title);
            refreshStatusIcon();
        } catch (Exception e) {
            Log.e(Constants.TAG, "null bar? " + e.getMessage());
        }
    }

    public void setSynching() {
        View ds = actionBar.getRootView();
        if (ds != null) {
            ImageButton online = ds.findViewById(R.id.online_status_icon);
            TextView statusOnline = ds.findViewById(R.id.online_status);
            switch (numTries) {//sempre piu verso il rosso
                case 0:
                    online.setBackgroundResource(R.drawable.red5);
                    break;
                case 1:
                    online.setBackgroundResource(R.drawable.red4);
                    break;
                case 2:
                    online.setBackgroundResource(R.drawable.red3);
                    break;
            }
            statusOnline.setTextColor(ContextCompat.getColor(this, R.color.std_yellow));
            statusOnline.setText(R.string.synch);
        }
    }
}
