package it.angelic.soulissclient.net.webserver;

import it.angelic.soulissclient.Constants;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class HTTPService extends Service {

	private WebServer server = null;
	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();

		server = new WebServer(this);
		startWebServer();
	}

	@Override
	public void onDestroy() {
		server.stopThread();

		super.onDestroy();
	}

	private void startWebServer() {
    		if (server == null || !server.isAlive()) {
    			
    			server.setPriority(Thread.MIN_PRIORITY+1);
    			
    			server.startThread();
    			Log.i(Constants.TAG, "webserver started");
    		}
    	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		startWebServer();

		showNotification();

		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void showNotification() {
		Log.i(Constants.TAG, "TOAST TO DO");
		/*
		 * String text = getString(R.string.service_started); Notification
		 * notification = new Notification(R.drawable.notificationicon, text,
		 * System.currentTimeMillis());
		 * 
		 * Intent startIntent = new Intent(this,SoulissClient.class);
		 * 
		 * startIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		 * 
		 * PendingIntent intent = PendingIntent.getActivity(this, 0,
		 * startIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		 * 
		 * notification.flags |= Notification.FLAG_ONGOING_EVENT |
		 * Notification.FLAG_NO_CLEAR;
		 * 
		 * notification.setLatestEventInfo(this,
		 * getString(R.string.notification_started_title),
		 * getString(R.string.notification_started_text), intent);
		 * 
		 * 
		 * notifyManager.notify(NOTIFICATION_STARTED_ID, notification);
		 */
	}
	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public HTTPService getService() {
			return HTTPService.this;
		}
	}
}
