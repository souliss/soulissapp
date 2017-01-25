package it.angelic.soulissclient.adapters;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.SoulissModelException;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.SoulissUtils;

/**
 * solo per implementare la posizione e passare  gli eventi
 * Created by Ale on 08/03/2015.
 */
public class TagDetailParallaxExenderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final TagDetailActivity context;
    protected SoulissTag mDataset;
    private SoulissPreferenceHelper opzioni;
    private long tagId;

    public TagDetailParallaxExenderAdapter(SoulissPreferenceHelper pref, TagDetailActivity father, SoulissTag data, long tagId) {
        super();
        mDataset = data;
        this.tagId = tagId;
        opzioni = pref;
        context = father;
    }

    @Override
    public int getItemCount() {
        if (mDataset != null)
            return mDataset.getAssignedTypicals().size();
        else
            return 0;
    }

    public List<ISoulissObject> getItems() {
        ArrayList<ISoulissObject> tifr = new ArrayList<>();
        tifr.addAll(mDataset.getChildTags());
        tifr.addAll(mDataset.getAssignedTypicals());
        return tifr;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypicalCardViewHolder)
        onBindViewHolderImpl(holder, position);
        else
            throw new SoulissModelException("TOIMPLEMENT");
    }

    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, final int i) {
        Log.d(Constants.TAG, "Element " + i + " set: last upd: " + SoulissUtils.getTimeAgo(mDataset.getAssignedTypicals().get(i).getTypicalDTO().getRefreshedAt()));
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        ((TypicalCardViewHolder) viewHolder).getTextView().setText(
                mDataset.getAssignedTypicals().get(i).getParentNode().getNiceName()
                        + " "
                        + mDataset.getAssignedTypicals().get(i).getNiceName());
        ((TypicalCardViewHolder) viewHolder).getTextView().setTag(i);
        mDataset.getAssignedTypicals().get(i).setOutputDescView(((TypicalCardViewHolder) viewHolder).getTextViewInfo1());
        ((TypicalCardViewHolder) viewHolder).getTextViewInfo2().setText(opzioni.getContx().getString(R.string.update) + " "
                + SoulissUtils.getTimeAgo(mDataset.getAssignedTypicals().get(i).getTypicalDTO().getRefreshedAt()));
        /* Icona del nodo */
        FontAwesomeUtil.prepareFontAweTextView(context, ((TypicalCardViewHolder) viewHolder).getImageView(), mDataset.getAssignedTypicals().get(i).getIconResourceId());
        //((TypicalCardViewHolder) viewHolder).getImageView().setImageResource(mDataset.get(i).getIconResourceId());
        LinearLayout sghembo = ((TypicalCardViewHolder) viewHolder).getLinearActionsLayout();
        sghembo.removeAllViews();
        if (opzioni.isLightThemeSelected()) {
            ((TypicalCardViewHolder) viewHolder).getCardView().setCardBackgroundColor(opzioni.getContx().getResources().getColor(R.color.background_floating_material_light));
        }
        //viewHolder.getTextView().setOnClickListener(this);
        mDataset.getAssignedTypicals().get(i).getActionsLayout(opzioni.getContx(), sghembo);
        ((TypicalCardViewHolder) viewHolder).getCardView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "OnClick");
                context.showDetails(i);
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolderImpl(parent, viewType);

    }

    public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup viewGroup, final int i) {
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

    public void removeAt(int deletedPosition) {
        mDataset.getAssignedTypicals().remove(deletedPosition);
        notifyItemRemoved(deletedPosition);
        notifyItemRangeChanged(deletedPosition, mDataset.getAssignedTypicals().size());
    }

    public void setData(SoulissTag data) {
        mDataset = data;
    }


    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class TagCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView textViewInfo1;
        private final TextView textViewInfo2;
        private final CardView cardView;
        private TextView imageView;
        private LinearLayout linearActionsLayout;

        public TagCardViewHolder(View v) {
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
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class TypicalCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView textViewInfo1;
        private final TextView textViewInfo2;
        private final CardView cardView;
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
