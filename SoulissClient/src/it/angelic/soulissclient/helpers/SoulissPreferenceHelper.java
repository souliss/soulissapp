package it.angelic.soulissclient.helpers;

import static it.angelic.soulissclient.Constants.TAG;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.net.UDPHelper;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Gestione standard preferenze/opzioni Cntiene anche le shared preferences per
 * valori personalizzati. Va prelevata da SoulissClient
 * 
 * @author shine
 * 
 */
public class SoulissPreferenceHelper implements Serializable {
	// private static final Object countLock = new Object();
	private static final long serialVersionUID = -7522863636731669014L;
	private Context contx;
	// numNodes ed altri valori cached
	private SharedPreferences customSharedPreference;

	// effetti fade-in e cagate
	private boolean fxOn;
	private boolean lightTheme;

	private String IPPreference;
	private String IPPreferencePublic;
	private float ListDimensTesto;// dimensione testo tipici e info autore
	private int remoteTimeoutPref;
	private String PrefFont;
	private String cachedAddr;
	private int dataServiceInterval;
	private boolean dataServiceEnabled;
	private String DimensTesto;
	// private InetAddress cachedInet;
	private int userIndex;
	private int nodeIndex;
	private int backoff = 1;
	private boolean animations;
	private boolean antitheftPresent;
	private boolean antitheftNotify;
	private long serviceLastrun;
	private long nextServiceRun;
	private float eqLow;
	private float eqMed;
	private float eqHigh;
	private boolean webserverEnabled;
	private int homeThold;
	private boolean broadCastEnabled;
	private String chosenHtmlRootfile;
	private boolean rgbSendAllDefault;

	public SoulissPreferenceHelper(Context contx) {
		super();
		this.contx = contx;
		customSharedPreference = contx.getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
		initializePrefs();
		// Log.d(TAG, "Constructing prefs");
	}

	public void reload() {
		// SharedPreferences prefs =
		// PreferenceManager.getDefaultSharedPreferences(contx);
		initializePrefs();
		if (userIndex == -1) {// MAI inizializzato, lo calcolo
			/* USER INDEX, statico */
			Random r = new Random(Calendar.getInstance().getTimeInMillis());
			int casual = r.nextInt(Constants.MAX_USER_IDX-1);// 100
			setUserIndex(casual);
			Log.i(Constants.TAG, "automated userIndex-index Using: " + casual);
		}
		if (nodeIndex == -1) {// MAI inizializzato, lo calcolo
			/* PHONE ID diventa node index */
			try {
				final TelephonyManager tm = (TelephonyManager) contx.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm.getDeviceId() != null)
					nodeIndex = (int) (Long.parseLong(tm.getDeviceId()) % (Constants.MAX_NODE_IDX-1));
				else
					nodeIndex = ((Secure.getString(contx.getContentResolver(), Secure.ANDROID_ID)).hashCode() % (Constants.MAX_NODE_IDX-1));
				nodeIndex = Math.abs(nodeIndex);
				if (nodeIndex == 0)
					nodeIndex++;
				Log.w(TAG, "Pref init END. Node index hash = " + userIndex);
				setNodeIndex(nodeIndex);
			} catch (Exception e) {// fallito il computo, uso random e lo salvo
				Random r = new Random(Calendar.getInstance().getTimeInMillis());
				int casual = r.nextInt(98) + 1;
				setNodeIndex(casual);
				Log.e(Constants.TAG, "automated Node-index fail " + e.getMessage() + ". Using " + casual);
			}
		}
		// reset cachedAddress to shared prefs one
		clearCachedAddress();
		resetBackOff();
		getAndSetCachedAddress();
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
		DimensTesto = prefs.getString("listPref", "0");
		PrefFont = prefs.getString("fontPref", "Futura.ttf");
		remoteTimeoutPref = Integer.parseInt(prefs.getString("remoteTimeout", "10000"));
		dataServiceInterval = prefs.getInt("updateRate", 10) * 1000;
		homeThold = prefs.getInt("distanceThold", 150);
		dataServiceEnabled = prefs.getBoolean("checkboxService", false);
		webserverEnabled = prefs.getBoolean("webserverEnabled", false);
		userIndex = prefs.getInt("userIndex", -1);
		nodeIndex = prefs.getInt("nodeIndex", -1);
		animations = prefs.getBoolean("checkboxAnimazione", true);
		antitheftPresent = prefs.getBoolean("antitheft", false);
		antitheftNotify = prefs.getBoolean("antitheftNotify", false);
		broadCastEnabled = prefs.getBoolean("checkboxBroadcast", true);
		rgbSendAllDefault = prefs.getBoolean("rgbSendAllDefault", true);
		eqLow = prefs.getFloat("eqLow", 1f);
		eqMed = prefs.getFloat("eqMed", 1f);
		eqHigh = prefs.getFloat("eqHigh", 1f);
		chosenHtmlRootfile= prefs.getString("mChosenFile", "");
		Calendar fake = Calendar.getInstance();
		fake.add(Calendar.MONTH, -2);// Default value in the past
		serviceLastrun = prefs.getLong("serviceLastrun", Calendar.getInstance().getTimeInMillis());
		nextServiceRun = prefs.getLong("nextServiceRun", fake.getTimeInMillis());
		try {
			ListDimensTesto = Float.valueOf(DimensTesto);
		} catch (Exception e) {
			ListDimensTesto = 14;
		}

