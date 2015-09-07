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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private int lastPosition = -1;
    private LayoutInflater mInflater;
    private SoulissPreferenceHelper opzioni;


    public TagRecyclerAdapter(Activity context, SoulissTag[] versio, SoulissPreferenceHelper opts) {
        mInflater = LayoutInflater.from(context);
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
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cardview_tag, parent, false);

        TagViewHolder hero = new TagViewHolder(itemView);
        return hero;
    }

    @Override
    public void onBindViewHolder(final TagViewHolder holder, final int position) {
        List<SoulissTypical> appoggio = soulissTags[position].getAssignedTypicals();
        // SoulissCommandDTO dto = holder.data.getCommandDTO();
        // if (name == null || "".compareTo(name) == 0)
        // name = context.getString(appoggio.getAliasNameResId());
        holder.textCmd.setText(soulissTags[position].getName());
        String strMeatFormat = "Contains %1$d devices";
        holder.textCmdWhen.setText(String.format(strMeatFormat, appoggio.size()));
        holder.data = soulissTags[position];
        holder.imageTag.setImageDrawable(ContextCompat.getDrawable(context, soulissTags[position].getIconResourceId()));
        // Here you apply the animation when the view is bound
        setAnimation(holder.container, position);

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
                                Pair.create((View) holder.textCmd, "tag_title")
                        );

                ActivityCompat.startActivity(context, nodeDatail, options.toBundle());
                //context.startActivity(nodeDatail);
                //if (opzioni.isAnimationsEnabled())
                //   overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        //holder.image.setImageResource(soulissTags[position].getIconResourceId());
        try{
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

        }catch (Exception io){
            Log.i(Constants.TAG, "cant load image " + holder.data.getImagePath());
            holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home_automation));
        }
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return soulissTags.length;
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition && opzioni.isAnimationsEnabled()) {
            Animation animation = AnimationUtils.loadAnimation(context,  R.anim.slide_in_left);
            animation.setStartOffset(position * 100);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

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
            imageTag = (ImageView) itemView.findViewById(R.id.imageViewTag);
        }


    }
}