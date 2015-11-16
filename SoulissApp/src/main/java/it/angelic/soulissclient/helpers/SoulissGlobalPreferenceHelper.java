package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.angelic.soulissclient.Constants;

/**
 * Gestione standard preferenze/opzioni Cntiene anche le shared preferences per
 * valori personalizzati. Va prelevata da SoulissClient
 *
 * Da non cancellare mai, valutare cloud
 *
 * @author shine
 */
public class SoulissGlobalPreferenceHelper implements Serializable {
    // private static final Object countLock = new Object();
    private static final long serialVersionUID = -7522233636731669014L;
    private final Context contx;
    private SharedPreferences customCachedPrefs;
    private Set<String> ipDictionary;

    public SoulissGlobalPreferenceHelper(Context contx) {
        super();
        this.contx = contx;

        initializePrefs(contx);
    }

    public void addWordToIpDictionary(String word) {
        ipDictionary.add(word);
        customCachedPrefs.edit().putStringSet("ipDictionary", ipDictionary).apply();
        Log.i(Constants.TAG, "IP added to dictionary. Current size:" + ipDictionary.size());
    }

    public List<String> getIpDictionary() {
        return new ArrayList(ipDictionary);
    }

    /**
     * Load preferences and set cached address
     */
    public void initializePrefs(Context contx) {
        // Get the xml/preferences.xml preferences
        customCachedPrefs = contx.getSharedPreferences("SoulissGlobalPrefs", Activity.MODE_PRIVATE);

        Set<String> newDefaults = new HashSet<>();
        //autocomplete initial values
        newDefaults.add(Constants.DEMO_PUBLIC_IP);
        newDefaults.add("192.168.1.17");
        ipDictionary = customCachedPrefs.getStringSet("ipDictionary", newDefaults);

    }

}
