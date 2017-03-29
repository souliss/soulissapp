package it.angelic.soulissclient.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * Fragment of database options
 */
public class DbSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference dbinfopref;
    private Preference imortDBPref;
    private SoulissPreferenceHelper opzioni;


    /*
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getActivity().getTheme();
            theme.resolveAttribute(R.attr.backgroundTint, typedValue, true);
            @ColorInt  int color = typedValue.data;
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(color);

            return view;
        }
    */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		opzioni = SoulissApp.getOpzioni();
		addPreferencesFromResource(R.xml.settings_db);
		Preference createDbPref = findPreference("createdb");
		Preference dropDbPref = findPreference("dropdb");
        Preference exportDBPref = findPreference("dbexp");
        imortDBPref = findPreference("dbimp");
        Preference optimDBPref = findPreference("dbopt");
        dbinfopref = findPreference("dbinfo");
        Preference sharesettingspref = findPreference("settingshare");
		/* listeners DB */
        DbPreferenceListener dbPrefListener = new DbPreferenceListener(getActivity());
        exportDBPref.setOnPreferenceClickListener(dbPrefListener);
        imortDBPref.setOnPreferenceClickListener(dbPrefListener);
        createDbPref.setOnPreferenceClickListener(dbPrefListener);
        dropDbPref.setOnPreferenceClickListener(dbPrefListener);
        optimDBPref.setOnPreferenceClickListener(dbPrefListener);
        sharesettingspref.setOnPreferenceClickListener(dbPrefListener);

        String strMeatFormat = getResources().getString(R.string.opt_dbinfo_desc);
        String strMeFormat = getResources().getString(R.string.opt_dbimp_desc);
        String nonode = getString(R.string.dialog_disabled_db);
		final String strMeatMsg = opzioni.getCustomPref().getInt("numNodi", 0) == 0 ? nonode : String.format(
				strMeatFormat, opzioni.getCustomPref().getInt("numNodi", 0),
				opzioni.getCustomPref().getInt("numTipici", 0));
		dbinfopref.setSummary(strMeatMsg);

        String strimortDBPrefMsg = String.format(
                strMeFormat, SoulissApp.getCurrentConfig());
        imortDBPref.setSummary(strimortDBPrefMsg);

	}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            String strMeatFormat = getResources().getString(R.string.opt_dbinfo_desc);
            String nonode = getString(R.string.dialog_disabled_db);
            final String strMeatMsg = opzioni.getCustomPref().getInt("numNodi", 0) == 0 ? nonode : String.format(
                    strMeatFormat, opzioni.getCustomPref().getInt("numNodi", 0),
                    opzioni.getCustomPref().getInt("numTipici", 0));
            dbinfopref.setSummary(strMeatMsg);
        } catch (Exception ree) {
            //amen
            Log.e(Constants.TAG, ree.getMessage());
        }
    }
}