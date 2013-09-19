package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class NodesListActivity extends SherlockFragmentActivity {
	private SoulissDBHelper datasource;
	List<SoulissNode> goer;
	private SoulissPreferenceHelper opzioni;

	// private FragmentTabHost mTabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		opzioni.reload();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		datasource = new SoulissDBHelper(getBaseContext());
		// use fragmented panel/ separate /land
		setContentView(R.layout.main_frags);

	}
	@SuppressLint("NewApi")
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(getString(R.string.manual_title));
		}
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
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.nodeslist_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            // app icon in action bar clicked; go home
            /*Intent intent = new Intent(NodesListActivity.this, LauncherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;*/
			finish();
			return true;
		case R.id.Opzioni:
			Intent settingsActivity = new Intent(NodesListActivity.this, PreferencesActivity.class);
			startActivity(settingsActivity);
			return true;
			// TODO scelta tipo ordinamento
		case R.id.Refresh:
			new Thread(new Runnable() {
				@Override
				public void run() {
					UDPHelper.healthRequest(opzioni, goer.size(), 0);

				}
			}).start();

			if (!opzioni.isSoulissReachable())
				Toast.makeText(NodesListActivity.this, "Refresh failed: " + getString(R.string.status_souliss_notreachable),
						Toast.LENGTH_SHORT).show();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
}
