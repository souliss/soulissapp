package it.angelic.soulissclient.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.angelic.soulissclient.AbstractStatusedFragmentActivity;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.TypicalDetailFragWrapper;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.adapters.TypicalsListAdapter.TypicalViewHolder;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.model.typicals.SoulissTypical14PulseOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.model.typicals.SoulissTypical18StepRelay;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.model.typicals.SoulissTypical32AirCon;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical6nAnalogue;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.LauncherElementEnum;
import it.angelic.soulissclient.util.SoulissUtils;
import it.angelic.tagviewlib.SimpleTagViewUtils;

import static junit.framework.Assert.assertTrue;


public class NodeDetailFragment extends ListFragment {
    private Timer autoUpdate;
    private SoulissNode collected;
    private SoulissDBHelper datasource;
    private ListView listaTypicalsView;
    private SoulissDataService mBoundService;
    private boolean mDualPane;
    private boolean mIsBound;
    private TextView nodeic;
    private SoulissPreferenceHelper opzioni;
    private ProgressBar par;
    private SwipeRefreshLayout swipeLayout;
    //private ActionBar actionBar;
    //private TextView tt;
    private TypicalsListAdapter ta;
    /* SOULISS DATA SERVICE BINDING */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            mBoundService = ((SoulissDataService.LocalBinder) service).getService();
            if (ta != null)
                ta.setmBoundService(mBoundService);
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            // if (ta != null)
            ta.setmBoundService(null);
        }
    };
    private Handler timeoutHandler;
    // private Toolbar actionBar;
    private TextView upda;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(Constants.TAG, "Detected data arrival, refresh from DB");
                // cancel timeout
                swipeLayout.setRefreshing(false);
                if (listaTypicalsView == null) {
                    return;
                }
                SoulissDBHelper.open();

                collected = datasource.getSoulissNode(collected.getNodeId());
                refreshHeader();

                List<SoulissTypical> goer = collected.getActiveTypicals();
                SoulissTypical[] typs = new SoulissTypical[goer.size()];
                typs = goer.toArray(typs);

                ta.setTypicals(typs);
                ta.notifyDataSetChanged();

                // save index and top position
                int index = listaTypicalsView.getFirstVisiblePosition();
                View v = listaTypicalsView.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                // Adapter della lista
                //listaTypicalsView.setAdapter(ta);
                listaTypicalsView.invalidateViews();
                listaTypicalsView.setSelectionFromTop(index, top);
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error in data receival, connection closed?" + e.getMessage());
            }
        }
    };

    public static NodeDetailFragment newInstance(int index, SoulissNode content) {
        NodeDetailFragment f = new NodeDetailFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        // Ci metto il nodo dentro
        if (content != null) {
            args.putSerializable("NODO", content.getNodeId());
        }
        f.setArguments(args);

        return f;
    }

    // private SwipeGestureListener gestureListener;

    /**
     * Riga grigia cra spazio
     */
    private void createHeader() {

        if (collected.getIconResourceId() != 0)
            FontAwesomeUtil.prepareAwesomeFontAweTextView(getActivity(), nodeic, SimpleTagViewUtils.getAwesomeNames(getActivity()).get(collected.getIconResourceId()));


        par.setMax(Constants.MAX_HEALTH);

        // ProgressBar sfumata
        final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(Constants.roundedCorners, null, null));
        final LinearGradient gradient = new LinearGradient(0, 0, 250, 0, getResources().getColor(color.aa_red),
                getResources().getColor(color.aa_green), android.graphics.Shader.TileMode.CLAMP);
        // pgDrawable.getPaint().setStrokeWidth(3);
        pgDrawable.getPaint().setDither(true);
        pgDrawable.getPaint().setShader(gradient);

        ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
        par.setBackgroundResource(android.R.drawable.progress_horizontal);
        par.setProgressDrawable(progress);
        par.setMax(50);
        par.setProgress(20);
        par.setProgress(0); // <-- BUG Android
        par.setMax(Constants.MAX_HEALTH);
        refreshHeader();

        Log.d(Constants.TAG,
                "Setting bar at " + collected.getHealth() + " win width=" + SoulissApp.getDisplayWidth() / 2);
    }

    void doBindService() {
        if (!mIsBound) {
            getActivity().bindService(new Intent(getActivity(), SoulissDataService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    protected int getShownIndex() {
        if (getArguments() != null)
            return getArguments().getInt("index", 0);
        else
            return 0;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();

        if (opzioni.isLightThemeSelected())
            getActivity().setTheme(R.style.LightThemeSelector);
        else
            getActivity().setTheme(R.style.DarkThemeSelector);

        super.onActivityCreated(savedInstanceState);
        timeoutHandler = new Handler();
        setHasOptionsMenu(true);
        datasource = new SoulissDBHelper(getActivity());
        View detailsFrame = getActivity().findViewById(R.id.detailPane);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        // nodoInfo.removeAllViews();
        //tt = (TextView) getActivity().findViewById(R.id.TextViewTypicalsTitle);
        // health = (TextView) findViewById(R.id.TextViewHealth);
        upda = (TextView) getActivity().findViewById(R.id.TextViewNodeUpdate);
        par = (ProgressBar) getActivity().findViewById(R.id.progressBarNodo);
        swipeLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshContainer);

        if (upda == null)
            return;
        Bundle extras = getActivity().getIntent().getExtras();
        // recuper nodo da extra
        Short colId = 0;
        if (extras != null && extras.get("NODO") != null)
            colId = extras.getShort("NODO");
        else if (getArguments() != null) {
            colId = getArguments().getShort("NODO");
        }

        try {
            Log.w(Constants.TAG, "DB load node id" + colId);
            collected = datasource.getSoulissNode(colId);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Error retriving node:" + e.getMessage());
            return;
        }


        assertTrue("NODO NULLO", collected != null);
        getActivity().setTitle(collected.getNiceName());

        // SFONDO
        SoulissApp.setBackground(getActivity().findViewById(R.id.containerlista), getActivity()
                .getWindowManager());
        // listaTypicalsView = (ListView) getListView();
        listaTypicalsView = getListView();
        nodeic = (TextView) getActivity().findViewById(R.id.node_icon);
        // Icona, puo esser nullo dopo rotazione schermo
        if (nodeic != null) {
            FontAwesomeUtil.prepareAwesomeFontAweTextView(getActivity(), nodeic, SimpleTagViewUtils.getAwesomeNames(getActivity()).get(collected.getIconResourceId()));
            createHeader();
            registerForContextMenu(listaTypicalsView);
        } else
            Log.e(Constants.TAG, "icona nulla? impossible...");


        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (collected != null) {
                            //  UDPHelper.pollRequest(opzioni, 1, collected.getNodeId());
                            // state req. meglio, fa subscribe
                            UDPHelper.pollRequest(opzioni, 1, collected.getNodeId());

                        }

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
            }
        });
        swipeLayout.setColorSchemeResources(R.color.std_blue,
                R.color.std_blue_shadow);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        View detailsFrame = getActivity().findViewById(R.id.detailPane);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        Log.i(Constants.TAG, "DUALPANE:" + mDualPane);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        TypicalsListAdapter ada = (TypicalsListAdapter) listaTypicalsView.getAdapter();

        long arrayAdapterPosition = info.position;
        final SoulissTypical todoItem = (SoulissTypical) ada.getItem((int) arrayAdapterPosition);

        switch (item.getItemId()) {
            case R.id.rinomina:
                AlertDialog.Builder alert3 = AlertDialogHelper.renameSoulissObjectDialog(getActivity(), null,
                        listaTypicalsView, datasource, todoItem);
                alert3.show();
                break;
            case R.id.sceglicona:
                SoulissTypical convertView = (SoulissTypical) listaTypicalsView.getItemAtPosition(item.getOrder());
                TextView at = new TextView(getActivity());
                FontAwesomeUtil.prepareFontAweTextView(getActivity(), at, convertView.getIconResourceId());
                AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(getActivity(), at, listaTypicalsView,
                        datasource, todoItem);
                alert2.show();
                break;
            case R.id.addTo:
                SoulissDBTagHelper dbt = new SoulissDBTagHelper(getActivity());
                //
                AlertDialog.Builder alert4 = AlertDialogHelper.addTagCommandDialog(getActivity(), dbt, todoItem, null, getListView());
                AlertDialog built = alert4.create();
                built.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                built.show();
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        // Rinomina nodo e scelta icona
        inflater.inflate(R.menu.typical_ctx_menu, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Rinomina nodo e scelta icona
        inflater.inflate(R.menu.rebuildnode_ctx_menu, menu);
        Log.i(Constants.TAG, "Inflated Equalizer menu");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_nodedetail, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.Ricostruisci:
                AlertDialog.Builder alertt = AlertDialogHelper.rebuildNodeDialog(getActivity(), collected, opzioni);
                alertt.show();
                return true;
            case R.id.AddToDashboard:
                SoulissDBLauncherHelper dbl = new SoulissDBLauncherHelper(getActivity());
                LauncherElement nodeLauncher = new LauncherElement();
                nodeLauncher.setComponentEnum(LauncherElementEnum.NODE);
                nodeLauncher.setLinkedObject(collected);
                dbl.addElement(nodeLauncher);
                Toast.makeText(getActivity(), collected.getNiceName() + " " + getActivity().getString(R.string.added_to_dashboard), Toast.LENGTH_SHORT).show();
                return true;
        }

        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        autoUpdate.cancel();
        getActivity().unregisterReceiver(datareceiver);
        doUnbindService();
    }

    @Override
    public void onResume() {
        super.onResume();
        // pezza
        SoulissDBHelper.open();
        doBindService();
        IntentFilter filtere = new IntentFilter();
        filtere.addAction("it.angelic.soulissclient.GOT_DATA");
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        getActivity().registerReceiver(datareceiver, filtere);


        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (listaTypicalsView != null)
                            listaTypicalsView.invalidateViews();
                    }
                });
            }
        }, 100, Constants.GUI_UPDATE_INTERVAL); // updates GUI each 40 secs
    }

    @Override
    public void onStart() {
        super.onStart();
        if (collected == null || upda == null) {
            Log.w(Constants.TAG, "Empty Typical!!");
            return;// no detail selected
        }
        //refreshStatusIcon();

        if (opzioni.isDbConfigured()) {
            SoulissDBHelper.open();
            // per il refresh dal dettaglio
            collected = datasource.getSoulissNode(collected.getNodeId());
            doBindService();

            // poll 1 node
            new Thread(new Runnable() {
                @Override
                public void run() {
                    /*
                     * UDPHelper.healthRequest(opzioni, 1, collected.getNodeId());
					 * try { Thread.sleep(500); } catch (InterruptedException e)
					 * { e.printStackTrace(); }
					 */
                    //UDPHelper.pollRequest(opzioni, 1, collected.getNodeId());
                    UDPHelper.pollRequest(opzioni, 1, collected.getNodeId());
                }
            }).start();

            createHeader();
            // tipici dal DB
            List<SoulissTypical> goer = collected.getActiveTypicals();

            SoulissTypical[] typs = new SoulissTypical[goer.size()];
            typs = goer.toArray(typs);


            ta = new TypicalsListAdapter(getActivity(), mBoundService, typs, getActivity().getIntent(),
                    opzioni);
            listaTypicalsView = getListView();
            // Adapter della lista
            listaTypicalsView.setAdapter(ta);

            listaTypicalsView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    TypicalViewHolder holder = (TypicalViewHolder) arg1.getTag();
                    //Log.i(getTag(), "Showing typical idx:"+arg2+" holder slot: "+holder.data.getTypicalDTO().getSlot());
                    showDetails(arg2, holder.data);
                }
            });
            //serve per back da dettaglio
            ((AbstractStatusedFragmentActivity) getActivity()).setActionBarInfo(collected.getNiceName());

        }
    }

    private void refreshHeader() {
        par.setProgress(collected.getHealth());
        par.setProgress(20);
        par.setProgress(0); // <-- BUG Android
        par.setProgress(collected.getHealth());
        upda.setText(getResources().getString(R.string.update) + " " + SoulissUtils.getTimeAgo(collected.getRefreshedAt()));

    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a whole new
     * activity in which it is displayed.
     */
    private void showDetails(int index, SoulissTypical target) {
        // mCurCheckPosition = index;
        if (target == null)
            return;//capita con lista vuota
        if (mDualPane) {
            ListView li = getListView();
            li.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // listaNodiView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            li.setItemChecked(index, true);
        } else {
            //  ((AbstractStatusedFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Fragment NewFrag = null;
        // Istanzia e ci mette l'indice
        if (target instanceof SoulissTypical6nAnalogue)
            NewFrag = T6nAnalogueFragment.newInstance(index, target);
        else if (target instanceof SoulissTypical31Heating)
            NewFrag = T31HeatingFragment.newInstance(index, target);
        else if (target.isSensor())
            NewFrag = T5nSensorFragment.newInstance(index, target);
        else if (target instanceof SoulissTypical16AdvancedRGB)
            NewFrag = T16RGBAdvancedFragment.newInstance(index, target);
        else if (target instanceof SoulissTypical19AnalogChannel)
            NewFrag = T19SingleChannelLedFragment.newInstance(index, target);
        else if (target instanceof SoulissTypical11DigitalOutput || target instanceof SoulissTypical12DigitalOutputAuto)
            NewFrag = T1nGenericLightFragment.newInstance(index, target);
        else if (target instanceof SoulissTypical41AntiTheft || target instanceof SoulissTypical42AntiTheftPeer || target instanceof SoulissTypical43AntiTheftLocalPeer)
            NewFrag = T4nFragment.newInstance(index, target);
        else if (target instanceof SoulissTypical32AirCon)
            NewFrag = T32AirConFragment.newInstance(index, target);
        else if (target instanceof SoulissTypical14PulseOutput || target instanceof SoulissTypical18StepRelay) {
            //no detail, notice user and return
            Toast.makeText(getActivity(),
                    getString(R.string.status_souliss_nodetail), Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            //last resort, TWrapper
            // Activity Dettaglio nodo
            //TODO transform in frags
            Intent nodeDatail = new Intent(getActivity(), TypicalDetailFragWrapper.class);
            nodeDatail.putExtra("TIPICO", target);
            getActivity().startActivity(nodeDatail);
            // getActivity().supportFinishAfterTransition();
            return;//launched wrapper activity
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (opzioni.isAnimationsEnabled())
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        ft.replace(R.id.detailPane, NewFrag);
        ft.addToBackStack(null);

        // ft.remove(details);
        //ft.add(NewFrag,"BOH");
        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();


    }

}
