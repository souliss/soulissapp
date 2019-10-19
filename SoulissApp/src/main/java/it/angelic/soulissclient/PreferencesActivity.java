package it.angelic.soulissclient;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.preferences.BroadcastSettingsFragment;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.preferences.LauncherSettingsFragment;
import it.angelic.soulissclient.preferences.NetSettingsFragment;
import it.angelic.soulissclient.preferences.ServiceSettingsFragment;
import it.angelic.soulissclient.preferences.VisualSettingsFragment;

import static it.angelic.soulissclient.Constants.TAG;

public class PreferencesActivity extends PreferenceActivity {


    @Override
    public void onBuildHeaders(List<Header> target) {
        Log.i(TAG, "PreferenceActivityonBuildHeaders()");
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "Permission granted! Please retry", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied from user", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onResume() {
        // IDEM, serve solo per reporting
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        // registerReceiver(macacoRawDataReceiver, filtere);

        super.onResume();
    }

    @Override
    protected void onPause() {
        // unregisterReceiver(macacoRawDataReceiver);
        //lo lascio per garanzia che SoulissApp.getOpzioni() sia aggiornato
        SoulissApp.getOpzioni().reload();

        super.onPause();
    }


    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (DbSettingsFragment.class.getName().equals(fragmentName))
            return true;
        if (NetSettingsFragment.class.getName().equals(fragmentName))
            return true;
        if (ServiceSettingsFragment.class.getName().equals(fragmentName))
            return true;
        if (VisualSettingsFragment.class.getName().equals(fragmentName))
            return true;
        if (LauncherSettingsFragment.class.getName().equals(fragmentName))
            return true;
        return BroadcastSettingsFragment.class.getName().equals(fragmentName);

    }


}
