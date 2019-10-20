package it.angelic.soulissclient;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import it.angelic.soulissclient.adapters.TagRecyclerAdapter;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.INavDrawerItem;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogGridHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ContextMenuRecyclerView;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;


/**
 * Activity per mostrare una lista di scenari
 * <p/>
 * legge dal DB tipici e tipici
 *
 * @author Ale
 */
public class TagGridActivity extends AbstractStatusedFragmentActivity {
    private SoulissDBTagHelper datasource;
    private List<SoulissTag> goer;
    private ContextMenuRecyclerView mRecyclerView;
    private TagRecyclerAdapter tagAdapter;
    //private SoulissTag[] tags;

    /**
     * Don't forget to call setResult(Activity.RESULT_OK) in the returning
     * activity or else this method won't be called!
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        // Postpone the shared element return transition.
        postponeEnterTransition();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.i(Constants.TAG, "SAVED IMG RESULT:" + resultCode);


        if (resultCode == RESULT_OK) {
            Uri selectedImage = imageReturnedIntent.getData();
            Log.i(Constants.TAG, "SAVED IMG PATH:" + selectedImage.toString());
            goer.get(requestCode).setImagePath(selectedImage.toString());
            //String[] filePathColumn = {MediaStore.Images.Media.DATA};
            datasource.createOrUpdateTag(goer.get(requestCode));
            //Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
            Log.i(Constants.TAG, "SAVED IMG PATH:" + goer.get(requestCode).getImagePath());
            tagAdapter.notifyItemChanged(requestCode);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation change
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /*
    With Drag&Drop this WON'T BE CALLED

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        TagRecyclerAdapter ada = (TagRecyclerAdapter) mRecyclerView.getAdapter();

        long arrayAdapterPosition = info.position;
        final SoulissTag todoItem = ada.getTag((int) arrayAdapterPosition);

        switch (item.getItemId()) {

            case R.id.rinominaTag:
                AlertDialog.Builder alert3 = AlertDialogGridHelper.renameSoulissObjectDialog(this, null, tagAdapter,
                        datasource, todoItem);
                alert3.show();
                return true;
            case R.id.scegliconaTag:
                AlertDialog.Builder alert2 = AlertDialogGridHelper.chooseIconDialog(this, tagAdapter, datasource,
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
*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_tags_grid);

        final TextView toHid = findViewById(R.id.TextViewTagsDesc);
        final TextView textViewTagsDescFa = findViewById(R.id.TextViewTagsDescFa);
        FontAwesomeUtil.prepareMiniFontAweTextView(this, textViewTagsDescFa, FontAwesomeEnum.fa_close.getFontName());
        //NASCONDI HINT
        textViewTagsDescFa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewTagsDescFa.setVisibility(View.GONE);
                toHid.setVisibility(View.GONE);
                opzioni.setDontShowAgain("tagsInfo", true);
            }
        });
        if (opzioni.getDontShowAgain("tagsInfo")) {
            textViewTagsDescFa.setVisibility(View.GONE);
            toHid.setVisibility(View.GONE);
        }

        mRecyclerView = findViewById(R.id.recyclerViewLauncherItems);
        TextView textAwesomeUpperRight = findViewById(R.id.back_icon);
        FontAwesomeUtil.prepareAwesomeFontAweTextView(this, textAwesomeUpperRight, FontAwesomeEnum.fa_tags.getFontName());

        //3 colonne in horiz
        RecyclerView.LayoutManager gridManager = new GridLayoutManager(this, getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2);
        mRecyclerView.setLayoutManager(gridManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//FIXME
        //Floatin Button
        FloatingActionButton fab = findViewById(R.id.fab);

        //ADD NEW TAG
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long rest = datasource.createOrUpdateTag(null);
                // prendo comandi dal DB, setto adapter
                goer = datasource.getRootTags();

                tagAdapter.setTagArray(goer);

                tagAdapter.notifyItemInserted(tagAdapter.getItemCount());
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    //fai finire l'animazione
                    public void run() {
                        //force rebind of click listeners
                        tagAdapter.notifyDataSetChanged();
                    }
                }, 500);  // msec

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
        SoulissDBHelper.open();
        // prendo comandi dal DB, setto adapter
        goer = datasource.getRootTags();

        tagAdapter = new TagRecyclerAdapter(this, goer, opzioni, fab);
        // Adapter della lista
        mRecyclerView.setAdapter(tagAdapter);


        if (opzioni.isSoulissReachable()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    UDPHelper.stateRequest(opzioni, goer.size(), 0);
                }
            });
        }


        // Extend the Callback class
        ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
                //  return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                //        ItemTouchHelper.DOWN | ItemTouchHelper.UP |);
            }

            //and in your imlpementaion of
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissApp.getAppContext());
                if (viewHolder.getAdapterPosition() < target.getAdapterPosition()) {
                    for (int i = viewHolder.getAdapterPosition(); i < target.getAdapterPosition(); i++) {
                        Collections.swap(tagAdapter.getTagArray(), i, i + 1);
                    }
                } else {
                    for (int i = viewHolder.getAdapterPosition(); i > target.getAdapterPosition(); i--) {
                        Collections.swap(tagAdapter.getTagArray(), i, i - 1);
                    }
                }


                // and notify the adapter that its dataset has changed
                tagAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                List<SoulissTag> tagList = tagAdapter.getTagArray();
                int i = 0;
                for (SoulissTag tag :
                        tagList) {
                    tag.setTagOrder(i++);
                    dbt.refreshTag(tag);
                }
                return true;
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                SoulissTag todoItem = tagAdapter.getTag(viewHolder.getAdapterPosition());
                AlertDialogGridHelper.removeTagDialog(TagGridActivity.this, tagAdapter, datasource, todoItem);
                //forse non serve
                clearView(mRecyclerView, viewHolder);
            }

        };

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(mRecyclerView);

        // DRAWER
        super.initDrawer(this, DrawerMenuHelper.TAGS);

        //   registerForContextMenu(mRecyclerView);

        //TODEBUG TRANSACTIONS
        /*
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
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scenelist_menu, menu);
        return true;

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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
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
        ArrayAdapter<INavDrawerItem> navAdapter = new NavDrawerAdapter(TagGridActivity.this, R.layout.drawer_list_item, dmh.getStuff(), DrawerMenuHelper.TAGS);
        mDrawerList.setAdapter(navAdapter);
        //un po bruto
        tagAdapter.setTagArray(datasource.getRootTags());
        tagAdapter.notifyDataSetChanged();
        scheduleStartPostponedTransition(mDrawerList);
    }

}
