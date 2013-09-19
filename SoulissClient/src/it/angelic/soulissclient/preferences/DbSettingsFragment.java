package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

@TargetApi(11)
public class DbSettingsFragment extends PreferenceFragment {

	private SoulissPreferenceHelper opzioni;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		//String settings;
		if (opzioni.isLightThemeSelected()) {
			getActivity().setTheme(R.style.LightThemeSelector);
		} else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings_db);
		Preference createDbPref = (Preference) findPreference("createdb");
		Preference dropDbPref = (Preference) findPreference("dropdb");
		Preference exportDBPref = (Preference) findPreference("dbexp");
		Preference imortDBPref = (Preference) findPreference("dbimp");
		Preference dbinfopref = (Preference) findPreference("dbinfo");
		/* listeners DB */
		exportDBPref.setOnPreferenceClickListener(new DbPreferenceListener(getActivity()));
		imortDBPref.setOnPreferenceClickListener(new DbPreferenceListener(getActivity()));
		createDbPref.setOnPreferenceClickListener(new DbPreferenceListener(getActivity()));
		dropDbPref.setOnPreferenceClickListener(new DbPreferenceListener(getActivity()));

		String strMeatFormat = getResources().getString(R.string.opt_dbinfo_desc);
		String nonode = "Souliss not configured yet, DB is empty";
		final String strMeatMsg = opzioni.getCustomPref().getInt("numNodi", 0) == 0 ? nonode : String.format(
				strMeatFormat, opzioni.getCustomPref().getInt("numNodi", 0),
				opzioni.getCustomPref().getInt("numTipici", 0));
		dbinfopref.setSummary(strMeatMsg);

	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

}