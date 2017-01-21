package it.angelic.soulissclient;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import it.angelic.receivers.NetworkStateReceiver;
import it.angelic.soulissclient.adapters.StaggeredLauncherElementAdapter;
import it.angelic.soulissclient.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.util.FontAwesomeUtil;

import static it.angelic.soulissclient.Constants.TAG;

/**
 * This will not work so great since the heights of the imageViews
 * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get
 * the (intrinsic) image width and height and set the views height manually. I will
 * look into a fix once I find extra time.
 *
 * @author Maurycy Wojtowicz
 */
public class MainActivity extends AbstractStatusedFragmentActivity {
    private Timer autoUpdate;
    private SoulissDBLauncherHelper dbLauncher;
    private StaggeredLauncherElementAdapter launcherMainAdapter;
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
                @SuppressWarnings("unchecked")
                ArrayList<Short> vers = (ArrayList<Short>) extras.get("MACACO");
                // FIXME TEMPORARY
                dbLauncher.refreshMap();
                List<LauncherElement> launcherItems = dbLauncher.getLauncherItems(MainActivity.this);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                Set<String> visibili = preferences.getStringSet("launcher_elems", new HashSet<String>());
                Set<LauncherElement> removeSet = new HashSet<>();
                for (int i = 0; i < launcherItems.size(); i++) {
                    if (!visibili.contains("" + launcherItems.get(i).getId())) {
                        removeSet.add(launcherItems.get(i));
                    }
                }
                launcherItems.removeAll(removeSet);
                launcherMainAdapter.setLauncherElements(launcherItems);
                launcherMainAdapter.notifyDataSetChanged();

            } else {
                Log.e(TAG, "EMPTY response!!");
            }
        }
    };
    private SoulissDataService mBoundService;
    private boolean mIsBound;
    private RecyclerView mRecyclerView;
    private ArrayAdapter<INavDrawerItem> navAdapter;
    private SoulissPreferenceHelper opzioni;
    /* SOULISS DATA SERVICE BINDINGS */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((SoulissDataService.LocalBinder) service).getService();
            Log.i(TAG, "Dataservice connected, BackedOffServiceInterval=" + opzioni.getBackedOffServiceIntervalMsec());
            SoulissPreferenceHelper pref = SoulissApp.getOpzioni();
            if (pref.isDataServiceEnabled()) {
                //will detect if late
                mBoundService.reschedule(false);
            } else {
                Log.i(TAG, "Dataservice DISABLED");
            }
            //setServiceInfo();
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(MainActivity.this, "Dataservice disconnected", Toast.LENGTH_SHORT).show();
            mIsBound = false;
        }
    };

    private void doBindService() {
        Log.d(TAG, "doBindService(), BIND_AUTO_CREATE.");
        bindService(new Intent(MainActivity.this, SoulissDataService.class), mConnection, BIND_AUTO_CREATE);
    }


    private void doUnbindService() {
        if (mIsBound) {
            Log.d(TAG, "UNBIND, Detach our existing connection.");
            unbindService(mConnection);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String permissions[], @NonNull int[] grantResults) {
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

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    /*
     * @Override public void setTitle(CharSequence title) { mTitle = title;
	 * getActionBar().setTitle(mTitle); }
	 */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * This will not work so great since the heights of the imageViews
     * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get
     * the (intrinsic) image width and height and set the views height manually. I will
     * look into a fix once I find extra time.
     */
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


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewLauncherItems);
        final TextView toHid = (TextView) findViewById(R.id.TextViewTagsDesc);
        final TextView textViewTagsDescFa = (TextView) findViewById(R.id.TextViewTagsDescFa);
        FontAwesomeUtil.prepareMiniFontAweTextView(this, textViewTagsDescFa, "fa-close");

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
        StaggeredGridLayoutManager gm = new StaggeredGridLayoutManager(gridsize, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(gm);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//FIXME

        dbLauncher = new SoulissDBLauncherHelper(this);
        List launcherItems = dbLauncher.getLauncherItems(this);
        // LauncherElement[] array = (LauncherElement[]) launcherItems.toArray(new LauncherElement[launcherItems.size()]);


        new Thread(new Runnable() {
            @Override
            public void run() {
                // subscribe a tutti i nodi, in teoria non serve*/
                UDPHelper.stateRequest(opzioni, dbLauncher.countNodes(), 0);
            }
        }).start();


        launcherMainAdapter = new StaggeredLauncherElementAdapter(this, launcherItems, mBoundService);

        mRecyclerView.setAdapter(launcherMainAdapter);
        launcherMainAdapter.notifyDataSetChanged();


        new Thread(new Runnable() {
            @Override
            public void run() {
                doBindService();
            }
        }).start();


        // DRAWER
        initDrawer(this, DrawerMenuHelper.TAGS);

        // Extend the Callback class
        ItemTouchHelper.Callback launcherCallback = new LauncherStaggeredCallback(launcherMainAdapter);
        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(launcherCallback);
        ith.attachToRecyclerView(mRecyclerView);


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
                Intent preferencesActivity = new Intent(getBaseContext(), PreferencesActivity.class);
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
        super.onPause();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // macaco pack
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        registerReceiver(datareceiver, filtere);

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
        navAdapter = new NavDrawerAdapter(MainActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.TAGS);
        mDrawerList.setAdapter(navAdapter);
    }

    private static class LauncherStaggeredCallback extends ItemTouchHelper.Callback {
        private final StaggeredLauncherElementAdapter adapter;
        View.OnClickListener mOnClickListener;

        public LauncherStaggeredCallback(StaggeredLauncherElementAdapter adapter) {
            this.adapter = adapter;
            mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // readd item
                }
            };
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

            Collections.swap(adapter.getLauncherElements(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
            // and notify the launcherMainAdapter that its dataset has changed
            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            //SoulissTag todoItem = launcherMainAdapter.getItem(viewHolder.getAdapterPosition());
            //forse non serve
            adapter.removeAt(viewHolder.getAdapterPosition());
            // launcherMainAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            //clearView(mRecyclerView, viewHolder);

            Snackbar.make(viewHolder.itemView, "Tile removed", Snackbar.LENGTH_SHORT).setAction(R.string.cancel, mOnClickListener).show(); // Don’t forget to show!
        }
    }
}