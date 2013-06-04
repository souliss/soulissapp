package it.angelic.soulissclient.fragments;

import static it.angelic.soulissclient.Constants.TAG;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.LauncherActivity;
import it.angelic.soulissclient.NodeDetailActivity;
import it.angelic.soulissclient.PreferencesActivity;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.adapters.NodesListAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
public class NodesListFragment extends ListFragment {
	private SoulissNode[] nodiArray;
	SoulissPreferenceHelper opzioni;
	private SoulissDBHelper datasource;
	private NodesListAdapter nodesAdapter;
	int mCurCheckPosition = 0;

	private Timer autoUpdate;

	private TextView tt;
	private boolean mDualPane;
	private TextView textHeadListInfo;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		View detailsFrame = getActivity().findViewById(R.id.details);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_nodelist, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		opzioni = SoulissClient.getOpzioni();
		setHasOptionsMenu(true);
		opzioni.reload();
		// Remove title bar
		// tema
		if (opzioni.isLightThemeSelected())
			getActivity().setTheme(R.style.LightThemeSelector);
		else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onActivityCreated(savedInstanceState);
		// getActivity().setContentView(R.layout.frag_nodelist);
		getActivity().setTitle(getString(R.string.app_name) + " - " + getString(R.string.nodes));

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
		}
		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.details);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

		ImageView nodeic = (ImageView) getActivity().findViewById(R.id.scene_icon);
		tt = (TextView) getActivity().findViewById(R.id.TextViewTypicals);
		textHeadListInfo = (TextView) getActivity().findViewById(R.id.TextViewManualDesc);
		if ("def".compareToIgnoreCase(opzioni.getPrefFont()) != 0) {
			Typeface font = Typeface.createFromAsset(getActivity().getAssets(), opzioni.getPrefFont());
			tt.setTypeface(font, Typeface.NORMAL);
			// titolo.setTypeface(font, Typeface.NORMAL);
		}

		SoulissClient.setBackground((RelativeLayout) getActivity().findViewById(R.id.containerlista),
				getActivity().getWindowManager());

		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured()) {
			// refreshButton.setEnabled(false);
			// GoButt.setEnabled(false);
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(getActivity());
			alert.show();
		}
		datasource = new SoulissDBHelper(getActivity());
		datasource.open();

		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(getActivity());
		} else {

			final List<SoulissNode> goer = datasource.getAllNodes();
			nodiArray = new SoulissNode[goer.size()];
			nodiArray = goer.toArray(nodiArray);

			getListView().setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Log.i(TAG, "Showing Node Detail:" + arg2);
					// Activity Dettaglio nodo
					showDetails(arg2);
				}
			});
			registerForContextMenu(getListView());
			if (mDualPane) {
				// Make sure our UI is in the correct state.
				showDetails(mCurCheckPosition);
				textHeadListInfo.setVisibility(View.GONE);
				nodeic.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 */
	private void showDetails(int index) {
		mCurCheckPosition = index;

		if (mDualPane) {
			//getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.
			//getListView().setItemChecked(index, true);
			// Check what fragment is currently shown, replace if needed.
			// Fragment details = (Fragment)
			// getFragmentManager().findFragmentById(R.id.details);
			// if (details == null)
			// Istanzia e ci mette l'indice
			NodeDetailFragment details = NodeDetailFragment.newInstance(index, nodiArray[index]);

			// Execute a transaction, replacing any existing fragment
			// with this one inside the frame.
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			if (opzioni.isAnimationsEnabled())
				ft.setCustomAnimations(R.anim.slide_in_left_delay, R.anim.slide_out_left);
			ft.replace(R.id.details, details);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			ft.commit();

		} else {
			// Otherwise alla vecchia
			// the dialog fragment with selected text.
			Intent intent = new Intent();
			intent.setClass(getActivity(), NodeDetailActivity.class);
			intent.putExtra("index", index);
			intent.putExtra("NODO", (SoulissNode) nodiArray[index]);
			startActivity(intent);
			if (opzioni.isAnimationsEnabled())
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurCheckPosition);
	}

	@Override
	public void onStart() {
		super.onStart();
		opzioni.reload();
		// doBindService();
		datasource.open();
		// prendo tipici dal DB
		final List<SoulissNode> goer = datasource.getAllNodes();
		nodiArray = new SoulissNode[goer.size()];
		nodiArray = goer.toArray(nodiArray);

		Log.i(TAG, "mostro numnodi:" + goer.size());
		// final TextView shootTextView = (TextView)
		// findViewById(R.id.TextViewListaTitle);
		// shootTextView.setText(strMeatMsg);

		nodesAdapter = new NodesListAdapter(getActivity().getApplicationContext(), nodiArray, opzioni);
		// Adapter della lista
		// setListAdapter(nodesAdapter);
		getListView().setAdapter(nodesAdapter);
		getListView().invalidateViews();

	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Broadcast received, refresh from DB");
			datasource.open();
			// i nuovi programmi sono gia sul DB
			// prendo tipici dal DB
			try {
				List<SoulissNode> goer = datasource.getAllNodes();
				nodiArray = new SoulissNode[goer.size()];
				nodiArray = goer.toArray(nodiArray);
				nodesAdapter.setNodes(nodiArray);
				nodesAdapter.notifyDataSetChanged();
				getListView().invalidateViews();
			} catch (IllegalStateException e) {
				Log.e(TAG, "DB read Impossible", e);
			}
		}
	};

	

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		// Rinomina nodo e scelta icona
		inflater.inflate(R.menu.node_ctx_menu, menu);
		Log.w(TAG, "inflate");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		nodesAdapter = (NodesListAdapter) getListView().getAdapter();
		long arrayAdapterPosition = info.position;
		final SoulissNode todoItem = (SoulissNode) nodesAdapter.getItem((int) arrayAdapterPosition);

		switch (item.getItemId()) {
		case R.id.rinominaNodo:
			AlertDialog.Builder alert = AlertDialogHelper.renameSoulissObjectDialog(getActivity(), null, getListView(),
					datasource, todoItem);
			alert.show();
			// nodesAdapter.notifyDataSetChanged();
			// listaNodiView.invalidateViews();
			return true;
		case R.id.changenodeicon:
			SoulissNode convertView = (SoulissNode) getListView().getItemAtPosition(item.getOrder());
			ImageView at = new ImageView(getActivity().getApplicationContext());
			at.setImageResource(convertView.getDefaultIconResourceId());
			AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(getActivity(), at, getListView(),
					datasource, todoItem);
			alert2.show();
			// nodesAdapter.notifyDataSetChanged();
			// listaNodiView.invalidateViews();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onPause() {
		autoUpdate.cancel();
		super.onPause();

		getActivity().unregisterReceiver(datareceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction("it.angelic.soulissclient.GOT_DATA");
		filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		getActivity().registerReceiver(datareceiver, filtere);
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						try {
							getListView().invalidateViews();
						} catch (Exception e) {
							Log.e(Constants.TAG, "InvalidateViews fallita");
						}

					}
				});
			}
		}, 2500, Constants.GUI_UPDATE_INTERVAL); // updates UI each 5 secs
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// doUnbindService();
		//datasource.close();
	}

}
