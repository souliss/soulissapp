package it.angelic.soulissclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.drawer.DrawerItemClickListener;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

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
    TextView actionTitle;
    private Toolbar actionBar;
    private boolean hasPosted;
    Runnable timeExpired = new Runnable() {
        @Override
        public void run() {
            refreshStatusIcon();
            opzioni.getAndSetCachedAddress();
            hasPosted = false;
        }
    };
    private TextView info1;
    private TextView info2;
    private FloatingActionButton mDrawerFloatButt;
    private Handler timeoutHandler = new Handler();
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // opzioni.initializePrefs();
            // rimuove timeout
            Log.i(TAG, "TIMEOUT CANCELED");
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

    void initDrawer(final Activity parentActivity, int activeSection) {

        // DRAWER
        dmh = new DrawerMenuHelper();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        info1 = (TextView) findViewById(R.id.textViewDrawerInfo1);
        info2 = (TextView) findViewById(R.id.textViewDrawerInfo2);
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
                info2.setText(getString(R.string.app_name) + " " + (opzioni.isSoulissReachable() ? getString(R.string.Online) : getString(R.string.offline)));
                info1.setText("Souliss is controlling " + opzioni
                        .getCustomPref().getInt("numTipici", 0) + " Things");
            }
        };
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLinear = (LinearLayout) findViewById(R.id.left_drawer_linear);
        mDrawerFloatButt = (FloatingActionButton) findViewById(R.id.fabSmall);
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
                    myIntent.putExtra("TAG", SoulissDB.FAVOURITES_TAG_ID);

                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    AbstractStatusedFragmentActivity.this.startActivity(myIntent);
                }
            });
        } else {
            mDrawerFloatButt.setVisibility(View.INVISIBLE);
        }

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
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
            // ((TextView)findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
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
        filtera.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_TIMEOUT);
        registerReceiver(packetSentNotificationReceiver, filtera);

        // IDEM, serve solo per reporting
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        registerReceiver(datareceiver, filtere);

        //DEVASTO TUTTO
        opzioni.setBestAddress();
    }


    @Override
    protected void onStart() {
        actionBar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        //if (actionBar != null)
        setSupportActionBar(actionBar);
        super.onStart();

    }

    public void refreshStatusIcon() {
        try {
            View ds = actionBar.getRootView();
            if (ds != null) {
                ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
                TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);

                if (!opzioni.isSoulissReachable()) {
                    online.setBackgroundResource(R.drawable.red);
                    statusOnline.setTextColor(getResources().getColor(R.color.std_red));
                    statusOnline.setText(R.string.offline);
                } else {
                    online.setBackgroundResource(R.drawable.green);
                    statusOnline.setTextColor(getResources().getColor(R.color.std_green));
                    statusOnline.setText(R.string.Online);
                }
                statusOnline.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setActionBarInfo(String title) {
        try {
            //actionBar = getSupportActionBar();
            View ds = actionBar.getRootView();

            actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
            actionTitle.setText(title);
            refreshStatusIcon();
        } catch (Exception e) {
            Log.e(Constants.TAG, "null bar? " + e.getMessage());
        }
    }

    public void setSynching() {
        View ds = actionBar.getRootView();
        if (ds != null) {
            ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
            TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);
            online.setBackgroundResource(R.drawable.red5);
            statusOnline.setTextColor(getResources().getColor(R.color.std_yellow));
            statusOnline.setText(R.string.synch);
        }
    }
}
