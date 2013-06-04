package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.net.UDPRunnable;
import it.angelic.soulissclient.typicals.SoulissTypical;
import it.angelic.soulissclient.typicals.SoulissTypical54LuxSensor;
import it.angelic.soulissclient.typicals.SoulissTypicalHumiditySensor;
import it.angelic.soulissclient.typicals.SoulissTypicalTemperatureSensor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SoulissDataService extends Service implements LocationListener {
	// LOGGA a parte
	private static final String TAG = "SoulissDataService";
	
	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();
	private LocationManager locationManager;
	private Calendar lastupd = Calendar.getInstance();
	private Handler mHandler = new Handler();

	// private Timer timer = new Timer();
	private SoulissPreferenceHelper opts;
	// private long uir;
	private SoulissDBHelper db;
	private String provider;
	private float homeDist;
	NotificationManager nm;
	private String cached;
	private Thread udpThread;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		opts = SoulissClient.getOpzioni();
		// toDoDBAdapter = new ToDoDBAdapter(getApplicationContext());
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		crit.setPowerRequirement(Criteria.POWER_LOW);
		provider = locationManager.getBestProvider(crit, false);
		db = new SoulissDBHelper(this);
		// db.open();
		locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL,
				Constants.POSITION_UPDATE_MIN_DIST, SoulissDataService.this);

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Create the object with the run() method
		Runnable runnable = new UDPRunnable(opts, SoulissDataService.this);
		// Create the udpThread supplying it with the runnable
		// object
		udpThread = new Thread(runnable);
		// Start the udpThread
		udpThread.start();
		Log.i(TAG, "UDP thread started" + opts.getBackedOffServiceInterval());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// timer.cancel();
		mHandler.removeCallbacks(mUpdateSoulissRunnable);
		db.close();
		locationManager.removeUpdates(SoulissDataService.this);
		if (udpThread != null && udpThread.isAlive())
			udpThread.interrupt();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		opts = SoulissClient.getOpzioni();
		// opts.getCachedAddress();

		// uir = opts.getDataServiceInterval();
		Log.i(TAG, "Service StartCommand, Scheduling every " + opts.getBackedOffServiceInterval());
		// delle opzioni

		reschedule();

		return START_STICKY;
	}

	/**
	 * Schedule a new execution of the srvice via mHandler.postDelayed
	 */
	public void reschedule() {

		opts.initializePrefs();//reload interval
		mHandler.removeCallbacks(mUpdateSoulissRunnable);
		// reschedule self
		Log.i(TAG, "Regular mode, rescheduling self every " + opts.getBackedOffServiceInterval() / 1000 + " seconds");
		mHandler.postDelayed(mUpdateSoulissRunnable, opts.getDataServiceIntervalMsec());

		if (udpThread == null || !udpThread.isAlive()) {
			// Create the object with the run() method
			Runnable runnable = new UDPRunnable(opts, SoulissDataService.this);
			// Create the udpThread supplying it with the runnable
			// object
			udpThread = new Thread(runnable);
			// Start the udpThread
			udpThread.start();
			Log.i(TAG, "UDP thread started" + opts.getBackedOffServiceInterval());
		}
	}

	public Calendar getLastupd() {
		return lastupd;
	}

	public void setLastupd(Calendar lastupd) {
		this.lastupd = lastupd;
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public SoulissDataService getService() {
			return SoulissDataService.this;
		}
	}

	/*
	 * @Override public void onLowMemory() { // TODO Auto-generated method stub,
	 * o quasi super.onLowMemory(); Log.w(TAG,
	 * "Low memory, schedule a reserve task");
	 * mHandler.postDelayed(mUpdateSoulissRunnable,
	 * opts.getDataServiceInterval() * 10000); }
	 */

	private Runnable mUpdateSoulissRunnable = new Runnable() {

		public void run() {
			// locationManager.requestSingleUpdate(provider, null);
			Log.v(TAG,
					"Service run " + SoulissDataService.this.hashCode() + " backedoffInterval="
							+ opts.getBackedOffServiceInterval());
			opts = SoulissClient.getOpzioni();
			if (!opts.isDbConfigured()) {
				Log.w(TAG, "Database empty, closing service");
				// mHandler.removeCallbacks(mUpdateSoulissRunnable);
				reschedule();
				// SoulissDataService.this.stopSelf();
				return;
			}
			if (!opts.getCustomPref().contains("numNodi")) {
				Log.w(TAG, "Souliss didn't answer yet, rescheduling");
				// mHandler.removeCallbacks(mUpdateSoulissRunnable);
				reschedule();
				return;
			}

			/*
			 * if (uir != opts.getDataServiceInterval()) { uir =
			 * opts.getDataServiceInterval(); Log.w(TAG, "pace changed: " +
			 * opts.getDataServiceInterval()); }
			 */
			cached = opts.getAndSetCachedAddress();

			final byte nodesNum = (byte) opts.getCustomPref().getInt("numNodi", 0);

			if (opts.getCustomPref().contains("connection") && cached != null) {
				if (cached.compareTo("") == 0
						|| cached.compareTo(SoulissDataService.this.getResources().getString(R.string.unavailable)) == 0) {
					Log.e(TAG, "Souliss Unavailable, rescheduling");
					reschedule();
					// SoulissDataService.this.stopSelf();
					return;
				}
				// db.open();
				float homeDistPrev = opts.getPrevDistance();
				Log.i(TAG, "Previous distance " + homeDistPrev + " current " + homeDist);
				// PROGRAMMI POSIZIONALI /
				if (homeDist != homeDistPrev) {
					processPositionalPrograms(homeDistPrev);
				}

				// Timed commands
				new Thread(new Runnable() {
					@Override
					public void run() {
						db.open();
						LinkedList<SoulissCommand> unexecuted = db.getUnexecutedCommands(SoulissDataService.this);
						Log.i(TAG, "checked unexecuted commands: " + unexecuted.size());
						for (SoulissCommand unexnex : unexecuted) {
							Calendar now = Calendar.getInstance();
							if (unexnex.getType() == Constants.COMMAND_TIMED
									&& now.after(unexnex.getCommandDTO().getScheduledTime())) {
								// esegui comando
								Log.w(TAG, "issuing command: " + unexnex.toString());
								UDPHelper.issueSoulissCommand(unexnex, opts);
								unexnex.getCommandDTO().setExecutedTime(now);
								unexnex.getCommandDTO().persistCommand(db);
								// Se ricorsivo, ricrea
								if (unexnex.getCommandDTO().getInterval() > 0) {
									
									SoulissCommand nc = new SoulissCommand(SoulissDataService.this,
											unexnex.getParentTypical());
									nc.getCommandDTO().setNodeId(unexnex.getCommandDTO().getNodeId());
									nc.getCommandDTO().setSlot(unexnex.getCommandDTO().getSlot());
									nc.getCommandDTO().setCommand(unexnex.getCommandDTO().getCommand());
									nc.getCommandDTO().setInterval(unexnex.getCommandDTO().getInterval());
									Calendar cop = Calendar.getInstance();
									cop.add(Calendar.SECOND, unexnex.getCommandDTO().getInterval());
									nc.getCommandDTO().setScheduledTime(cop);
									nc.getCommandDTO().persistCommand(db);
									Log.w(TAG, "recreate recursive command");
								}
								sendNotification(SoulissDataService.this, "Souliss Timed Program Executed",
										unexnex.toString() + " " + unexnex.getParentTypical().toString(),
										R.drawable.clock);
							}
						}
						// db.close();
					};
				}).start();

				/* SENSORS REFRESH THREAD */
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							db.open();

							Map<Short, SoulissNode> refreshedNodes = new HashMap<Short, SoulissNode>();

							List<SoulissNode> ref = db.getAllNodes();

							for (SoulissNode soulissNode : ref) {
								refreshedNodes.put(soulissNode.getId(), soulissNode);
								List<SoulissTypical> tips = soulissNode.getTypicals();
								for (SoulissTypical tp : tips) {
									if (tp.isSensor()) {
										Log.i(TAG, "Issuing sensor refresh..");
										if (tp instanceof SoulissTypicalTemperatureSensor) {
											UDPHelper.issueSoulissCommand(
													"" + soulissNode.getId(),
													"" + tp.getTypicalDTO().getSlot(),
													opts,
													Constants.COMMAND_SINGLE,
													""
															+ it.angelic.soulissclient.typicals.Constants.Souliss_T_TemperatureSensor_refresh);
											break;// basta uno per nodo
										} else if (tp instanceof SoulissTypicalHumiditySensor) {
											UDPHelper.issueSoulissCommand(
													"" + soulissNode.getId(),
													"" + tp.getTypicalDTO().getSlot(),
													opts,
													Constants.COMMAND_SINGLE,
													""
															+ it.angelic.soulissclient.typicals.Constants.Souliss_T_HumiditySensor_refresh);
											break;// basta uno per nodo
										}else if (tp instanceof SoulissTypical54LuxSensor) {
											UDPHelper.issueSoulissCommand(
													"" + soulissNode.getId(),
													"" + tp.getTypicalDTO().getSlot(),
													opts,
													Constants.COMMAND_SINGLE,
													""+ it.angelic.soulissclient.typicals.Constants.Souliss_T_HumiditySensor_refresh);
											break;// basta uno per nodo
										} else {
											Log.e(TAG, "Uninplemented..");
										}
									}
								}
							}
							logThings(refreshedNodes);

							// try a local reach, just in case ..
							UDPHelper.checkSoulissUdp(2000, opts, opts.getPrefIPAddress());

						} catch (Exception e) {
							Log.e(TAG, "Service error, scheduling again ", e);
						} /*finally {
							db.close();
						}*/

						// spostato per consentire comandi manuali
						if (!opts.isDataServiceEnabled()) {
							Log.w(TAG, "Service disabled, is not going to be re-scheduled");
							mHandler.removeCallbacks(mUpdateSoulissRunnable);
							// SoulissDataService.this.stopSelf();
							return;
						} else {
							// refresh della subscription
							new Thread(new Runnable() {
								@Override
								public void run() {
									Log.v(TAG, "issuing subscribe, numnodes=" + nodesNum);
									UDPHelper.stateRequest(opts, nodesNum, 0);
								}
							}).start();
							Log.v(TAG, "Service end run " + SoulissDataService.this.hashCode());
							setLastupd(Calendar.getInstance());
							reschedule();
						}

					}

				}).start();

			} else {// NO CONNECTION, NO NODES!!
				Intent i = new Intent();
				i.setAction(Constants.CUSTOM_INTENT);
				setLastupd(Calendar.getInstance());
				getApplicationContext().sendBroadcast(i);
				reschedule();
			}
		}

		private void processPositionalPrograms(float homeDistPrev) {
			db.open();
			LinkedList<SoulissCommand> unexecuted = db.getPositionalPrograms(SoulissDataService.this);
			Log.i(TAG, "active positional programs: " + unexecuted.size());
			if (homeDistPrev > Constants.POSITION_AWAY_THRESHOLD && homeDist < Constants.POSITION_AWAY_THRESHOLD) {
				// tornato a casa
				for (SoulissCommand soulissCommand : unexecuted) {
					final SoulissCommand cmd = soulissCommand;
					if (soulissCommand.getType() == Constants.COMMAND_COMEBACK_CODE) {
						Log.w(TAG, "issuing COMEBACK command: " + soulissCommand.toString());
						new Thread(new Runnable() {
							@Override
							public void run() {
								UDPHelper.issueSoulissCommand(cmd, opts);
								Calendar cop = Calendar.getInstance();
								cmd.getCommandDTO().setExecutedTime(cop);
								cmd.getCommandDTO().setSceneId(null);
								cmd.getCommandDTO().persistCommand(db);
								sendNotification(SoulissDataService.this, "Souliss Positional Program Executed",
										cmd.toString() + " " + cmd.getParentTypical().toString(), R.drawable.exit);
							}
						}).start();
					}
				}
				opts.setPrevDistance(homeDist);
			} else if (homeDistPrev < Constants.POSITION_AWAY_THRESHOLD && homeDist > Constants.POSITION_AWAY_THRESHOLD) {
				// uscito di casa
				for (SoulissCommand soulissCommand : unexecuted) {
					final SoulissCommand cmd = soulissCommand;
					if (soulissCommand.getType() == Constants.COMMAND_GOAWAY_CODE) {
						Log.w(TAG, "issuing AWAY command: " + soulissCommand.toString());
						new Thread(new Runnable() {
							@Override
							public void run() {
								UDPHelper.issueSoulissCommand(cmd, opts);
								Calendar cop = Calendar.getInstance();
								cmd.getCommandDTO().setExecutedTime(cop);
								cmd.getCommandDTO().setSceneId(null);
								cmd.getCommandDTO().persistCommand(db);
								sendNotification(SoulissDataService.this, "Souliss Positional Program Executed",
										cmd.toString() + " " + cmd.getParentTypical().toString(), R.drawable.exit);
							}
						}).start();
					}
				}
				opts.setPrevDistance(homeDist);
			} else {// gestione BACKOFF
				Log.w(TAG, "resetting backoff at meters " + homeDist);
				locationManager.removeUpdates(SoulissDataService.this);
				if (homeDist > 25000) {
					locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL * 100,
							Constants.POSITION_UPDATE_MIN_DIST * 100, SoulissDataService.this);
				} else if (homeDist > 5000) {
					locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL * 10,
							Constants.POSITION_UPDATE_MIN_DIST * 10, SoulissDataService.this);
				} else {
					locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL,
							Constants.POSITION_UPDATE_MIN_DIST, SoulissDataService.this);
				}
			}
			//db.close();
		}
	};

	private void logThings(Map<Short, SoulissNode> refreshedNodes) {
		Log.i(Constants.TAG, "logging sensors for " + refreshedNodes.size() + " nodes");
		for (Short index : refreshedNodes.keySet()) {
			SoulissNode pirt = refreshedNodes.get(index);
			List<SoulissTypical> tips = pirt.getTypicals();
			for (SoulissTypical soulissTypical : tips) {
				if (soulissTypical.isSensor()) {
					db.logTypical(soulissTypical);
				}
			}
		}
	}

	public static void sendNotification(Context ctx, String desc, String longdesc, int icon) {

		Intent notificationIntent = new Intent(ctx, ProgramListActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
		NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		Resources res = ctx.getResources();

		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

		builder.setContentIntent(contentIntent).setSmallIcon(android.R.drawable.stat_sys_upload_done)
				.setLargeIcon(BitmapFactory.decodeResource(res, icon)).setTicker("Souliss program activated")
				.setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(desc).setContentText(longdesc);
		Notification n = builder.build();
		nm.notify(665, n);
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, "Service received Provider Disabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i(TAG, "Service received Provider ENABLED");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "Service location status Provider changed to: " + status);
	}

	@Override
	public void onLocationChanged(Location location) {
		opts = SoulissClient.getOpzioni();
		double lat = (location.getLatitude());
		double lng = (location.getLongitude());

		float[] res = new float[3];
		try {
			Location.distanceBetween(lat, lng, opts.getHomeLatitude(), opts.getHomeLongitude(), res);
			Log.i(TAG, "Service received new Position. Home Distance:" + (int) res[0]);
			homeDist = res[0];
			if (opts.getPrevDistance() == 0) {
				Log.w(TAG, "Resetting prevdistance =>" + homeDist);
				opts.setPrevDistance(homeDist);
			}
		} catch (Exception e) {// home note set
			homeDist = 0;
			Log.w(TAG, "can't compute home distance, home position not set");
		}

	}
}
