package it.angelic.soulissclient;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * Contenitore metodi globali per reperimento contesto
 * e dimensioni finestra
 *
 * @author pegoraro
 */
public class SoulissApp extends Application implements Serializable {

    private static final long serialVersionUID = 962480567399715745L;
    private static volatile Context context;
    private static DisplayMetrics metrics = new DisplayMetrics();
    private static float displayWidth;
    private static float displayHeight;
    private static SoulissPreferenceHelper opzioni;
    private static SharedPreferences soulissConfigurationPreference;

    public static Context getAppContext() {
        return SoulissApp.context;
    }

    public static float getDisplayHeight() {
        return displayHeight;
    }

    public static float getDisplayWidth() {
        return displayWidth;
    }

    /**
     * Setta la dimensione del background Drawable
     * gli errori vengono ignorati e sparati in warn
     *
     * @param target
     * @param mgr
     */
    public static void setBackground(View target, WindowManager mgr) {
        try {
            mgr.getDefaultDisplay().getMetrics(metrics);
            //StateListDrawable gras = (StateListDrawable) target.getRootView().getBackground();
            GradientDrawable gra = (GradientDrawable) target.getRootView().getBackground();
            gra.setGradientRadius(metrics.widthPixels / 2f);
            //displayWidth = metrics.widthPixels;
            //displayHeight = metrics.heightPixels;
            gra.setDither(true);
        } catch (Exception e) {
            Log.w(Constants.TAG, "Couldn't set background:" + e.getMessage());
        }
    }

    public static SoulissPreferenceHelper getOpzioni() {
        return opzioni;
    }

    /*public static InetAddress getBroadcastAddress() throws UnknownHostException {
        WifiManager wifi = (WifiManager) SoulissClient.context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
          quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress("255.255.255.255");
    }*/
    private static void setOpzioni(SoulissPreferenceHelper opzioni) {
        SoulissApp.opzioni = opzioni;
    }

    /*private static void setCtx(Context ct){
        SoulissClient.context = ct;
	}*/
    public static boolean isWelcomeDisabled() {
        return soulissConfigurationPreference.getBoolean("welcome_disabled", false);
    }

    public static void saveWelcomeDisabledPreference(boolean val) {
        SharedPreferences.Editor edit = soulissConfigurationPreference.edit();
        edit.putBoolean("welcome_disabled", val).apply();
    }

    public static String getCurrentConfig() {
        return soulissConfigurationPreference.getString("current_config", "");
    }

    public static void setCurrentConfig(String newConfig) {
        soulissConfigurationPreference.edit().putString("current_config", newConfig).apply();
    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        setOpzioni(new SoulissPreferenceHelper(getApplicationContext()));

        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        displayWidth = displayMetrics.heightPixels / displayMetrics.density;
        displayHeight = displayMetrics.widthPixels / displayMetrics.density;
        soulissConfigurationPreference = getSharedPreferences("SoulissConfigPrefs", Activity.MODE_PRIVATE);
    }

    public static void addConfiguration(String newConfig) {
        SharedPreferences.Editor editor = soulissConfigurationPreference.edit();
        Set<String> current = getConfigurations();
        current.add(newConfig);
        editor.putStringSet(Constants.SOULISS_CONFIGURATIONS_KEY, current);
        editor.apply();
    }

    public static void deleteConfiguration(String newConfig) {
        SharedPreferences.Editor editor = soulissConfigurationPreference.edit();
        Set<String> current = getConfigurations();
        if (current.contains(newConfig))
            current.remove(newConfig);
        editor.putStringSet(Constants.SOULISS_CONFIGURATIONS_KEY, current);
        editor.apply();
    }

    public static Set<String> getConfigurations() {
        return soulissConfigurationPreference.getStringSet(Constants.SOULISS_CONFIGURATIONS_KEY, new HashSet<String>());
    }



}
