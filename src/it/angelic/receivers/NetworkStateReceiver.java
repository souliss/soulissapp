package it.angelic.receivers;

import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
	private static final String TAG = "Souliss:Network Monitor";
	private SoulissPreferenceHelper opzioni;

	
	
	public void onReceive(Context context, Intent intent) {
		// super.onReceive(context, intent);
		// final SharedPreferences customSharedPreference = context
		// .getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
		if (opzioni == null)
			opzioni = SoulissClient.getOpzioni();
		//solo if UDP?
		opzioni.clearCachedAddress();
		SharedPreferences.Editor editor = opzioni.getCustomPref().edit();
		Log.d(TAG, "Network connectivity change");
		if (intent.getExtras() != null) {
			NetworkInfo ni = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	        //NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			storeNetworkInfo(ni, opzioni);
			Log.i(TAG, "Calling setBestAddress to check connectivity");
			opzioni.setBestAddress();
		}
		if (intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
			Log.d(TAG, "There's no network connectivity");
			if (opzioni.getCustomPref().contains("connection"))
				editor.remove("connection");
			if (opzioni.getCustomPref().contains("connectionName"))
				editor.remove("connectionName");
			// editor.putInt("connection",Constants.CONNECTION_NONE);
			editor.commit();
		}
	}

	/**
	 * Mette nei SharedPreferences dell'app i valori relativi al TIPO di
	 * connessione
	 * 
	 * @param ni
	 * @param opz 
	 * @param soulissOpts
	 */
	public static void storeNetworkInfo(NetworkInfo ni, SoulissPreferenceHelper opz) {
		SharedPreferences.Editor editor = opz.getCustomPref().edit();
		if (ni==null || !ni.isConnected()) {
			editor.remove("connection");
			editor.remove("connectionName");
			Log.w(TAG, "No connection");
		}
		else {
			Log.i(TAG, "Network " + ni.getTypeName() + " connected");

			// sistema configurato
			if (opz.getCustomPref().contains("connection"))
				editor.remove("connection");
			if (opz.getCustomPref().contains("connectionName"))
				editor.remove("connectionName");
			editor.putInt("connection", ni.getType());
			editor.putString("connectionName", ni.getTypeName());
			
		} 
		editor.commit();
	}
}
