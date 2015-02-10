package it.angelic.soulissclient;

import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.fragments.NodeDetailFragment;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


public class NodeDetailActivity extends AbstractStatusedFragmentActivity {
	private SoulissPreferenceHelper opzioni;
	private TypicalsListAdapter ta;
	private SoulissDBHelper database;
	private SoulissDataService mBoundService;
	private boolean mIsBound;

	/* SOULISS DATA SERVICE BINDING */
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {

			mBoundService = ((SoulissDataService.LocalBinder) service).getService();
			if (ta != null)
				ta.setmBoundService(mBoundService);
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
			 if (ta != null)
			ta.setmBoundService(null);
			Toast.makeText(NodeDetailActivity.this, "Dataservice disconnected", Toast.LENGTH_SHORT).show();
		}
	};
	//private ImageView nodeic;
	//private Handler timeoutHandler;
	private SoulissNode collected;

	void doBindService() {
		if (!mIsBound) {
			bindService(new Intent(NodeDetailActivity.this, SoulissDataService.class), mConnection,
					Context.BIND_AUTO_CREATE);
			mIsBound = true;
		}
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		// recuper nodo da extra
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// If the screen is now in landscape mode, we can show the
			// dialog in-line with the list so we don't need this activity.
			finish();
			return;
		}
		
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.get("NODO") != null)
			collected = (SoulissNode) extras.get("NODO");
		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			NodeDetailFragment details = new NodeDetailFragment();
			details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, details).commit();
        }
	}
	@Override
	protected void onStart() {
		database = new SoulissDBHelper(this);
		super.onStart();
			ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    /**
	 * chiamato dal layout
	 */
	public void startOptions(View v){
		opzioni.setBestAddress();
		Toast.makeText(this, getString(R.string.ping)+" - "+getString(R.string.command_sent), Toast.LENGTH_SHORT).show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nodedetail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ImageView icon = (ImageView) findViewById(R.id.node_icon);
		switch (item.getItemId()) {
		case android.R.id.home:
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// nothing to do here...
			} else {
				finish();
				if (opzioni.isAnimationsEnabled())
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
			return true;
		case R.id.Opzioni:
			Intent settingsActivity = new Intent(this, PreferencesActivity.class);
			startActivity(settingsActivity);
			final Intent preferencesActivity = new Intent(this.getBaseContext(), PreferencesActivity.class);
			// evita doppie aperture per via delle sotto-schermate
			preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(preferencesActivity);
			return true;
		case R.id.CambiaIcona:
			AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(this, icon, null, database, collected);
			alert2.show();
			return true;
		case R.id.Rinomina:
			AlertDialog.Builder alert = AlertDialogHelper.renameSoulissObjectDialog(this, null, null, database,
					collected);
			alert.show();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// meccanismo per timeout detection
	/*
	 * private BroadcastReceiver timeoutReceiver = new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) {
	 * Log.w(TAG, "Posting timeout from " + intent.toString()); Bundle extras =
	 * intent.getExtras(); int delay = extras.getInt("REQUEST_TIMEOUT_MSEC"); }
	 * };
	 */

}