		/*
		 * try { cachedInet = InetAddress.getByName(IPPreference); } catch
		 * (UnknownHostException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	public void clearCachedAddress() {
		SharedPreferences.Editor editor = customSharedPreference.edit();
		if (customSharedPreference.contains("cachedAddress"))
			editor.remove("cachedAddress");
		editor.commit();
		cachedAddr = null;
	}

	/**
	 * Serve perche l'oggetto PreferenceHelper potrebbe essere ri-creato e non
	 * consistente rispetto alla rete. Salvo in
	 * customSharedPreference.cachedAddress l'indirizzo attuale dopo la chiamata
	 * a setBestAddress
	 * 
	 * @return
	 */
	public float getPrevDistance() {
		return customSharedPreference.getFloat("lastDistance", 0);
	}

	public void setPrevDistance(float in) {
		SharedPreferences.Editor editor = customSharedPreference.edit();
		editor.putFloat("lastDistance", in);
		editor.commit();
	}
	
	

	/*
	 * return currently used Souliss address may return null and then
	 * re-initialize it
	 */
	public String getAndSetCachedAddress() {
		if (cachedAddr == null) {
			if (customSharedPreference.contains("cachedAddress")) {

				cachedAddr = customSharedPreference.getString("cachedAddress", "");
				Log.i(TAG, "returning Cachedaddress: " + cachedAddr);

				return cachedAddr;
			}
			increaseBackoffTimeout();
			Log.i(TAG, "cached Address null, increased backoff to: " + backoff);
			setBestAddress();
		}
		return cachedAddr;
	}

	public String getCachedAddress() {
		return cachedAddr;
	}

	/**
	 * Called to refresh cached address
	 * 
	 * invia al piu due ping, che innescano il check di rete
	 */
	public void setBestAddress() {

		new Thread() {
			public void run() {
				// basta che sia connesso
				if (isSoulissPublicIpConfigured() && customSharedPreference.contains("connection"))
					UDPHelper.checkSoulissUdp(getRemoteTimeoutPref() * 3, SoulissPreferenceHelper.this,
							getIPPreferencePublic());
				// ci vuole WIFI
				if (isSoulissIpConfigured()
						&& customSharedPreference.getInt("connection", -1) == ConnectivityManager.TYPE_WIFI)
					UDPHelper.checkSoulissUdp(getRemoteTimeoutPref() * 3, SoulissPreferenceHelper.this,
							getPrefIPAddress());
				else if (broadCastEnabled
						&& customSharedPreference.getInt("connection", -1) == ConnectivityManager.TYPE_WIFI) {
					// Broadcast
					Log.w(Constants.TAG, "if everything bad, try BROADCAST address");
						UDPHelper.checkSoulissUdp(getRemoteTimeoutPref() * 3, SoulissPreferenceHelper.this, it.angelic.soulissclient.net.Constants.BROADCASTADDR);
				}
			}
		}.start();
		return;

	}

