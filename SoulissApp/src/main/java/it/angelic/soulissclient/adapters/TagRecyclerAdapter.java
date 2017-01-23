package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;

public class TagRecyclerAdapter extends RecyclerView.Adapter<TagRecyclerAdapter.TagViewHolder> {
    private final FloatingActionButton fab;
    List<SoulissTag> soulissTags;
    private Activity context;
    // Allows to remember the last item shown on screen

    private SoulissPreferenceHelper opzioni;


    public TagRecyclerAdapter(Activity context, @NonNull List<SoulissTag> versio, SoulissPreferenceHelper opts, FloatingActionButton fab) {
        //  mInflater = LayoutInflater.from(context);
        this.context = context;
        this.soulissTags = versio;
        opzioni = opts;
        this.fab = fab;
    }

    public SoulissTag getTag(int position) {
        return soulissTags.get(position);
    }

    public List<SoulissTag> getTagArray() {
        return soulissTags;
    }


    public void setTagArray(List<SoulissTag> scene) {
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
        String quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                0);
        try {
            List<SoulissTypical> appoggio = soulissTags.get(position).getAssignedTypicals();
            quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                    appoggio.size(), appoggio.size());
        } catch (Exception ce) {
            Log.w(Constants.TAG, "TAG Empty? ");
        }
        holder.fabTag = fab;
        holder.textCmd.setText(soulissTags.get(position).getName());
        holder.textCmdWhen.setText(quantityString);
        holder.data = soulissTags.get(position);
        if (soulissTags.get(position).getIconResourceId() != 0) {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.imageTag, soulissTags.get(position).getIconResourceId());
            // holder.imageTag.setImageResource(soulissTags[position].getIconResourceId());
            holder.imageTag.setVisibility(View.VISIBLE);
        } else {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.imageTag, FontAwesomeEnum.fa_tag.getFontName());
            //holder.imageTag.setImageResource(R.drawable.window);//avoid exc
            // holder.imageTag.setVisibility(View.INVISIBLE);
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



        // Here you apply the animation when the view is bound
        //setAnimation(holder.container, position);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating TAG " + position);
                Intent nodeDatail = new Intent(context, TagDetailActivity.class);
                // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                nodeDatail.putExtra("TAG", soulissTags.get(position).getTagId());

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                                //holder.image,   // The view which starts the transition
                                //"photo_hero"    // The transitionName of the view we’re transitioning to
                                Pair.create((View) holder.image, "photo_hero"),
                                Pair.create((View) holder.shadowbar, "shadow_hero"),
                                Pair.create((View) holder.imageTag, "tag_hero"),
                                Pair.create((View) holder.fabTag, "fab_hero")
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
            File picture = new File(TagDetailFragment.getRealPathFromURI(context, Uri.parse(holder.data.getImagePath())));

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
        return soulissTags.get(position).getTagId();
    }

    @Override
    public int getItemCount() {
        return soulissTags.size();
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

        private final TextView imageTag;
        public SoulissTag data;
        public CardView container;
        TextView textCmd;
        TextView textCmdWhen;
        ImageView image;
        public ImageView shadowbar;
        public FloatingActionButton fabTag;

        public TagViewHolder(View itemView) {
            super(itemView);
            textCmd = (TextView) itemView.findViewById(R.id.TextViewTagTitle);
            textCmdWhen = (TextView) itemView.findViewById(R.id.TextViewTagDesc);
            image = (ImageView) itemView.findViewById(R.id.imageViewTag);
            container = (CardView) itemView.findViewById(R.id.TagCard);
            imageTag = (TextView) itemView.findViewById(R.id.imageTagIconFa);
            shadowbar = (ImageView) itemView.findViewById(R.id.infoTagAlpha);
        }


    }
}