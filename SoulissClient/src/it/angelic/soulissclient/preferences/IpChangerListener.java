package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.net.UDPHelper;

import java.net.InetAddress;

import android.app.Activity;
import android.os.Looper;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class IpChangerListener implements OnPreferenceChangeListener {

	private Activity parent;
	private SoulissPreferenceHelper opzioni;

	public IpChangerListener(Activity parent) {
		super();
		this.parent = parent;
		opzioni = SoulissClient.getOpzioni();
	}

	@Override
	public boolean onPreferenceChange(final Preference preference, final Object newValue) {
		new Thread() {
			public void run() {
				Looper.prepare();
				// find old preference
				String old;
				if (preference.getKey() != null && preference.getKey().compareTo("edittext_IP_pubb") == 0)
					old = opzioni.getIPPreferencePublic();
				else if (preference.getKey() != null && preference.getKey().compareTo("edittext_IP") == 0)
					old = opzioni.getPrefIPAddress();
				else {
					Log.e(Constants.TAG, "Error UNIMPLEMENTED");
					old = "";
				}
				final String newval = newValue.toString();

				if (newval != null && newval.compareTo("") == 0) {
					parent.runOnUiThread(new Runnable() {
						public void run() {
							if ("edittext_IP_pubb".compareTo(preference.getKey()) == 0)
								preference.setSummary(parent.getString(R.string.summary_edittext_IP_pubb));
							else if ("edittext_IP".compareTo(preference.getKey()) == 0)
								preference.setSummary(parent.getString(R.string.summary_edittext_IP));
						}
					});

					opzioni.setIPPreferencePublic("");
					return;
				}
				// sanity check
				try {
					final InetAddress checkIPt = InetAddress.getByName(newValue.toString());
					final String pars = " (" + checkIPt.getHostName() + ")";
					parent.runOnUiThread(new Runnable() {
						public void run() {
							preference.setSummary(checkIPt.getHostAddress() + (pars.length() > 3 ? pars : ""));
						}
					});

				} catch (final Exception e) {
					Log.e(Constants.TAG, "Error in address parsing: " + e.getMessage(), e);
					parent.runOnUiThread(new Runnable() {
						public void run() {
							preference.setSummary(parent.getResources().getString(R.string.opt_invalidAddress) + ": "
									+ e.getMessage());
						}
					});

					return;
				}
				// trigger connection test se il valore pubblico e`
				// ok e
				// diverso dal vecchio
				if (old.compareTo(newval) != 0) {

					opzioni.clearCachedAddress();
					UDPHelper.checkSoulissUdp(opzioni.getRemoteTimeoutPref(), opzioni, newval);
					// TODO error after timeout
					if (preference.getKey() != null && preference.getKey().compareTo("edittext_IP_pubb") == 0) {
						opzioni.setIPPreferencePublic(newval);
					} else if (preference.getKey() != null && preference.getKey().compareTo("edittext_IP") == 0) {
						opzioni.setIPPreference(newval);
					}
					opzioni.setBestAddress();

				}

				return;
			}
		}.start();
		return true;
	}

}