	public SharedPreferences getCustomPref() {
		return customSharedPreference;
	}

	public String getIPPreferencePublic() {
		return IPPreferencePublic;
	}

	public Float getListDimensTesto() {
		return ListDimensTesto;
	}

	public String getPrefFont() {
		return PrefFont;
	}

	public String getPrefIPAddress() {
		return IPPreference;
	}

	public boolean getTextFx() {
		return fxOn;
	}

	public boolean isLightThemeSelected() {
		return lightTheme;
	}

	public boolean isAnimationsEnabled() {
		return animations;
	}

	/**
	 * Ritorna se il DB � configurato. Basato sulla presenza o meno in
	 * sharedPrefs del numero di nodi su DB
	 * 
	 * @return true se DB popolato
	 */
	public boolean isDbConfigured() {
		if ((customSharedPreference.contains("numNodi")) && customSharedPreference.getInt("numNodi", 0) != 0)
			return true;
		return false;
	}

	/**
	 * legge cached address per vedere se connesso
	 * 
	 * @return Unavailable quando la rete c'�, ma souliss non risponde
	 */
	public boolean isSoulissReachable() {
		// getCachedAddress();
		if (cachedAddr == null || cachedAddr.compareTo("") == 0
				|| cachedAddr.compareTo(contx.getString(R.string.unavailable)) == 0)
			return false;

		return true;
	}

	public boolean isSoulissIpConfigured() {
		// check se IP non settato
		if (getPrefIPAddress() == null || "".compareTo(getPrefIPAddress()) == 0) {
			return false;
		}
		return true;
	}

	public boolean isSoulissPublicIpConfigured() {
		if (getIPPreferencePublic() == null || "".compareTo(getIPPreferencePublic()) == 0) {
			return false;
		}
		return true;
	}

	public void setFont(TextView in) {
		Typeface font = Typeface.createFromAsset(contx.getAssets(), this.getPrefFont());
		in.setTypeface(font);
	}

	public void setIPPreferencePublic(String iPPreferencePublic) {
		IPPreferencePublic = iPPreferencePublic;
	}

	public void setPrefFont(String iPPreference) {
		PrefFont = iPPreference;
	}

	public void setIPPreference(String newIP) {// serve anche commit perche`
												// chiamata fuori da prefs
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putString("edittext_IP", newIP);
		pesta.commit();
		this.IPPreference = newIP;
	}

	public void setTextFx(boolean nfx) {
		fxOn = nfx;
	}

	public int getRemoteTimeoutPref() {
		return remoteTimeoutPref;
	}

	public void setRemoteTimeoutPref(int remoteTimeoutPref) {
		this.remoteTimeoutPref = remoteTimeoutPref;
	}

	public int getDataServiceIntervalMsec() {
		return dataServiceInterval * 60;
	}

	public int getHomeThresholdDistance() {
		return homeThold;
	}

	public boolean isDataServiceEnabled() {
		return dataServiceEnabled;
	}
	
	public boolean isRgbSendAllDefault() {
		return rgbSendAllDefault;
	}

	public void setRgbSendAllDefault(boolean rgbSendAllDefault) {
		this.rgbSendAllDefault = rgbSendAllDefault;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putBoolean("rgbSendAllDefault", rgbSendAllDefault);
		pesta.commit();
	}

	public void setDontShowAgain(String string, boolean val) {
		Editor pesta = customSharedPreference.edit();
		pesta.putBoolean("dontshow" + string, val);
		pesta.commit();
	}

