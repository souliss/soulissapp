package it.angelic.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * Receive per controllo esecuzione servizio. Viene invocato dopo il boot, e
 * all'USER_PRESENT
 * http://www.hascode.com/2011/11/managing-background-tasks-on-android
 * -using-the-alarm-manager/
 */
@Deprecated
public class WatchDogEventReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(final Context ctx, final Intent intent) {
		Log.d(Constants.TAG + ":WatchDog", "WatchDog.onReceive() called, looking for Souliss Dataservice");
		SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		if (opzioni.isDataServiceEnabled()) {
			Intent eventService = new Intent(ctx, SoulissDataService.class);
            ctx.startService(eventService);//sempre, ci pensa poi lui
		}

	}

	/*
	private boolean isMyServiceRunning(final Context ctx, ActivityManager manager) {
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (SoulissDataService.class.getName().equals(service.service.getClassName())) {
                Log.d(Constants.TAG + ":WatchDog", "Service already running since:"+ new Date(service.activeSince).toString());
                return true;
			}

		}
		return false;
	}*/


}