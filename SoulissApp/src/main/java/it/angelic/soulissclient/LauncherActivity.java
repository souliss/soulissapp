package it.angelic.soulissclient;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import it.angelic.receivers.NetworkStateReceiver;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.helpers.Eula;
import it.angelic.soulissclient.helpers.ListButton;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.net.NetUtils;
import it.angelic.soulissclient.net.webserver.HTTPService;

import static it.angelic.soulissclient.Constants.TAG;


/**
 * SoulissApp main screen
 *
 * @author Ale
 */
public class LauncherActivity extends AbstractStatusedFragmentActivity implements LocationListener {

    protected PendingIntent netListenerPendingIntent;
    ConnectivityManager mConnectivity;
    TelephonyManager mTelephony;
    Runnable timeExpired = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "TIMEOUT!!!");
            serviceInfoFoot.setText(Html.fromHtml(getString(R.string.timeout_expired)));
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
            // opzioni.initializePrefs();
            // rimuove timeout
            timeoutHandler.removeCallbacks(timeExpired);
            Bundle extras = intent.getExtras();

            if (extras != null && extras.get("MACACO") != null) {
                Log.i(TAG, "Broadcast receive, refresh from DB");
                @SuppressWarnings("unchecked")
                ArrayList<Short> vers = (ArrayList<Short>) extras.get("MACACO");
                // Log.d(TAG, "RAW DATA: " + vers);

                setHeadInfo();
                setServiceInfo();
                setWebServiceInfo();
                setAntiTheftInfo();
                serviceInfoFoot.setText(Html.fromHtml("<b>" + getString(R.string.last_update) + "</b> "
                        + Constants.hourFormat.format(new Date()) + " - " + vers.size() + " " + context.getString(R.string.bytes_received)));
                // questo sovrascrive nodesinf

            } else {
                Log.e(TAG, "EMPTY response!!");
            }
        }
    };
    private LocationManager locationManager;
    private String provider;
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
    private TextView serviceInfoAntiTheft;
    private Button programsActivity;
    private SoulissPreferenceHelper opzioni;
    private SoulissDataService mBoundService;
    private boolean mIsBound;
    private HTTPService mBoundWebService;
    private TextView webserviceInfo;

    /* SOULISS DATA SERVICE BINDINGS */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((SoulissDataService.LocalBinder) service).getService();
            Log.i(TAG, "Dataservice connected, BackedOffServiceInterval=" + opzioni.getBackedOffServiceIntervalMsec());
            SoulissPreferenceHelper pref = SoulissClient.getOpzioni();
            if (pref.isDataServiceEnabled()) {
                //will detect if late
                mBoundService.reschedule(false);
            } else {
                Log.i(TAG, "Dataservice DISABLED");
            }
            setServiceInfo();
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
    private boolean mIsWebBound;
    private ServiceConnection mWebConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundWebService = ((HTTPService.LocalBinder) service).getService();
            Log.i(TAG, "WEBSERVER connected");
            mIsWebBound = true;
            setWebServiceInfo();
        }

        public void onServiceDisconnected(ComponentName className) {
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundWebService = null;
            Toast.makeText(LauncherActivity.this, "WEBSERVER disconnected", Toast.LENGTH_SHORT).show();
            mIsWebBound = false;
        }
    };
    private Timer autoUpdate;
    private Geocoder geocoder;
    private SoulissDBTagHelper db;
    private View webServiceInfoLine;
    private Criteria criteria;
    private CardView cardViewBasicInfo;
    private CardView cardViewPositionInfo;
    private CardView cardViewServiceInfo;
    private CardView cardViewFav;
    private List<SoulissTag> tags;

    void doBindService() {
        Log.d(TAG, "doBindService(), BIND_AUTO_CREATE.");
        bindService(new Intent(LauncherActivity.this, SoulissDataService.class), mConnection, BIND_AUTO_CREATE);
    }

    void doBindWebService() {
        //FIXME check flags, add BIND_NOT_FOREGROUND
        Log.d(TAG, "doBindWebService(), BIND_NOT_FOREGROUND.");
        bindService(new Intent(LauncherActivity.this, HTTPService.class), mWebConnection, BIND_NOT_FOREGROUND);
    }

    void doUnbindService() {
        if (mIsBound) {
            Log.d(TAG, "UNBIND, Detach our existing connection.");
            unbindService(mConnection);
        }
    }

    void doUnbindWebService() {
        if (mIsWebBound) {
            Log.d(TAG, "UNBIND WEB, Detach our existing connection.");
            unbindService(mWebConnection);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissClient.getOpzioni();
        //getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        Eula.show(this);

        setContentView(R.layout.main_launcher);

        db = new SoulissDBTagHelper(this);

        geocoder = new Geocoder(this, Locale.getDefault());
        soulissSceneBtn = (Button) findViewById(R.id.ButtonScene);
        soulissManualBtn = (Button) findViewById(R.id.ButtonManual);
        programsActivity = (Button) findViewById(R.id.Button06);
        basinfo = (TextView) findViewById(R.id.textViewBasicInfo);
        // basinfoLine = (View) findViewById(R.id.textViewBasicInfoLine);
        serviceinfoLine = findViewById(R.id.TextViewServiceLine);
        dbwarn = (TextView) findViewById(R.id.textViewDBWarn);
        dbwarnline = findViewById(R.id.textViewDBWarnLine);
        posInfoLine = findViewById(R.id.PositionWarnLine);
        webServiceInfoLine = findViewById(R.id.TextViewWebServiceLine);
        serviceInfo = (TextView) findViewById(R.id.TextViewServiceActions);
        webserviceInfo = (TextView) findViewById(R.id.TextViewWebService);
        coordinfo = (TextView) findViewById(R.id.TextViewCoords);
        homedist = (TextView) findViewById(R.id.TextViewFromHome);
        serviceInfoFoot = (TextView) findViewById(R.id.TextViewNodes);
        serviceInfoAntiTheft = (TextView) findViewById(R.id.TextViewAntiTheft);

        cardViewBasicInfo = (CardView) findViewById(R.id.BasicInfoCard);
        cardViewPositionInfo = (CardView) findViewById(R.id.dbAndPositionCard);
        cardViewServiceInfo = (CardView) findViewById(R.id.ServiceInfoCard);
        cardViewFav = (CardView) findViewById(R.id.TagsCard);
        // previously invisible view


        // gestore timeout dei comandi
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        timeoutHandler = new Handler();
        // Get the location manager
        // Define the criteria how to select the locatioin provider
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);


        // DRAWER
        initDrawer(this, DrawerMenuHelper.MANUAL);

        doBindService();
        if (opzioni.isWebserverEnabled())
            doBindWebService();

        Log.d(Constants.TAG, Constants.TAG + " onCreate() call end, bindService() called");
        // Log.w(TAG, "WARNTEST");
        if (opzioni.isLightThemeSelected()) {
            cardViewBasicInfo.setCardBackgroundColor(getResources().getColor(R.color.background_floating_material_light));
            cardViewPositionInfo.setCardBackgroundColor(getResources().getColor(R.color.background_floating_material_light));
            cardViewServiceInfo.setCardBackgroundColor(getResources().getColor(R.color.background_floating_material_light));
            cardViewFav.setCardBackgroundColor(getResources().getColor(R.color.background_floating_material_light));
        }


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(150);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            cardViewServiceInfo.setVisibility(View.VISIBLE);
                        }
                    });
                    Thread.sleep(500);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            cardViewPositionInfo.setVisibility(View.VISIBLE);
                        }
                    });
                    Thread.sleep(500);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            cardViewBasicInfo.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }}).start();
       /* Animation animatio = AnimationUtils.loadAnimation(cardViewFav.getContext(), (R.anim.slide_in_left));
        cardViewFav.startAnimation(animatio);
        Animation animation = AnimationUtils.loadAnimation(cardViewBasicInfo.getContext(), (R.anim.slide_in_left));
        animation.setStartOffset(500);
        cardViewBasicInfo.startAnimation(animation);
        Animation animation2 = AnimationUtils.loadAnimation(cardViewBasicInfo.getContext(), (R.anim.slide_in_left));
        animation2.setStartOffset(1000);
        cardViewPositionInfo.startAnimation(animation2);
        Animation animation3 = AnimationUtils.loadAnimation(cardViewBasicInfo.getContext(), (R.anim.slide_in_left));
        animation3.setStartOffset(1500);
        cardViewServiceInfo.startAnimation(animation3);
        */
    }

	/*
     * @Override public void setTitle(CharSequence title) { mTitle = title;
	 * getActionBar().setTitle(mTitle); }
	 */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLinear);
        // menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        opzioni.reload();
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo inf = connectivity.getActiveNetworkInfo();

        NetworkStateReceiver.storeNetworkInfo(inf, opzioni);

        initLocationProvider();
        /*TAGS*/
        OnClickListener ssc = new OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(LauncherActivity.this, TagListActivity.class);
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(LauncherActivity.this,
                                cardViewFav,   // The view which starts the transition
                                "helloTags"    // The transitionName of the view we’re transitioning to
                        );

                ActivityCompat.startActivity(LauncherActivity.this, myIntent, options.toBundle());

                // myIntent.putExtra("TAG", ()1);
                return;
            }
        };
        cardViewFav.setOnClickListener(ssc);
        /* SCENES */
        OnClickListener simpleOnClickListener2 = new OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(LauncherActivity.this, SceneListActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(LauncherActivity.this,
                                soulissSceneBtn,   // The view which starts the transition
                                "helloScenes"    // The transitionName of the view we’re transitioning to
                        );
                ActivityCompat.startActivity(LauncherActivity.this, myIntent, options.toBundle());


                // LauncherActivity.this.startActivity(myIntent);
                return;
            }
        };
        soulissSceneBtn.setOnClickListener(simpleOnClickListener2);

		/* PROGRAMS */
        OnClickListener simpleOnClickListenerProgr = new OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(LauncherActivity.this, ProgramListActivity.class);
                //myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(LauncherActivity.this,
                                programsActivity,   // The view which starts the transition
                                "helloPrograms"    // The transitionName of the view we’re transitioning to
                        );
                ActivityCompat.startActivity(LauncherActivity.this, myIntent, options.toBundle());
                //LauncherActivity.this.startActivity(myIntent);
                return;
            }
        };
        programsActivity.setOnClickListener(simpleOnClickListenerProgr);

		/* MANUAL */
        OnClickListener simpleOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(LauncherActivity.this, NodesListActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(LauncherActivity.this,
                                soulissManualBtn,   // The view which starts the transition
                                "helloManual"    // The transitionName of the view we’re transitioning to
                        );
                ActivityCompat.startActivity(LauncherActivity.this, myIntent, options.toBundle());
                //LauncherActivity.this.startActivity(myIntent);
                return;
            }
        };
        soulissManualBtn.setOnClickListener(simpleOnClickListener);
        // forza refresh drawer
        //mDrawerAdapter = new NavDrawerAdapter(LauncherActivity.this, R.layout.drawer_list_item, dmh.getStuff(), -99);
        //mDrawerList.setAdapter(mDrawerAdapter);

        // refresh testo
        setHeadInfo();
        setDbAndFavouritesInfo();
        setServiceInfo();
        setWebServiceInfo();
        setAntiTheftInfo();
        if (opzioni.isSoulissIpConfigured() && opzioni.isDataServiceEnabled())
            serviceInfoFoot.setText(Html.fromHtml("<b>" + getString(R.string.waiting) + "</b> "));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
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
                Intent myIntents = new Intent(LauncherActivity.this, ManualUDPTestActivity.class);
                LauncherActivity.this.startActivity(myIntents);
                return true;
            case R.id.Esci:
                super.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setHeadInfo() {
        setActionBarInfo(getString(R.string.app_name));
        //basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_green));
        // check se IP non settato check system configured
        if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissPublicIpConfigured()) {
            basinfo.setText(Html.fromHtml(getString(R.string.notconfigured)));
            //basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_red));
            return;
        }
        if (!opzioni.getCustomPref().contains("connectionName")) {
            basinfo.setText(getString(R.string.warn_connection));
            //basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_yellow));
            return;
        }
        if (!opzioni.isSoulissPublicIpConfigured()
                && !("WIFI".compareTo(opzioni.getCustomPref().getString("connectionName", "")) == 0)) {
            basinfo.setText(Html.fromHtml(getString(R.string.warn_wifi)));
            //	basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_red));
            return;
        }
        String base = opzioni.getAndSetCachedAddress();
        Log.d(TAG, "cached Address: " + base+ " backoff: "+ opzioni.getBackoff());
        if (base != null && "".compareTo(base) != 0) {
            basinfo.setText(Html.fromHtml(getString(R.string.contact_at) + "<font color=\"#99CC00\"><b> " + base
                    + "</b></font> via <b>" + opzioni.getCustomPref().getString("connectionName", "ERROR") + "</b>"));
        } else if (base != null && getString(R.string.unavailable).compareTo(base) != 0) {
            basinfo.setText(getString(R.string.souliss_unavailable));
        } else {
            basinfo.setText(getString(R.string.contact_progress));
        }

    }

    private void setDbAndFavouritesInfo() {

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
            db.open();
            dbwarn.setText(getString(R.string.db_size) + ": " + db.getSize() + "B");
            dbwarn.setVisibility(View.VISIBLE);
            dbwarnline.setVisibility(View.GONE);
            //FAVOURITES
            final int favCount = db.countFavourites();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final TextView textViewFav = (TextView) findViewById(R.id.textViewFav);
                    final TextView textViewFav2 = (TextView) findViewById(R.id.textViewFav2);
                    final LinearLayout tagCont = (LinearLayout) findViewById(R.id.tagCont);
                    tags = db.getTags(LauncherActivity.this);
                    if (tags.size() > 1 || favCount > 0) {//1 di sicuro

                        try {
                            Thread.sleep(1500);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    cardViewFav.setVisibility(View.VISIBLE);
                                    tagCont.removeAllViews();
                                    String strMeatFormat = LauncherActivity.this.getString(R.string.tag_info_format);
                                    textViewFav.setText(String.format(strMeatFormat, db.countTypicalTags(), db.countTags()));
                                    textViewFav2.setText(getString(R.string.typical) + " Marked as Favourites:" + db.countFavourites());
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        for (final SoulissTag tag : tags) {
                            final ListButton turnOffButton = new ListButton(LauncherActivity.this);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    turnOffButton.setText(tag.getName());
                                    OnClickListener ssc = new OnClickListener() {
                                        public void onClick(View v) {
                                            Intent myIntent = new Intent(LauncherActivity.this, TagDetailActivity.class);
                                            myIntent.putExtra("TAG", (long) tag.getTagId());
                                            LauncherActivity.this.startActivity(myIntent);
                                            return;
                                        }
                                    };
                                    turnOffButton.setOnClickListener(ssc);
                                    tagCont.addView(turnOffButton);
                                }
                            });

                        }

                    }
                }
            }).start();
        }
    }

    private void setAntiTheftInfo() {
        if (opzioni.isAntitheftPresent()) {
            serviceInfoAntiTheft.setVisibility(View.VISIBLE);
            db.open();
            try {
                SoulissTypical41AntiTheft at = db.getAntiTheftMasterTypical();
                serviceInfoAntiTheft.setText(Html.fromHtml("<b>" + getString(R.string.antitheft_status) + "</b> "
                        + at.getOutputDesc()));
            } catch (Exception e) {
                Log.e(TAG, "cant set ANTITHEFT info: " + e.getMessage());
            }
        }
    }

    private void setServiceInfo() {
        StringBuilder sb = new StringBuilder();
        serviceinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_green));
        /* SERVICE MANAGEMENT */
        if (!opzioni.isDataServiceEnabled()) {
            if (mIsBound && mBoundService != null) {// in esecuzione? strano
                sb.append("<br/><b>").append(getResources().getString(R.string.service_disabled)).append("!</b> ");
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
                sb.append("<b>").append(getString(R.string.service_lastexec)).append("</b> ").append(Constants.getTimeAgo(mBoundService.getLastupd())).append("<br/><b>");
                sb.append(getString(R.string.opt_serviceinterval) + ":</b> "
                        + Constants.getScaledTime(opzioni.getDataServiceIntervalMsec() / 1000));
            } else {
                sb.append(getString(R.string.service_warnbound));
                Intent serviceIntent = new Intent(this, SoulissDataService.class);
                Log.w(TAG, "Service not bound yet, restarting");
                startService(serviceIntent);
                serviceinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_yellow));
            }
        }
        serviceInfo.setText(Html.fromHtml(sb.toString()));
    }

    private void setWebServiceInfo() {
        StringBuilder sb = new StringBuilder();
        // webserviceInfo.setBackgroundColor(this.getResources().getColor(R.color.std_green));
        /* SERVICE MANAGEMENT */
        if (!opzioni.isWebserverEnabled()) {
            if (mIsWebBound && mBoundWebService != null) {
                // in esecuzione? strano
                mBoundWebService.stopSelf();
            }
            webserviceInfo.setVisibility(View.GONE);
        } else {
            webserviceInfo.setVisibility(View.VISIBLE);
            if (mIsWebBound && mBoundWebService != null) {
                sb.append(getString(R.string.webservice_enabled));
                sb.append(NetUtils.getLocalIpAddress()).append(":");
                sb.append(mBoundWebService.getPort());
                webServiceInfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_green));
            } else {
                sb.append(getString(R.string.service_warnbound));
                Intent serviceIntent = new Intent(this, HTTPService.class);
                Log.w(TAG, "WEB Service not bound yet, restarting");
                startService(serviceIntent);
                webServiceInfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_yellow));
            }
        }
        webserviceInfo.setText(Html.fromHtml(sb.toString()));
    }

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
                        //setDbAndFavouritesInfo();
                        setServiceInfo();
                        setWebServiceInfo();
                        setAntiTheftInfo();
                    }
                });
            }
            // UI updates every 5 secs.
        }, 100, Constants.GUI_UPDATE_INTERVAL * opzioni.getBackoff());
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
    public void onLocationChanged(Location location) {
        final double lat = (location.getLatitude());
        final double lng = (location.getLongitude());

        new Thread(new Runnable() {
            @Override
            public void run() {
                String adString = "";
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
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "Geocoder ERROR", e);
                            homedist.setVisibility(View.VISIBLE);
                            homedist.setText(Html.fromHtml("Geocoder <font color=\"#FF4444\">ERROR</font>: " + e.getMessage()));
                            posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_red));
                        }
                    });
                    loc = Constants.gpsDecimalFormat.format(lat) + " : " + Constants.gpsDecimalFormat.format(lng);
                }
                final String ff = loc;
                final String sonoIncapace = adString;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        coordinfo.setVisibility(View.VISIBLE);
                        coordinfo.setText(Html.fromHtml(getString(R.string.positionfrom) + " <b>" + provider + "</b>: " + ff
                                + sonoIncapace));
                    }
                });
                final float[] res = new float[3];
                // Location.distanceBetween(lat, lng, 44.50117265d, 11.34518103, res);
                Location.distanceBetween(lat, lng, opzioni.getHomeLatitude(), opzioni.getHomeLongitude(), res);
                if (opzioni.getHomeLatitude() != 0) {
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
                            homedist.setVisibility(View.VISIBLE);
                            homedist.setText(Html.fromHtml("<b>" + getString(R.string.homedist) + "</b> "
                                    + (int) res[0] + unit
                                    + (ff == null ? "" : " (" + getString(R.string.currentlyin) + " " + ff + ")")));
                            posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_green));
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            homedist.setText(Html.fromHtml(getString(R.string.homewarn)));
                            posInfoLine.setBackgroundColor(getResources().getColor(R.color.std_yellow));
                        }
                    });
                }
            }
        }).start();
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

    private void initLocationProvider() {
        // criteria.setAccuracy(Criteria.ACCURACY_HIGH);
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
                onLocationChanged(location);
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
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        doUnbindWebService();
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
}
