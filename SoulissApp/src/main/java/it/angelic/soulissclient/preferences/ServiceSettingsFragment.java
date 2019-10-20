package it.angelic.soulissclient.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.util.SoulissUtils;

@TargetApi(11)
public class ServiceSettingsFragment extends PreferenceFragmentCompat {
    Preference homeDist;
    private SoulissPreferenceHelper opzioni;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();

        super.onCreate(savedInstanceState);
        final LocationManager locationManager;
        // EXPORT

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // datasource = new SoulissDBHelper(getActivity());

        final Preference serviceActive = findPreference("checkboxService");
        final Preference setHomeLocation = findPreference("setHomeLocation");
        Preference updateRate = findPreference("updateRate");
        Preference homeDist = findPreference("distanceThold");
        /* START STOP SoulissDataService */
        serviceActive.setOnPreferenceChangeListener(new ServicePreferenceListener(getActivity()));

        // Setta home location
        setHomeLocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    String provider = locationManager.getBestProvider(SoulissUtils.getGeoCriteria(), true);
                    //Location luogo = locationManager.getLastKnownLocation(provider);

                    // faccio sto schifo per trigger di SecurityException.
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    opzioni.setHomeLatitude(location.getLatitude());
                    opzioni.setHomeLongitude(location.getLongitude());
                    opzioni.initializePrefs();
                    resetMesg(setHomeLocation);
                    Toast.makeText(getActivity(), getString(R.string.opt_homepos_set), Toast.LENGTH_SHORT).show();
                } catch (SecurityException xe) {
                    Log.e(Constants.TAG, "PERMISSION DENIED", xe);
                    Toast.makeText(getActivity(), "location permission denied from user", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(Constants.TAG, getString(R.string.opt_homepos_err), e);
                    Toast.makeText(getActivity(), getString(R.string.opt_homepos_err), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        updateRate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Integer) newValue < 15) {
                    return false;
                }
                try {
                    Log.w(Constants.TAG, "Service rete new val: " + newValue);
                    opzioni.getDataServiceIntervalMsec();
                    final Preference updateRate = findPreference("updateRate");
                    String strMeatFormat = getResources().getString(R.string.opt_serviceinterval_desc);
                    // opzioni.initializePrefs();
                    updateRate.setSummary(getResources().getString(R.string.every) + " " + newValue + " min. " + strMeatFormat);
                } catch (Exception ree) {
                    //amen
                    Log.e(Constants.TAG, ree.getMessage());
                }
                return true;
            }
        });

        homeDist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    Preference homeDist = findPreference("distanceThold");
                    String strMeatFormat = getResources().getString(R.string.opt_Thold_desc);
                    homeDist.setSummary(strMeatFormat + " (meters): " + newValue);
                } catch (Exception ree) {
                    //amen
                    Log.e(Constants.TAG, ree.getMessage());
                }
                return true;
            }
        });

        // Setta home location
        resetMesg(setHomeLocation);

    }

    //private static final int DIALOG_LOAD_FILE = 1000;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_dataservice);
    }


    private void resetMesg(Preference setHomeLocation) {
        String loc = null;
        SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        if (opzioni.getHomeLatitude() != 0) {

            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> list;
            try {
                list = geocoder.getFromLocation(opzioni.getHomeLatitude(), opzioni.getHomeLongitude(), 1);
                if (list != null && list.size() > 0) {
                    Address address = list.get(0);
                    loc = address.getLocality();
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "LOCATION ERR:" + e.getMessage());
            }

        }
        setHomeLocation.setSummary(getString(R.string.opt_homepos_set) + ": " + (loc == null ? "" : loc) + " ("
                + opzioni.getHomeLatitude() + " : " + opzioni.getHomeLongitude() + ")");
    }
}
