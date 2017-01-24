/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package it.angelic.soulissclient.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.SQLDataException;
import java.util.Collections;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.adapters.ParallaxExenderAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.util.FontAwesomeUtil;

/**
 * Demonstrates the use of {@link android.support.v7.widget.RecyclerView} with a {@link android.support.v7.widget.LinearLayoutManager} and a
 * {@link android.support.v7.widget.GridLayoutManager}.
 */
public class TagDetailFragment extends AbstractTypicalFragment implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected ParallaxExenderAdapter parallaxExtAdapter;
    protected RecyclerView.LayoutManager mTagTypicalsLayoutManager;
    private AppBarLayout appBarLayout;
    private TextView tagTitle;
    private CollapsingToolbarLayout collapseToolbar;
    private SoulissTag collectedTag;

    private SoulissDBTagHelper datasource;
    private FloatingActionButton fab;
    private TextView mLogoIcon;
    private ImageView mLogoImg;
    private SoulissPreferenceHelper opzioni;
    private SwipeRefreshLayout swipeLayout;
    private long tagId;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast received, refresh from DB");
            swipeLayout.setRefreshing(false);
            SoulissDBHelper.open();
            initDataset(getActivity());
            parallaxExtAdapter.setData(collectedTag);
            parallaxExtAdapter.notifyDataSetChanged();
            mRecyclerView.invalidate();
        }
    };

    public static String getRealPathFromURI(Context ctx, Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = ctx.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;

    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset(Context ctx) {
        datasource = new SoulissDBTagHelper(ctx);
        SoulissDBHelper.open();

        try {
            collectedTag = datasource.getTag(tagId);
        } catch (SQLDataException e) {
            Log.e(Constants.TAG, "CANT LOAD tagId" + tagId);
        }
        Log.i(Constants.TAG, "initDataset tagId" + tagId);
        List<SoulissTypical> favs = datasource.getTagTypicals(collectedTag);
        Log.i(Constants.TAG, "getTagTypicals() returned " + favs.size());
        if (!opzioni.isDbConfigured())
            AlertDialogHelper.dbNotInitedDialog(ctx);
        else {
            //collectedTagTypicals = new SoulissTypical[favs.size()];
            collectedTag.setAssignedTypicals(favs);
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();

        if (opzioni.isLightThemeSelected())
            getActivity().setTheme(R.style.LightThemeSelector);
        else
            getActivity().setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        Bundle extras = getActivity().getIntent().getExtras();
        // recuper nodo da extra
        if (extras != null && extras.get("TAG") != null)
            tagId = (long) extras.get("TAG");
        initDataset(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        rootView.setTag(TAG);
        Log.i(Constants.TAG, "onCreateView with size of data:" + collectedTag.getAssignedTypicals().size());
        appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.appBar_layout);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshContainer);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (collectedTag != null) {
                            UDPHelper.stateRequest(opzioni, 1, collectedTag.getAssignedTypicals().get(0).getNodeId());
                            Log.d(Constants.TAG, "stateRequest for node:" + collectedTag.getAssignedTypicals().get(0).getNodeId());
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
        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshContainer);


        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }

        mCurrentLayoutManagerType = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                LayoutManagerType.GRID_LAYOUT_MANAGER : LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        parallaxExtAdapter = new ParallaxExenderAdapter(opzioni, (TagDetailActivity) getActivity(), collectedTag, tagId);
        //HeaderLayoutManagerFixed layoutManagerFixed = new HeaderLayoutManagerFixed(getActivity());

        //HEADER
        //  View header = getLayoutInflater(null).inflate(R.layout.head_tagdetail, tagContainer, false);
        // layoutManagerFixed.setHeaderIncrementFixer(header);

        mLogoIcon = (TextView) getActivity().findViewById(R.id.imageTagIconFAwe);
        if (collectedTag.getIconResourceId() != 0) {
            FontAwesomeUtil.prepareFontAweTextView(getActivity(), mLogoIcon, collectedTag.getIconResourceId());
        } else
            FontAwesomeUtil.prepareFontAweTextView(getActivity(), mLogoIcon, "fa-tag");

        TypedValue a = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            int color = a.data;
            mLogoIcon.setTextColor(color);
        } else {
            // windowBackground is not a color, probably a drawable
            Drawable d = getActivity().getResources().getDrawable(a.resourceId);
            Log.w(Constants.TAG, "not getting window background");
        }
        //mLogoIcon.setTextColor(getActivity().getResources().getColor(R.color.white));
        mLogoImg = (ImageView) getActivity().findViewById(R.id.photo);


        tagTitle = (TextView) getActivity().findViewById(R.id.tagTextView);
        collapseToolbar = (CollapsingToolbarLayout) getActivity().findViewById(R.id.Collapselayout);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fabTag);
        //EDIT TAG
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = AlertDialogHelper.renameSoulissObjectDialog(getActivity(), tagTitle, null, datasource,
                        collectedTag);
                alert.show();
            }
        });
        fab.hide();
        fab.postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show();
            }
        }, 500);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        for (SoulissTypical typ : collectedTag.getAssignedTypicals()) {
                            UDPHelper.stateRequest(opzioni, 1, typ.getSlot());
                        }
                        //Avvisa solo
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


        if (tagTitle != null && collectedTag != null) {
            tagTitle.setText(getActivity().getResources().getQuantityString(R.plurals.Devices,
                    collectedTag.getAssignedTypicals().size(), collectedTag.getAssignedTypicals().size()));
            // tagTitle.setText(collectedTag.getNiceName());
            collapseToolbar.setTitle(collectedTag.getNiceName());
        }


        if (collectedTag != null && collectedTag.getImagePath() != null) {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                File picture = new File(getRealPathFromURI(getActivity(), Uri.parse(collectedTag.getImagePath())));
                // File picture = new File(Uri.parse(collectedTag.getImagePath()).getPath());
                if (picture.exists()) {
                    //ImageView imageView = (ImageView)findViewById(R.id.imageView);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap myBitmap = BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
                    if (myBitmap.getHeight() > mRecyclerView.getWidth())
                        myBitmap = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth() / 2, myBitmap.getHeight() / 2, true);
                    Log.i(Constants.TAG, "bitmap size " + myBitmap.getRowBytes());
                    mLogoImg.setImageBitmap(myBitmap);

                }
                try {
                    mLogoImg.setImageURI(Uri.parse(collectedTag.getImagePath()));

                } catch (Exception e) {
                    Log.d(TAG, "can't set logo", e);
                }
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.MY_PERMISSIONS_READ_EXT_STORAGE);
            }

        }
        //parallaxExtAdapter.setShouldClipView(true);
        // parallaxExtAdapter.setParallaxHeader(header, mRecyclerView);

        registerForContextMenu(mRecyclerView);

        return rootView;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        //The Refresh must be only active when the offset is zero :
        swipeLayout.setEnabled(i == 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(datareceiver);
        appBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filtere = new IntentFilter();
        filtere.addAction("it.angelic.soulissclient.GOT_DATA");
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        getActivity().registerReceiver(datareceiver, filtere);
        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        refreshStatusIcon();

        Log.i(Constants.TAG, "mCurrentLayoutManagerType: " + mCurrentLayoutManagerType);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(parallaxExtAdapter);


        // Extend the Callback class
        ItemTouchHelper.Callback launcherCallback = new TagDetailFragment.ParallaxGridCallback(getActivity(), parallaxExtAdapter, datasource, collectedTag);
        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(launcherCallback);
        ith.attachToRecyclerView(mRecyclerView);

    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
       /* if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }*/

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mTagTypicalsLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mTagTypicalsLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mTagTypicalsLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mTagTypicalsLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private static class ParallaxGridCallback extends ItemTouchHelper.Callback {
        private final ParallaxExenderAdapter adapter;
        private final Context context;
        View.OnClickListener mOnClickListener;
        private SoulissDBTagHelper database;
        private SoulissTag targetTag;

        public ParallaxGridCallback(Context xct, final ParallaxExenderAdapter adapter, SoulissDBTagHelper database, final SoulissTag targetTag) {
            this.adapter = adapter;
            this.context = xct;
            this.database = database;
            this.targetTag = targetTag;

        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        //and in your imlpementaion of
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            // get the viewHolder's and target's positions in your launcherMainAdapter data, swap them
            Collections.swap(adapter.getItems(), viewHolder.getAdapterPosition(), target.getAdapterPosition());

            // and notify the launcherMainAdapter that its dataset has changed
            adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

            //nel ordine del adapter aggiorno tutti senza complimenti
            database.updateTagTypicalsOrder(adapter.getItems(), targetTag);
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int deletedPosition = viewHolder.getAdapterPosition();
            final SoulissTypical tbr = adapter.getItems().get(deletedPosition);


            adapter.removeAt(deletedPosition);
            // launcherMainAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            //clearView(mRecyclerView, viewHolder);
            database.deleteTagTypical(targetTag.getTagId(), tbr.getNodeId(), tbr.getSlot());
            adapter.notifyDataSetChanged();


        }
    }
}
