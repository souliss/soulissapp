package it.angelic.soulissclient.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.LauncherElementEnum;

public class StaggeredAdapter extends RecyclerView.Adapter<StaggeredAdapter.ViewHolder> {


    private LauncherElement[] launcherElements;

    public StaggeredAdapter(LauncherElement[] launcherElements) {
        this.launcherElements = launcherElements;
    }

    public List getList(){
        return Arrays.asList(launcherElements);
    }
    @Override
    public int getItemCount() {
        return launcherElements.length;
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return launcherElements[position].getComponentEnum().ordinal();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LauncherElement item = launcherElements[position];
        LauncherElementEnum enumVal = LauncherElementEnum.values()[position];

        //holder.container.removeAllViews();
        //holder.textView.setText(item.title);
       // holder.container = launcherElements[position].inflateCardView();

        final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) lp;
            sglp.setFullSpan(item.isFullSpan());
            Log.w(Constants.TAG, "Full span for element?"+holder.getItemViewType() );
            holder.itemView.setLayoutParams(sglp);
        }

        switch (enumVal) {
            case SCENES:
                Button sce = (Button) holder.container.findViewById(R.id.ButtonManual);
                break;
            case MANUAL:
                Button man = (Button) holder.container.findViewById(R.id.ButtonManual);
                man.setText("porcatroia");
                break;
            case PROGRAMS:

                break;
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LauncherElementEnum enumVal = LauncherElementEnum.values()[viewType];
        View itemView =  LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cardview_launcher2, parent, false);
        switch (enumVal) {

            case SCENES:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.card_info_service, parent, false);

                break;
            case MANUAL:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.card_button_manual, parent, false);
                break;
            case PROGRAMS:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.card_button_manual, parent, false);
                break;


        }
        return new ViewHolder(itemView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView container;

        public ViewHolder(View itemView) {
            super(itemView);
            container = (CardView) itemView.getRootView();
        }

        //enum componentEnum


    }
}