package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class NodesListActivity extends AbstractStatusedFragmentActivity {
	private SoulissDBHelper datasource;
	List<SoulissNode> goer;
	
	private ImageButton online;

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

	}

	@Override
	protected void onStart() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setCustomView(R.layout.custom_actionbar); // load your layout
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM ); // show
		actionBar.setDisplayHomeAsUpEnabled(true);
		View ds = actionBar.getCustomView();
		online = (ImageButton) ds.findViewById(R.id.action_starred);
		TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);
		TextView actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
		
		actionTitle.setText(getString(R.string.manual_title));
		if (!opzioni.isSoulissReachable()) {
			online.setBackgroundResource(R.drawable.red);
			statusOnline.setTextColor(getResources().getColor(R.color.std_red));
			statusOnline.setText(R.string.offline);
		} else {
			online.setBackgroundResource(R.drawable.green);
			statusOnline.setTextColor(getResources().getColor(R.color.std_green));
			statusOnline.setText(R.string.Online);
		}
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
			// app icon in action bar clicked; go home
			finish();
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
