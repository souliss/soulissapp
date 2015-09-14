package it.angelic.soulissclient;

import android.app.Activity;
import android.content.Intent;
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
import java.util.List;
import java.util.Locale;

import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.drawer.DrawerItemClickListener;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;


/**
 * Tutte le activity con l'icona stato online devono estendere questa
 *
 * @author Ale
 */
public abstract class AbstractStatusedFragmentActivity extends AppCompatActivity {
    private static final int REQUEST_OK = 1;
    SoulissPreferenceHelper opzioni = SoulissClient.getOpzioni();
    private Toolbar actionBar;

    //IL drawer ci piace qui
    LinearLayout mDrawerLinear;
    // private CharSequence mTitle;

    private TextView info1;
    private TextView info2;
    DrawerMenuHelper dmh;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;
    NavDrawerAdapter mDrawermAdapter;
    private FloatingActionButton mDrawerFloatButt;
    TextView actionTitle;

    /**
     * chiamato dal layout xml
     * <p/>
     * public void startOptions(View v) {
     * opzioni.setBestAddress();
     * Toast.makeText(this, getString(R.string.ping) + " " + getString(R.string.command_sent), Toast.LENGTH_SHORT)
     * .show();
     * }
     */

    @Override
    protected void onStart() {
        actionBar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        //if (actionBar != null)
        setSupportActionBar(actionBar);
        super.onStart();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ctx_menu_voice, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.voiceCommand) {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
            try {
                startActivityForResult(i, REQUEST_OK);
            } catch (Exception e) {
                Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
            }
            return true;//cliccato sul drawer, non far altro
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // ((TextView)findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
                    final String yesMan = thingsYouSaid.get(0).toLowerCase();
                    Log.i(Constants.TAG, "onActivityResult, searching command");
                    final StringBuilder comandToSend = new StringBuilder();
                    if (yesMan.contains(getString(R.string.TurnON).toLowerCase())) {
                        comandToSend.append("" + it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OnCmd);
                    } else if (yesMan.contains(getString(R.string.TurnOFF).toLowerCase())) {
                        comandToSend.append("" + it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OffCmd);
                    }

                    if (comandToSend.length() > 0) {//se c'e un comando

                        Log.i(Constants.TAG, "Command recognized:" + yesMan);
                        SoulissDBHelper db = new SoulissDBHelper(AbstractStatusedFragmentActivity.this);
                        List<SoulissNode> nodes = db.getAllNodes();
                        for (final SoulissNode premio : nodes) {
                            List<SoulissTypical> tippi = premio.getTypicals();
                            for (final SoulissTypical treppio : tippi) {
                                if (treppio.getName() != null && yesMan.contains(treppio.getName().toLowerCase())) {
                                    Log.i(Constants.TAG, "Voice Command SENT!! :" + treppio.getName());
                                    Toast.makeText(AbstractStatusedFragmentActivity.this, getString(R.string.command_sent), Toast.LENGTH_LONG).show();

                                    if (yesMan.contains(getString(R.string.all)))
                                        UDPHelper.issueMassiveCommand("" + treppio.getTypical(), opzioni, comandToSend.toString());
                                    else
                                        UDPHelper.issueSoulissCommand("" + premio.getId(), "" + treppio.getSlot(), opzioni, comandToSend.toString());
                                    return;
                                }


                            }
                        }
                    }

                }
            }).start();
        }
    }


    void setActionBarInfo(String title) {
        try {
            //actionBar = getSupportActionBar();
            View ds = actionBar.getRootView();

            ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
            TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);
            actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
            actionTitle.setText(title);
            if (!opzioni.isSoulissReachable()) {
                online.setBackgroundResource(R.drawable.red);
                statusOnline.setTextColor(getResources().getColor(R.color.std_red));
                statusOnline.setText(R.string.offline);

            } else {
                online.setBackgroundResource(R.drawable.green);
                statusOnline.setTextColor(getResources().getColor(R.color.std_green));
                statusOnline.setText(R.string.Online);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "null bar? " + e.getMessage());
        }
    }

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
}
