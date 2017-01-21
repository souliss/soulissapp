package it.angelic.soulissclient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import it.angelic.soulissclient.adapters.ProgramListAdapter;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.db.SoulissDBHelper;

import static it.angelic.soulissclient.Constants.TAG;


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
    private List<SoulissCommand> programsArray;
    private ListView listaProgrammiView;
	private SoulissDBHelper datasource;
	private ProgramListAdapter progsAdapter;
	private TextView tt;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissApp.getOpzioni();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_programs);
		setTitle(getString(R.string.souliss_app_name) + " - " + getString(R.string.programs_title));
		tt = (TextView) findViewById(R.id.TextViewTypicals);

		listaProgrammiView = (ListView) findViewById(R.id.ListViewListaProgs);
		SoulissApp.setBackground(findViewById(R.id.containerlistaProgrammi), getWindowManager());

		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
			alert.show();
		}
		datasource = new SoulissDBHelper(this);

		super.initDrawer(this, DrawerMenuHelper.PROGRAMS);

		listaProgrammiView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.w(TAG, "Activating SCENE " + arg2);
                Intent nodeDatail = new Intent(ProgramListActivity.this, AddProgramActivity.class);
                nodeDatail.putExtra("PROG", programsArray.get(arg2));
                ProgramListActivity.this.startActivity(nodeDatail);
                if (opzioni.isAnimationsEnabled())
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});


		registerForContextMenu(listaProgrammiView);

        SoulissApp.setBackground(findViewById(R.id.containerlistaScenes), getWindowManager());


	}

	@Override
	protected void onStart() {
		super.onStart();
		setActionBarInfo(getString(R.string.programs_title));
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		SoulissDBHelper.open();
		opzioni.initializePrefs();
		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(this);
		}
		if (!opzioni.isDataServiceEnabled()) {
			AlertDialogHelper.serviceNotActiveDialog(this);
		}

		// forza refresh drawer
		mDrawermAdapter = new NavDrawerAdapter(ProgramListActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.PROGRAMS);
		mDrawerList.setAdapter(mDrawermAdapter);

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

        // prendo comandi dal DB, setto adapter
        programsArray = datasource.getUnexecutedCommands(this);
        if (programsArray.size() == 0)
            tt.setText(getString(R.string.programs_no));

        progsAdapter = new ProgramListAdapter(this, programsArray,
                datasource.getTriggerMap(this), opzioni);
        // Adapter della lista
        listaProgrammiView.setAdapter(progsAdapter);
        listaProgrammiView.invalidateViews();

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
            programsArray = datasource.getUnexecutedCommands(ProgramListActivity.this);

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
		return super.onCreateOptionsMenu(menu);
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
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, OldLauncherActivity.class);
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
