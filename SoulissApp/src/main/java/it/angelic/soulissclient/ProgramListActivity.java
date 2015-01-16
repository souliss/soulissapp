package it.angelic.soulissclient;

import static it.angelic.soulissclient.Constants.TAG;
import it.angelic.soulissclient.adapters.ProgramListAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.drawer.DrawerItemClickListener;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.model.SoulissCommand;

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;


/**
 * Activity per mostrare una lista di risultati (Nodi Souliss) questa modalita`
 * e` manuale, ovvero l'utente interagisce direttamente coi tipici
 * 
 * dalla lista dei tipici si accede TypicalsActivity
 * 
 * legge dal DB tipici e tipici
 * 
 * @author Ale
 * 
 */
public class ProgramListActivity extends AbstractStatusedFragmentActivity {
	private SoulissCommand[] programsArray;
	private ListView listaProgrammiView;
	private SoulissDBHelper datasource;
	private ProgramListAdapter progsAdapter;
	private TextView tt;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	// private CharSequence mTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerMenuHelper dmh;
	private ArrayAdapter<INavDrawerItem> drawerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_programs);
		setTitle(getString(R.string.app_name) + " - " + getString(R.string.programs_title));
		tt = (TextView) findViewById(R.id.TextViewTypicals);

		listaProgrammiView = (ListView) findViewById(R.id.ListViewListaProgs);
		SoulissClient.setBackground((RelativeLayout) findViewById(R.id.containerlistaProgrammi), getWindowManager());

		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
			alert.show();
		}
		datasource = new SoulissDBHelper(this);

		// DRAWER
		dmh = new DrawerMenuHelper();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.string.warn_wifi, /* "open drawer" description */
		R.string.warn_wifi /* "close drawer" description */
		) {
			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				ActivityCompat.invalidateOptionsMenu(ProgramListActivity.this);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				ActivityCompat.invalidateOptionsMenu(ProgramListActivity.this);
			}
		};
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(true);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		drawerAdapter = new NavDrawerAdapter(ProgramListActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.PROGRAMS);
		mDrawerList.setAdapter(drawerAdapter);
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener(this, mDrawerList, mDrawerLayout));

		listaProgrammiView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.w(TAG, "press not implemented?" + arg2);
			}
		});

		registerForContextMenu(listaProgrammiView);

        SoulissClient.setBackground((LinearLayout) findViewById(R.id.containerlistaScenes), getWindowManager());


	}

	@Override
	protected void onStart() {
		super.onStart();
		setActionBarInfo(getString(R.string.programs_title));
		datasource.open();
		opzioni.initializePrefs();
		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(this);
		}
		if (!opzioni.isDataServiceEnabled()) {
			AlertDialogHelper.serviceNotActiveDialog(this);
		}
		// prendo comandi dal DB, setto adapter
		LinkedList<SoulissCommand> goer = datasource.getUnexecutedCommands(this);
		if (goer.size() == 0)
			tt.setText(getString(R.string.programs_no));
		programsArray = new SoulissCommand[goer.size()];
		programsArray = goer.toArray(programsArray);
		progsAdapter = new ProgramListAdapter(this.getApplicationContext(), programsArray,
				datasource.getTriggerMap(this), opzioni);
		// Adapter della lista
		listaProgrammiView.setAdapter(progsAdapter);
		listaProgrammiView.invalidateViews();
		// forza refresh drawer
		drawerAdapter = new NavDrawerAdapter(ProgramListActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.PROGRAMS);
		mDrawerList.setAdapter(drawerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listaProgrammiView);
        //ADD NEW PROGRAM Listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntentt = new Intent(ProgramListActivity.this, AddProgramActivity.class);
                ProgramListActivity.this.startActivityForResult(myIntentt, 12);
            }
        });

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "new command created, resultCode = " + resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			int msg = data.getIntExtra("returnedData", -1);
			if (resultCode == RESULT_OK && requestCode == 12) {
				switch (msg) {
				case Constants.COMMAND_TIMED:
					Toast.makeText(this, getString(R.string.command_inserted), Toast.LENGTH_SHORT).show();
					break;
				case Constants.COMMAND_COMEBACK_CODE:
					// fall throught
				case Constants.COMMAND_GOAWAY_CODE:
					Toast.makeText(this, getString(R.string.command_inserted), Toast.LENGTH_SHORT).show();
					break;
				case Constants.COMMAND_TRIGGERED:
					Toast.makeText(this, getString(R.string.command_inserted), Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(this, getString(R.string.command_inserted_fail), Toast.LENGTH_LONG).show();
					break;
				}
			} else {
				Toast.makeText(this, getString(R.string.command_inserted_fail), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		android.view.MenuInflater inflater = getMenuInflater();
		// Rinomina nodo e scelta icona
		inflater.inflate(R.menu.programs_ctx_menu, menu);
		Log.w(TAG, "inflate");
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		ProgramListAdapter ada = (ProgramListAdapter) listaProgrammiView.getAdapter();

		long arrayAdapterPosition = info.position;
		final SoulissCommand todoItem = (SoulissCommand) ada.getItem((int) arrayAdapterPosition);

		switch (item.getItemId()) {
		case R.id.elimina:
			AlertDialog.Builder alert = AlertDialogHelper.removeCommandDialog(this, listaProgrammiView, datasource,
					todoItem);
			alert.show();

			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			List<SoulissCommand> goer = datasource.getUnexecutedCommands(ProgramListActivity.this);
			programsArray = new SoulissCommand[goer.size()];
			int q = 0;
			for (SoulissCommand object : goer) {
				programsArray[q++] = object;
			}

			progsAdapter = new ProgramListAdapter(ProgramListActivity.this.getApplicationContext(), programsArray,
					datasource.getTriggerMap(ProgramListActivity.this), opzioni);
			// Adapter della lista
			listaProgrammiView.setAdapter(progsAdapter);
			listaProgrammiView.invalidateViews();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.programslist_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
			return true;//cliccato sul drawer, non far altro
		}
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, LauncherActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.Opzioni:
			Intent settingsActivity = new Intent(getBaseContext(), PreferencesActivity.class);
			startActivity(settingsActivity);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(datareceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction("it.angelic.soulissclient.GOT_DATA");
		registerReceiver(datareceiver, filtere);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		datasource.close();
	}

}
