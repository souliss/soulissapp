package it.angelic.soulissclient;

import static it.angelic.soulissclient.Constants.TAG;
import it.angelic.receivers.NetworkStateReceiver;
import it.angelic.soulissclient.helpers.Eula;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
/**
 * SoulissApp main screen
 * @author Ale
 *
 */
public class LauncherActivity extends Activity implements LocationListener {

	private LocationManager locationManager;
	private String provider;
	ConnectivityManager mConnectivity;
	TelephonyManager mTelephony;
	private TextView coordinfo;
	private TextView homedist;
	private TextView basinfo;
	private TextView dbwarn;
	private View dbwarnline;
	private View posInfoLine;
	private TextView serviceInfoFoot;
	private TextView serviceInfo;
	private View serviceinfoLine;
	private View basinfoLine;
	private Handler timeoutHandler;
	private Button soulissSceneBtn;
	private Button soulissManualBtn;
	private Button programsActivity;

	protected PendingIntent netListenerPendingIntent;
	private SoulissPreferenceHelper opzioni;

	private SoulissDataService mBoundService;
	private boolean mIsBound;
	private Timer autoUpdate;
	private Geocoder geocoder;

	/* SOULISS DATA SERVICE BINDING */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((SoulissDataService.LocalBinder) service).getService();
			//Toast.makeText(LauncherActivity.this, "Dataservice connected, scheduling Souliss Update",
			//		Toast.LENGTH_SHORT).show();
			mBoundService.reschedule();
			mIsBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
			Toast.makeText(LauncherActivity.this, "Dataservice disconnected", Toast.LENGTH_SHORT).show();
			mIsBound = false;
		}
	};

	void doBindService() {
		bindService(new Intent(LauncherActivity.this, SoulissDataService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		Eula.show(this);
		opzioni.clearCachedAddress();
		setContentView(R.layout.main_launcher);
		geocoder = new Geocoder(this, Locale.getDefault());
		soulissSceneBtn = (Button) findViewById(R.id.ButtonScene);
		soulissManualBtn = (Button) findViewById(R.id.ButtonManual);
		programsActivity = (Button) findViewById(R.id.Button06);
		basinfo = (TextView) findViewById(R.id.textViewBasicInfo);
		basinfoLine = (View) findViewById(R.id.textViewBasicInfoLine);
		serviceinfoLine = (View) findViewById(R.id.TextViewServiceLine);
		dbwarn = (TextView) findViewById(R.id.textViewDBWarn);
		dbwarnline = (View) findViewById(R.id.textViewDBWarnLine);
		posInfoLine = (View) findViewById(R.id.PositionWarnLine);
		serviceInfo = (TextView) findViewById(R.id.TextViewServiceActions);
		coordinfo = (TextView) findViewById(R.id.TextViewCoords);
		homedist = (TextView) findViewById(R.id.TextViewFromHome);
		serviceInfoFoot = (TextView) findViewById(R.id.TextViewNodes);
		// gestore timeout dei comandi
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		timeoutHandler = new Handler();

		// Get the location manager
		// Define the criteria how to select the locatioin provider
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		// criteria.setAccuracy(Criteria.ACCURACY_HIGH);
		// criteria.setSpeedRequired(true);
		provider = locationManager.getBestProvider(criteria, true);
		boolean enabled = (provider != null && locationManager.isProviderEnabled(provider) && opzioni.getHomeLatitude() != 0);
		if (enabled) {
			coordinfo.setText(Html.fromHtml(getString(R.string.status_geoprovider_enabled) + " (<b>" + provider
					+ "</b>)"));
			// ogni minuto, minimo 100 metri
			locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL,
					Constants.POSITION_UPDATE_MIN_DIST, this);
			Location location = locationManager.getLastKnownLocation(provider);
			// Initialize the location fields
			if (location != null) {
				Log.i(TAG, "Geo-Provider " + provider + getString(R.string.status_provider_selected));
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				coordinfo.setText((Html.fromHtml("Position from <b>" + provider + "</b>: " + Constants.df.format(lat)
						+ " : " + Constants.df.format(lng))));
				float[] res = new float[3];
				Location.distanceBetween(lat, lng, opzioni.getHomeLatitude(), opzioni.getHomeLongitude(), res);
				homedist.setText(Html.fromHtml(getString(R.string.homedist) + res[0]));
				posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_green));
			}
		} else if (opzioni.getHomeLatitude() != 0) {
			coordinfo.setText(Html.fromHtml(getString(R.string.status_geoprovider_disabled)));
			homedist.setVisibility(View.GONE);
			posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_yellow));
		} else {
			coordinfo.setVisibility(View.GONE);
			homedist.setText(Html.fromHtml(getString(R.string.homewarn)));
			posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_yellow));
		}
		Log.d(Constants.TAG, Constants.TAG + " onCreate() call end");
		// Log.i(TAG, "INFOTEST");
		// Log.w(TAG, "WARNTEST");
	}

	@Override
	protected void onStart() {
		super.onStart();
		doBindService();
		opzioni.reload();
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo inf = connectivity.getActiveNetworkInfo();

		NetworkStateReceiver.storeNetworkInfo(inf, opzioni);

		/* SCENES */
		OnClickListener simpleOnClickListener2 = new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(LauncherActivity.this, SceneListActivity.class);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				LauncherActivity.this.startActivity(myIntent);
				return;
			}
		};
		soulissSceneBtn.setOnClickListener(simpleOnClickListener2);

		/* PROGRAMS */
		OnClickListener simpleOnClickListenerProgr = new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(LauncherActivity.this, ProgramListActivity.class);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				LauncherActivity.this.startActivity(myIntent);
				return;
			}
		};
		programsActivity.setOnClickListener(simpleOnClickListenerProgr);

		/* MANUAL */
		OnClickListener simpleOnClickListener = new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(LauncherActivity.this, NodesListActivity.class);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				LauncherActivity.this.startActivity(myIntent);
				return;
			}
		};
		soulissManualBtn.setOnClickListener(simpleOnClickListener);

		// refresh testo
		setHeadInfo();
		setDbInfo();
		setServiceInfo();
		if (opzioni.isSoulissIpConfigured() && opzioni.isDataServiceEnabled())
			serviceInfoFoot.setText(Html.fromHtml("<b>"+getString(R.string.waiting)+"</b> "));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.Opzioni:
			Intent preferencesActivity = new Intent(getBaseContext(), PreferencesActivity.class);
			// evita doppie aperture per via delle sotto-schermate
			preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(preferencesActivity);
			return true;
		case R.id.TestUDP:
			Intent myIntents = new Intent(LauncherActivity.this, ManualUDPTestActivity.class);
			LauncherActivity.this.startActivity(myIntents);
			return true;
		case R.id.Esci:
			super.finish();
		}

		return super.onOptionsItemSelected(item);
	}

	private void setHeadInfo() {
		basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_green));
		// check se IP non settato check system configured
		if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissPublicIpConfigured()) {
			basinfo.setText(Html.fromHtml(getString(R.string.notconfigured)));
			basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_red));
			return;
		}
		if (!opzioni.getCustomPref().contains("connectionName")) {
			basinfo.setText(getString(R.string.warn_connection));
			basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_yellow));
			return;
		}
		if (!opzioni.isSoulissPublicIpConfigured()
				&& !("WIFI".compareTo(opzioni.getCustomPref().getString("connectionName", "")) == 0)) {
			basinfo.setText(Html.fromHtml(getString(R.string.warn_wifi)));
			basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_red));
			return;
		}
		String base = opzioni.getAndSetCachedAddress();
		Log.i(TAG, "cached Address: "+base);
		if (base != null && "".compareTo(base) != 0) {
			basinfo.setText(Html.fromHtml(getString(R.string.contact_at)+"<font color=\"#99CC00\"> " + base
					+ "</font> via <b>" + opzioni.getCustomPref().getString("connectionName", "ERROR")
					+ "</b>"));
		} else if (base != null && getString(R.string.unavailable).compareTo(base) != 0) {
			basinfo.setText(getString(R.string.unavailable));
		} else {
			basinfo.setText(getString(R.string.contact_progress));
		}

	}

	private void setDbInfo() {
		/* DB Warning */
		if (!opzioni.isDbConfigured()) {
			dbwarn.setVisibility(View.VISIBLE);
			dbwarn.setText(getString(R.string.dialog_notinited_db));
			dbwarnline.setVisibility(View.VISIBLE);
			if (opzioni.getTextFx()) {
				Animation a2 = AnimationUtils.loadAnimation(this, R.anim.alpha_out);
				a2.reset();
				a2.setStartOffset(1000);
				dbwarnline.startAnimation(a2);
				Animation a3 = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
				a3.reset();
				a3.setStartOffset(1800);
				dbwarnline.startAnimation(a3);
			}
		} else {
			dbwarn.setVisibility(View.GONE);
			dbwarnline.setVisibility(View.GONE);
		}
	}

	private void setServiceInfo() {
		StringBuilder sb = new StringBuilder();
		serviceinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_green));
		/* SERVICE MANAGEMENT */
		if (!opzioni.isDataServiceEnabled()) {
			if (mIsBound && mBoundService != null) {// in esecuzione? strano
				sb.append("<br/><b>" + getResources().getString(R.string.service_disabled) + "!</b> ");
				serviceinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_red));
				if (opzioni.getTextFx()) {
					Animation a2 = AnimationUtils.loadAnimation(this, R.anim.alpha_out);
					a2.reset();
					serviceinfoLine.startAnimation(a2);
				}
				mBoundService.stopSelf();
			} else {
				sb.append("<b>" + getResources().getString(R.string.service_disabled) + "</b> "
						+ (mIsBound ? " but <b>bound</b>" : " and not <b>bound</b>"));
			}

		} else {
			if (mIsBound && mBoundService != null) {
				sb.append("<b>" + getString(R.string.service_lastexec) + "</b> "
						+ Constants.getTimeAgo(mBoundService.getLastupd()) + "<br/><b>");
				sb.append(getString(R.string.opt_serviceinterval) + ":</b> " + Constants.getScaledTime(opzioni.getDataServiceIntervalMsec() / 1000));
			} else {
				sb.append("Souliss Data service <b>enabled</b> but service <b>not bound</b>");
				Intent serviceIntent = new Intent(this, SoulissDataService.class);
				startService(serviceIntent);
				serviceinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_yellow));
			}
		}
		serviceInfo.setText(Html.fromHtml(sb.toString()));
	}

	Runnable timeExpired = new Runnable() {
		@Override
		public void run() {
			Log.e(TAG, "TIMEOUT!!!");
			serviceInfoFoot.setText(Html.fromHtml("Command timeout <b><font color=\"#FF4444\">expired</font></b>."
					+ " Be sure to set correct Souliss address "));
			opzioni.getAndSetCachedAddress();
		}
	};
	// meccanismo per network detection
	private BroadcastReceiver timeoutReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			int delay = extras.getInt("REQUEST_TIMEOUT_MSEC");
			timeoutHandler.postDelayed(timeExpired, delay);
		}
	};

	// invoked when RAW data is received
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			opzioni.initializePrefs();
			//rimuove timeout
			timeoutHandler.removeCallbacks(timeExpired);
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Log.i(TAG, "Broadcast receive, refresh from DB");
				@SuppressWarnings("unchecked")
				ArrayList<Short> vers = (ArrayList<Short>) extras.get("MACACO");
				//Log.d(TAG, "RAW DATA: " + vers);

				StringBuilder tmp = new StringBuilder("<b>"+getString(R.string.last_update)+"</b> "
						+ Constants.hourFormat.format(new Date()));
				tmp.append(" - " + vers);
				
				setHeadInfo();
				setServiceInfo();
				serviceInfoFoot.setText(Html.fromHtml(tmp.toString()));
				// questo sovrascrive nodesinf

			} else {
				Log.e(TAG, "EMPTY response!!");
			}
			
			
		}
	};

	/**
	 * Request updates at startup
	 * 
	 * @see NetworkStateReceiver
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// this is only used for refresh UI
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		// registerReceiver(connectivityReceiver, filter);

		// this is only used for refresh UI
		IntentFilter filtera = new IntentFilter();
		filtera.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_TIMEOUT);
		registerReceiver(timeoutReceiver, filtera);

		// IDEM, serve solo per reporting
		IntentFilter filtere = new IntentFilter();
		filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		registerReceiver(datareceiver, filtere);

		if (provider != null) {
			locationManager.requestLocationUpdates(provider, Constants.POSITION_UPDATE_INTERVAL,
					Constants.POSITION_UPDATE_MIN_DIST, this);
		}

		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						setHeadInfo();
						setDbInfo();
						setServiceInfo();
					}
				});
			}
		}, 100, Constants.GUI_UPDATE_INTERVAL*opzioni.getBackoff()); // updates UI each 5 secs
	}

	/*
	 * Remove the locationlistener updates when Activity is paused and
	 * unregister connectivity updates
	 */
	@Override
	protected void onPause() {
		// unregisterReceiver(connectivityReceiver);
		unregisterReceiver(datareceiver);
		unregisterReceiver(timeoutReceiver);
		super.onPause();
		autoUpdate.cancel();
		dbwarnline.clearAnimation();
		locationManager.removeUpdates(this);
		timeoutHandler.removeCallbacks(timeExpired);
	}

	@Override
	protected void onDestroy() {
		doUnbindService();
		// Muovo i log su file
		Log.w(TAG, "Closing app, moving logs");
		try {
			File filename = new File(Environment.getExternalStorageDirectory() + "/souliss.log");
			filename.createNewFile();
			// String cmd = "logcat -d -v time  -f " +
			// filename.getAbsolutePath()
			String cmd = "logcat -d -v time  -f " + filename.getAbsolutePath()
					+ " SoulissApp:W SoulissDataService:D *:S ";
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		final double lat = (location.getLatitude());
		final double lng = (location.getLongitude());
		coordinfo.setVisibility(View.VISIBLE);
		homedist.setVisibility(View.VISIBLE);
		coordinfo.setText(Html.fromHtml(getString(R.string.positionfrom)+" <b>" + provider + "</b>: " + Constants.df.format(lat) + " : "
				+ Constants.df.format(lng)));

		final float[] res = new float[3];
		// Location.distanceBetween(lat, lng, 44.50117265d, 11.34518103, res);
		Location.distanceBetween(lat, lng, opzioni.getHomeLatitude(), opzioni.getHomeLongitude(), res);
		if (opzioni.getHomeLatitude() != 0) {

			new Thread(new Runnable() {
				@Override
				public void run() {
					String loc = null;

					try {
						List<Address> list;
						list = geocoder.getFromLocation(lat, lng, 1);
						if (list != null && list.size() > 0) {
							Address address = list.get(0);
							loc = address.getLocality();
						}
					} catch (final IOException e) {
						Log.e(TAG, "Geocoder ERROR", e);
						runOnUiThread(new Runnable() {
							public void run() {
								homedist.setText(Html.fromHtml("Geocoder <font color=\"#FF4444\">ERROR</font>: "
										+ e.getMessage()));
								posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_red));

							}
						});
						loc = null;
					}

					final String ff = loc;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// calcola unita di misura e localita col
							// geocoder
							String unit = "m";
							if (res[0] > 2000) {// usa chilometri
								unit = "km";
								res[0] = res[0] / 1000;
							}
							homedist.setText(Html.fromHtml(getString(R.string.homedist)+ (int) res[0] + unit
									+ (ff == null ? "" : " ("+getString(R.string.currentlyin)+" " + ff + ")")));
							posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_green));

						}
					});

				}
			}).start();

		} else {
			homedist.setText(Html
					.fromHtml(getString(R.string.homewarn)));
			posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_yellow));
		}

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(Constants.TAG, "status change " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
		Log.i(TAG, "Enabled new provider " + provider);

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
		Log.i(TAG, "Disabled provider " + provider);
	}
}
