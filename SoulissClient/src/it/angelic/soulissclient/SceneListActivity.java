package it.angelic.soulissclient;

import static it.angelic.soulissclient.Constants.TAG;
import it.angelic.soulissclient.adapters.SceneListAdapter;
import it.angelic.soulissclient.adapters.SceneListAdapter.SceneViewHolder;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
import it.angelic.soulissclient.model.SoulissScene;

import java.util.LinkedList;
import java.util.List;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class SceneListActivity extends AbstractStatusedFragmentActivity {
	private SoulissScene[] scenesArray;
	private ListView listaScenesView;
	private SoulissDBHelper datasource;
	private SceneListAdapter progsAdapter;
	private TextView tt;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		// Remove title bar
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.main_scenes);
		//final Button buttAddProgram = (Button) findViewById(R.id.buttonAddScene);
		tt = (TextView) findViewById(R.id.TextViewScenes);
		/*if ("def".compareToIgnoreCase(opzioni.getPrefFont()) != 0) {
			Typeface font = Typeface.createFromAsset(getAssets(), opzioni.getPrefFont());
			tt.setTypeface(font, Typeface.NORMAL);
		}*/
		
		listaScenesView = (ListView) findViewById(R.id.ListViewListaScenes);

		SoulissClient.setBackground((LinearLayout) findViewById(R.id.containerlistaScenes), getWindowManager());

		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
			alert.show();
		}

		// TODO error management
		datasource = new SoulissDBHelper(this);
		//datasource.open();

		listaScenesView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.w(TAG, "Activating SCENE " + arg2);
				Intent nodeDatail = new Intent(SceneListActivity.this, SceneDetailActivity.class);
				SceneViewHolder holder = (SceneViewHolder) arg1.getTag();
				nodeDatail.putExtra("SCENA", (SoulissScene) holder.data);
				SceneListActivity.this.startActivity(nodeDatail);
				if (opzioni.isAnimationsEnabled())
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		registerForContextMenu(listaScenesView);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setActionBarInfo(getString(R.string.app_name)+" - "+getString(R.string.scenes_title));
		opzioni.initializePrefs();
		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(this);
		}
		datasource.open();

		// prendo comandi dal DB, setto adapter
		LinkedList<SoulissScene> goer = datasource.getScenes(SoulissClient.getAppContext());
		scenesArray = new SoulissScene[goer.size()];
		scenesArray = goer.toArray(scenesArray);
		progsAdapter = new SceneListAdapter(this, scenesArray,opzioni);
		// Adapter della lista
		listaScenesView.setAdapter(progsAdapter);
		listaScenesView.invalidateViews();
		//ImageView nodeic = (ImageView) findViewById(R.id.scene_icon);
		//nodeic.setAlpha(150);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		android.view.MenuInflater inflater = getMenuInflater();
		// Rinomina nodo e scelta icona
		inflater.inflate(R.menu.scenes_ctx_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}


	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		SceneListAdapter ada = (SceneListAdapter) listaScenesView.getAdapter();

		long arrayAdapterPosition = info.position;
		final SoulissScene todoItem = (SoulissScene) ada.getItem((int) arrayAdapterPosition);

		switch (item.getItemId()) {
		case R.id.eseguiScena:
			ScenesDialogHelper.executeSceneDialog(SceneListActivity.this, todoItem,opzioni);
			return true;
		case R.id.eliminaScena:
			ScenesDialogHelper.removeSceneDialog(this, listaScenesView, datasource,
					todoItem, opzioni);
			return true;
		case R.id.rinominaScena:
			AlertDialog.Builder alert3 = AlertDialogHelper.renameSoulissObjectDialog(this, null, listaScenesView, datasource,
					todoItem);
			alert3.show();
			return true;
		case R.id.scegliconaScena:
			SoulissScene convertView = (SoulissScene) listaScenesView.getItemAtPosition(item.getOrder());
			ImageView at = new ImageView(getApplicationContext());
			at.setImageResource(convertView.getDefaultIconResourceId());
			AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(this, at, listaScenesView, datasource,
					todoItem);
			alert2.show();
			// nodesAdapter.notifyDataSetChanged();
			// listaNodiView.invalidateViews();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	
	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			List<SoulissScene> goer = datasource.getScenes(SceneListActivity.this);
			scenesArray = new SoulissScene[goer.size()];
			int q = 0;
			for (SoulissScene object : goer) {
				scenesArray[q++] = object;
			}

			progsAdapter = new SceneListAdapter(SceneListActivity.this.getApplicationContext(), scenesArray,opzioni);
			// Adapter della lista
			listaScenesView.setAdapter(progsAdapter);
			listaScenesView.invalidateViews();
			setActionBarInfo(getString(R.string.scenes_title));
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.scenelist_menu, menu);
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
		case R.id.AddScene:
			int rest = datasource.createOrUpdateScene(null);
			// prendo comandi dal DB, setto adapter
			LinkedList<SoulissScene> goer = datasource.getScenes(SoulissClient.getAppContext());
			scenesArray = new SoulissScene[goer.size()];
			scenesArray = goer.toArray(scenesArray);
			progsAdapter = new SceneListAdapter(SceneListActivity.this, scenesArray , opzioni);
			// Adapter della lista
			listaScenesView.setAdapter(progsAdapter);
			listaScenesView.invalidateViews();
			Toast.makeText(SceneListActivity.this,
					"Scene " + rest + " inserted, long-press to rename it and choose icon", Toast.LENGTH_LONG)
					.show();
			return true;
		case R.id.TestUDP:
			Intent myIntents = new Intent(SceneListActivity.this, ManualUDPTestActivity.class);
			SceneListActivity.this.startActivity(myIntents);
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
	}

}
