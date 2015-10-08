package it.angelic.soulissclient.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.angelic.soulissclient.AbstractStatusedFragmentActivity;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.NodeDetailActivity;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.adapters.NodesListAdapter;
import it.angelic.soulissclient.adapters.NodesListAdapter.NodeViewHolder;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;

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
public class NodesListFragment extends ListFragment {
	private SoulissNode[] nodiArray;
	private SoulissPreferenceHelper opzioni;
	private SoulissDBHelper datasource;
	private NodesListAdapter nodesAdapter;
	int mCurCheckPosition = 0;
	//private Handler timeoutHandler;
	private Timer autoUpdate;

	//private TextView tt;
	private boolean mDualPane;
	private TextView textHeadListInfo;
    private SwipeRefreshLayout swipeLayout;
  //  private Toolbar actionBar;

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		View detailsFrame = getActivity().findViewById(R.id.detailPane);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_nodelist, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		opzioni = SoulissApp.getOpzioni();
		/*ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
		actionBar.setCustomView(R.layout.custom_actionbar); // load your layout
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM); // show
		actionBar.setDisplayHomeAsUpEnabled(true);
*/

		setHasOptionsMenu(true);
		//opzioni.reload();
		// Remove title bar
		// tema
		if (opzioni.isLightThemeSelected())
			getActivity().setTheme(R.style.LightThemeSelector);
		else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onActivityCreated(savedInstanceState);
		// getActivity().setContentView(R.layout.frag_nodelist);
		//getActivity().setTitle(getString(R.string.app_name) + " - " + getString(R.string.nodes));
		Bundle extras = getActivity().getIntent().getExtras();
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
		}else if (extras != null && extras.get("index") != null){
			mCurCheckPosition = (Integer)extras.get("index");
		}
		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.detailPane);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		

		ImageView nodeic = (ImageView) getActivity().findViewById(R.id.scene_icon);
		//tt = (TextView) getActivity().findViewById(R.id.TextViewTypicals);
		textHeadListInfo = (TextView) getActivity().findViewById(R.id.TextViewManualDesc);
		swipeLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshContainer);
        swipeLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (nodiArray != null)
                            UDPHelper.healthRequest(opzioni, nodiArray.length, 0);
                        if (!opzioni.isSoulissReachable()) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(),
                                            getString(R.string.status_souliss_notreachable), Toast.LENGTH_SHORT)
                                            .show();
                                    swipeLayout.setRefreshing(false);
                                }
                            });


                        }
                    }
                }).start();
            }} );
        swipeLayout.setColorSchemeResources (R.color.std_blue,
                R.color.std_blue_shadow);
		SoulissApp.setBackground(getActivity().findViewById(R.id.relativeLayout1),
				getActivity().getWindowManager());

		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
			// refreshButton.setEnabled(false);
			// GoButt.setEnabled(false);
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(getActivity());
			alert.show();
		}
		datasource = new SoulissDBHelper(getActivity());
		SoulissDBHelper.open();

		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(getActivity());
		} else {

			final List<SoulissNode> goer = datasource.getAllNodes();
			nodiArray = new SoulissNode[goer.size()];
			nodiArray = goer.toArray(nodiArray);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getListView().setNestedScrollingEnabled(true);
			}

			getListView().setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Log.i(TAG, "Showing Node Detail:" + arg2);
					NodeViewHolder holder = (NodeViewHolder) arg1.getTag();
					
					// Activity Dettaglio nodo
					showDetails(arg2, holder.data);
				}
			});
			registerForContextMenu(getListView());
			if (mDualPane) {
				// Capita arrayOutofbound con size 0
				if (mCurCheckPosition < nodiArray.length)
					showDetails(mCurCheckPosition, nodiArray[mCurCheckPosition]);
				if (textHeadListInfo != null)
				textHeadListInfo.setVisibility(View.GONE);
				if (nodeic != null)
				nodeic.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 * @param data 
	 */
	private void showDetails(int index, SoulissNode data) {
		mCurCheckPosition = index;
		if (mDualPane) {

			((AbstractStatusedFragmentActivity)getActivity()).setActionBarInfo(data.getNiceName());
			((AbstractStatusedFragmentActivity)getActivity()).refreshStatusIcon();
			//getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.
			//getListView().setItemChecked(index, true);
			// Check what fragment is currently shown, replace if needed.
			// Fragment details = (Fragment)
			// getFragmentManager().findFragmentById(R.id.details);
			// if (details == null)
			// Istanzia e ci mette l'indice
			NodeDetailFragment details = NodeDetailFragment.newInstance(index, data);

			// Execute a transaction, replacing any existing fragment
			// with this one inside the frame.
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			if (opzioni.isAnimationsEnabled())
				ft.setCustomAnimations(R.anim.slide_in_left_delay, R.anim.slide_out_left);
			ft.replace(R.id.detailPane, details);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			ft.commit();

		} else {
			// Otherwise alla vecchia
			// the dialog fragment with selected text.
			Intent intent = new Intent();
			intent.setClass(getActivity(), NodeDetailActivity.class);
			intent.putExtra("index", index);
			intent.putExtra("NODO", data);
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
		opzioni.initializePrefs();
		// doBindService();
		SoulissDBHelper.open();

		// prendo tipici dal DB
		final List<SoulissNode> goer = datasource.getAllNodes();
		nodiArray = new SoulissNode[goer.size()];
		nodiArray = goer.toArray(nodiArray);
		//timeoutHandler = new Handler();
		Log.i(TAG, "mostro numnodi:" + goer.size());
		
		nodesAdapter = new NodesListAdapter(getActivity().getApplicationContext(), nodiArray, opzioni);
		// Adapter della lista
		// setListAdapter(nodesAdapter);
		getListView().setAdapter(nodesAdapter);
		getListView().invalidateViews();

        //actionBar = (Toolbar) getActivity().findViewById(R.id.my_awesome_toolbar);
       // ((AbstractStatusedFragmentActivity)getActivity()).setSupportActionBar(actionBar);
       // ((AbstractStatusedFragmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // ((AbstractStatusedFragmentActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
		//((AbstractStatusedFragmentActivity)getActivity()).setActionBarInfo(co);
		//((AbstractStatusedFragmentActivity) getActivity()).refreshStatusIcon();
	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Broadcast received, refresh from DB");
			SoulissDBHelper.open();
			//timeoutHandler.removeCallbacks(timeExpired);
			// ferma la rotellina del refresh
            swipeLayout.setRefreshing(false);
			try {
                // prendo tipici dal DB
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
		//BUG?
		if (info.position >= nodiArray.length){
			Log.e(TAG, "info.position >= nodiArray.length");
			return super.onContextItemSelected(item);
		}
			
		final SoulissNode todoItem = nodiArray[info.position];

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
			at.setImageResource(convertView.getIconResourceId());
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
		//getActivity().unregisterReceiver(timeoutReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction("it.angelic.soulissclient.GOT_DATA");
		filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		getActivity().registerReceiver(datareceiver, filtere);
		// timeout handler
		//IntentFilter filtera = new IntentFilter();
		//filtera.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_TIMEOUT);
		//getActivity().registerReceiver(timeoutReceiver, filtera);
		
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						try {
							((AbstractStatusedFragmentActivity) getActivity()).refreshStatusIcon();
							getListView().invalidateViews();
						} catch (Exception e) {
							Log.e(Constants.TAG, "InvalidateViews fallita:"+e.getMessage());
						}

					}
				});
			}
		}, 2500, Constants.GUI_UPDATE_INTERVAL); // updates UI each 5 secs
	}


    /*private void refreshStatusIcon() {
        try {
            //actionBar = getSupportActionBar();
            View ds = actionBar.getRootView();
            TextView info1 = (TextView) ds.findViewById(R.id.TextViewInfoStatus);
            TextView info2 = (TextView) ds.findViewById(R.id.TextViewInfo2);
            ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
            TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);
            TextView actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
            actionTitle.setText("Manual");
            if (!opzioni.isSoulissReachable()) {
                online.setBackgroundResource(R.drawable.red);
                statusOnline.setTextColor(ContextCompat.getColor(getContext(), R.color.std_red));
                statusOnline.setText(R.string.offline);

            } else {
                online.setBackgroundResource(R.drawable.green);
                statusOnline.setTextColor(ContextCompat.getColor(getContext(), R.color.std_green));
                statusOnline.setText(R.string.Online);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "null bar? " + e.getMessage());
        }
    }*/
}
