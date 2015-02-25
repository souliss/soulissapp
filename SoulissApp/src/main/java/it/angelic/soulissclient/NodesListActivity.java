package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class NodesListActivity extends AbstractStatusedFragmentActivity {
	private SoulissDBHelper datasource;
	List<SoulissNode> goer;


    // private FragmentTabHost mTabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		opzioni.initializePrefs();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);

		datasource = new SoulissDBHelper(getBaseContext());
		// use fragmented panel/ separate /land
		setContentView(R.layout.main_frags);

        super.initDrawer(NodesListActivity.this, DrawerMenuHelper.MANUAL);
	}

	@Override
	protected void onStart() {
		/*ActionBar actionBar = getSupportActionBar();
		actionBar.setCustomView(R.layout.custom_actionbar); // load your layout
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM ); // show
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(true);*/
		//View ds = actionBar.getCustomView();
		setActionBarInfo(getString(R.string.manual_title));
		super.onStart();
		datasource.open();
		// prendo tipici dal DB
		goer = datasource.getAllNodes();

		new Thread(new Runnable() {
			@Override
			public void run() {
				UDPHelper.healthRequest(opzioni, goer.size(), 0);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// subscribe a tutti i nodi
				UDPHelper.stateRequest(opzioni, goer.size(), 0);
			}
		}).start();

        mDrawermAdapter = new NavDrawerAdapter(NodesListActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.MANUAL);
        mDrawerList.setAdapter(mDrawermAdapter);
        mDrawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nodeslist_menu, menu);
		return true;
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            if (mDrawerLayout.isDrawerOpen(mDrawerLinear)) {
                mDrawerLayout.closeDrawer(mDrawerLinear);
            } else {
                mDrawerLayout.openDrawer(mDrawerLinear);
            }
            return true;
		case R.id.Opzioni:
			Intent settingsActivity = new Intent(NodesListActivity.this, PreferencesActivity.class);
			startActivity(settingsActivity);
			return true;
			// TODO scelta tipo ordinamento
		}

		return super.onOptionsItemSelected(item);
	}

}
