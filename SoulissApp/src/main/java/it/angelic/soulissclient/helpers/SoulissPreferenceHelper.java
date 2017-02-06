package it.angelic.soulissclient.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.net.UDPHelper;

import static it.angelic.soulissclient.Constants.TAG;

/**
 * Gestione standard preferenze/opzioni Cntiene anche le shared preferences per
 * valori personalizzati. Va prelevata da SoulissClient
 *
 * @author shine
 */
public class SoulissPreferenceHelper implements Serializable {
    // private static final Object countLock = new Object();
    private static final long serialVersionUID = -7522863636731669014L;
    private String IPPreference;
    private String IPPreferencePublic;
    private float ListDimensTesto;// dimensione testo tipici e info autore
    private Integer UDPPort;
    private boolean animations;
    private boolean antitheftNotify;
    private boolean antitheftPresent;
    private int audioInputChannel;
    private int backoff = 1;
    private boolean broadCastEnabled;

    private Context contx;
    // numNodes ed altri valori cached
    private String cachedAddr;
    private boolean dataServiceEnabled;
    private int dataServiceInterval;
    private float eqHigh;
    private float eqHighRange;
    private float eqLow;
    private float eqLowRange;
    private float eqMed;
    private float eqMedRange;
    private boolean fahrenheitChosen;
    // effetti fade-in e cagate
    private boolean fxOn;
    private int homeThold;
    private boolean lightTheme;
    private boolean logHistoryEnabled;
    private int nodeIndex;
    private int remoteTimeoutPref;
    private boolean rgbSendAllDefault;
    private long serviceLastrun;
    // private InetAddress cachedInet;
    private int userIndex;
    private boolean voiceCommandEnabled;

    private boolean isTaskerEnabled;
    private boolean isTaskerInterested;

    private SharedPreferences customCachedPrefs;

    public SoulissPreferenceHelper(Context contx) {
        super();
        this.contx = contx;
        //customCachedPrefs = contx.getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
        customCachedPrefs = PreferenceManager.getDefaultSharedPreferences(contx);
        initializePrefs();
        // Log.d(TAG, "Constructing prefs");
    }

    public void clearCachedAddress() {
        SharedPreferences.Editor editor = customCachedPrefs.edit();
        if (customCachedPrefs.contains("cachedAddress"))
            editor.remove("cachedAddress");
        editor.apply();
        cachedAddr = null;
    }

    /*
     * return currently used Souliss address may return null and then
     * re-initialize it
     */
    public String getAndSetCachedAddress() {
        if (cachedAddr == null) {
            if (customCachedPrefs.contains("cachedAddress")) {

                cachedAddr = customCachedPrefs.getString("cachedAddress", "");
                Log.i(TAG, "returning Cachedaddress: " + cachedAddr);

                return cachedAddr;
            }
            increaseBackoffTimeout();
            Log.i(TAG, "cached Address null, increased backoff to: " + backoff);
            setBestAddress();
        }
        return cachedAddr;
    }

    public int getAudioInputChannel() {
        return audioInputChannel;
    }

    public Long getBackedOffServiceIntervalMsec() {
        return (long) (getDataServiceIntervalMsec() * backoff);
    }

    public int getBackoff() {
        return backoff;
    }

    public String getCachedAddress() {
        return cachedAddr;
    }

    public Context getContx() {
        return contx;
    }

    public SharedPreferences getCustomPref() {
        return customCachedPrefs;
    }

    public int getDataServiceIntervalMsec() {
        return dataServiceInterval * 60;
    }

    public boolean getDontShowAgain(String string) {
        return customCachedPrefs.getBoolean("dontshow" + string, false);
    }

    public float getEqHigh() {
        return eqHigh;
    }

    public void setAudioInputChannel(int audioInputChannel) {
        this.audioInputChannel = audioInputChannel;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putInt("audioChan", audioInputChannel);
        pesta.commit();
    }

    public void setEqHigh(float eqHigh) {
        this.eqHigh = eqHigh;
        Editor pesta = customCachedPrefs.edit();
        pesta.putFloat("eqHigh", eqHigh);
        pesta.apply();
    }

    public float getEqHighRange() {
        return eqHighRange;
    }

    public void setEqHighRange(float eqHighRange) {
        this.eqHighRange = eqHighRange;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putFloat("eqHighRange", eqHighRange);
        pesta.apply();
    }

    public float getEqLow() {
        return eqLow;
    }

