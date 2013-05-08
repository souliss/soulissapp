package it.angelic.receivers;

import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LifeCheckerSetupReceiver extends BroadcastReceiver {
	private static final String APP_TAG = "Souliss:LifeCheckerSetup";

	SoulissPreferenceHelper opts = SoulissClient.getOpzioni();
	//private static final int EXEC_INTERVAL = 200 * 1000;


	@Override
	public void onReceive(final Context ctx, final Intent intent) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean dataServiceEnabled = opts.isDataServiceEnabled();
		
		if (dataServiceEnabled) {
			Log.d(APP_TAG, "LifeCheckerSetupReceiver.onReceive() called. Checking every msec."+opts.getBackedOffServiceInterval());
			AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(ctx, LifeCheckerEventReceiver.class); // explicit
																		// intent
			PendingIntent intentExecuted = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			Calendar now = Calendar.getInstance();
			now.add(Calendar.SECOND, 20);

			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), opts.getBackedOffServiceInterval(),
					intentExecuted);
		}
	}

}
