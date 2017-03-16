package it.angelic.soulissclient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import it.angelic.soulissclient.drawer.DrawerItemClickListener;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.net.UDPHelper;

import static it.angelic.soulissclient.Constants.TAG;

public class ManualUDPTestActivity extends AbstractStatusedFragmentActivity {
	private SoulissPreferenceHelper opzioni;
	private SoulissDBHelper datasource;
	private Button refreshButton;
	private Button stateRequestButton;
    private Button healthButton;
    private Button GoButt;
	private Handler timeoutHandler;
	private TextView errorText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		// tema
		opzioni = SoulissApp.getOpzioni();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_manualudptest);

		// DB
		datasource = new SoulissDBHelper(this);
		SoulissDBHelper.open();
		timeoutHandler = new Handler();
		// getWindow().setFormat(PixelFormat.RGBA_8888);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		refreshButton = (Button) findViewById(R.id.refreshButton);
		stateRequestButton = (Button) findViewById(R.id.resetButton);
        Button typreqButton = (Button) findViewById(R.id.typreqButton);
        healthButton = (Button) findViewById(R.id.healthreqButton);
		GoButt = (Button) findViewById(R.id.buttonForce);
		errorText = (TextView) findViewById(R.id.textOutputError);

		final Spinner idspinner = (Spinner) findViewById(R.id.spinner1);
		final Spinner slotspinner = (Spinner) findViewById(R.id.spinner2);
		final EditText editCmd = (EditText) findViewById(R.id.editText1);

		SoulissApp.setBackground(findViewById(R.id.container), getWindowManager());

        // DRAWER
        final TextView info1 = (TextView) findViewById(R.id.textViewDrawerInfo1);
        final TextView info2 = (TextView) findViewById(R.id.textViewDrawerInfo2);
		dmh = new DrawerMenuHelper(ManualUDPTestActivity.this);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.warn_wifi, /* "open drawer" description */
                R.string.warn_wifi /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                ActivityCompat.invalidateOptionsMenu(ManualUDPTestActivity.this);
                //TODO settext
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                ActivityCompat.invalidateOptionsMenu(ManualUDPTestActivity.this);
				info2.setText(getString(R.string.souliss_app_name) + " " + (opzioni.isSoulissReachable() ? getString(R.string.Online) : getString(R.string.offline)));
				info1.setText("Souliss can control "+opzioni
                        .getCustomPref().getInt("numTipici", 0)+" Things");
            }
        };
        mDrawerLinear = (LinearLayout)findViewById(R.id.left_drawer_linear);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        ArrayAdapter<INavDrawerItem> mAdapter = new NavDrawerAdapter(ManualUDPTestActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.SETTINGS_UDPTEST);
        mDrawerList.setAdapter(mAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(this, mDrawerList, mDrawerLayout, mDrawerLinear));
		/**
		 * Aggiorna le tabelle dei tipici
		 */
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				Thread t = new Thread() {
					public void run() {
						String cached = opzioni.getAndSetCachedAddress();
						final StringBuilder ret = new StringBuilder();
						if (cached == null) {
							cached = opzioni.isSoulissPublicIpConfigured() ? opzioni.getIPPreferencePublic() : opzioni
									.getPrefIPAddress();
						}
						try {
							ret.append(UDPHelper.ping(opzioni.getPrefIPAddress(), cached, opzioni.getUserIndex(), opzioni.getNodeIndex(), opzioni).getHostAddress());
						} catch (Exception e) {
							Log.e(Constants.TAG, "UDP test error:"+e.getMessage(),e);
						}

						refreshButton.post(new Runnable() {
							public void run() {
								errorText.setVisibility(View.GONE);
								refreshButton.setEnabled(true);
								// svuota la tabella e mette feedback
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								txt.setText(Constants.hourFormat.format(new Date()) + ": Ping sent to "+ret.toString());
							}
						});
					}
				};

				t.start();

			}
		});
		/**
		 * Manda RESET a Souliss
		 */
		stateRequestButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				Thread t = new Thread() {
					public void run() {
						UDPHelper.pollRequest(opzioni, opzioni.getCustomPref().getInt("numNodi", 1), 0);

						stateRequestButton.post(new Runnable() {
							public void run() {
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));

								txt.setText(Constants.hourFormat.format(new Date()) + ": poll request sent");
								errorText.setVisibility(View.GONE);
							}
						});
					}
				};

				t.start();

			}
		});
		/**
		 * Manda TYPICAL Request a Souliss
		 */
		typreqButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				final int numof = opzioni.getCustomPref().getInt("numNodi", 1);

				Thread t = new Thread() {
					public void run() {
						UDPHelper.typicalRequest(opzioni, numof, 0);

						stateRequestButton.post(new Runnable() {
							public void run() {
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));

								txt.setText(Constants.hourFormat.format(new Date())
										+ ": typical Request sent, 1 node starting from 0");
								errorText.setVisibility(View.GONE);
							}
						});
					}
				};

				t.start();

			}
		});

		/**
		 * Manda HEALTH Request a Souliss
		 */
		healthButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				Thread t = new Thread() {
					public void run() {
						UDPHelper.healthRequest(opzioni, 1, 0);
						// final LinearLayout stat = (LinearLayout)
						// findViewById(R.id.linearLayoutOutput);

						healthButton.post(new Runnable() {
							public void run() {
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));
								errorText.setVisibility(View.GONE);
								txt.setText(Constants.hourFormat.format(new Date())
										+ ": Health Request sent, 1 node starting from 0");
								errorText.setVisibility(View.GONE);
							}
						});
					}
				};

				t.start();

			}
		});
		/**
		 * Invia un comando
		 */
		GoButt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);
				GoButt.setEnabled(false);
				if (idspinner.getSelectedItem().toString().length() < 1
						|| slotspinner.getSelectedItem().toString().length() < 1
						|| editCmd.getText().toString().length() < 1) {
					Toast it2 = Toast.makeText(ManualUDPTestActivity.this,
							getResources().getString(R.string.inserire_almeno), Toast.LENGTH_SHORT);
					it2.show();
					GoButt.setEnabled(true);
					return;
				}

				Thread t = new Thread() {
					public void run() {
						// ISSUE SOULISS UDP command
						final String cmdOutput = UDPHelper.issueSoulissCommand(idspinner.getSelectedItem().toString(),
								slotspinner.getSelectedItem().toString(), opzioni,  editCmd
										.getText().toString());
						//final LinearLayout OutputLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutOutput);

						GoButt.post(new Runnable() {
							public void run() {
								// Refresh Output area
								GoButt.setEnabled(true);
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));
								errorText.setVisibility(View.GONE);
								txt.setText(Constants.hourFormat.format(new Date()) + "Command sent to: "
										+ opzioni.getPrefIPAddress() + " - " + cmdOutput);
							}
						});
					}
				};

				t.start();

			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();

        setActionBarInfo(getString(R.string.menu_test_udp));

		opzioni = SoulissApp.getOpzioni();
		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
			refreshButton.setEnabled(false);
			stateRequestButton.setEnabled(false);
			GoButt.setEnabled(false);
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
			alert.show();
		}
		// check rete disponibile
		else if (!opzioni.isSoulissReachable()) {
			Toast it = Toast.makeText(ManualUDPTestActivity.this, getString(R.string.souliss_unavailable),
					Toast.LENGTH_LONG);
			it.show();
		} else {
			refreshButton.setEnabled(true);
			stateRequestButton.setEnabled(true);
			GoButt.setEnabled(true);
		}

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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manual_menu, menu);
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
			Intent settingsActivity = new Intent(getBaseContext(), PreferencesActivity.class);
			startActivity(settingsActivity);
			return true;

		case R.id.Esci:
			super.finish();
		}

		return super.onOptionsItemSelected(item);
	}

	// Aggiorna il feedback
	private BroadcastReceiver macacoRawDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (extras != null && extras.get("MACACO") != null) {
				ArrayList<Short> vers = (ArrayList<Short>) extras.get("MACACO");
				Log.d(TAG, "Broadcast RAW DATA: " + vers);
				timeoutHandler.removeCallbacks(timeExpired);
				TextView ito = (TextView) findViewById(R.id.textOutputError);
				ito.setVisibility(View.GONE);
				// final LinearLayout OutputLinearLayout = (LinearLayout)
				// findViewById(R.id.linearLayoutOutput);
				final TextView OutputLinearT = (TextView) findViewById(R.id.textOutput1);
				// OutputLinearLayout.removeViewAt(0);
				// TextView ito = new TextView(getApplicationContext());

				StringBuilder dump = new StringBuilder();
				for (int ig = 0; ig < vers.size(); ig++) {
					// 0xFF & buf[index]
					dump.append("0x").append(Long.toHexString(vers.get(ig))).append(" ");
					// dump.append(":"+packet.getData()[ig]);
				}
				if (opzioni.isLightThemeSelected())
					OutputLinearT.setTextColor(getResources().getColor(R.color.black));
				// aggiorna feedback
				String faker = OutputLinearT.getText().toString();
				OutputLinearT.setText(faker
						+ "\n"
						+ Html.fromHtml(Constants.hourFormat.format(new Date())
								+ ": Reply <font color=\"#99CC00\">received</font> - " + dump.toString()));

				// OutputLinearLayout.addView(ito);
				GoButt.setEnabled(true);

			} else {
				Log.e(TAG, "EMPTY response ( extras.get(\"MACACO\"))!!");
			}
		}
	};
	Runnable timeExpired = new Runnable() {

		@Override
		public void run() {
			// final LinearLayout OutputLinearLayout = (LinearLayout)
			// findViewById(R.id.linearLayoutOutput);

			Log.e(TAG, "TIMEOUT!!!");
			if (opzioni.isLightThemeSelected())
				errorText.setTextColor(getResources().getColor(R.color.black));

			errorText.setText(Html.fromHtml(Constants.hourFormat.format(new Date())
					+ ": Command timeout <b><font color=\"#FF4444\">expired</font></b>, no reply received "));
			// OutputLinearLayout.addView(ito);
			// Toast.makeText(LauncherActivity.this, "Request failed" +
			// provider, Toast.LENGTH_SHORT).show();
			errorText.setVisibility(View.VISIBLE);
		}
	};
	// meccanismo per network detection
	private BroadcastReceiver timeoutReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.w(TAG, "Posting timeout from " + intent.toString());
			Bundle extras = intent.getExtras();
			int delay = extras.getInt("REQUEST_TIMEOUT_MSEC");
			timeoutHandler.postDelayed(timeExpired, delay);
		}
	};

	@Override
	protected void onResume() {
		// IDEM, serve solo per reporting
		IntentFilter filtere = new IntentFilter();
		filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		registerReceiver(macacoRawDataReceiver, filtere);

		// this is only used for refresh UI
		IntentFilter filtera = new IntentFilter();
		filtera.addAction(Constants.CUSTOM_INTENT_SOULISS_TIMEOUT);
		registerReceiver(timeoutReceiver, filtera);

		SoulissDBHelper.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		datasource.close();
		unregisterReceiver(timeoutReceiver);
		unregisterReceiver(macacoRawDataReceiver);
		super.onPause();
	}
}