    public void setEqLow(float eqLow) {
        this.eqLow = eqLow;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putFloat("eqLow", eqLow);
        pesta.apply();
    }

    public float getEqLowRange() {
        return eqLowRange;
    }

    public void setEqLowRange(float eqLowRange) {
        this.eqLowRange = eqLowRange;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putFloat("eqLowRange", eqLowRange);
        pesta.apply();
    }

    public float getEqMed() {
        return eqMed;
    }

    public void setEqMed(float eqMed) {
        this.eqMed = eqMed;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putFloat("eqMed", eqMed);
        pesta.apply();
    }

    public float getEqMedRange() {
        return eqMedRange;
    }

    public void setEqMedRange(float eqMedRange) {
        this.eqMedRange = eqMedRange;
        Editor pesta = customCachedPrefs.edit();
        pesta.putFloat("eqMedRange", eqMedRange);
        pesta.apply();
    }

    public double getHomeLatitude() {
        return Double.parseDouble(customCachedPrefs.getString("homelatitude", "0"));
    }

    public void setHomeLatitude(double lat) {
        Editor pesta = customCachedPrefs.edit();
        pesta.putString("homelatitude", String.valueOf(lat));
        pesta.apply();
    }

    public double getHomeLongitude() {
        return Double.parseDouble(customCachedPrefs.getString("homelongitude", "0"));
    }

    public void setHomeLongitude(double lat) {
        Editor pesta = customCachedPrefs.edit();
        pesta.putString("homelongitude", String.valueOf(lat));
        pesta.apply();
    }

    public int getHomeThresholdDistance() {
        return homeThold;
    }

    public String getIPPreferencePublic() {
        return IPPreferencePublic;
    }

    public void setIPPreferencePublic(String iPPreferencePublic) {
        IPPreferencePublic = iPPreferencePublic;
    }

    public Float getListDimensTesto() {
        return ListDimensTesto;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
        Editor pesta = customCachedPrefs.edit();
        pesta.putInt("nodeIndex", nodeIndex);
        pesta.apply();
    }

    public String getPrefIPAddress() {
        return IPPreference;
    }

    /**
     * Serve perche l'oggetto PreferenceHelper potrebbe essere ri-creato e non
     * consistente rispetto alla rete. Salvo in
     * customCachedPrefs.cachedAddress l'indirizzo attuale dopo la chiamata
     * a setBestAddress
     *
     * @return
     */
    public float getPrevDistance() {
        return customCachedPrefs.getFloat("lastDistance", 0);
    }

    public void setPrevDistance(float in) {
        SharedPreferences.Editor editor = customCachedPrefs.edit();
        editor.putFloat("lastDistance", in);
        editor.apply();
    }

    public int getRemoteTimeoutPref() {
        return remoteTimeoutPref;
    }

    public void setRemoteTimeoutPref(int remoteTimeoutPref) {
        this.remoteTimeoutPref = remoteTimeoutPref;
        Editor pesta = customCachedPrefs.edit();
        pesta.putInt("remoteTimeout", userIndex);
        pesta.apply();
    }

    public long getServiceLastrun() {
        return serviceLastrun;
    }

    public boolean getTextFx() {
        return fxOn;
    }

    public void setTextFx(boolean nfx) {
        fxOn = nfx;
    }

    public Integer getUDPPort() {
        return UDPPort;
    }

    public void setUDPPort(Integer UDPPort) {
        this.UDPPort = UDPPort;
        Editor pesta = customCachedPrefs.edit();
        pesta.putInt("udpport", this.UDPPort);
        pesta.apply();
    }

    public int getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(int userIndex) {
        this.userIndex = userIndex;
        Editor pesta = customCachedPrefs.edit();
        pesta.putInt("userIndex", userIndex);
        pesta.apply();

    }

    public void increaseBackoffTimeout() {
        backoff++;
    }

