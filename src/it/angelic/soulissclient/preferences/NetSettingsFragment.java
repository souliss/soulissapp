package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

@TargetApi(11)
public class NetSettingsFragment extends PreferenceFragment {

	private SoulissPreferenceHelper opzioni;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		opzioni = SoulissClient.getOpzioni();
		PackageInfo packageInfo;
		String strVersionName;

		addPreferencesFromResource(R.xml.settings_net);
		Preference privateIP = (Preference) findPreference("edittext_IP");
		Preference publicIP = (Preference) findPreference("edittext_IP_pubb");

		String summar = getResources().getString(R.string.summary_edittext_IP);
		privateIP.setSummary(opzioni.getPrefIPAddress().compareToIgnoreCase("") == 0 ? summar : opzioni
				.getPrefIPAddress());
		// listener cambio IP
		String summarP = getResources().getString(R.string.summary_edittext_IP_pubb);
		publicIP.setSummary(opzioni.getIPPreferencePublic().compareToIgnoreCase("") == 0 ? summarP : opzioni
				.getIPPreferencePublic());

		OnPreferenceChangeListener ipChanger = new IpChangerListener(getActivity());
		privateIP.setOnPreferenceChangeListener(ipChanger);
		publicIP.setOnPreferenceChangeListener(ipChanger);
		try {
			packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			// strVersionCode = "Version Code: "
			// + String.valueOf(packageInfo.versionCode);
			strVersionName = packageInfo.versionName;

		} catch (NameNotFoundException e) {
			Log.e(Constants.TAG, "Cannot load Version!", e);
			strVersionName = "Cannot load Version!";
		}
		Preference creditsPref = (Preference) findPreference("credits");
		creditsPref.setTitle(getResources().getString(R.string.app_name) + " Version " + strVersionName);
		
		/*Preference checkConnectionPref = (Preference) findPreference("checkconnection");

		// test della connessione
		checkConnectionPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				String checkIP = opzioni.getPrefIPAddress();
				String checkIPPUBB = opzioni.getIPPreferencePublic();
				// se l'utente e` mona
				if (checkIP == null) {
					AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(getActivity());
					alert.show();
					return true;
				}
				
				 * NON VA CAZZO try { InetAddress checkIPt =
				 * InetAddress.getByName(checkIP); //InetAddress checkIPt =
				 * InetAddress.getByAddress(checkIP); if
				 * (checkIPt.isReachable(3000)) { Log.i("testConnection",
				 * "I hit google! ip is:" + checkIPt.getHostAddress()); } }
				 * catch (Exception e) { Log.e("testConnection",
				 * "PING error",e); }
				 
				ProgressDialog alert = AlertDialogHelper.checkConnectionResultDialog(getActivity(), checkIP,
						checkIPPUBB, opzioni);
				alert.show();
				return true;
			}
		});
	
*/
		final Preference userIdx = (Preference) findPreference("userindexIC");
		userIdx.setSummary("The user index identify this device on Souliss boards. Current value is "+opzioni.getUserIndex()+". Tap to change it");
		userIdx.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.w(Constants.TAG, "CHANGING USER INDEX:"+newValue);
				try {
					//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
					//Editor pesta = prefs.edit();
					String ics = (String) newValue;
					opzioni.setUserIndex(Integer.parseInt(ics));
					userIdx.setSummary("The user index identify this device on Souliss boards. Current value is "+opzioni.getUserIndex()+". Tap to change it");
				} catch (Exception e) {
					Toast.makeText(getActivity(), "Please insert a number in range 0-127", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
	}

}