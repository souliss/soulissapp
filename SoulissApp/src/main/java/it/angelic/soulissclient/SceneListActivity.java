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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedList;
import java.util.List;

import it.angelic.soulissclient.adapters.SceneListAdapter;
import it.angelic.soulissclient.adapters.SceneListAdapter.SceneViewHolder;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.LauncherElementEnum;

import static it.angelic.soulissclient.Constants.TAG;


/**
 * Activity per mostrare una lista di scenari
 * <p>
 * legge dal DB tipici e tipici
 *
 * @author Ale
 */
public class SceneListActivity extends AbstractStatusedFragmentActivity {
    private SoulissDBHelper datasource;
    private ListView listaScenesView;
    private SceneListAdapter progsAdapter;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<SoulissScene> goer = datasource.getScenes();
            //progsAdapter = new SceneListAdapter(SceneListActivity.this.getApplicationContext(), scenesArray, opzioni);
            progsAdapter.setScenes(goer);
            progsAdapter.notifyDataSetChanged();
            // Adapter della lista
            //listaScenesView.setAdapter(progsAdapter);
            listaScenesView.invalidateViews();
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation change
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        SceneListAdapter ada = (SceneListAdapter) listaScenesView.getAdapter();

        long arrayAdapterPosition = info.position;
        final SoulissScene todoItem = (SoulissScene) ada.getItem((int) arrayAdapterPosition);

        switch (item.getItemId()) {
            case R.id.eseguiScena:
                ScenesDialogHelper.executeSceneDialog(SceneListActivity.this, todoItem);
                return true;
            case R.id.addScenaDashboard:
                SoulissDBLauncherHelper dbl = new SoulissDBLauncherHelper(SceneListActivity.this);
                LauncherElement nodeLauncher = new LauncherElement();
                nodeLauncher.setComponentEnum(LauncherElementEnum.SCENE);
                nodeLauncher.setLinkedObject(todoItem);
                dbl.addElement(nodeLauncher);
                Toast.makeText(SceneListActivity.this, todoItem.getNiceName() + " " + getString(R.string.added_to_dashboard), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.eliminaScena:
                ScenesDialogHelper.removeSceneDialog(this, listaScenesView, datasource, todoItem, opzioni);
                return true;
            case R.id.rinominaScena:
                AlertDialog.Builder alert3 = AlertDialogHelper.renameSoulissObjectDialog(this, null, listaScenesView,
                        datasource, todoItem);
                alert3.show();
                return true;
            case R.id.scegliconaScena:
                SoulissScene convertView = (SoulissScene) listaScenesView.getItemAtPosition(item.getOrder());
                AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(this, null, listaScenesView, datasource,
                        todoItem);
                alert2.show();
                // nodesAdapter.notifyDataSetChanged();
                // listaNodiView.invalidateViews();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        // Remove title bar
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_scenes);

        listaScenesView = findViewById(R.id.ListViewListaScenes);

        TextView textAwesomeUpperRight = findViewById(R.id.scene_icon);

        final TextView toHid = findViewById(R.id.TextViewSceneDesc);
        final TextView textViewTagsDescFa = findViewById(R.id.TextViewSceneDescFa);
        FontAwesomeUtil.prepareMiniFontAweTextView(this, textViewTagsDescFa, FontAwesomeEnum.fa_close.getFontName());
        //NASCONDI
        textViewTagsDescFa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewTagsDescFa.setVisibility(View.GONE);
                toHid.setVisibility(View.GONE);
                opzioni.setDontShowAgain("scenesInfo", true);
            }
        });
        if (opzioni.getDontShowAgain("scenesInfo")) {
            textViewTagsDescFa.setVisibility(View.GONE);
            toHid.setVisibility(View.GONE);
        }


        FontAwesomeUtil.prepareAwesomeFontAweTextView(this, textAwesomeUpperRight, FontAwesomeEnum.fa_moon_o.getFontName());
        FloatingActionButton fab = findViewById(R.id.fab);

        //ADD NEW SCENE
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rest = datasource.createOrUpdateScene(null);
                // prendo comandi dal DB, setto adapter
                LinkedList<SoulissScene> goer = datasource.getScenes();
                progsAdapter.setScenes(goer);
                progsAdapter.notifyDataSetChanged();
                listaScenesView.setAdapter(progsAdapter);
                listaScenesView.invalidateViews();

                Toast.makeText(SceneListActivity.this, getString(R.string.scene_added), Toast.LENGTH_LONG).show();
            }
        });
        // check se IP non settato
        if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
            AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
            alert.show();
        }

        // TODO error management
        datasource = new SoulissDBHelper(this);
        // datasource.open();

        // DRAWER
        super.initDrawer(this, DrawerMenuHelper.SCENES);

        listaScenesView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.w(TAG, "Activating SCENE " + arg2);
                Intent nodeDatail = new Intent(SceneListActivity.this, SceneDetailActivity.class);
                SceneViewHolder holder = (SceneViewHolder) arg1.getTag();
                nodeDatail.putExtra("SCENA", holder.data);
                SceneListActivity.this.startActivity(nodeDatail);
                if (opzioni.isAnimationsEnabled())
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        registerForContextMenu(listaScenesView);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scenelist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerLinear)) {
                    mDrawerLayout.closeDrawer(mDrawerLinear);
                } else {
                    mDrawerLayout.openDrawer(mDrawerLinear);
                }
                return true;
            case R.id.Opzioni:
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filtere = new IntentFilter();
        filtere.addAction("it.angelic.soulissclient.GOT_DATA");
        registerReceiver(datareceiver, filtere);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(getString(R.string.scenes_title));
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        opzioni.initializePrefs();
        if (!opzioni.isDbConfigured()) {
            AlertDialogHelper.dbNotInitedDialog(this);
        }
        SoulissDBHelper.open();

        // prendo comandi dal DB, setto adapter
        LinkedList<SoulissScene> sceneList = datasource.getScenes();

        progsAdapter = new SceneListAdapter(this, sceneList, opzioni);
        // Adapter della lista
        listaScenesView.setAdapter(progsAdapter);
        //listaScenesView.invalidateViews();
        // ImageView nodeic = (ImageView) findViewById(R.id.scene_icon);
        // nodeic.setAlpha(150);
        ArrayAdapter<INavDrawerItem> mAdapter = new NavDrawerAdapter(SceneListActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.SCENES);
        mDrawerList.setAdapter(mAdapter);
    }

}
