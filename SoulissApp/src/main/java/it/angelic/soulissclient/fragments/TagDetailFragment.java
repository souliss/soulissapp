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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.poliveira.parallaxrecycleradapter.HeaderLayoutManagerFixed;
import com.poliveira.parallaxrecycleradapter.ParallaxRecyclerAdapter;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.TagListActivity;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;

import static it.angelic.soulissclient.Constants.TAG;

/**
 * Demonstrates the use of {@link android.support.v7.widget.RecyclerView} with a {@link android.support.v7.widget.LinearLayoutManager} and a
 * {@link android.support.v7.widget.GridLayoutManager}.
 */
public class TagDetailFragment extends AbstractTypicalFragment {

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RadioButton mLinearLayoutRadioButton;
    protected RadioButton mGridLayoutRadioButton;
    protected RecyclerView mRecyclerView;
    protected ParallaxRecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<SoulissTypical> mDataset;
    private SoulissDBTagHelper datasource;
    private SoulissPreferenceHelper opzioni;
    private long tagId;
    private ImageView mLogoImg;
    private SoulissTag fake;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissClient.getOpzioni();

        if (opzioni.isLightThemeSelected())
            getActivity().setTheme(R.style.LightThemeSelector);
        else
            getActivity().setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        Bundle extras = getActivity().getIntent().getExtras();
        // recuper nodo da extra
        if (extras != null && extras.get("TAG") != null)
            tagId = (long) extras.get("TAG");

