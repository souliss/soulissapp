package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

@TargetApi(11)
public class NetSettingsFragment extends PreferenceFragment {

	private SoulissPreferenceHelper opzioni;
	private Preference userIndex;
	private Preference nodeIndex;

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
		//GOOGLE PLUS link
		Preference creditsPref = (Preference) findPreference("credits");
		creditsPref.setTitle(getResources().getString(R.string.app_name) + " Version " + strVersionName);
		creditsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //open browser or intent here
            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/113934123042484468682/posts")));
            	return true;
            }
        });

		/*
		 * generale automaticamente l'ultimo byte (tra 0x01 e 0xFE) e
		 * permettendone la variazione manuale, questo byte sarebbe da
		 * identificare come "Node Index", mentre il primo byte "User Index" può
		 * essere impostato ad un valore di default, modificabile manualmente
		 * tra 0x01 e 0x64.
		 * 
		 * In questo modo, l'User Index non verrebbe mai toccato a meno che non
		 * vengano generati due "Node Index" uguali, a quel punto l'utente dovrà
		 * intervenire manualmente per modificarli.
		 */
		userIndex = (Preference) findPreference("userindexIC");
		String stdrMeatFormat = getActivity().getString(R.string.opt_userindex_desc);
		userIndex.setSummary(String.format(stdrMeatFormat,  opzioni.getUserIndex()));
		userIndex.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.w(Constants.TAG, "CHANGING USER INDEX:" + newValue);
				try {
					String ics = (String) newValue;
					Integer rete = Integer.parseInt(ics);
					if (rete > 63 || rete < 1 )//enforce 0 < x < 64
						throw new IllegalArgumentException();
					opzioni.setUserIndex(rete);
					String stdrMeatFormat = getActivity().getString(R.string.opt_userindex_desc);
					userIndex.setSummary(String.format(stdrMeatFormat,  opzioni.getUserIndex()));
				} catch (Exception e) {
					Toast.makeText(getActivity(), "Please insert a number in range 1-64", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});

		nodeIndex = (Preference) findPreference("nodeindexIC");
		String strMeatFormat = getActivity().getString(R.string.opt_nodeindex_desc);
		nodeIndex.setSummary(String.format(strMeatFormat,  opzioni.getNodeIndex()));
		
		nodeIndex.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.w(Constants.TAG, "CHANGING NODE INDEX:" + newValue);
				try {
					String ics = (String) newValue;
					if (Integer.parseInt(ics) > 127 || Integer.parseInt(ics) < 1 )//enforce 0 < x < 64
						throw new IllegalArgumentException();
					opzioni.setNodeIndex(Integer.parseInt(ics));
					String strMeatFormat = getActivity().getString(R.string.opt_nodeindex_desc);
					nodeIndex.setSummary(String.format(strMeatFormat,  opzioni.getNodeIndex()));
				} catch (Exception e) {
					Toast.makeText(getActivity(), "Please insert a number in range 1-127", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
	}
@Override
public void onStart() {
	String nodeFormat = getActivity().getString(R.string.opt_nodeindex_desc);
	String userFormat = getActivity().getString(R.string.opt_userindex_desc);
	super.onStart();
	userIndex.setSummary(String.format(userFormat,  opzioni.getUserIndex()));
	nodeIndex.setSummary(String.format(nodeFormat,  opzioni.getNodeIndex()));
}
}