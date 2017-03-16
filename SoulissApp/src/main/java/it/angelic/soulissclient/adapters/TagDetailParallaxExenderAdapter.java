package it.angelic.soulissclient.adapters;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.SoulissModelException;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.SoulissUtils;

/**
 * solo per implementare la posizione e passare  gli eventi
 * Created by Ale on 08/03/2015.
 */
public class TagDetailParallaxExenderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_TAG_TYPICAL = 1;
    public static final int VIEW_TYPE_TAG_NESTED = 2;
    private final TagDetailActivity context;
    protected SoulissTag mDataset;
    private SoulissPreferenceHelper opzioni;

    public TagDetailParallaxExenderAdapter(SoulissPreferenceHelper pref, TagDetailActivity father, SoulissTag data, long tagId) {
        super();
        mDataset = data;
        long tagId1 = tagId;
        opzioni = pref;
        context = father;
    }

    @Override
    public int getItemCount() {
        if (mDataset != null)
            return mDataset.getAssignedTypicals().size() + mDataset.getChildTags().size();
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {

        if (getItems().get(position) instanceof SoulissTypical)
            return VIEW_TYPE_TAG_TYPICAL;
        else if (getItems().get(position) instanceof SoulissTag)
            return VIEW_TYPE_TAG_NESTED;
        else
            throw new SoulissModelException("TOIMPLEMENT");
    }

    /**
     * Non posso ordinare col comparator A MENO
     * di introdurre correttamente TagTypical.java
     * <p>
     * Il typico NON ha order
     *
     * @return
     */
    public List<ISoulissObject> getItems() {
        List<ISoulissObject> tifr = new ArrayList<>();
        List<SoulissTypical> currentTyps = mDataset.getAssignedTypicals();

        tifr.addAll(mDataset.getChildTags());//gia ordinati

        //poi i tipici ordinati
        tifr.addAll(currentTyps);

        /*for (SoulissTag itta :
                mDataset.getChildTags()) {
            //inserisco tag ordinatamente
            for (int i = 0; i < getItemCount(); i++) {
                if (itta.getTagOrder() <= i) {
                    tifr.add(i, itta);
                    break;
                }
                if (i > currentTyps.size()){
                    tifr.add(itta);
                    break;
                }
            }
        }*/
        if (tifr.size() != getItemCount())
            throw new SoulissModelException("REMOVE ME");

        return tifr;
    }

    private void onBindTypicalCardViewHolderImpl(final TagRecyclerAdapter.TagCardViewHolder holder, final int position) {
        String quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                0);
        holder.data = (SoulissTag) getItems().get(position);
        try {
            List<SoulissTypical> appoggio = holder.data.getAssignedTypicals();
            quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                    appoggio.size(), appoggio.size());
        } catch (Exception ce) {
            Log.w(Constants.TAG, "TAG Empty? ");
        }
        holder.textCmd.setText(holder.data.getName() + " (" + context.getString(R.string.subtag) + ") ");
        holder.textCmdWhen.setText(quantityString);

        if (holder.data.getIconResourceId() != 0) {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.imageTag, holder.data.getIconResourceId());
            holder.imageTag.setVisibility(View.VISIBLE);
        } else {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.imageTag, FontAwesomeEnum.fa_tag.getFontName());
        }

        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            int color = a.data;
            holder.imageTag.setTextColor(color);
        } else {
            // windowBackground is not a color, probably a drawable
            Drawable d = context.getResources().getDrawable(a.resourceId);
            Log.w(Constants.TAG, "not getting window background");
        }

        try {
            File picture = new File(TagDetailFragment.getRealPathFromURI(context, Uri.parse(holder.data.getImagePath())));

            // File picture = new File(Uri.parse(collectedTag.getImagePath()).getPath());
            if (picture.exists()) {
                //ImageView imageView = (ImageView)findViewById(R.id.imageView);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inPreferQualityOverSpeed = false;
                Bitmap myBitmap = BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
                Log.d(Constants.TAG, picture.getAbsolutePath() + "loaded, bitmap size " + myBitmap.getRowBytes());
                holder.image.setImageBitmap(myBitmap);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.image.setTransitionName("photo_hero" + holder.data.getTagId());
                }
            }
        } catch (Exception io) {
            Log.i(Constants.TAG, "cant load TAG image " + holder.data.getImagePath());
            holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home_automation));
        }
        //queste vengono eseguite in tempo grazie a postponeEnterTrans
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && opzioni.isAnimationsEnabled()) {
            Log.d(Constants.TAG, "setting setTransitionName for subtag:" + holder.data.getTagId());
            holder.image.setTransitionName("photo_hero" + holder.data.getTagId());
            holder.shadowbar.setTransitionName("shadow_hero" + holder.data.getTagId());
            holder.imageTag.setTransitionName("tag_hero" + holder.data.getTagId());
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating TAG " + position);
                Intent nodeDatail = new Intent(context, TagDetailActivity.class);
                // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                nodeDatail.putExtra("TAG", holder.data.getTagId());
                // nodeDatail.putExtra("FATHERTAG", holder.data.getFatherId());
                if (opzioni.isAnimationsEnabled()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                                    //verso i subtag
                                    Pair.create((View) holder.image, "photo_hero" + holder.data.getTagId()),
                                    Pair.create((View) holder.shadowbar, "shadow_hero" + holder.data.getTagId()),
                                    Pair.create((View) holder.imageTag, "tag_hero" + holder.data.getTagId())
                            );

                    ActivityCompat.startActivity(context, nodeDatail, options.toBundle());
                } else
                    ActivityCompat.startActivity(context, nodeDatail, null);
            }


        });

    }

    private void onBindTypicalCardViewHolderImpl(TypicalCardViewHolder viewHolder, final int i) {
        viewHolder.setData((SoulissTypical) getItems().get(i));
        Log.d(Constants.TAG, "Element " + i + " set: last upd: " + SoulissUtils.getTimeAgo(viewHolder.getData().getTypicalDTO().getRefreshedAt()));

        viewHolder.getTextView().setText(
                viewHolder.getData().getParentNode().getNiceName()
                        + " "
                        + viewHolder.getData().getNiceName());
        viewHolder.getTextView().setTag(i);
        viewHolder.getData().setOutputDescView(viewHolder.getTextViewInfo1());
        viewHolder.getTextViewInfo2().setText(opzioni.getContx().getString(R.string.update) + " "
                + SoulissUtils.getTimeAgo(viewHolder.getData().getTypicalDTO().getRefreshedAt()));
        /* Icona del nodo */
        FontAwesomeUtil.prepareFontAweTextView(context, viewHolder.getImageView(), viewHolder.getData().getIconResourceId());
        //comandi
        LinearLayout sghembo = viewHolder.getLinearActionsLayout();
        sghembo.removeAllViews();
        if (opzioni.isLightThemeSelected()) {
            viewHolder.getCardView().setCardBackgroundColor(opzioni.getContx().getResources().getColor(R.color.background_floating_material_light));
        }
        //viewHolder.getTextView().setOnClickListener(this);
        viewHolder.getData().getActionsLayout(opzioni.getContx(), sghembo);
        viewHolder.getCardView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "OnClick");
                context.showTypical(i, (SoulissTypical) getItems().get(i));
            }
        });
        /* Dario dice di togliere...
        if (opzioni.isSoulissReachable()) {
            // richiama l'overloaded del tipico relativo

        } else {
            TextView na = new TextView(SoulissApp.getAppContext());
            na.setText(SoulissApp.getAppContext().getString(R.string.souliss_unavailable));
            if (opzioni.isLightThemeSelected()) {
                na.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.black));
            }
            sghembo.addView(na);
        }*/

    }

    @Override//bellissimo shine deve dormire di piu'
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypicalCardViewHolder)
            onBindTypicalCardViewHolderImpl((TypicalCardViewHolder) holder, position);
        else if (holder instanceof TagRecyclerAdapter.TagCardViewHolder)
            onBindTypicalCardViewHolderImpl((TagRecyclerAdapter.TagCardViewHolder) holder, position);
        else
            throw new SoulissModelException("TOIMPLEMENT");
    }

    private RecyclerView.ViewHolder onCreateTagViewHolderImpl(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_subtag, viewGroup, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(Constants.TAG, "nel holder TAG click");
            }
        });
        return new TagRecyclerAdapter.TagCardViewHolder(v);
    }

    private RecyclerView.ViewHolder onCreateTypicalViewHolderImpl(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_typical, viewGroup, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(Constants.TAG, "nel holder click");
            }
        });
        return new TypicalCardViewHolder(v);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_TAG_TYPICAL)
            return onCreateTypicalViewHolderImpl(parent);
        else if (viewType == VIEW_TYPE_TAG_NESTED)
            return onCreateTagViewHolderImpl(parent);
        else
            throw new SoulissModelException("TOIMPLEMENT");

    }

    public void removeAt(int deletedPosition) {

        final ISoulissObject tbr = getItems().get(deletedPosition);

        if (tbr instanceof SoulissTypical) {
            mDataset.getAssignedTypicals().remove(tbr);
        } else if (tbr instanceof SoulissTag) {
            mDataset.getChildTags().remove(tbr);
        } else
            throw new SoulissModelException("E ADESSO DOVE SI VA?");

        notifyItemRemoved(deletedPosition);
        //notifyItemRangeChanged(deletedPosition, mDataset.getAssignedTypicals().size());
    }

    public void setData(SoulissTag data) {
        mDataset = data;
    }

    public void swap(int adapterPosition, int TgtadapterPosition1) throws SoulissModelException {
        ISoulissObject start = getItems().get(adapterPosition);
        ISoulissObject to = getItems().get(TgtadapterPosition1);

        if ((start instanceof SoulissTypical && to instanceof SoulissTypical)
                || (start instanceof SoulissTag && to instanceof SoulissTag)) {

            if (start instanceof SoulissTag) {
                Collections.swap(mDataset.getChildTags(), adapterPosition, TgtadapterPosition1);

            } else if (start instanceof SoulissTypical) {
                Collections.swap(mDataset.getAssignedTypicals(), adapterPosition - mDataset.getChildTags().size(), TgtadapterPosition1 - mDataset.getChildTags().size());
            }
        } else {
            throw new SoulissModelException("DA GESTIRE NEL CHIAMANTE");
        }

        notifyItemMoved(adapterPosition, TgtadapterPosition1);
    }


    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class TypicalCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView textViewInfo1;
        private final TextView textViewInfo2;
        private final CardView cardView;
        private SoulissTypical data;
        private TextView imageView;
        private LinearLayout linearActionsLayout;


        public TypicalCardViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.TextViewTypicalsTitle);
            imageView = (TextView) v.findViewById(R.id.card_thumbnail_image2);
            linearActionsLayout = (LinearLayout) v.findViewById(R.id.linearLayoutButtons);
            textViewInfo1 = (TextView) v.findViewById(R.id.TextViewInfoStatus);
            textViewInfo2 = (TextView) v.findViewById(R.id.TextViewInfo2);
            cardView = (CardView) v.findViewById(R.id.TypCard);
            //v.setOnCreateContextMenuListener(this);
        }

        public CardView getCardView() {
            return cardView;
        }

        public SoulissTypical getData() {
            return data;
        }

        public void setData(SoulissTypical data) {
            this.data = data;
        }

        public TextView getImageView() {
            return imageView;
        }

        public LinearLayout getLinearActionsLayout() {
            return linearActionsLayout;
        }

        public TextView getTextView() {
            return textView;
        }

        public TextView getTextViewInfo1() {
            return textViewInfo1;
        }

        public TextView getTextViewInfo2() {
            return textViewInfo2;
        }


    }
}
