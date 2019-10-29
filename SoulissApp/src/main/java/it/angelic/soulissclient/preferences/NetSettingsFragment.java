package it.angelic.soulissclient.preferences;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;


public class NetSettingsFragment extends PreferenceFragmentCompat {

	private SoulissPreferenceHelper opzioni;
	private Preference userIndex;
    private Preference nodeIndex;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.settings_net, rootKey);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		opzioni = SoulissApp.getOpzioni();


		Preference privateIP =  findPreference("edittext_IP");
		Preference publicIP =  findPreference("edittext_IP_pubb");
		Preference bCast = findPreference("advbroadcastKey");
        userIndex =  findPreference("userindexIC");
        Preference udpport = findPreference("udpportIC");
        nodeIndex = findPreference("nodeindexIC");

		String summar = getResources().getString(R.string.summary_edittext_IP);
		privateIP.setSummary(opzioni.getPrefIPAddress().compareToIgnoreCase("") == 0 ? summar : opzioni
				.getPrefIPAddress());
		// listener cambio IP
		String summarP = getResources().getString(R.string.summary_edittext_IP_pubb);
		publicIP.setSummary(opzioni.getIPPreferencePublic().compareToIgnoreCase("") == 0 ? summarP : opzioni
				.getIPPreferencePublic());

		androidx.preference.Preference.OnPreferenceChangeListener ipChanger = new IpChangerListener(getActivity());
		privateIP.setOnPreferenceChangeListener(ipChanger);
		publicIP.setOnPreferenceChangeListener(ipChanger);

		//BROADCAST Settings
        //bCast.setOnPreferenceClickListener(new BroadcastSettingsPreferenceListener(getActivity()));


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
		 *
		 */

		String stdrMeatFormat = getActivity().getString(R.string.opt_userindex_desc);
		userIndex.setSummary(String.format(stdrMeatFormat, opzioni.getUserIndex()));
        //udpport.setSummary(String.format(stdrMeatFormat, opzioni.getUDPPort()));
		udpport.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.w(Constants.Net.TAG, "CHANGING UDP PORT:" + newValue);
				try {
					String ics = (String) newValue;
                    Integer rete = Integer.parseInt(ics);
                    // enforce 0 < x < 0xfe
                    if (rete >= 65535 || rete < 1)
                        throw new IllegalArgumentException();
                    opzioni.setUDPPort(rete);
                    String stdrMeatFormat = getString(R.string.opt_udpport);

                } catch (Exception e) {
                    Toast.makeText(getActivity(), getString(R.string.udphint), Toast.LENGTH_SHORT)
                            .show();
				}
				return true;
			}
		});
		userIndex.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.w(Constants.TAG, "CHANGING USER INDEX:" + newValue);
				try {
					String ics = (String) newValue;
					Integer rete = Integer.parseInt(ics);
					if (rete >= Constants.MAX_USER_IDX || rete < 1)// enforce 0 < x < 64
						throw new IllegalArgumentException();
					opzioni.setUserIndex(rete);
					String stdrMeatFormat = getActivity().getString(R.string.opt_userindex_desc);
					userIndex.setSummary(String.format(stdrMeatFormat, opzioni.getUserIndex()));
				} catch (Exception e) {
					Toast.makeText(getActivity(), getString(R.string.useridxhint), Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});


		String strMeatFormat = getActivity().getString(R.string.opt_nodeindex_desc);
		nodeIndex.setSummary(String.format(strMeatFormat, opzioni.getNodeIndex()));

		nodeIndex.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.w(Constants.TAG, "CHANGING NODE INDEX:" + newValue);
				try {
					String ics = (String) newValue;
					if (Integer.parseInt(ics) >= Constants.MAX_NODE_IDX || Integer.parseInt(ics) < 1)
						throw new IllegalArgumentException();
					opzioni.setNodeIndex(Integer.parseInt(ics));
					String strMeatFormat = getActivity().getString(R.string.opt_nodeindex_desc);
					nodeIndex.setSummary(String.format(strMeatFormat, opzioni.getNodeIndex()));
				} catch (Exception e) {
					Toast.makeText(getActivity(), getString(R.string.nodeidxhint), Toast.LENGTH_SHORT).show();
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
		userIndex.setSummary(String.format(userFormat, opzioni.getUserIndex()));
		nodeIndex.setSummary(String.format(nodeFormat, opzioni.getNodeIndex()));
	}

	
}