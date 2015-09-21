package it.angelic.receivers;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WatchDogSetupReceiver extends BroadcastReceiver {

	SoulissPreferenceHelper opts = SoulissApp.getOpzioni();
	//private static final int EXEC_INTERVAL = 200 * 1000;


	@Override
	public void onReceive(final Context ctx, final Intent intent) {
		//controlliamo al doppio della frequenza servizio
		boolean dataServiceEnabled = opts.isDataServiceEnabled();
		
		if (dataServiceEnabled) {
			Log.d(Constants.TAG+":WDSetup", "LifeCheckerSetupReceiver.onReceive() called. Checking every msec."+opts.getBackedOffServiceIntervalMsec()/2);
			AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(ctx, WatchDogEventReceiver.class); // explicit
																		// intent
			PendingIntent patTheDog = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			Calendar now = Calendar.getInstance();
			now.add(Calendar.SECOND, 20);

			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), opts.getBackedOffServiceIntervalMsec()/2,
					patTheDog);
		}
	}

}