    /**
     * Load preferences and set cached address
     */
    public void initializePrefs() {
        // Get the xml/preferences.xml preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contx);
        fxOn = prefs.getBoolean("checkboxTestoEffetti", false);
        lightTheme = prefs.getBoolean("checkboxHoloLight", true);
        IPPreference = prefs.getString("edittext_IP", "");
        IPPreferencePublic = prefs.getString("edittext_IP_pubb", "");
        remoteTimeoutPref = Integer.parseInt(prefs.getString("remoteTimeout", "3000"));
        dataServiceInterval = prefs.getInt("updateRate", 10) * 1000;
        homeThold = prefs.getInt("distanceThold", 150);
        dataServiceEnabled = prefs.getBoolean("checkboxService", false);
        fahrenheitChosen = prefs.getBoolean("checkboxFahrenheit", false);
        voiceCommandEnabled = prefs.getBoolean("checkboxVoiceCommand", true);
        userIndex = prefs.getInt("userIndex", -1);
        nodeIndex = prefs.getInt("nodeIndex", -1);
        UDPPort = prefs.getInt("udpport", Constants.Net.DEFAULT_SOULISS_PORT);
        isTaskerEnabled = prefs.getBoolean("taskerEnabled", false);
        isTaskerInterested = prefs.getBoolean("taskerInterested", false);
        animations = prefs.getBoolean("checkboxAnimazione", true);
        antitheftPresent = prefs.getBoolean("antitheft", false);
        antitheftNotify = prefs.getBoolean("antitheftNotify", false);
        broadCastEnabled = prefs.getBoolean("checkboxBroadcast", true);
        rgbSendAllDefault = prefs.getBoolean("rgbSendAllDefault", true);
        logHistoryEnabled = prefs.getBoolean("checkboxLogHistory", true);
        audioInputChannel = prefs.getInt("audioChan", 0);//0 default, 1 MIC
        eqLow = prefs.getFloat("eqLow", 1f);
        eqMed = prefs.getFloat("eqMed", 1f);
        eqHigh = prefs.getFloat("eqHigh", 1f);
        eqLowRange = prefs.getFloat("eqLowRange", 0.33f);
        eqMedRange = prefs.getFloat("eqMedRange", 0.66f);
        eqHighRange = prefs.getFloat("eqHighRange", 1f);

        Calendar fake = Calendar.getInstance();
        fake.add(Calendar.MONTH, -2);// Default value in the past
        serviceLastrun = prefs.getLong("serviceLastrun", Calendar.getInstance().getTimeInMillis());

    }

    public boolean isAnimationsEnabled() {
        return animations;
    }

    public boolean isAntitheftNotify() {
        return antitheftNotify;
    }

    public void setAntitheftNotify(boolean antith) {
        antitheftNotify = antith;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putBoolean("antitheftNotify", antitheftNotify);
        pesta.apply();

    }

    public boolean isAntitheftPresent() {
        return antitheftPresent;
    }

    public void setAntitheftPresent(boolean antitheftPresent) {
        this.antitheftPresent = antitheftPresent;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putBoolean("antitheft", antitheftPresent);
        pesta.apply();
    }

    public boolean isBroadCastEnabled() {
        return broadCastEnabled;
    }

    public boolean isDataServiceEnabled() {
        return dataServiceEnabled;
    }

    /**
     * Ritorna se il DB � configurato. Basato sulla presenza o meno in
     * sharedPrefs del numero di nodi su DB
     *
     * @return true se DB popolato
     */
    public boolean isDbConfigured() {
        return (customCachedPrefs.contains("numNodi")) && customCachedPrefs.getInt("numNodi", 0) != 0;
    }

    public boolean isFahrenheitChosen() {
        return fahrenheitChosen;
    }

    public boolean isLightThemeSelected() {
        return lightTheme;
    }

    public boolean isLogHistoryEnabled() {
        return logHistoryEnabled;
    }

    public boolean isRgbSendAllDefault() {
        return rgbSendAllDefault;
    }

    public void setRgbSendAllDefault(boolean rgbSendAllDefault) {
        this.rgbSendAllDefault = rgbSendAllDefault;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putBoolean("rgbSendAllDefault", rgbSendAllDefault);
        pesta.apply();
    }

    public boolean isTaskerEnabled() {
        return isTaskerEnabled;
    }

    public void setTaskerEnabled(boolean isTaskerEnabled) {
        this.isTaskerEnabled = isTaskerEnabled;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putBoolean("taskerEnabled", isTaskerEnabled);
        pesta.apply();
    }

    public boolean isTaskerInterested() {
        return isTaskerInterested;
    }

    public void setTaskerInterested(boolean isTaskerInterested) {
        this.isTaskerInterested = isTaskerInterested;
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putBoolean("taskerInterested", isTaskerEnabled);
        pesta.commit();
    }

    public boolean isSoulissIpConfigured() {
        // check se IP non settato
        return !(getPrefIPAddress() == null || "".compareTo(getPrefIPAddress()) == 0);
    }

    public boolean isSoulissPublicIpConfigured() {
        return !(getIPPreferencePublic() == null || "".compareTo(getIPPreferencePublic()) == 0);
    }

