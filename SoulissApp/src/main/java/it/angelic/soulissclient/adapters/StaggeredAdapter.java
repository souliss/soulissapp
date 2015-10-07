package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import junit.framework.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.LauncherElementEnum;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;

public class StaggeredAdapter extends RecyclerView.Adapter<StaggeredAdapter.ViewHolder> {


    private LauncherElement[] launcherElements;
    private Activity context;

    public StaggeredAdapter(Activity context, LauncherElement[] launcherElements) {
        this.launcherElements = launcherElements;
        this.context = context;
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

        switch (item.getComponentEnum()) {
            case SCENES:
                Button sce = (Button) holder.container.findViewById(R.id.ButtonManual);
                break;
            case MANUAL:
                Button man = (Button) holder.container.findViewById(R.id.ButtonManual);
                man.setText("porcatroia");
                break;
            case PROGRAMS:

                break;
            case TYPICAL:

                SoulissTypical tipico = (SoulissTypical) item.getLinkedObject();
                Log.d(Constants.TAG, "Element " + position + " set: last upd: "+Constants.getTimeAgo(tipico.getTypicalDTO().getRefreshedAt()));

                TextView textView = (TextView) holder.container.findViewById(R.id.TextViewTypicalsTitle);
                ImageView imageView = (ImageView) holder.container.findViewById(R.id.card_thumbnail_image2);
                LinearLayout linearActionsLayout = (LinearLayout) holder.container.findViewById(R.id.linearLayoutButtons);
                TextView textViewInfo1 = (TextView)holder.container.findViewById(R.id.TextViewInfoStatus);
                TextView textViewInfo2 = (TextView) holder.container.findViewById(R.id.TextViewInfo2);

                textView.setText(tipico.getNiceName());
                textView.setTag(position);
                tipico.setOutputDescView(textViewInfo1);
                textViewInfo2.setText(SoulissApp.getAppContext().getString(R.string.update) + " "
                        + Constants.getTimeAgo(tipico.getTypicalDTO().getRefreshedAt()));
                imageView.setImageResource(tipico.getIconResourceId());

                linearActionsLayout.removeAllViews();
                tipico.getActionsLayout(SoulissApp.getAppContext(), linearActionsLayout);
                if (SoulissApp.getOpzioni().isLightThemeSelected()) {
                    holder.container.setCardBackgroundColor(SoulissApp.getAppContext().getResources().getColor(R.color.background_floating_material_light));
                }
                break;
            case TAGS:
                final SoulissTag soulissTag = (SoulissTag) item.getLinkedObject();
                TextView textCmd = (TextView) holder.container.findViewById(R.id.TextViewTagTitle);
                TextView textCmdWhen = (TextView) holder.container.findViewById(R.id.TextViewTagDesc);
                final ImageView image = (ImageView) holder.container.findViewById(R.id.imageViewTag);
                final ImageView imageTag = (ImageView) holder.container.findViewById(R.id.imageTagIcon);
                final ImageView shadowbar = (ImageView) holder.container.findViewById(R.id.infoTagAlpha);
                String quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                        0);
                try {
                    List<SoulissTypical> appoggio = soulissTag.getAssignedTypicals();
                    quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                            appoggio.size(), appoggio.size());
                } catch (Exception ce) {
                    Log.w(Constants.TAG, "TAG Empty? ");
                }
                 textCmd.setText(soulissTag.getName());
                 textCmdWhen.setText(quantityString);
                if (soulissTag.getIconResourceId() != 0) {
                     imageTag.setImageResource(soulissTag.getIconResourceId());
                    imageTag.setVisibility(View.VISIBLE);
                } else {
                     imageTag.setImageResource(R.drawable.window);//avoid exc
                     imageTag.setVisibility(View.INVISIBLE);
                }
                // Here you apply the animation when the view is bound
                //setAnimation(holder.container, position);

                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.w(Constants.TAG, "Activating TAG " + soulissTag.getNiceName());
                        Intent nodeDatail = new Intent(SoulissApp.getAppContext(), TagDetailActivity.class);
                        // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                        nodeDatail.putExtra("TAG", soulissTag.getTagId());

                        ActivityOptionsCompat options =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                                        //holder.image,   // The view which starts the transition
                                        //"photo_hero"    // The transitionName of the view we’re transitioning to
                                        Pair.create((View)  image, "photo_hero"),
                                        Pair.create((View)  shadowbar, "shadow_hero"),
                                        Pair.create((View)  imageTag, "tag_icon")
                                );

                        ActivityCompat.startActivity(context, nodeDatail, options.toBundle());
                    }


                });



                holder.container.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return false;//chiama Parentàs onItemClickListener
                    }
                });

                //holder.image.setImageResource(soulissTags[position].getIconResourceId());
                try {
                    File picture = new File(TagDetailFragment.getRealPathFromURI(Uri.parse( soulissTag.getImagePath())));

                    // File picture = new File(Uri.parse(collectedTag.getImagePath()).getPath());
                    if (picture.exists()) {
                        //ImageView imageView = (ImageView)findViewById(R.id.imageView);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        options.inPreferQualityOverSpeed = false;
                        Bitmap myBitmap = BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
                        Log.i(Constants.TAG, "bitmap size " + myBitmap.getRowBytes());
                         image.setImageBitmap(myBitmap);
                    }

                } catch (Exception io) {
                    Log.i(Constants.TAG, "cant load image " + soulissTag.getImagePath());
                     image.setImageDrawable(ContextCompat.getDrawable(SoulissApp.getAppContext(), R.drawable.home_automation));
                }
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
            case TYPICAL:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.cardview_typical, parent, false);
                break;
            case TAGS:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.cardview_tag , parent, false);
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