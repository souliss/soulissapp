package it.angelic.soulissclient.preferences;

import android.app.Activity;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.net.webserver.HTTPService;

public class WebServerPreferenceListener implements OnPreferenceChangeListener {

	private Activity parent;

	// EXPORT

	// private SoulissPreferenceHelper opzioni;
	// private SoulissDBHelper datasource;

	public WebServerPreferenceListener(Activity parent) {
		super();
		this.parent = parent;
		// opzioni = SoulissClient.getOpzioni();
		// Define the criteria how to select the locatioin provider -> use
		// default
		// datasource = new SoulissDBHelper(parent);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if ("webserverEnabled".equals(preference.getKey())) {
			try {
				Intent serviceIntent = new Intent(parent, HTTPService.class);
				if ((Boolean) newValue) {
					parent.startService(serviceIntent);
					Log.w(Constants.TAG, "Startin Souliss WEBSERVER");
				} else {
					parent.stopService(serviceIntent);
					Log.w(Constants.TAG, "Stopping Souliss WEBSERVER");
				}
				return true;
			} catch (Exception e) {
				Log.e(Constants.TAG, "errore webServer", e);
				Toast.makeText(parent, parent.getString(R.string.opt_WebServerPass_err), Toast.LENGTH_SHORT).show();
				return true;
			}
		} else {
			return false;
		}

	}

}
