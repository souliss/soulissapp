package it.angelic.soulissclient.preferences;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

@TargetApi(11)
public class VisualSettingsFragment extends PreferenceFragmentCompat {

	private SoulissPreferenceHelper opzioni;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		opzioni = SoulissApp.getOpzioni();

		final Preference restoreWarns = findPreference("restoredialogs");
		final Preference restoreWelcome = findPreference("restorewelcome");

		// Rimette i dialogs
        restoreWarns.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				opzioni.setDontShowAgain(getResources().getString(R.string.dialog_disabled_db), false);
				opzioni.setDontShowAgain(getResources().getString(R.string.dialog_disabled_service), false);
                opzioni.setDontShowAgain("launcherInfo", false);
                opzioni.setDontShowAgain("tagsInfo", false);
                opzioni.setDontShowAgain("scenesInfo", false);
                opzioni.setDontShowAgain("programsInfo", false);
                opzioni.setDontShowAgain("manualInfo", false);
                Toast.makeText(getActivity(), SoulissApp.getAppContext().getString(R.string.opt_dialog_restored),
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});

        restoreWelcome.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				SoulissApp.saveWelcomeDisabledPreference(false);
				Toast.makeText(getActivity(), SoulissApp.getAppContext().getString(R.string.opt_welcome_restored),
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});
	}

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_visual);
    }

}