package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

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
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		else
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
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
	
	
	
}
