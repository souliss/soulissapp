package it.angelic.soulissclient;

import static it.angelic.soulissclient.Constants.TAG;
import it.angelic.soulissclient.adapters.ProgramListAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;

import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
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
public class ProgramListActivity extends SherlockActivity {
	private SoulissCommand[] programsArray;
	SoulissPreferenceHelper opzioni;
	private ListView listaProgrammiView;
	private SoulissDBHelper datasource;
	private ProgramListAdapter progsAdapter;
	private TextView tt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = new SoulissPreferenceHelper(this.getApplicationContext());
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_programs);
		setTitle(getString(R.string.app_name) + " - " + getString(R.string.programs_title));
		tt = (TextView) findViewById(R.id.TextViewTypicals);
		if ("def".compareToIgnoreCase(opzioni.getPrefFont()) != 0) {
			Typeface font = Typeface.createFromAsset(getAssets(), opzioni.getPrefFont());
			tt.setTypeface(font, Typeface.NORMAL);
		}
		//tt.setTextSize(TypedValue.COMPLEX_UNIT_SP, opzioni.getListDimensTesto() + tt.getTextSize());

		//ImageView nodeic = (ImageView) findViewById(R.id.scene_icon);
		//nodeic.setAlpha(150);

		listaProgrammiView = (ListView) findViewById(R.id.ListViewListaProgs);
		
		SoulissClient.setBackground((RelativeLayout) findViewById(R.id.containerlistaProgrammi), getWindowManager());

		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured() ) {
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
			alert.show();
		}
		datasource = new SoulissDBHelper(this);

		listaProgrammiView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.w(TAG, "press not implemented?" + arg2);
			}
		});
		

		registerForContextMenu(listaProgrammiView);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onStart() {
		super.onStart();
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = this.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		//opzioni = new SoulissPreferenceHelper(this.getApplicationContext());
		datasource.open();
		opzioni.initializePrefs();
		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(this);
		}
		if (!opzioni.isDataServiceEnabled()){
			AlertDialogHelper.serviceNotActiveDialog(this);
		}
		// prendo comandi dal DB, setto adapter
		LinkedList<SoulissCommand> goer = datasource.getUnexecutedCommands(this);
		if (goer.size() ==0)
			tt.setText(getString(R.string.programs_no));
		programsArray = new SoulissCommand[goer.size()];
		programsArray = goer.toArray(programsArray);
		progsAdapter = new ProgramListAdapter(this.getApplicationContext(), programsArray, datasource.getTriggerMap(this), opzioni);
		// Adapter della lista
		listaProgrammiView.setAdapter(progsAdapter);
		listaProgrammiView.invalidateViews();

	}

	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"new command created, resultCode = "+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
        int msg = data.getIntExtra("returnedData", -1);
        if(resultCode==RESULT_OK && requestCode==12){
        	switch (msg) {
			case Constants.COMMAND_TIMED:
				Toast.makeText(this, "Timed Command inserted", Toast.LENGTH_SHORT).show();
				break;
			case Constants.COMMAND_COMEBACK_CODE:	
			case Constants.COMMAND_GOAWAY_CODE:	
				Toast.makeText(this, "Positional Command inserted", Toast.LENGTH_SHORT).show();
				break;
			case Constants.COMMAND_TRIGGERED:
				Toast.makeText(this, "Triggered Command inserted", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(this, "Insertion Failed", Toast.LENGTH_LONG).show();
				break;
			}
        }
        else{
            Toast.makeText(this, "Insertion Failed", Toast.LENGTH_LONG).show();
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
			AlertDialog.Builder alert = AlertDialogHelper
					.removeCommandDialog(this, listaProgrammiView, datasource, todoItem);
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

			progsAdapter = new ProgramListAdapter(ProgramListActivity.this.getApplicationContext(), programsArray, datasource.getTriggerMap(ProgramListActivity.this), opzioni);
			// Adapter della lista
			listaProgrammiView.setAdapter(progsAdapter);
			listaProgrammiView.invalidateViews();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.programslist_menu, menu);
		return true;
	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
		case R.id.AddProgram:
			Intent myIntentt = new Intent(ProgramListActivity.this, AddProgramActivity.class);
			ProgramListActivity.this.startActivityForResult(myIntentt, 12);
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
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		datasource.close();
	}

}