        initDataset();
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
        datasource = new SoulissDBTagHelper(getActivity());
        datasource.open();
        fake = datasource.getTag(getActivity(), tagId);
        Log.i(Constants.TAG, "SHOW TAG" + tagId);
        List<SoulissTypical> favs = datasource.getTagTypicals(fake);
        Log.i(Constants.TAG, "getTagTypicals() returned" + favs.size());
        if (!opzioni.isDbConfigured())
            AlertDialogHelper.dbNotInitedDialog(getActivity());
        else {
            //mDataset = new SoulissTypical[favs.size()];
            mDataset = favs;
       /* mDataset = new String[DATASET_COUNT];
        for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "This is element #" + i;
        }*/
        }
    }

    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast received, refresh from DB");
            datasource.open();
            initDataset();
            // mAdapter = new ParallaxRecyclerAdapter(mDataset);
            // Set CustomAdapter as the adapter for RecyclerView.
            //    mRecyclerView.setAdapter(mAdapter);
            //  mRecyclerView.invalidate();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        rootView.setTag(TAG);
        Log.i(Constants.TAG, "onCreateView with size of data:" + mDataset.size());
        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                LayoutManagerType.GRID_LAYOUT_MANAGER : LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshStatusIcon();
        mCurrentLayoutManagerType = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                LayoutManagerType.GRID_LAYOUT_MANAGER : LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new ParallaxRecyclerAdapter(mDataset);
        HeaderLayoutManagerFixed layoutManagerFixed = new HeaderLayoutManagerFixed(getActivity());
        mRecyclerView.setLayoutManager(layoutManagerFixed);
        View header = getLayoutInflater(null).inflate(R.layout.head_tagdetail, mRecyclerView, false);
        layoutManagerFixed.setHeaderIncrementFixer(header);
        mLogoImg = (ImageView) header.findViewById(R.id.photo);

        Log.i(Constants.TAG, "setting logo" + fake.getImagePath());
        if (fake != null && fake.getImagePath() != null) {
            mLogoImg.setImageURI(Uri.parse(fake.getImagePath()));

        }
        mAdapter.setShouldClipView(true);
        mAdapter.setParallaxHeader(header, mRecyclerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAdapter.setOnParallaxScroll(new ParallaxRecyclerAdapter.OnParallaxScroll() {
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
                        Drawable d = getActivity().getResources().getDrawable(a.resourceId);

                    }
                }
            });
        }

        mAdapter.setOnClickEvent(new ParallaxRecyclerAdapter.OnClickEvent() {
            @Override
            public void onClick(View view, int i) {

                Log.d(TAG, "Element clicked:" + i);
                ((TagDetailActivity) getActivity()).showDetails(i);


            }
        });

        mAdapter.implementRecyclerAdapterMethods(new ParallaxRecyclerAdapter.RecyclerAdapterMethods() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                // Create a new view.
                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.cardview_typical, viewGroup, false);


                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
                Log.d(TAG, "Element " + position + " set.");
                // Get element from your dataset at this position and replace the contents of the view
                // with that element
                ((ViewHolder) viewHolder).getTextView().setText((CharSequence) mDataset.get(position).getNiceName());
                ((ViewHolder) viewHolder).getTextView().setTag(position);
                mDataset.get(position).setOutputDescView(((ViewHolder) viewHolder).getTextViewInfo1());
                ((ViewHolder) viewHolder).getTextViewInfo2().setText(getString(R.string.update) + " "
                        + Constants.getTimeAgo(mDataset.get(position).getTypicalDTO().getRefreshedAt()));
                ((ViewHolder) viewHolder).getImageView().setImageResource(mDataset.get(position).getIconResourceId());
                LinearLayout sghembo = ((ViewHolder) viewHolder).getLinearActionsLayout();
                sghembo.removeAllViews();
                if (opzioni.isLightThemeSelected()) {
                    ((ViewHolder) viewHolder).getCardView().setCardBackgroundColor(getResources().getColor(R.color.background_floating_material_light));
                }
                //viewHolder.getTextView().setOnClickListener(this);
                if (opzioni.isSoulissReachable()) {
                    // richiama l'overloaded del tipico relativo
                    mDataset.get(position).getActionsLayout(getActivity(), sghembo);
                } else {
                    TextView na = new TextView(getActivity());
                    na.setText(getActivity().getString(R.string.souliss_unavailable));
                    if (opzioni.isLightThemeSelected()) {
                        na.setTextColor(getActivity().getResources().getColor(R.color.black));
                    }
                    sghembo.addView(na);
                }
            }
            // END_INCLUDE(recyclerViewOnCreateViewHolder)

            /**
             * Return the size of your dataset (invoked by the layout manager)
             * chissa perche ogni tanto nullo
             */
           /* @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                Log.d(TAG, "Element clicked:"+pos);
              //  context.showDetails(pos);
            }-*/
            @Override
            public int getItemCount() {
                if (mDataset != null)
                    return mDataset.size();
                else
                    return 0;
            }

        });


        // mAdapter = new FavouriteTypicalAdapter((TagDetailActivity)getActivity(), mDataset, opzioni);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)

    }


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(datareceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filtere = new IntentFilter();
        filtere.addAction("it.angelic.soulissclient.GOT_DATA");
        filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        getActivity().registerReceiver(datareceiver, filtere);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView textViewInfo1;
        private final TextView textViewInfo2;
        private final CardView cardView;

        public CardView getCardView() {
            return cardView;
        }

        LinearLayout linearActionsLayout;
        private ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            /*v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                   // context.showDetails(getPosition());
                }
            });*/
            textView = (TextView) v.findViewById(R.id.TextViewTypicalsTitle);
            imageView = (ImageView) v.findViewById(R.id.card_thumbnail_image2);
            linearActionsLayout = (LinearLayout) v.findViewById(R.id.linearLayoutButtons);
            textViewInfo1 = (TextView) v.findViewById(R.id.TextViewInfoStatus);
            textViewInfo2 = (TextView) v.findViewById(R.id.TextViewInfo2);
            cardView = (CardView) v.findViewById(R.id.TypCard);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public LinearLayout getLinearActionsLayout() {
            return linearActionsLayout;
        }

        public TextView getTextViewInfo1() {
            return textViewInfo1;
        }

        public TextView getTextViewInfo2() {
            return textViewInfo2;
        }

    }

}
