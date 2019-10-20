package it.angelic.soulissclient.preferences;

import android.content.pm.PackageInfo;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * Created by shine@angelic.it on 21/01/2017.
 */

public class LauncherSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        PackageInfo packageInfo;
        String strVersionName;

        //bind opzione reset
        Preference exportDBPref = findPreference("rstlauncher");
        LauncherRstListener dbPrefListener = new LauncherRstListener(getActivity());
        exportDBPref.setOnPreferenceClickListener(dbPrefListener);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_launcher);
    }
}
