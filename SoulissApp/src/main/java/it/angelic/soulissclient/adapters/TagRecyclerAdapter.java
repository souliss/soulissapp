package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;

public class TagRecyclerAdapter extends RecyclerView.Adapter<TagRecyclerAdapter.TagViewHolder> {
    SoulissTag[] soulissTags;
    private Activity context;
    // Allows to remember the last item shown on screen

    private SoulissPreferenceHelper opzioni;


    public TagRecyclerAdapter(Activity context,@NonNull SoulissTag[] versio, SoulissPreferenceHelper opts) {
      //  mInflater = LayoutInflater.from(context);
        this.context = context;
        this.soulissTags = versio;
        opzioni = opts;
    }

    public Object getTag(int position) {
        return soulissTags[position];
    }

    public SoulissTag[] getTagArray() {
        return soulissTags;
    }

    public void setTagArray(SoulissTag[] scene) {
        this.soulissTags = scene;
    }



    @Override
    public void onViewRecycled(TagViewHolder holder) {
        holder.container.setOnClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cardview_tag, parent, false);

        TagViewHolder hero = new TagViewHolder(itemView);
        return hero;
    }

    @Override
    public void onBindViewHolder(final TagViewHolder holder, final int position) {
        String quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                0 );
        try {
            List<SoulissTypical> appoggio = soulissTags[position].getAssignedTypicals();
            quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                    appoggio.size(), appoggio.size() );
        } catch (Exception ce) {
            Log.w(Constants.TAG, "TAG Empty? ");
        }

        holder.textCmd.setText(soulissTags[position].getName());
        holder.textCmdWhen.setText(quantityString);
        holder.data = soulissTags[position];
        if (soulissTags[position].getIconResourceId() != 0) {
            holder.imageTag.setImageResource(soulissTags[position].getIconResourceId());
            holder.imageTag.setVisibility(View.VISIBLE);
        } else
            holder.imageTag.setVisibility(View.INVISIBLE);
        // Here you apply the animation when the view is bound
        //setAnimation(holder.container, position);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating TAG " + position);
                Intent nodeDatail = new Intent(context, TagDetailActivity.class);
                // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                nodeDatail.putExtra("TAG", soulissTags[position].getTagId());

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                                //holder.image,   // The view which starts the transition
                                //"photo_hero"    // The transitionName of the view we’re transitioning to
                                Pair.create((View) holder.image, "photo_hero"),
                                Pair.create((View) holder.imageTag, "tag_icon")
                        );

                ActivityCompat.startActivity(context, nodeDatail, options.toBundle());
            }
        });
        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        //holder.image.setImageResource(soulissTags[position].getIconResourceId());
        try {
            File picture = new File(TagDetailFragment.getRealPathFromURI(Uri.parse(holder.data.getImagePath())));

            // File picture = new File(Uri.parse(collectedTag.getImagePath()).getPath());
            if (picture.exists()) {
                //ImageView imageView = (ImageView)findViewById(R.id.imageView);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inPreferQualityOverSpeed = false;
                Bitmap myBitmap = BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
                Log.i(Constants.TAG, "bitmap size " + myBitmap.getRowBytes());
                holder.image.setImageBitmap(myBitmap);
            }

        } catch (Exception io) {
            Log.i(Constants.TAG, "cant load image " + holder.data.getImagePath());
            holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home_automation));
        }
    }
    @Override
    public long getItemId(int position) {
        return soulissTags[position].getTagId();
    }

    @Override
    public int getItemCount() {
        return soulissTags.length;
    }

    /**
     * Here is the key method to apply the animation
     * <p/>
     * private void setAnimation(View viewToAnimate, int position) {
     * // If the bound view wasn't previously displayed on screen, it's animated
     * if ( opzioni.isAnimationsEnabled()) {
     * Animation animation = AnimationUtils.loadAnimation(context,  R.anim.slide_in_left);
     * animation.setStartOffset(position * 100);
     * viewToAnimate.startAnimation(animation);
     * lastPosition = position;
     * }
     * }
     */
    public static class TagViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageTag;
        public SoulissTag data;
        public CardView container;
        TextView textCmd;
        TextView textCmdWhen;
        ImageView image;

        public TagViewHolder(View itemView) {
            super(itemView);
            textCmd = (TextView) itemView.findViewById(R.id.TextViewTagTitle);
            textCmdWhen = (TextView) itemView.findViewById(R.id.TextViewTagDesc);
            image = (ImageView) itemView.findViewById(R.id.imageViewTag);
            container = (CardView) itemView.findViewById(R.id.TagCard);
            imageTag = (ImageView) itemView.findViewById(R.id.imageTagIcon);
        }


    }
}