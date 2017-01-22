package it.angelic.soulissclient.preferences;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * Created by shine@angelic.it on 21/01/2017.
 */

public class LauncherSettingsFragment extends PreferenceFragment {

    private SoulissPreferenceHelper opzioni;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        opzioni = SoulissApp.getOpzioni();
        PackageInfo packageInfo;
        String strVersionName;

        addPreferencesFromResource(R.xml.settings_launcher);

        Preference exportDBPref = findPreference("rstlauncher");
        LauncherPreferenceListener dbPrefListener = new LauncherPreferenceListener(getActivity());
        exportDBPref.setOnPreferenceClickListener(dbPrefListener);
    }
}
