package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.SoulissUtils;

public class TagRecyclerAdapter extends RecyclerView.Adapter<TagRecyclerAdapter.TagCardViewHolder> {
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

    @Override
    public int getItemCount() {
        return soulissTags.size();
    }

    @Override
    public long getItemId(int position) {
        return soulissTags.get(position).getTagId();
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
    public void onBindViewHolder(final TagCardViewHolder holder, int position) {
        String quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                0);
        String quantityStringTag = "";
        try {
            List<SoulissTypical> appoggio = soulissTags.get(position).getAssignedTypicals();
            quantityString = context.getResources().getQuantityString(R.plurals.Devices,
                    appoggio.size(), appoggio.size());
            quantityStringTag = " - " + context.getResources().getQuantityString(R.plurals.SubTags,
                    soulissTags.get(position).getChildTags().size(), soulissTags.get(position).getChildTags().size());
        } catch (Exception ce) {
            Log.w(Constants.TAG, "TAG Empty? ");
        }
        holder.fabTag = fab;
        holder.textCmd.setText(soulissTags.get(position).getName());
        holder.textCmdWhen.setText(quantityString + quantityStringTag);
        holder.data = soulissTags.get(position);

        FontAwesomeUtil.prepareFontAweTextView(context, holder.imageTag, soulissTags.get(position).getIconResourceId());


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

        //queste vengono eseguite in tempo grazie a postponeEnterTrans
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(Constants.TAG, "setting setTransitionName for subtag:" + holder.data.getTagId());
            holder.image.setTransitionName("photo_hero" + holder.data.getTagId());
            holder.shadowbar.setTransitionName("shadow_hero" + holder.data.getTagId());
            holder.imageTag.setTransitionName("tag_hero" + holder.data.getTagId());
        }

        // Here you apply the animation when the view is bound
        //setAnimation(holder.container, position);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating TAG " + holder.data.getName());
                Intent nodeDatail = new Intent(context, TagDetailActivity.class);
                // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                nodeDatail.putExtra("TAG", holder.data.getTagId());
                if (opzioni.isAnimationsEnabled()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                                    //holder.image,   // The view which starts the transition
                                    //"photo_hero"    // The transitionName of the view we’re transitioning to
                                    Pair.create((View) holder.image, "photo_hero" + holder.data.getTagId()),
                                    Pair.create((View) holder.shadowbar, "shadow_hero" + holder.data.getTagId()),
                                    Pair.create((View) holder.imageTag, "tag_hero" + holder.data.getTagId()),
                                    Pair.create((View) holder.fabTag, "fab_hero")
                            );

                    ActivityCompat.startActivity(context, nodeDatail, options.toBundle());
                } else
                    ActivityCompat.startActivity(context, nodeDatail, null);
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
            File picture = new File(SoulissUtils.getRealPathFromURI(context, Uri.parse(holder.data.getImagePath())));

            // File picture = new File(Uri.parse(collectedTag.getImagePath()).getPath());
            if (picture.exists()) {
                //ImageView imageView = (ImageView)findViewById(R.id.imageView);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap myBitmap = BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
                //Log.i(Constants.TAG, "bitmap size " + myBitmap.getRowBytes());
                holder.image.setImageBitmap(myBitmap);
            }

        } catch (Exception io) {
            Log.i(Constants.TAG, "cant load tag grid image " + holder.data.getImagePath());
            holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home_automation));
        }
    }

    @Override
    public TagCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cardview_tag, parent, false);

        TagCardViewHolder hero = new TagCardViewHolder(itemView);
        return hero;
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
    public static class TagCardViewHolder extends RecyclerView.ViewHolder {

        public TextView imageTag;
        public SoulissTag data;
        public CardView container;
        public ImageView shadowbar;
        public FloatingActionButton fabTag;
        TextView textCmd;
        TextView textCmdWhen;
        ImageView image;

        public TagCardViewHolder(View itemView) {
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