package it.angelic.soulissclient.adapters;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * solo per implementare la posizione e passare  gli eventi
 * Created by Ale on 08/03/2015.
 */
public class ParallaxExenderAdapter extends ParallaxRecyclerAdapter {
    protected List<SoulissTypical> mDataset;
    private long tagId;
    private SoulissPreferenceHelper opzioni;



    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, ParallaxRecyclerAdapter parallaxRecyclerAdapter, int i) {
        Log.d(Constants.TAG, "Element " + i + " set.");
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        ((ViewHolder) viewHolder).getTextView().setText(mDataset.get(i).getNiceName());
        ((ViewHolder) viewHolder).getTextView().setTag(i);
        mDataset.get(i).setOutputDescView(((ViewHolder) viewHolder).getTextViewInfo1());
        ((ViewHolder) viewHolder).getTextViewInfo2().setText(SoulissClient.getAppContext().getString(R.string.update) + " "
                + Constants.getTimeAgo(mDataset.get(i).getTypicalDTO().getRefreshedAt()));
        ((ViewHolder) viewHolder).getImageView().setImageResource(mDataset.get(i).getIconResourceId());
        LinearLayout sghembo = ((ViewHolder) viewHolder).getLinearActionsLayout();
        sghembo.removeAllViews();
        if (opzioni.isLightThemeSelected()) {
            ((ViewHolder) viewHolder).getCardView().setCardBackgroundColor(SoulissClient.getAppContext().getResources().getColor(R.color.background_floating_material_light));
        }
        //viewHolder.getTextView().setOnClickListener(this);
        if (opzioni.isSoulissReachable()) {
            // richiama l'overloaded del tipico relativo
            mDataset.get(i).getActionsLayout(SoulissClient.getAppContext(), sghembo);
        } else {
            TextView na = new TextView(SoulissClient.getAppContext());
            na.setText(SoulissClient.getAppContext().getString(R.string.souliss_unavailable));
            if (opzioni.isLightThemeSelected()) {
                na.setTextColor(SoulissClient.getAppContext().getResources().getColor(R.color.black));
            }
            sghembo.addView(na);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup viewGroup, ParallaxRecyclerAdapter parallaxRecyclerAdapter, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_typical, viewGroup, false);
        opzioni = SoulissClient.getOpzioni();
        return new ViewHolder(v);
    }

    @Override
    public int getItemCountImpl(ParallaxRecyclerAdapter parallaxRecyclerAdapter) {

        if (mDataset != null)
            return mDataset.size();
        else
            return 0;
    }

    public ParallaxExenderAdapter(List data, long tagId) {
        super(data);
        mDataset = data;
        this.tagId = tagId;
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final TextView textView;
        private final TextView textViewInfo1;
        private final TextView textViewInfo2;
        private final CardView cardView;
        private LinearLayout linearActionsLayout;
        private ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.TextViewTypicalsTitle);
            imageView = (ImageView) v.findViewById(R.id.card_thumbnail_image2);
            linearActionsLayout = (LinearLayout) v.findViewById(R.id.linearLayoutButtons);
            textViewInfo1 = (TextView) v.findViewById(R.id.TextViewInfoStatus);
            textViewInfo2 = (TextView) v.findViewById(R.id.TextViewInfo2);
            cardView = (CardView) v.findViewById(R.id.TypCard);
            v.setOnCreateContextMenuListener(this);
        }

        public CardView getCardView() {
            return cardView;
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

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(Menu.NONE, R.id.eliminaTag, Menu.NONE, R.string.tag_delete);

        }

    }
}
