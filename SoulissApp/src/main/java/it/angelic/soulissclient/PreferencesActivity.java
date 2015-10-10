package it.angelic.soulissclient;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.preferences.BroadcastSettingsFragment;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.preferences.NetSettingsFragment;
import it.angelic.soulissclient.preferences.ServiceSettingsFragment;
import it.angelic.soulissclient.preferences.VisualSettingsFragment;

import static it.angelic.soulissclient.Constants.TAG;

public class PreferencesActivity extends PreferenceActivity {

    SoulissPreferenceHelper opzioni;

    private String currentScreen;
    // Aggiorna la schermata
    private BroadcastReceiver macacoRawDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Bundle extras = intent.getExtras();
                ArrayList<Short> vers = (ArrayList<Short>) extras.get("MACACO");
                Log.w(TAG, "RAW DATA: " + vers);
                switch (vers.get(0)) {
                    case Constants.Net.Souliss_UDP_function_typreq_resp:
                        // fallthrought x refresh dicitura tipici
                    case Constants.Net.Souliss_UDP_function_db_struct_resp:
                        Log.w(TAG, "DB STRUCT: " + currentScreen);
                        //if (currentScreen != null && currentScreen.equals("db_setup")) {
                       /* Intent inten = PreferencesActivity.this.getIntent();
                        inten.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        PreferencesActivity.this.finish();
                        PreferencesActivity.this.overridePendingTransition(0, 0);
                        inten.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
                        inten.setAction("db_setup");
                        inten.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        Toast.makeText(PreferencesActivity.this,
                                PreferencesActivity.this.getResources().getString(R.string.dbstruct_req),
                                Toast.LENGTH_SHORT).show();
                        PreferencesActivity.this.startActivity(inten);*/
                        getFragmentManager().beginTransaction()
                                .replace(android.R.id.content, new DbSettingsFragment()).commit();
                        //}
                        break;
                /*case Constants.Souliss_UDP_function_ping_resp:// restart
                    if (currentScreen != null && currentScreen.equals("network_setup")) {
						Intent intend = PreferencesActivity.this.getIntent();
						intend.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						PreferencesActivity.this.finish();
						PreferencesActivity.this.overridePendingTransition(0, 0);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
							AlertDialogHelper.setExtra(intend, NetSettingsFragment.class.getName());
						// preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
						intend.setAction("network_setup");

						intend.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						PreferencesActivity.this.overridePendingTransition(0, 0);
						PreferencesActivity.this.startActivity(intend);
					}
					break;*/
                    default:
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "EMPTY RAW dATA !!");
            }
        }
    };


    @Override
    public void onBuildHeaders(List<Header> target) {
        Log.i(TAG, "PreferenceActivityonBuildHeaders()");
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void fit(View v) {
        v.setFitsSystemWindows(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        /*if (opzioni.isLightThemeSelected()) {
            setTheme(R.style.LightThemeSelector);
        } else
            setTheme(R.style.DarkThemeSelector);*/
        super.onCreate(savedInstanceState);

        //ListView v = getListView();
       // v.setCacheColorHint(0);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //setActionBarInfo(getString(R.string.app_opt));
        //currentScreen = getIntent().getExtras().getString("opt_screen");
        ListView v = getListView();
      //  v.setCacheColorHint(0);
        //LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        //bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.custom_actionbar, root, false);
        //root.addView(bar, 0); // insert at top
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            fit(v);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.d(TAG, "Going thru preference onStart(), screeN: " + currentScreen);
            return;
        }
    }

    @Override
    protected void onResume() {
        // IDEM, serve solo per reporting
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(Constants.Net.CUSTOM_INTENT_SOULISS_RAWDATA);
        registerReceiver(macacoRawDataReceiver, filtere);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(macacoRawDataReceiver);
        //lo lascio per garanzia che SoulissApp.getOpzioni() sia aggiornato
        opzioni.reload();

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
        return BroadcastSettingsFragment.class.getName().equals(fragmentName);

    }


}
