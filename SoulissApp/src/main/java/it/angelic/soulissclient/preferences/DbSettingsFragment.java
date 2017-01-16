package it.angelic.soulissclient.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * Fragment of database options
 */
public class DbSettingsFragment extends PreferenceFragment {

    private DbPreferenceListener dbPrefListener;
    private Preference exportDBPref;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();


		addPreferencesFromResource(R.xml.settings_db);
		Preference createDbPref = findPreference("createdb");
		Preference dropDbPref = findPreference("dropdb");
        exportDBPref = findPreference("dbexp");
        Preference imortDBPref = findPreference("dbimp");
		Preference optimDBPref = findPreference("dbopt");
		Preference dbinfopref = findPreference("dbinfo");
		Preference sharesettingspref = findPreference("settingshare");
		/* listeners DB */
        dbPrefListener = new DbPreferenceListener(getActivity());
        exportDBPref.setOnPreferenceClickListener(dbPrefListener);
        imortDBPref.setOnPreferenceClickListener(dbPrefListener);
        createDbPref.setOnPreferenceClickListener(dbPrefListener);
        dropDbPref.setOnPreferenceClickListener(dbPrefListener);
        optimDBPref.setOnPreferenceClickListener(dbPrefListener);
        sharesettingspref.setOnPreferenceClickListener(dbPrefListener);

        String strMeatFormat = getResources().getString(R.string.opt_dbinfo_desc);
		String nonode = getString(R.string.dialog_disabled_db);
		final String strMeatMsg = opzioni.getCustomPref().getInt("numNodi", 0) == 0 ? nonode : String.format(
				strMeatFormat, opzioni.getCustomPref().getInt("numNodi", 0),
				opzioni.getCustomPref().getInt("numTipici", 0));
		dbinfopref.setSummary(strMeatMsg);

	}

}