    /**
     * legge cached address per vedere se connesso
     *
     * @return Unavailable quando la rete c'�, ma souliss non risponde
     */
    public boolean isSoulissReachable() {
        // getCachedAddress();
        return !(cachedAddr == null || cachedAddr.compareTo("") == 0
                || cachedAddr.compareTo(contx.getString(R.string.unavailable)) == 0);

    }

    public boolean isVoiceCommandEnabled() {
        return voiceCommandEnabled;
    }

    static Random r = new Random(Calendar.getInstance().getTimeInMillis());
    public void reload() {
        Log.i(TAG, "Going thru preference reload()");
        // SharedPreferences prefs =
        // PreferenceManager.getDefaultSharedPreferences(contx);
        initializePrefs();
        if (userIndex == -1) {// MAI inizializzato, lo calcolo
            /* USER INDEX, statico */

            int casual = r.nextInt(Constants.MAX_USER_IDX - 1);// 100
            setUserIndex(casual);
            Log.i(Constants.TAG, "automated userIndex-index Using: " + casual);
        }
        if (nodeIndex == -1) {// MAI inizializzato, lo calcolo
            /* PHONE ID diventa node index */
            try {
                    nodeIndex = ((Secure.getString(contx.getContentResolver(), Secure.ANDROID_ID)).hashCode() % (Constants.MAX_NODE_IDX - 1));
                nodeIndex = Math.abs(nodeIndex);
                if (nodeIndex == 0)
                    nodeIndex++;
                Log.w(TAG, "Pref init END. Node index = " + nodeIndex);
                setNodeIndex(nodeIndex);
            } catch (Exception e) {// fallito il computo, uso random e lo salvo
                //Random r = new Random(Calendar.getInstance().getTimeInMillis());
                int casual = r.nextInt(Constants.MAX_NODE_IDX);
                setNodeIndex(casual);
                Log.e(Constants.TAG, "automated Node-index fail " + e.getMessage() + ". Using " + casual);
            }
        }
        // reset cachedAddress to shared prefs one
        //clearCachedAddress();
        //resetBackOff();
        getAndSetCachedAddress();
    }

    public void resetBackOff() {
        backoff = 1;
    }

    /**
     * Called to refresh cached address
     * <p/>
     * invia al piu due ping, che innescano il check di rete
     */
    public void setBestAddress() {

        new Thread() {
            public void run() {
                // basta che sia connesso
                if (isSoulissPublicIpConfigured() && customCachedPrefs.contains("connection"))
                    UDPHelper.checkSoulissUdp(getRemoteTimeoutPref() * 3, SoulissPreferenceHelper.this,
                            getIPPreferencePublic());
                // ci vuole WIFI
                if (isSoulissIpConfigured()
                        && customCachedPrefs.getInt("connection", -1) == ConnectivityManager.TYPE_WIFI)
                    UDPHelper.checkSoulissUdp(getRemoteTimeoutPref() * 3, SoulissPreferenceHelper.this,
                            getPrefIPAddress());
                else if (broadCastEnabled
                        && customCachedPrefs.getInt("connection", -1) == ConnectivityManager.TYPE_WIFI) {
                    // Broadcast
                    Log.w(Constants.TAG, "if everything bad, try BROADCAST address");
                    UDPHelper.checkSoulissUdp(getRemoteTimeoutPref() * 3, SoulissPreferenceHelper.this, Constants.Net.BROADCASTADDR);
                }
            }
        }.start();

    }

    public void setCachedAddr(String cachedAd) {
        cachedAddr = cachedAd;
    }

    public void setDontShowAgain(String string, boolean val) {
        Editor pesta = customCachedPrefs.edit();
        pesta.putBoolean("dontshow" + string, val);
        pesta.commit();
    }

    public void setIPPreference(String newIP) {// serve anche commit perche`
        // chiamata fuori da prefs
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putString("edittext_IP", newIP);
        pesta.apply();
        this.IPPreference = newIP;
    }

    public void setLastServiceRun(Calendar lastupd) {
        serviceLastrun = lastupd.getTimeInMillis();
        Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
        pesta.putLong("serviceLastrun", serviceLastrun);
        pesta.commit();
    }

	/*public void setLogHistoryEnabled(boolean logHistoryEnabled) {
        this.logHistoryEnabled = logHistoryEnabled;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putBoolean("checkboxLogHistory", logHistoryEnabled);
		pesta.commit();
	}*/
}
