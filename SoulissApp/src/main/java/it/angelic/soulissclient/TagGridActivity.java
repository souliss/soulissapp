package it.angelic.soulissclient;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import junit.framework.Assert;

import java.util.List;
import java.util.Map;

import it.angelic.soulissclient.adapters.TagRecyclerAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogGridHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ContextMenuRecyclerView;
import it.angelic.soulissclient.model.SoulissTag;


/**
 * Activity per mostrare una lista di scenari
 * <p/>
 * legge dal DB tipici e tipici
 *
 * @author Ale
 */
public class TagGridActivity extends AbstractStatusedFragmentActivity {
    private SoulissTag[] tags;
    private SoulissDBTagHelper datasource;
    private TagRecyclerAdapter tagAdapter;

    private ArrayAdapter<INavDrawerItem> navAdapter;
    private ContextMenuRecyclerView mRecyclerView;
    private RecyclerView.LayoutManager gridManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissClient.getOpzioni();
        // Remove title bar
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_tags_grid);
        // final Button buttAddProgram = (Button)
        // findViewById(R.id.buttonAddScene);
        // tt = (TextView) findViewById(R.id.TextViewScenes);
        /*
         * if ("def".compareToIgnoreCase(opzioni.getPrefFont()) != 0) { Typeface
		 * font = Typeface.createFromAsset(getAssets(), opzioni.getPrefFont());
		 * tt.setTypeface(font, Typeface.NORMAL); }
		 */

        mRecyclerView = (ContextMenuRecyclerView) findViewById(R.id.recyclerViewTags);

        //3 colonne in horiz
        gridManager = new GridLayoutManager(this, getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2);
        mRecyclerView.setLayoutManager(gridManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//FIXME
        //Floatin Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);


        //ADD NEW SCENE
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SoulissTag> goerBck = datasource.getTags(SoulissClient.getAppContext());

                long rest = datasource.createOrUpdateTag(null);
                // prendo comandi dal DB, setto adapter
                List<SoulissTag> goer = datasource.getTags(SoulissClient.getAppContext());
                goer.removeAll(goerBck);
                Assert.assertTrue(goer.size() == 1);
                SoulissTag newTag = goer.get(0);
                goerBck.add(1, newTag);

                // goer.removeAll(goerBck);
                tags = new SoulissTag[goerBck.size()];
                tags = goerBck.toArray(tags);
                //tagAdapter = new TagListAdapter(TagGridActivity.this, tags, opzioni);
                // Adapter della lista
                //SceneListAdapter t = listaTagsView.getAdapter();
                tagAdapter.setTagArray(tags);

                tagAdapter.notifyItemInserted(1);
                //force rebind of click listeners
                tagAdapter.notifyDataSetChanged();
                //listaTagsView.invalidateViews();
                Toast.makeText(TagGridActivity.this,
                        getString(R.string.tag) + rest + " inserted, long-press to rename it and choose icon", Toast.LENGTH_LONG).show();
            }
        });
        // check se IP non settato
        if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
            AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
            alert.show();
        }


        datasource = new SoulissDBTagHelper(this);
        datasource.open();
        // prendo comandi dal DB, setto adapter
        List<SoulissTag> goer = datasource.getTags(SoulissClient.getAppContext());
        tags = new SoulissTag[goer.size()];
        tags = goer.toArray(tags);
        tagAdapter = new TagRecyclerAdapter(this, tags, opzioni);
        // Adapter della lista
        mRecyclerView.setAdapter(tagAdapter);


        // DRAWER
        super.initDrawer(this, DrawerMenuHelper.TAGS);

        registerForContextMenu(mRecyclerView);

        //TODEBUG TRANSACTIONS
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                Log.d(Constants.TAG, "ExitSharedElementCallback.onSharedElementStart:" + sharedElementNames.size());
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Log.d(Constants.TAG, "ExitSharedElementCallback.onMapSharedElements:"
                        + names.get(0));
                super.onMapSharedElements(names, sharedElements);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onRejectSharedElements(List<View> rejectedSharedElements) {
                Log.d(Constants.TAG, "ExitSharedElementCallback.onRejectSharedElements:"
                        + rejectedSharedElements.size());

                super.onRejectSharedElements(rejectedSharedElements);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                Log.i(Constants.TAG, "ExitSharedElementCallback.onSharedElementEnd");
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(getString(R.string.tag));
        opzioni.initializePrefs();
        if (!opzioni.isDbConfigured()) {
            AlertDialogHelper.dbNotInitedDialog(this);
        }
        SoulissDBHelper.open();
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navAdapter = new NavDrawerAdapter(TagGridActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.TAGS);
        mDrawerList.setAdapter(navAdapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        // Rinomina nodo e scelta icona
        inflater.inflate(R.menu.ctx_menu_tags, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        TagRecyclerAdapter ada = (TagRecyclerAdapter) mRecyclerView.getAdapter();

        long arrayAdapterPosition = info.position;
        final SoulissTag todoItem = (SoulissTag) ada.getTag((int) arrayAdapterPosition);

        switch (item.getItemId()) {

            case R.id.eliminaTag:
                AlertDialogGridHelper.removeTagDialog(this, tagAdapter, datasource, todoItem, opzioni);
                return true;
            case R.id.rinominaTag:
                AlertDialog.Builder alert3 = AlertDialogGridHelper.renameSoulissObjectDialog(this, null, tagAdapter,
                        datasource, todoItem);
                alert3.show();
                return true;
            case R.id.scegliconaTag:
                SoulissTag convertView = (SoulissTag) tagAdapter.getTag(item.getOrder());
                ImageView at = new ImageView(getApplicationContext());
                at.setImageResource(convertView.getIconResourceId());
                AlertDialog.Builder alert2 = AlertDialogGridHelper.chooseIconDialog(this, at, tagAdapter, datasource,
                        todoItem);
                alert2.show();
                // nodesAdapter.notifyDataSetChanged();
                // listaNodiView.invalidateViews();
                return true;
            case R.id.scegliImmagineTag:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, (int) arrayAdapterPosition);

                return true;
            case R.id.scegliOrdineTag:
                AlertDialog alertOrder = AlertDialogGridHelper.tagOrderPickerDialog(this, todoItem,item.getOrder(), tagAdapter);
                alertOrder.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.i(Constants.TAG, "SAVING IMG RESULT:" + resultCode);


        if (resultCode == RESULT_OK) {
            Uri selectedImage = imageReturnedIntent.getData();
            Log.i(Constants.TAG, "SAVED IMG PATH:" + selectedImage.toString());
            tags[requestCode].setImagePath(selectedImage.toString());
            //String[] filePathColumn = {MediaStore.Images.Media.DATA};

            datasource.createOrUpdateTag(tags[requestCode]);
            //Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
            Log.i(Constants.TAG, "SAVED IMG PATH:" + tags[requestCode].getImagePath());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scenelist_menu, menu);
        return true;

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(mDrawerLinear)) {
                mDrawerLayout.closeDrawer(mDrawerLinear);
            } else {
                mDrawerLayout.openDrawer(mDrawerLinear);
            }
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                supportFinishAfterTransition();
                return true;
            case R.id.Opzioni:
                Intent settingsActivity = new Intent(getBaseContext(), PreferencesActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.TestUDP:
                Intent myIntents = new Intent(TagGridActivity.this, ManualUDPTestActivity.class);
                TagGridActivity.this.startActivity(myIntents);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation change
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
