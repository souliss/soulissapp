package it.angelic.soulissclient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import it.angelic.soulissclient.adapters.TagListAdapter;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
import it.angelic.soulissclient.model.SoulissTag;

import static it.angelic.soulissclient.Constants.TAG;


/**
 * Activity per mostrare una lista di scenari
 * <p/>
 * legge dal DB tipici e tipici
 *
 * @author Ale
 */
public class TagListActivity extends AbstractStatusedFragmentActivity {
    private SoulissTag[] tags;
    private ListView listaTagsView;
    private SoulissDBTagHelper datasource;
    private TagListAdapter tagAdapter;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<SoulissTag> goer = datasource.getTags(TagListActivity.this.getApplicationContext());
            tags = new SoulissTag[goer.size()];
            int q = 0;
            for (SoulissTag object : goer) {
                tags[q++] = object;
            }

            tagAdapter = new TagListAdapter(TagListActivity.this.getApplicationContext(), tags, opzioni);
            // Adapter della lista
            listaTagsView.setAdapter(tagAdapter);
            listaTagsView.invalidateViews();
        }
    };
    private TextView tt;
    private ArrayAdapter<INavDrawerItem> mAdapter;

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

        setContentView(R.layout.main_tags);
        // final Button buttAddProgram = (Button)
        // findViewById(R.id.buttonAddScene);
       // tt = (TextView) findViewById(R.id.TextViewScenes);
        /*
		 * if ("def".compareToIgnoreCase(opzioni.getPrefFont()) != 0) { Typeface
		 * font = Typeface.createFromAsset(getAssets(), opzioni.getPrefFont());
		 * tt.setTypeface(font, Typeface.NORMAL); }
		 */

        listaTagsView = (ListView) findViewById(R.id.ListViewListaScenes);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listaTagsView);
        SoulissClient.setBackground((LinearLayout) findViewById(R.id.containerlistaScenes), getWindowManager());

        //ADD NEW SCENE
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long rest = datasource.createOrUpdateTag(null);
                // prendo comandi dal DB, setto adapter
                List<SoulissTag> goer = datasource.getTags(SoulissClient.getAppContext());
                //tags = new SoulissScene[goer.size()];
                tags = goer.toArray(tags);
                tagAdapter = new TagListAdapter(TagListActivity.this, tags, opzioni);
                // Adapter della lista
                //SceneListAdapter t = listaTagsView.getAdapter();
                tagAdapter.setScenes(tags);
                tagAdapter.notifyDataSetChanged();
                listaTagsView.setAdapter(tagAdapter);
                listaTagsView.invalidateViews();
                Toast.makeText(TagListActivity.this,
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

        listaTagsView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.w(TAG, "Activating TAG " + arg2);
                Intent nodeDatail = new Intent(TagListActivity.this, TagDetailActivity.class);
                TagListAdapter.TagViewHolder holder = (TagListAdapter.TagViewHolder) arg1.getTag();
                nodeDatail.putExtra("TAG", holder.data.getTagId());

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(TagListActivity.this,
                                arg1,   // The view which starts the transition
                                "photo_hero"    // The transitionName of the view we’re transitioning to
                        );
                ActivityCompat.startActivity(TagListActivity.this, nodeDatail, options.toBundle());
                //TagListActivity.this.startActivity(nodeDatail);
                //if (opzioni.isAnimationsEnabled())
                 //   overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        registerForContextMenu(listaTagsView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(getString(R.string.tag));
        opzioni.initializePrefs();
        if (!opzioni.isDbConfigured()) {
            AlertDialogHelper.dbNotInitedDialog(this);
        }
        datasource.open();

        // prendo comandi dal DB, setto adapter
        List<SoulissTag> goer = datasource.getTags(SoulissClient.getAppContext());
        tags = new SoulissTag[goer.size()];
        tags = goer.toArray(tags);
        tagAdapter = new TagListAdapter(this, tags, opzioni);
        // Adapter della lista
        listaTagsView.setAdapter(tagAdapter);
        listaTagsView.invalidateViews();
        // ImageView nodeic = (ImageView) findViewById(R.id.scene_icon);
        // nodeic.setAlpha(150);
        mAdapter = new NavDrawerAdapter(TagListActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.TAGS);
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
        TagListAdapter ada = (TagListAdapter) listaTagsView.getAdapter();

        long arrayAdapterPosition = info.position;
        final SoulissTag todoItem = (SoulissTag) ada.getItem((int) arrayAdapterPosition);

        switch (item.getItemId()) {

            case R.id.eliminaTag:
                ScenesDialogHelper.removeTagDialog(this, listaTagsView, datasource, todoItem, opzioni);
                return true;
            case R.id.rinominaTag:
                AlertDialog.Builder alert3 = AlertDialogHelper.renameSoulissObjectDialog(this, null, listaTagsView,
                        datasource, todoItem);
                alert3.show();
                return true;
            case R.id.scegliconaTag:
                SoulissTag convertView = (SoulissTag) listaTagsView.getItemAtPosition(item.getOrder());
                ImageView at = new ImageView(getApplicationContext());
                at.setImageResource(convertView.getIconResourceId());
                AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(this, at, listaTagsView, datasource,
                        todoItem);
                alert2.show();
                // nodesAdapter.notifyDataSetChanged();
                // listaNodiView.invalidateViews();
                return true;
            case R.id.scegliImmagineTag:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, (int) arrayAdapterPosition);
               /* Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
                Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
                chooserIntent.putExtra
                        (
                                Intent.EXTRA_INITIAL_INTENTS,
                                new Intent[]{takePhotoIntent}
                        );
                //uso come reqId il TagId cosi da riconoscere cosa avevo richiesto
                startActivityForResult(chooserIntent, (int) arrayAdapterPosition);*/
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
                Intent intent = new Intent(this, LauncherActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.Opzioni:
                Intent settingsActivity = new Intent(getBaseContext(), PreferencesActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.TestUDP:
                Intent myIntents = new Intent(TagListActivity.this, ManualUDPTestActivity.class);
                TagListActivity.this.startActivity(myIntents);
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
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
