package it.angelic.soulissclient;

import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import java.io.Serializable;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
/**
 * Contenitore metodi globali per reperimento contesto
 * e dimensioni finestra
 * 
 * @author pegoraro
 *
 */
public class SoulissClient extends Application implements Serializable{

	private static final long serialVersionUID = 962480567399715745L;
	private static volatile Context context;
	private static DisplayMetrics metrics = new DisplayMetrics();
	private static int displayWidth;
	private static int displayHeight;
	private static SoulissPreferenceHelper opzioni;
	
	public static Context getAppContext() {
        return SoulissClient.context;
    }
    public static int getDisplayHeight() {
		return displayHeight;
	}

    public static int getDisplayWidth() {
		return displayWidth;
	}
    /**
     * Setta la dimensione del background Drawable
     * gli errori vengono ignorati e sparati in warn
     * @param target
     * @param mgr
     */
    public static void setBackground(View target, WindowManager mgr){
    	try {
			mgr.getDefaultDisplay().getMetrics(metrics);

			StateListDrawable gras = (StateListDrawable) target.getRootView().getBackground();
			GradientDrawable gra = (GradientDrawable) target.getRootView().getBackground();
			gra.setGradientRadius(metrics.widthPixels / 2f);
			displayWidth = metrics.widthPixels;
			displayHeight = metrics.heightPixels;
			gra.setDither(true);
		} catch (Exception e) {
			// fa nulla, solo chincaglierie
			e.printStackTrace();
		}
    }
    public void onCreate(){
        super.onCreate();
        if (SoulissClient.context == null)
        	SoulissClient.context = getApplicationContext();
		setOpzioni(new SoulissPreferenceHelper(SoulissClient.context));
    }
	public static SoulissPreferenceHelper getOpzioni() {
		return opzioni;
	}
	private static void setOpzioni(SoulissPreferenceHelper opzioni) {
		SoulissClient.opzioni = opzioni;
	}
	/*private static void setCtx(Context ct){
		SoulissClient.context = ct;
	}*/
}
