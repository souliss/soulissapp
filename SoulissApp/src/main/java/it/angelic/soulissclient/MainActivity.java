package it.angelic.soulissclient;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import it.angelic.receivers.NetworkStateReceiver;
import it.angelic.soulissclient.adapters.StaggeredDashboardElementAdapter;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissModelException;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.programmi.GeofenceRunnable;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;

import static it.angelic.soulissclient.Constants.TAG;

/**
 * SoulissApp Main screen - Dashboard
 *
 * @author shine@angelic.it
 */
public class MainActivity extends AbstractStatusedFragmentActivity {
    private SoulissDBLauncherHelper database;
    private StaggeredDashboardElementAdapter launcherMainAdapter;
    private LocationSettingsResponse locSettings;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient locationCli;
    private SoulissDataService mBoundService;
    // invoked when RAW data is received
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // opzioni.initializePrefs();
            // rimuove timeout
            //  timeoutHandler.removeCallbacks(timeExpired);
            Bundle extras = intent.getExtras();

            if (extras != null && extras.get("MACACO") != null) {
                Log.i(TAG, "Broadcast receive, refresh from DB");
                database.refreshMapFromDB();
                List<LauncherElement> launcherItems = database.getLauncherItems(MainActivity.this);
                launcherMainAdapter.setLauncherElements(launcherItems);
                launcherMainAdapter.notifyDataSetChanged();
                if (mBoundService != null) {
                    SimpleDateFormat dfDate = new SimpleDateFormat("dd MMM yyyy hh.mm.ss");
                    Log.i(TAG, "Service lastupd: " + dfDate.format(mBoundService.getLastupd().getTime()));
                }
            } else {
                Log.e(TAG, "EMPTY response!!");
            }
        }
    };
    private NetworkStateReceiver netReceiver;
    private SoulissPreferenceHelper opzioni;
    /* SOULISS DATA SERVICE BINDINGS */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((SoulissDataService.LocalBinder) service).getService();
            launcherMainAdapter.setmBoundService(mBoundService);

            Log.i(TAG, "Dataservice connected, BackedOffServiceInterval=" + opzioni.getBackedOffServiceIntervalMsec());
            SoulissPreferenceHelper pref = SoulissApp.getOpzioni();
            if (pref.isDataServiceEnabled()) {
                //will detect if late
                mBoundService.reschedule(false);
            } else {
                Log.w(TAG, "Dataservice DISABLED");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(MainActivity.this, "Dataservice disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private void configureVoiceFab() {
        //VOICE SEARCH
        FloatingActionButton fab = findViewById(R.id.fab);
        if (opzioni.isVoiceCommandEnabled() && opzioni.isDbConfigured()) {

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    i.putExtra(RecognizerIntent.EXTRA_PROMPT, MainActivity.this.getString(R.string.voice_command_help));
                    try {
                        startActivityForResult(i, Constants.VOICE_REQUEST_OK);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            fab.show();
        } else {
            fab.hide();
        }
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        if (BuildConfig.DEBUG) {
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            locationRequest.setInterval(100000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        return locationRequest;
    }

    private void doBindService() {
        Log.i(TAG, "doBindService(), BIND_AUTO_CREATE.");
        bindService(new Intent(MainActivity.this, SoulissDataService.class), mConnection, BIND_AUTO_CREATE);
    }

    private void doUnbindService() {
        if (mBoundService != null) {
            Log.i(TAG, "UNBIND, Detach our existing connection.");
            unbindService(mConnection);
        }
    }

    private void enqueueJobs() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        WorkManager.getInstance(MainActivity.this).cancelAllWork();
        if (opzioni.isDataServiceEnabled()) {
            PeriodicWorkRequest requestService =
                    // Executes MyWorker every 15 minutes
                    new PeriodicWorkRequest.Builder(WorkerZombieRestore.class, opzioni.getDataServiceIntervalMsec(), TimeUnit.MILLISECONDS)
                            //.setInputData(input)
                            .setConstraints(constraints)
                            .setInitialDelay(10, TimeUnit.SECONDS)
                            .build();

            PeriodicWorkRequest requestTimed =
                    // Executes MyWorker every 15 minutes
                    new PeriodicWorkRequest.Builder(WorkerTimedCommands.class, opzioni.getDataServiceIntervalMsec(), TimeUnit.MILLISECONDS)
                            .setConstraints(constraints)
                            //.setInputData(input)
                            .build();

            WorkManager.getInstance(MainActivity.this)
                    .enqueueUniquePeriodicWork("souliss-zombie-restore", ExistingPeriodicWorkPolicy.REPLACE, requestService);

            WorkManager.getInstance(MainActivity.this)
                    // Use ExistingWorkPolicy.REPLACE to cancel and delete any existing pending
                    // (uncompleted) work with the same unique name. Then, insert the newly-specified
                    // work.
                    .enqueueUniquePeriodicWork("souliss-timed-commands", ExistingPeriodicWorkPolicy.REPLACE, requestTimed);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        opzioni.reload();
        // Remove title bar
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_launcher2);

        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewLauncherItems);
        final TextView toHid = findViewById(R.id.TextViewTagsDesc);
        final TextView textViewTagsDescFa = findViewById(R.id.TextViewDashboardDescFa);
        FontAwesomeUtil.prepareMiniFontAweTextView(this, textViewTagsDescFa, FontAwesomeEnum.fa_close.getFontName());

        //NASCONDI
        textViewTagsDescFa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewTagsDescFa.setVisibility(View.GONE);
                toHid.setVisibility(View.GONE);
                opzioni.setDontShowAgain("launcherInfo", true);
            }
        });
        if (opzioni.getDontShowAgain("launcherInfo")) {
            textViewTagsDescFa.setVisibility(View.GONE);
            toHid.setVisibility(View.GONE);
        }

        int gridsize = 2;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridsize = 3;
        if (getResources().getBoolean(R.bool.isTablet))
            gridsize++;
        StaggeredGridLayoutManager staggeredGridManager = new StaggeredGridLayoutManager(gridsize, StaggeredGridLayoutManager.VERTICAL);
        //staggeredGridManager.setGapStrategy(GAP_HANDLING_NONE);

        locationCli = LocationServices.getFusedLocationProviderClient(this);

        mRecyclerView.setLayoutManager(staggeredGridManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//FIXME

        database = new SoulissDBLauncherHelper(this);
        List launcherItems = database.getLauncherItems(this);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                doBindService();
                // subscribe a tutti i nodi, in teoria non serve*/
                UDPHelper.stateRequest(opzioni, database.countNodes(), 0);
            }
        });
        //TODO test&tune
        enqueueJobs();


        launcherMainAdapter = new StaggeredDashboardElementAdapter(this, launcherItems, mBoundService);

        mRecyclerView.setAdapter(launcherMainAdapter);

        // DRAWER
        initDrawer(this, DrawerMenuHelper.TAGS);

        // Extend the Callback class
        ItemTouchHelper.Callback launcherCallback = new LauncherStaggeredCallback(MainActivity.this, launcherMainAdapter, database);
        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(launcherCallback);
        ith.attachToRecyclerView(mRecyclerView);

        //opzioni.reload();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    private void onLocationChanged(Location location) {
        final double lat = (location.getLatitude());
        final double lng = (location.getLongitude());
        if (launcherMainAdapter.getLocationLauncherElements() != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String adString = "";
                    final StringBuilder out1 = new StringBuilder();
                    final StringBuilder out2 = new StringBuilder();
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    String loc = null;
                    try {
                        List<Address> list;
                        list = geocoder.getFromLocation(lat, lng, 1);

                        if (list != null && list.size() > 0) {
                            Address address = list.get(0);
                            loc = address.getLocality();
                            if (address.getAddressLine(0) != null)
                                adString = ", " + address.getAddressLine(0);
                        }
                    } catch (final Exception e) {
                        out1.append(Html.fromHtml("Geocoder <font color=\"#FF4444\">ERROR</font>: " + e.getMessage())).toString();
                    }
                    if (loc == null)
                        loc = Constants.gpsDecimalFormat.format(lat) + " : " + Constants.gpsDecimalFormat.format(lng);
                    //final String ff = loc;
                    if (locSettings != null) {
                        //final String sonoIncapace = adString;
                        out2.append(Html.fromHtml(getString(R.string.positionfrom)
                                + " <b>"
                                + (locSettings.getLocationSettingsStates().isGpsUsable() ? "GPS" : "3G")
                                + "</b>: " + loc
                                + adString)).toString();
                    }
                    final float[] res = new float[3];
                    // Location.distanceBetween(lat, lng, 44.50117265d, 11.34518103, res);
                    Location.distanceBetween(lat, lng, opzioni.getHomeLatitude(), opzioni.getHomeLongitude(), res);
                    if (opzioni.getHomeLatitude() != 0) {

                        // calcola unita di misura e localita col
                        // geocoder
                        String unit = "m";
                        if (res[0] > 2000) {// usa chilometri
                            unit = "km";
                            res[0] = res[0] / 1000;
                        }
                        // homedist.setVisibility(View.VISIBLE);
                        out1.append(Html.fromHtml("<b>" + getString(R.string.homedist) + "</b> "
                                + (int) res[0] + unit
                                + (loc == null ? "" : " (" + getString(R.string.currentlyin) + " " + loc + ")"))).toString();

                    } else {

                        out1.append(Html.fromHtml(getString(R.string.homewarn))).toString();

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(Constants.TAG, "appending location change " + out1.toString() + " - " + out2.toString());
                            if (launcherMainAdapter.getLocationLauncherElements() != null) {
                                LauncherElement p1 = launcherMainAdapter.getLocationLauncherElements();
                                p1.setDesc(out1.toString() + "\n" + out2.toString());
                                database.updateLauncherElement(p1);
                            }
                            launcherMainAdapter.notifyItemChanged(launcherMainAdapter.getLauncherElements().indexOf(launcherMainAdapter.getLocationLauncherElements()));
                        }
                    });
                }
            }).start();
        } else {
            Log.e(Constants.TAG, "Launcher element not found: location ");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerLinear)) {
                mDrawerLayout.closeDrawer(mDrawerLinear);
            } else {
                mDrawerLayout.openDrawer(mDrawerLinear);
            }
            return true;//cliccato sul drawer, non far altro
        }
        switch (item.getItemId()) {
            case R.id.Opzioni:
                Intent preferencesActivity = new Intent(getBaseContext(), SettingsActivity.class);
                // evita doppie aperture per via delle sotto-schermate
                preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(preferencesActivity);
                return true;
            case R.id.TestUDP:
                Intent myIntents = new Intent(MainActivity.this, ManualUDPTestActivity.class);
                MainActivity.this.startActivity(myIntents);
                return true;
            case R.id.Esci:
                super.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(datareceiver);
        unregisterReceiver(netReceiver);

        super.onPause();
        //autoUpdate.cancel();
        if (locationCli != null && locationCallback != null) {
            locationCli.removeLocationUpdates(locationCallback);
        }
        //non mettere nulla qui
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted! Please retry", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied from user", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case Constants.MY_PERMISSIONS_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //provider = locationManager.getBestProvider(SoulissUtils.getGeoCriteria(), true);
                    Log.w(TAG, "MY_PERMISSIONS_ACCESS_LOCATION permission granted");

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.wtf(TAG, "boh. permesso negato su risposta permesso");
                        return;
                    }
                    startLocationUpdates();

                } else {
                    // quello stronzo. Utente nega permesso
                    // if (cardViewPositionInfo != null)
                    //   cardViewPositionInfo.setVisibility(View.GONE);
                }
                return;


            }
            default:
                Log.wtf(TAG, "BOH. permesso non riconosciuto: " + requestCode);

                // other 'case' lines to check for other
                // permissions this app might request
        }
    }

    /**
     * Request updates at startup
     *
     * @see NetworkStateReceiver
     */
    @Override
    protected void onResume() {
        super.onResume();

        // macaco pack
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        registerReceiver(datareceiver, filtere);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netReceiver = new NetworkStateReceiver();
        registerReceiver(netReceiver, filter);

        List<LauncherElement> launcherItems = database.getLauncherItems(MainActivity.this);

        launcherMainAdapter.setLauncherElements(launcherItems);
        launcherMainAdapter.notifyDataSetChanged();


       /* autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //setDbAndFavouritesInfo();
                        //TODO add staggered refresh
                    }
                });
            }
            // UI updates every 5 secs.
        }, 100, Constants.GUI_UPDATE_INTERVAL * opzioni.getBackoff());*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setActionBarInfo(getString(R.string.souliss_app_name));
        opzioni.initializePrefs();

        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo inf = connectivity.getActiveNetworkInfo();
        NetworkStateReceiver.storeNetworkInfo(inf, opzioni);

        if (!opzioni.isDbConfigured()) {
            AlertDialogHelper.dbNotInitedDialog(this);
        }
        // this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ArrayAdapter<INavDrawerItem> navAdapter = new NavDrawerAdapter(MainActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.DASHBOARD);
        mDrawerList.setAdapter(navAdapter);

        configureVoiceFab();

        boolean permissionAccessCoarseLocationApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (permissionAccessCoarseLocationApproved) {
            //refresh geofences
            AsyncTask.execute(new GeofenceRunnable(MainActivity.this));
            startLocationUpdates();
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED && database.getPositionalPrograms().size() > 0) {
            // App doesn't have access to the device's location at all. Make full request
            // for permission.
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, Constants.MY_PERMISSIONS_ACCESS_LOCATION);
        }
    }

    private void startLocationUpdates() {
        Log.w(Constants.TAG, "Requesting POS updates ");

        LocationRequest locReq = createLocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.w(Constants.TAG, "RECEIVE POS updates " + locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (launcherMainAdapter.getLocationLauncherElements() != null) {
                        onLocationChanged(location);
                    }
                }
            }
        };
        locationCli.requestLocationUpdates(locReq,
                locationCallback,
                Looper.getMainLooper());

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locSettings = locationSettingsResponse;
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                locSettings = null;
            }
        });
    }

    private static class LauncherStaggeredCallback extends ItemTouchHelper.Callback {
        private final StaggeredDashboardElementAdapter adapter;
        private SoulissDBLauncherHelper database;

        public LauncherStaggeredCallback(Context xct, final StaggeredDashboardElementAdapter adapter, SoulissDBLauncherHelper database) {
            this.adapter = adapter;
            this.database = database;

        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
            //  return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
            //        ItemTouchHelper.DOWN | ItemTouchHelper.UP |);
        }

        //and in your imlpementaion of
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            // get the viewHolder's and target's positions in your launcherMainAdapter data, swap them
            // Collections.swap(adapter.getLauncherElements(), viewHolder.getAdapterPosition(), target.getAdapterPosition());


            if (viewHolder.getAdapterPosition() < target.getAdapterPosition()) {
                for (int i = viewHolder.getAdapterPosition(); i < target.getAdapterPosition(); i++) {
                    Collections.swap(adapter.getLauncherElements(), i, i + 1);
                }
            } else {
                for (int i = viewHolder.getAdapterPosition(); i > target.getAdapterPosition(); i--) {
                    Collections.swap(adapter.getLauncherElements(), i, i - 1);
                }
            }

            // and notify the launcherMainAdapter that its dataset has changed
            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

            //occhio! sono gia swappati
            LauncherElement p1 = adapter.getLauncherElements().get(viewHolder.getAdapterPosition());
            LauncherElement p2 = adapter.getLauncherElements().get(target.getAdapterPosition());
            p1.setOrder((short) viewHolder.getAdapterPosition());
            p2.setOrder((short) target.getAdapterPosition());
            //alla fine persisto
            database.updateLauncherElement(p1);
            database.updateLauncherElement(p2);
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int deletedPosition = viewHolder.getAdapterPosition();
            final LauncherElement tbr = adapter.getLauncherElements().get(deletedPosition);
            //SoulissTag todoItem = launcherMainAdapter.getItem(viewHolder.getAdapterPosition());
            //forse non serve
            adapter.removeAt(deletedPosition);
            // launcherMainAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            //clearView(mRecyclerView, viewHolder);
            database.remove(tbr);
            adapter.notifyDataSetChanged();

            Snackbar.make(viewHolder.itemView, R.string.tile_removed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                database.addElement(tbr);
                                adapter.notifyDataSetChanged();
                                //adapter.addAt(deletedPosition, tbr);
                            } catch (SoulissModelException e) {
                                e.printStackTrace();
                            }
                        }
                    }).show();
        }
    }


}