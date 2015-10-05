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
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.sql.SQLDataException;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.adapters.ParallaxExenderAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

/**
 * Demonstrates the use of {@link android.support.v7.widget.RecyclerView} with a {@link android.support.v7.widget.LinearLayoutManager} and a
 * {@link android.support.v7.widget.GridLayoutManager}.
 */
public class TagDetailFragment extends AbstractTypicalFragment implements AppBarLayout.OnOffsetChangedListener{

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected ParallaxExenderAdapter parallaxExtAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    private AppBarLayout appBarLayout;
    private SoulissDBTagHelper datasource;
    private FloatingActionButton fab;
    private ImageView mLogoIcon;
    private ImageView mLogoImg;
    private SoulissPreferenceHelper opzioni;
    private long tagId;
    private TextView bro;
    private SoulissTag collectedTag;
    private List<SoulissTypical> collectedTagTypicals;
    private SwipeRefreshLayout swipeLayout;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast received, refresh from DB");
            swipeLayout.setRefreshing(false);
            SoulissDBHelper.open();
            initDataset(getActivity());
            parallaxExtAdapter.setData(collectedTagTypicals);
            parallaxExtAdapter.notifyDataSetChanged();
            mRecyclerView.invalidate();
        }
    };
    private CollapsingToolbarLayout collapseToolbar;

    public static String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = SoulissApp.getAppContext().getContentResolver().query(contentUri, proj, null, null, null);
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
            collectedTag = datasource.getTag(ctx, tagId);
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
            collectedTagTypicals = favs;
       /* collectedTagTypicals = new String[DATASET_COUNT];
        for (int i = 0; i < DATASET_COUNT; i++) {
            collectedTagTypicals[i] = "This is element #" + i;
        }*/
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected id:" + item.getItemId());
        int position = item.getOrder();

        /*ContextMenuRecyclerView.RecyclerContextMenuInfo info =
                (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();*/
        switch (item.getItemId()) {
            case R.id.eliminaTag:
                SoulissTypical soulissTypical = collectedTagTypicals.get(position);
                Log.i(Constants.TAG, "DELETE TYP POS:" + position);
                datasource.deleteTagTypical(collectedTag.getTagId().intValue(), soulissTypical.getNodeId(), soulissTypical.getSlot());
                collectedTagTypicals.remove(position);
                parallaxExtAdapter.setData(collectedTagTypicals);
                parallaxExtAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Device deleted", Toast.LENGTH_SHORT).show();
                mRecyclerView.invalidate();
                break;
            //TODO increase/dec priority
            default:
                Log.i(Constants.TAG, "not doing shit");
                break;
        }
        return super.onContextItemSelected(item);
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
        Log.i(Constants.TAG, "onCreateView with size of data:" + collectedTagTypicals.size());
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
        // LinearLayout tagContainer = (LinearLayout) rootView.findViewById(R.id.tagContainer);

        //mLayoutManager = new LinearLayoutManager(getActivity());


        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }

        mCurrentLayoutManagerType = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                LayoutManagerType.GRID_LAYOUT_MANAGER : LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        parallaxExtAdapter = new ParallaxExenderAdapter(opzioni, collectedTagTypicals, tagId);
        //HeaderLayoutManagerFixed layoutManagerFixed = new HeaderLayoutManagerFixed(getActivity());

        //HEADER
        //  View header = getLayoutInflater(null).inflate(R.layout.head_tagdetail, tagContainer, false);
        // layoutManagerFixed.setHeaderIncrementFixer(header);

        mLogoIcon = (ImageView) getActivity().findViewById(R.id.imageTagIcon);
        if (collectedTag.getIconResourceId() != 0)
            mLogoIcon.setImageResource(collectedTag.getIconResourceId());
        mLogoImg = (ImageView) getActivity().findViewById(R.id.photo);
        bro = (TextView) getActivity().findViewById(R.id.tagTextView);
        collapseToolbar = (CollapsingToolbarLayout) getActivity().findViewById(R.id.Collapselayout);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fabTag);
        //EDIT TAG
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = AlertDialogHelper.renameSoulissObjectDialog(getActivity(), bro, null, datasource,
                        collectedTag);
                alert.show();
            }
        });
        fab.hide(false);
        fab.postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show(true);
            }
        }, 500);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        for (SoulissTypical typ : collectedTagTypicals) {
                            UDPHelper.stateRequest(opzioni, 4, typ.getSlot());
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


        if (bro != null && collectedTag != null) {
            // bro.setText(collectedTag.getNiceName());
            collapseToolbar.setTitle(collectedTag.getNiceName());
        }


        if (collectedTag != null && collectedTag.getImagePath() != null) {

            File picture = new File(getRealPathFromURI(Uri.parse(collectedTag.getImagePath())));

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
        filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
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
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            parallaxExtAdapter.setOnParallaxScroll(new ParallaxRecyclerAdapter.OnParallaxScroll() {
                @SuppressLint("NewApi")
                @Override
                public void onParallaxScroll(float v, float v2, View view) {
                    // actionBar.setBackgroundColor(getActivity().getResources().getColor(R.color.black));
                    Drawable c = actionBar.getBackground();
                    TypedValue a = new TypedValue();
                    getActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
                    if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        // windowBackground is a color
                        int color = a.data;
                        //c.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        Log.d(TAG, "SET BACKG ALPHA" + c.getAlpha());
                        c.setAlpha(Math.round(v * 255));
                        TagDetailFragment.this.actionBar.setBackground(c);
                        // view.setBackground(c);
                    } else {
                        // windowBackground is not a color, probably a drawable
                        Log.e(TAG, "WTF:" + a.toString());
                        // Drawable d = getActivity().getResources().getDrawable(a.resourceId);

                    }
                }
            });
        }*/


     /*   parallaxExtAdapter.setOnClickEvent(new ParallaxRecyclerAdapter.OnClickEvent() {
            @Override
            public void onClick(View view, int i) {

                if (i >= 0) {//puo essere -1
                    Log.d(TAG, "Element clicked:" + i);
                    ((TagDetailActivity) getActivity()).showDetails(i);
                }

            }

        });
*/
        Log.i(Constants.TAG, "mCurrentLayoutManagerType: " + mCurrentLayoutManagerType);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(parallaxExtAdapter);

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
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }


}
