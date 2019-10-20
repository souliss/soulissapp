package it.angelic.soulissclient;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.preferences.BroadcastSettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private static String getVersionName(Context context) {
        PackageInfo packageInfo;
        String strVersionName;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            // strVersionCode = "Version Code: "
            // + String.valueOf(packageInfo.versionCode);
            strVersionName = packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.TAG, "Cannot load Version!", e);
            strVersionName = "Cannot load Version!";
        }
        return strVersionName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Bundle extras = getIntent().getExtras();
        String collected = "";
        if (extras != null)
            collected = (String) extras.get(PreferenceActivity.EXTRA_SHOW_FRAGMENT);
        boolean useBroadcast = BroadcastSettingsFragment.class.getName().equals(collected);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, useBroadcast ? new BroadcastFragment() : new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            // Github contrib link
            Preference creditsPref = findPreference("credits");
            creditsPref.setTitle(getResources().getString(R.string.souliss_app_name) + " Version " + getVersionName(getContext()));
            creditsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    // open browser or intent here
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("https://github.com/orgs/souliss/people")));
                    return true;
                }
            });

        }
    }

    public static class BroadcastFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_broadcast_new, rootKey);
        }
    }
}