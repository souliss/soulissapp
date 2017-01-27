package it.angelic.soulissclient;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.angelic.soulissclient.adapters.SceneCommandListAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.tagviewlib.SimpleTagViewUtils;

import static junit.framework.Assert.assertTrue;


public class SceneDetailActivity extends AbstractStatusedFragmentActivity {
    private SoulissScene collected;
    private SoulissDBHelper datasource;
    private ListView listaComandiView;
    // private boolean thru;
    private SceneCommandListAdapter ta;
    private TextView upda;

    /**
     * Riga grigia crea spazio
     *
     * @return
     */
    private void createHeader() {
        // nodoInfo.removeAllViews();
        // titolo = (TextView) findViewById(R.id.TextViewNodeTitle);
        //TextView tt = (TextView) findViewById(R.id.TextViewTypicals);
        TextView health = (TextView) findViewById(R.id.TextViewHealth);
        upda = (TextView) findViewById(R.id.TextViewNodeUpdate);
        TextView aweIcon = (TextView) findViewById(R.id.scene_icon);
        // titolo.setText(getString(R.string.scene) + " " +
        // Constants.int2roman(collected.getNodeId()));

        FontAwesomeUtil.prepareAwesomeFontAweTextView(this, aweIcon, SimpleTagViewUtils.getAwesomeNames(SceneDetailActivity.this).get(collected.getIconResourceId()));

        //icon.setImageResource(collected.getIconResourceId());
        // Animazione icona nodo
        if (opzioni.getTextFx()) {
            Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scalerotale);
            a.reset();
            aweIcon.startAnimation(a);
        }

        upda.setText(collected.toString());
        String strMeatFormat = getString(R.string.scene_subtitle);
        health.setText(String.format(strMeatFormat, collected.getCommandArray().size()));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        SceneCommandListAdapter ada = (SceneCommandListAdapter) listaComandiView.getAdapter();

        long arrayAdapterPosition = info.position;
        final SoulissCommand todoItem = (SoulissCommand) ada.getItem((int) arrayAdapterPosition);

        switch (item.getItemId()) {
            case R.id.eliminaComando:
                ScenesDialogHelper.removeCommandDialog(this, listaComandiView, datasource, collected, todoItem, opzioni);
                return true;
            case R.id.eseguiComando:
                Toast.makeText(this, "TOIMPLEMENT", Toast.LENGTH_SHORT).show();
                // nodesAdapter.notifyDataSetChanged();
                // listaNodiView.invalidateViews();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        // recuper nodo da extra
        Bundle extras = getIntent().getExtras();
        collected = (SoulissScene) extras.get("SCENA");
        assertTrue("SCENA NULLO", collected != null);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.souliss_app_name) + " - " + getString(R.string.scene) + " "
                + Constants.int2roman(collected.getId()));
        setContentView(R.layout.main_scenedetail);

        // Backg. stuff
        //SoulissApp.setBackground(findViewById(R.id.containerlista), getWindowManager());
        // Testata, info scena
        createHeader();

        listaComandiView = (ListView) findViewById(R.id.ListViewListaScene);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.attachToListView(listaComandiView);
        SoulissApp.setBackground(findViewById(R.id.containerlistaScenes), getWindowManager());

        //ADD NEW SCENE
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScenesDialogHelper.executeSceneDialog(SceneDetailActivity.this, collected);
            }
        });


        datasource = new SoulissDBHelper(this);
        // datasource.open();

        // typs = gottardo.toArray(typs);
        registerForContextMenu(listaComandiView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getMenuInflater();
        // Rinomina nodo e scelta icona
        inflater.inflate(R.menu.scenes_commands_ctx_menu, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scenedetail_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView icon = (TextView) findViewById(R.id.scene_icon);
        switch (item.getItemId()) {
            case R.id.Opzioni:
                Intent settingsActivity = new Intent(getBaseContext(), PreferencesActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.AddScene:
                //FrameLayout f1 = (FrameLayout) findViewById(android.R.id.custom);
                ScenesDialogHelper.addSceneCommandDialog(SceneDetailActivity.this, listaComandiView, datasource, collected,
                        opzioni).show();
                return true;
            case R.id.CambiaIconaScena:
                AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(this, icon, null, datasource, collected);
                alert2.show();
                return true;
            case R.id.RinominaScena:
                AlertDialog.Builder alert = AlertDialogHelper.renameSoulissObjectDialog(this, upda, listaComandiView,
                        datasource, collected);
                alert.show();
                return true;
            case android.R.id.home:
                finish();
                if (opzioni.isAnimationsEnabled())
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // thru = opzioni.isSoulissReachable();
        SoulissDBHelper.open();
        ArrayList<SoulissCommand> gottardo = datasource.getSceneCommands(collected.getId());
        SceneCommandListAdapter progsAdapter = new SceneCommandListAdapter(this, gottardo,
                opzioni);
        listaComandiView.setAdapter(progsAdapter);
        listaComandiView.invalidateViews();
    }

    @Override
    protected void onStart() {
        //int versionNumber = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
        super.onStart();
        setActionBarInfo(getString(R.string.scenes_title));
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        opzioni.initializePrefs();

        Bundle extras = getIntent().getExtras();
        collected = (SoulissScene) extras.get("SCENA");
        setActionBarInfo(collected == null ? getString(R.string.scenes_title) : collected.getNiceName());
        createHeader();

        // tipici dal DB
        SoulissDBHelper.open();

        listaComandiView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO exec single command ?
            }
        });
        //listaComandiView.invalidateViews();
    }


}
