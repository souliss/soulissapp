package it.angelic.soulissclient.helpers;

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
	private final String TAG = "SoulissApp:" + getClass().getSimpleName();
	// private InetAddress cachedInet;
	private int userIndex;
	private int backoff = 1;
	private boolean animations;

	public SoulissPreferenceHelper(Context contx) {
		super();
		this.contx = contx;
		customSharedPreference = contx.getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
		initializePrefs();
		// Log.d(TAG, "Constructing prefs");
	}

	public void reload() {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contx);
		initializePrefs();
		if (userIndex == -1) {//MAI inizializzato, lo calcolo
			/* PHONE ID */
			try {
				final TelephonyManager tm = (TelephonyManager) contx.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm.getDeviceId() != null)
					userIndex = (int) (Long.parseLong(tm.getDeviceId()) % 127);
				else
					userIndex = ((Secure.getString(contx.getContentResolver(), Secure.ANDROID_ID)).hashCode() % 127);
				userIndex = Math.abs(userIndex);
				Log.w(TAG, "Pref init END. User index hash = " + userIndex);
				setUserIndex(userIndex);
			} catch (Exception e) {//fallito il computo, uso random e lo salvo
				Random r = new Random(Calendar.getInstance().getTimeInMillis());
				int casual = r.nextInt(127);
				setUserIndex(casual);
				Log.e(Constants.TAG, "automated user-index fail " + e.getMessage()+". Using "+casual);
			}
		}
		// reset cachedAddress to shared prefs one
		cachedAddr = null;
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
		dataServiceEnabled = prefs.getBoolean("checkboxService", false);
		userIndex = prefs.getInt("userIndex", -1);
		animations = prefs.getBoolean("checkboxAnimazione", true);
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
	 * return currently used Souliss address may return null
	 * and then re-initialize it
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
	public boolean isAnimationsEnabled(){
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

	public void setIPPreference(String fontPreference) {
		this.IPPreference = fontPreference;
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

	public boolean isDataServiceEnabled() {
		return dataServiceEnabled;
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
}