	public boolean getDontShowAgain(String string) {
		return customSharedPreference.getBoolean("dontshow" + string, false);
	}

	public void setHomeLatitude(double lat) {
		Editor pesta = customSharedPreference.edit();
		pesta.putString("homelatitude", String.valueOf(lat));
		pesta.commit();
	}

	public double getHomeLatitude() {
		return Double.parseDouble(customSharedPreference.getString("homelatitude", "0"));
	}

	public void setHomeLongitude(double lat) {
		Editor pesta = customSharedPreference.edit();
		pesta.putString("homelongitude", String.valueOf(lat));
		pesta.commit();
	}

	public double getHomeLongitude() {
		double ret = Double.parseDouble(customSharedPreference.getString("homelongitude", "0"));
		return ret;
	}

	public void setCachedAddr(String cachedAd) {
		cachedAddr = cachedAd;
	}

	public Context getContx() {
		return contx;
	}

	public int getUserIndex() {
		return userIndex;
	}

	public Long getBackedOffServiceInterval() {
		return (long) (getDataServiceIntervalMsec() * backoff);
	}

	public void increaseBackoffTimeout() {
		backoff++;
	}

	public void resetBackOff() {
		backoff = 1;
	}

	public int getBackoff() {
		return backoff;
	}

	public void setUserIndex(int userIndex) {
		this.userIndex = userIndex;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putInt("userIndex", userIndex);
		pesta.commit();

	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putInt("nodeIndex", nodeIndex);
		pesta.commit();
	}

	public boolean isAntitheftPresent() {
		// TODO Auto-generated method stub
		return antitheftPresent;
	}

	public boolean isAntitheftNotify() {
		// TODO Auto-generated method stub
		return antitheftNotify;
	}

	public void setAntitheftPresent(boolean antitheftPresent) {
		this.antitheftPresent = antitheftPresent;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putBoolean("antitheft", antitheftPresent);
		pesta.commit();
	}

	public void setAntitheftNotify(boolean antith) {
		antitheftNotify = antith;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putBoolean("antitheftNotify", antitheftNotify);
		pesta.commit();

	}

	public void setLastServiceRun(Calendar lastupd) {
		serviceLastrun = lastupd.getTimeInMillis();
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putLong("serviceLastrun", serviceLastrun);
		pesta.commit();
	}

	public long getServiceLastrun() {
		return serviceLastrun;
	}

	public void setNextServiceRun(long schedTime) {
		nextServiceRun = schedTime;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putLong("nextServiceRun", nextServiceRun);
		pesta.commit();

	}

	public long getNextServiceRun() {
		return nextServiceRun;
	}

	public float getEqLow() {
		return eqLow;
	}

	public void setEqLow(float eqLow) {
		this.eqLow = eqLow;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putFloat("eqLow", eqLow);
		pesta.commit();
	}

	public float getEqMed() {
		return eqMed;
	}

	public void setEqMed(float eqMed) {
		this.eqMed = eqMed;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putFloat("eqMed", eqMed);
		pesta.commit();
	}

	public float getEqHigh() {
		return eqHigh;
	}

	public void setEqHigh(float eqHigh) {
		this.eqHigh = eqHigh;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putFloat("eqHigh", eqHigh);
		pesta.commit();
	}


	public boolean isWebserverEnabled() {
		return webserverEnabled;
	}

	public void setWebserverEnabled(boolean webserverEnabled) {
		this.webserverEnabled = webserverEnabled;
	}
	
	public boolean isBroadCastEnabled() {
		return broadCastEnabled;
	}

	public void setHtmlRoot(String mChosenFile) {
		chosenHtmlRootfile = mChosenFile;
		Editor pesta = PreferenceManager.getDefaultSharedPreferences(contx).edit();
		pesta.putString("mChosenFile", mChosenFile);
		pesta.commit();
		
	}

	public String getChosenHtmlRootfile() {
		return chosenHtmlRootfile;
	}
}
