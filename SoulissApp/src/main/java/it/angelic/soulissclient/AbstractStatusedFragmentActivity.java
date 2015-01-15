package it.angelic.soulissclient;

import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Tutte le activity con l'icona stato online devono estendere questa
 * 
 * @author Ale
 * 
 */
public class AbstractStatusedFragmentActivity extends ActionBarActivity {
	protected SoulissPreferenceHelper opzioni = SoulissClient.getOpzioni();
	protected ActionBar actionBar;

	/**
	 * chiamato dal layout xml
	 */
	public void startOptions(View v) {
		opzioni.setBestAddress();
		Toast.makeText(this, getString(R.string.ping) + " " + getString(R.string.command_sent), Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected void onStart() {

		actionBar = getSupportActionBar();

		actionBar.setCustomView(R.layout.custom_actionbar); // load your layout
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM); // show
		actionBar.setDisplayHomeAsUpEnabled(true);
		super.onStart();
	}

	protected void setActionBarInfo(String title) {
		try {
			actionBar = getSupportActionBar();
			View ds = actionBar.getCustomView();
			ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
			TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);
			TextView actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
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
}
