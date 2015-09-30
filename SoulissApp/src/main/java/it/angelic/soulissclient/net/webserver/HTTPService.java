package it.angelic.soulissclient.net.webserver;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import it.angelic.soulissclient.Constants;

public class HTTPService extends Service {

	private Zozzariello server = null;
	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();

		server = new Zozzariello(this);
		startWebServer();
	}

	@Override
	public void onDestroy() {
		server.stopThread();
		Log.i(Constants.TAG, "webserver onDestroy()");
		super.onDestroy();
	}

	private void startWebServer() {
		if (server != null && !server.isAlive()) {
			server.setPriority(Thread.MIN_PRIORITY + 1);
			server.startThread();
			Log.i(Constants.TAG, "webserver started");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		startWebServer();

		Log.i(Constants.TAG, "Service onStartCommand()");
		//Toast.makeText(server.getContext(), "Zozzariello ON", Toast.LENGTH_SHORT).show();
		

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

	

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public HTTPService getService() {
			return HTTPService.this;
		}
	}

	public String getPort() {
		if (server != null)
			return "" + server.getServerPort();
		else
			return "init..";
	}

}
