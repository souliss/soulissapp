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

package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.fragments.T16RGBAdvancedFragment;
import it.angelic.soulissclient.fragments.T19SingleChannelLedFragment;
import it.angelic.soulissclient.fragments.T1nGenericLightFragment;
import it.angelic.soulissclient.fragments.T31HeatingFragment;
import it.angelic.soulissclient.fragments.T4nFragment;
import it.angelic.soulissclient.fragments.T5nSensorFragment;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class FavouriteTypicalAdapter extends RecyclerView.Adapter<FavouriteTypicalAdapter.ViewHolder> implements View.OnClickListener{
    private static final String TAG = "CustomAdapter";
    private TagDetailActivity context;
    private SoulissPreferenceHelper opzioni;

    private SoulissTypical[] mDataSet;

    public FavouriteTypicalAdapter(TagDetailActivity context, @NonNull SoulissTypical[] dataSet, SoulissPreferenceHelper opz) {
        this.context = context;
        opzioni = opz;
        mDataSet = dataSet;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_typical, viewGroup, false);


        return new ViewHolder(v);
    }

    // END_INCLUDE(recyclerViewSampleViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
viewHolder.getImageView().setOnClickListener(this);
        viewHolder.getImageView().setTag(position);
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getTextView().setText((CharSequence) mDataSet[position].getNiceName());
        viewHolder.getTextView().setTag(position);
        mDataSet[position].setOutputDescView(viewHolder.getTextViewInfo1());
        viewHolder.getTextViewInfo2().setText(context.getString(R.string.update) + " "
                + Constants.getTimeAgo(mDataSet[position].getTypicalDTO().getRefreshedAt()) );
        viewHolder.getImageView().setImageResource(mDataSet[position].getIconResourceId());
        LinearLayout sghembo = viewHolder.getLinearActionsLayout();
        sghembo.removeAllViews();
        if (opzioni.isLightThemeSelected()){
            viewHolder.getCardView().setCardBackgroundColor(context.getResources().getColor(R.color.background_floating_material_light));
        }
        //viewHolder.getTextView().setOnClickListener(this);
        if (opzioni.isSoulissReachable()) {
            // richiama l'overloaded del tipico relativo
            mDataSet[position].getActionsLayout(context, sghembo);
        } else {
            TextView na = new TextView(context);
            na.setText(context.getString(R.string.souliss_unavailable));
            if (opzioni.isLightThemeSelected()) {
                na.setTextColor(context.getResources().getColor(R.color.black));
            }
            sghembo.addView(na);
        }
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    /**
     * Return the size of your dataset (invoked by the layout manager)
     * chissa perche ogni tanto nullo
     */
    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag();
        Log.d(TAG, "Element clicked:"+pos);
        context.ReturntoActivity(pos);
    }

    @Override
    public int getItemCount() {
        if (mDataSet != null)
            return mDataSet.length;
        else
            return 0;
    }


    // END_INCLUDE(recyclerViewOnBindViewHolder)

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
                   // context.ReturntoActivity(getPosition());
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
