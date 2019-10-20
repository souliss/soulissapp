package it.angelic.soulissclient.preferences;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import it.angelic.soulissclient.R;

/**
 * Created by shine@angelic.it on 21/01/2017.
 */

public class LauncherSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bind opzione reset
        Preference exportDBPref = findPreference("rstlauncher");
        LauncherRstListener dbPrefListener = new LauncherRstListener(getActivity());
        exportDBPref.setOnPreferenceClickListener(dbPrefListener);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_launcher);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof LauncherAddCustomPreference) {
            DialogFragment dialogFragment = LauncherAddCustomDialog.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), null);
        } else super.onDisplayPreferenceDialog(preference);

    }
}
