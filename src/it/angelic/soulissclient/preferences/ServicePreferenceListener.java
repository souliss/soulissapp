package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissDataService;
import android.app.Activity;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class ServicePreferenceListener implements OnPreferenceChangeListener {

	private Activity parent;
	// EXPORT

	//private SoulissPreferenceHelper opzioni;
	//private SoulissDBHelper datasource;

	public ServicePreferenceListener(Activity parent) {
		super();
		this.parent = parent;
		//opzioni = SoulissClient.getOpzioni();
		// Define the criteria how to select the locatioin provider -> use
		// default
		//datasource = new SoulissDBHelper(parent);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if ("checkboxService".equals(preference.getKey())) {
			Intent serviceIntent = new Intent(parent, SoulissDataService.class);
			if ((Boolean) newValue == true) {

				parent.startService(serviceIntent);
				Log.w(Constants.TAG, "Startin Souliss Service");
			} else {
				parent.stopService(serviceIntent);
				Log.w(Constants.TAG, "Stopping Souliss Service TODO");
			}
			return true;
		}  else {
			return false;
		}
	}

}
