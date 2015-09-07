package it.angelic.soulissclient;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import it.angelic.soulissclient.adapters.TagRecyclerAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
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

    private ArrayAdapter<INavDrawerItem> mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager gridManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissClient.getOpzioni();
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
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

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewTags);
        gridManager = new GridLayoutManager(this, 2);


        mRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("SDSDVV","sdvskudbcvsdiubv");
            }
        });

        mRecyclerView.setLayoutManager(gridManager);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);
        SoulissClient.setBackground(findViewById(R.id.containerlistaScenes), getWindowManager());

        //ADD NEW SCENE
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long rest = datasource.createOrUpdateTag(null);
                // prendo comandi dal DB, setto adapter
                List<SoulissTag> goer = datasource.getTags(SoulissClient.getAppContext());
                //tags = new SoulissScene[goer.size()];
                tags = goer.toArray(tags);
                //tagAdapter = new TagListAdapter(TagGridActivity.this, tags, opzioni);
                // Adapter della lista
                //SceneListAdapter t = listaTagsView.getAdapter();
                tagAdapter.setTags(tags);
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

        // TODO error management
        datasource = new SoulissDBTagHelper(this);

        // datasource.open();

        // DRAWER
        super.initDrawer(this, DrawerMenuHelper.TAGS);

     //FIXME
        registerForContextMenu(mRecyclerView);
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

        // prendo comandi dal DB, setto adapter
        List<SoulissTag> goer = datasource.getTags(SoulissClient.getAppContext());
        tags = new SoulissTag[goer.size()];
        tags = goer.toArray(tags);
        tagAdapter = new TagRecyclerAdapter(this, tags, opzioni);
        // Adapter della lista
        mRecyclerView.setAdapter(tagAdapter);
     /*   mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.w(Constants.TAG, "Activating TAG " + position);
                        Intent nodeDatail = new Intent(TagGridActivity.this, TagDetailActivity.class);
                       // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                        nodeDatail.putExtra("TAG", tags[position].getTagId());

                        ActivityOptionsCompat options =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(TagGridActivity.this,
                                        view,   // The view which starts the transition
                                        "photo_hero"    // The transitionName of the view we’re transitioning to
                                );

                       // ActivityCompat.startActivity(TagGridActivity.this, nodeDatail, options.toBundle());
                        TagGridActivity.this.startActivity(nodeDatail);
                        //if (opzioni.isAnimationsEnabled())
                        //   overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }));*/
        // ImageView nodeic = (ImageView) findViewById(R.id.scene_icon);
        // nodeic.setAlpha(150);
        mAdapter = new NavDrawerAdapter(TagGridActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.TAGS);
        mDrawerList.setAdapter(mAdapter);
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        TagRecyclerAdapter ada = (TagRecyclerAdapter) mRecyclerView.getAdapter();

        long arrayAdapterPosition = info.position;
        final SoulissTag todoItem = (SoulissTag) ada.getItem((int) arrayAdapterPosition);

        switch (item.getItemId()) {

            case R.id.eliminaTag:
                ScenesDialogHelper.removeTagDialog(this, null, datasource, todoItem, opzioni);
                return true;
            case R.id.rinominaTag:
                AlertDialog.Builder alert3 = AlertDialogHelper.renameSoulissObjectDialog(this, null, null,
                        datasource, todoItem);
                alert3.show();
                return true;
            case R.id.scegliconaTag:
                SoulissTag convertView = (SoulissTag) tagAdapter.getItem(item.getOrder());
                ImageView at = new ImageView(getApplicationContext());
                at.setImageResource(convertView.getIconResourceId());
                AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(this, at, null, datasource,
                        todoItem);
                alert2.show();
                // nodesAdapter.notifyDataSetChanged();
                // listaNodiView.invalidateViews();
                return true;
            case R.id.scegliImmagineTag:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, (int) arrayAdapterPosition);

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
