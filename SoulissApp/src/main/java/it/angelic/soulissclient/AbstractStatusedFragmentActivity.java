package it.angelic.soulissclient;

import it.angelic.soulissclient.adapters.SceneListAdapter;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.drawer.DrawerItemClickListener;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissScene;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.LinkedList;


/**
 * Tutte le activity con l'icona stato online devono estendere questa
 * 
 * @author Ale
 * 
 */
public abstract class AbstractStatusedFragmentActivity extends ActionBarActivity {
	protected SoulissPreferenceHelper opzioni = SoulissClient.getOpzioni();
	protected Toolbar actionBar;

    //IL drawer ci piace qui
    protected LinearLayout mDrawerLinear;
    // private CharSequence mTitle;

    protected TextView info1;
    protected TextView info2;
    protected DrawerMenuHelper dmh;
    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;
    protected NavDrawerAdapter mDrawermAdapter;
    private FloatingActionButton mDrawerFloatButt;
    protected TextView actionTitle;

    /**
	 * chiamato dal layout xml

	public void startOptions(View v) {
		opzioni.setBestAddress();
		Toast.makeText(this, getString(R.string.ping) + " " + getString(R.string.command_sent), Toast.LENGTH_SHORT)
				.show();
	}*/

    @Override
	protected void onStart() {
         actionBar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        //if (actionBar != null)
        setSupportActionBar(actionBar);
		super.onStart();
	}

	protected void setActionBarInfo(String title) {
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

    protected void initDrawer(final Activity parentActivity,int activeSection){

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
                info2.setText(getString(R.string.app_name)+" "+(opzioni.isSoulissReachable()?getString(R.string.Online):getString(R.string.offline)));
                info1.setText("Souliss is controlling "+opzioni
                        .getCustomPref().getInt("numTipici", 0)+" Things");
            }
        };
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLinear = (LinearLayout)findViewById(R.id.left_drawer_linear);
        mDrawerFloatButt  = (FloatingActionButton) findViewById(R.id.fabSmall);
        SoulissDBTagHelper db = new SoulissDBTagHelper(parentActivity);
        db.open();
        if (db.countFavourites() > 0) {
            mDrawerFloatButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerList.setItemChecked(DrawerMenuHelper.TAGS, true);
                    // setTitle(mPlanetTitles[position]);
                    mDrawerLayout.closeDrawer(mDrawerLinear);
                    Intent myIntent = new Intent(AbstractStatusedFragmentActivity.this, TagDetailActivity.class);
                    //I preferiti son sempre quelli
                    myIntent.putExtra("TAG", (long) SoulissDB.FAVOURITES_TAG_ID);

                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    AbstractStatusedFragmentActivity.this.startActivity(myIntent);
                }
            });
        }else{
            mDrawerFloatButt.setVisibility(View.INVISIBLE);
        }

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawermAdapter = new NavDrawerAdapter(parentActivity, R.layout.drawer_list_item, dmh.getStuff(), activeSection);
        mDrawerList.setAdapter(mDrawermAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(this, mDrawerList, mDrawerLayout,mDrawerLinear));

    }
}
