package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.NodeDetailActivity;
import it.angelic.soulissclient.NodesListActivity;
import it.angelic.soulissclient.ProgramListActivity;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SceneListActivity;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.TagGridActivity;
import it.angelic.soulissclient.TypicalDetailFragWrapper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.LauncherElementEnum;
import it.angelic.soulissclient.util.SoulissUtils;

import static it.angelic.soulissclient.Constants.TAG;

public class StaggeredDashboardElementAdapter extends RecyclerView.Adapter<StaggeredDashboardElementAdapter.ViewHolder> {

    private final Activity context;
    private List<LauncherElement> launcherElements;
    private SoulissDataService mBoundService;

    public StaggeredDashboardElementAdapter(Activity context, List<LauncherElement> launcherElements, SoulissDataService mBoundService) {
        this.launcherElements = launcherElements;
        this.context = context;
        this.mBoundService = mBoundService;
    }

    private void bindNodeElement(ViewHolder holder, LauncherElement item) {
        final SoulissNode nodo = (SoulissNode) item.getLinkedObject();
        Log.d(Constants.TAG, "Launcher Element node " + nodo.getNiceName() + "set: last upd: " + SoulissUtils.getTimeAgo(nodo.getRefreshedAt()));

        TextView textView = holder.container.findViewById(R.id.TextViewTypicalsTitle);
        TextView imageView = holder.container.findViewById(R.id.card_thumbnail_image2);
        TextView textViewInfo1 = holder.container.findViewById(R.id.TextViewInfoNodo1);
        TextView textViewInfo2 = holder.container.findViewById(R.id.TextViewInfoNodo2);

        textView.setText(nodo.getNiceName());

        textViewInfo1.setText(context.getResources().getQuantityString(R.plurals.Devices,
                nodo.getActiveTypicals().size(), nodo.getActiveTypicals().size()));

        //textView.setTag(position);
        textViewInfo2.setText(context.getString(R.string.update) + " " + SoulissUtils.getTimeAgo(nodo.getRefreshedAt()) + context.getString(R.string.health) + nodo.getHealthPercent());
        // imageView.setImageResource(FontAwesomeUtil.remapIconResId(tipico.getIconResourceId()));
        FontAwesomeUtil.prepareFontAweTextView(context, imageView, nodo.getIconResourceId());
        //tipico.getActionsLayout(SoulissApp.getAppContext(), linearActionsLayout);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating NODE " + nodo.getNiceName());
                Intent nodeDatail = new Intent(context, NodeDetailActivity.class);
                // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                nodeDatail.putExtra("NODO", nodo.getNodeId());
                context.startActivity(nodeDatail);
            }


        });
    }

    private void bindSceneElement(ViewHolder holder, LauncherElement item) {
        final SoulissScene nodo = (SoulissScene) item.getLinkedObject();
        Log.d(Constants.TAG, "Launcher Element scenesList " + nodo.getName());

        TextView commandIcon = holder.container.findViewById(R.id.command_icon);
        TextView textViewCommand = holder.container.findViewById(R.id.TextViewCommand);
        TextView textViewCommandWhen = holder.container.findViewById(R.id.TextViewCommandWhen);
        Button exe = holder.container.findViewById(R.id.sceneBtn);
        textViewCommand.setText(nodo.getNiceName());
        String strMeatFormat = context.getString(R.string.scene_subtitle);
        textViewCommandWhen.setText(String.format(strMeatFormat, nodo.getCommandArray().size()));

        FontAwesomeUtil.prepareFontAweTextView(context, commandIcon, nodo.getIconResourceId());
        exe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating SCENE " + nodo.getNiceName());
                nodo.execute();
            }


        });
    }

    private void bindTagElement(ViewHolder holder, LauncherElement item) {
        final SoulissTag soulissTag = (SoulissTag) item.getLinkedObject();
        TextView textCmd = holder.container.findViewById(R.id.TextViewTagTitle);
        TextView textCmdWhen = holder.container.findViewById(R.id.TextViewTagDesc);
        final ImageView image = holder.container.findViewById(R.id.imageViewTag);
        final TextView imageTag = holder.container.findViewById(R.id.imageTagIconFa);
        final ImageView shadowbar = holder.container.findViewById(R.id.infoTagAlpha);
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
            FontAwesomeUtil.prepareFontAweTextView(context, imageTag, soulissTag.getIconResourceId());
            imageTag.setVisibility(View.VISIBLE);
        } else {
            FontAwesomeUtil.prepareFontAweTextView(context, imageTag, FontAwesomeEnum.fa_window_maximize.getFontName());
            imageTag.setVisibility(View.INVISIBLE);
        }
        // Here you apply the animation when the view is bound
        //setAnimation(holder.container, position);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating TAG " + soulissTag.getNiceName());
                Intent nodeDatail = new Intent(context, TagDetailActivity.class);
                // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                nodeDatail.putExtra("TAG", soulissTag.getTagId());

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                                //holder.image,   // The view which starts the transition
                                //"photo_hero"    // The transitionName of the view we’re transitioning to
                                Pair.create((View) image, "photo_hero"),
                                Pair.create((View) shadowbar, "shadow_hero"),
                                Pair.create((View) imageTag, "tag_icon")
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
            File picture = new File(SoulissUtils.getRealPathFromURI(context, Uri.parse(soulissTag.getImagePath())));

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
            image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home_automation));
        }
    }

    private void bindTypicalElement(ViewHolder holder, LauncherElement item) {
        final SoulissTypical tipico = (SoulissTypical) item.getLinkedObject();
        Log.d(Constants.TAG, "Launcher Element typical " + tipico.getNiceName() + "set: last upd: " + SoulissUtils.getTimeAgo(tipico.getTypicalDTO().getRefreshedAt()));

        TextView textView = holder.container.findViewById(R.id.TextViewTypicalsTitle);
        TextView imageView = holder.container.findViewById(R.id.card_thumbnail_image2);
        LinearLayout linearActionsLayout = holder.container.findViewById(R.id.linearLayoutButtons);
        TextView textViewInfo1 = holder.container.findViewById(R.id.TextViewInfoStatus);
        TextView textViewInfo2 = holder.container.findViewById(R.id.TextViewInfo2);

        textView.setText(tipico.getNiceName());

        tipico.setOutputDescView(textViewInfo1);
        textViewInfo2.setText(SoulissUtils.getTimeAgo(tipico.getTypicalDTO().getRefreshedAt()));
        // imageView.setImageResource(FontAwesomeUtil.remapIconResId(tipico.getIconResourceId()));
        FontAwesomeUtil.prepareFontAweTextView(context, imageView, tipico.getIconResourceId());
        linearActionsLayout.removeAllViews();


        List<ISoulissCommand> pi = tipico.getCommands(context);
        for (final ISoulissCommand cmd : pi) {
            //  LinearLayout view = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.button_flat, null);
            // or LinearLayout buttonView = (LinearLayout)this.getLayoutInflater().inflate(R.layout.my_button, null);
            Button myButton = (AppCompatButton) LayoutInflater.from(context).inflate(R.layout.button_flat, linearActionsLayout, false);
            //myButton.setId(myButton.getId());
            myButton.setText(cmd.getName());
            // view.removeView(myButton);
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cmd.execute();
                }
            });
            linearActionsLayout.addView(myButton);

            //non ce ne stanno piu di due
            if (linearActionsLayout.getChildCount() >= 2)
                break;
        }
        //linearActionsLayout.removeAllViews();
        // LinearLayout ll = (LinearLayout)context.getLayoutInflater().inflate(R.layout.button_flat, linearActionsLayout);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(Constants.TAG, "Activating TYP" + tipico.getNiceName());
                Intent nodeDatail = new Intent(context, TypicalDetailFragWrapper.class);
                // TagRecyclerAdapter.TagViewHolder holder = ( TagRecyclerAdapter.TagViewHolder holder) view;
                nodeDatail.putExtra("TIPICO", tipico);
                context.startActivity(nodeDatail);
            }


        });
    }

    @Override
    public int getItemCount() {
        return launcherElements.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return launcherElements.get(position).getComponentEnum().ordinal();
    }

    public List<LauncherElement> getLauncherElements() {
        return launcherElements;
    }

    public void setLauncherElements(List<LauncherElement> in) {
        launcherElements = in;
        notifyDataSetChanged();
    }

    public LauncherElement getLocationLauncherElements() {

        for (LauncherElement lal :
                launcherElements) {
            if (lal.getComponentEnum().equals(LauncherElementEnum.STATIC_LOCATION))
                return lal;
        }

        return null;
    }

    public SoulissDataService getmBoundService() {
        return mBoundService;
    }

    public void setmBoundService(SoulissDataService mBoundService) {
        this.mBoundService = mBoundService;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LauncherElement item = launcherElements.get(position);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        //holder.container.removeAllViews();
        //holder.textView.setText(item.title);
        // holder.container = launcherElements[position].inflateCardView();

        final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) lp;
            sglp.setFullSpan(item.isFullSpan());
            //Log.d(Constants.TAG, "Full span for element?" + holder.getItemViewType());
            holder.itemView.setLayoutParams(sglp);

        }
        //qui la view c'e` gia
        switch (item.getComponentEnum()) {
            case STATIC_LOCATION:
                View viewLineL = holder.container.findViewById(R.id.StaticTileLine);
                TextView txtTitL = holder.container.findViewById(R.id.card_static_title);
                TextView txtDescL = holder.container.findViewById(R.id.card_static_desc);
                TextView txtAwesomL = holder.container.findViewById(R.id.card_thumbnail_fa);
                FontAwesomeUtil.prepareFontAweTextView(context, txtAwesomL, FontAwesomeEnum.fa_location_arrow.getFontName());
                txtTitL.setText(item.getTitle());
                txtDescL.setText(item.getDesc());

                viewLineL.setBackgroundColor(context.getResources().getColor(R.color.md_blue_grey_500));
                break;
            case STATIC_SCENES:
                View viewLine = holder.container.findViewById(R.id.StaticTileLine);
                TextView txtTit = holder.container.findViewById(R.id.card_static_title);
                TextView txtDesc = holder.container.findViewById(R.id.card_static_desc);
                TextView txtAwesom = holder.container.findViewById(R.id.card_thumbnail_fa);
                FontAwesomeUtil.prepareFontAweTextView(context, txtAwesom, FontAwesomeEnum.fa_moon_o.getFontName());
                txtTit.setText(item.getTitle());
                txtDesc.setText(item.getDesc());
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent nodeDatail = new Intent(context, SceneListActivity.class);
                        context.startActivity(nodeDatail);
                    }


                });
                viewLine.setBackgroundColor(context.getResources().getColor(R.color.std_yellow));
                break;
            case STATIC_MANUAL:
                View viewLine2 = holder.container.findViewById(R.id.StaticTileLine);
                TextView txtTit2 = holder.container.findViewById(R.id.card_static_title);
                TextView txtDesc2 = holder.container.findViewById(R.id.card_static_desc);
                TextView txtAwesom2 = holder.container.findViewById(R.id.card_thumbnail_fa);
                FontAwesomeUtil.prepareFontAweTextView(context, txtAwesom2, FontAwesomeEnum.fa_codepen.getFontName());
                txtTit2.setText(item.getTitle());
                txtDesc2.setText(item.getDesc());
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent nodeDatail = new Intent(context, NodesListActivity.class);
                        context.startActivity(nodeDatail);
                    }


                });
                viewLine2.setBackgroundColor(context.getResources().getColor(R.color.std_green));
                break;
            case STATIC_PROGRAMS:
                View viewLine3 = holder.container.findViewById(R.id.StaticTileLine);
                TextView txtTit3 = holder.container.findViewById(R.id.card_static_title);
                TextView txtDesc3 = holder.container.findViewById(R.id.card_static_desc);
                TextView txtAwesom3 = holder.container.findViewById(R.id.card_thumbnail_fa);
                FontAwesomeUtil.prepareFontAweTextView(context, txtAwesom3, "fa-calendar");
                txtTit3.setText(item.getTitle());
                txtDesc3.setText(item.getDesc());
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent nodeDatail = new Intent(context, ProgramListActivity.class);
                        context.startActivity(nodeDatail);
                    }


                });
                viewLine3.setBackgroundColor(context.getResources().getColor(R.color.std_blue));
                break;
            case STATIC_TAGS:
                View viewLine34 = holder.container.findViewById(R.id.StaticTileLine);
                TextView txtTit34 = holder.container.findViewById(R.id.card_static_title);
                TextView txtDesc34 = holder.container.findViewById(R.id.card_static_desc);
                TextView txtAwesom34 = holder.container.findViewById(R.id.card_thumbnail_fa);
                FontAwesomeUtil.prepareFontAweTextView(context, txtAwesom34, "fa-tags");
                txtTit34.setText(item.getTitle());
                txtDesc34.setText(item.getDesc());
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent nodeDatail = new Intent(context, TagGridActivity.class);
                        context.startActivity(nodeDatail);
                    }
                });
                viewLine34.setBackgroundColor(context.getResources().getColor(R.color.std_purple));
                break;
            case TYPICAL:
                bindTypicalElement(holder, item);
                break;
            case SCENE:

                bindSceneElement(holder, item);

                break;
            case NODE:
                bindNodeElement(holder, item);
                break;
            case STATIC_STATUS:
                TextView textCmdsd = holder.container.findViewById(R.id.textViewBasicInfo);
                TextView textCmdWhens = holder.container.findViewById(R.id.textViewBasicInfoLittle);
                setServiceInfo(textCmdsd, textCmdWhens);
                break;
            case TAG:
                bindTagElement(holder, item);
                break;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LauncherElementEnum enumVal = LauncherElementEnum.values()[viewType];
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cardview_launcher2, parent, false);
        switch (enumVal) {
            case STATIC_LOCATION:
            case STATIC_SCENES:
            case STATIC_PROGRAMS:
            case STATIC_MANUAL:
            case STATIC_TAGS:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.card_button_static, parent, false);
                break;
            case TYPICAL:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.cardview_typical_vertical, parent, false);
                break;
            case NODE:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.cardview_node_vertical, parent, false);
                break;
            case TAG:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.cardview_tag, parent, false);
                break;
            case SCENE:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.cardview_scene, parent, false);
                break;
            case STATIC_STATUS:
                itemView = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.cardview_basicinfo, parent, false);
                break;


        }
        return new ViewHolder(itemView);
    }

    public void removeAt(int position) {
        launcherElements.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, launcherElements.size());
    }

    private void setServiceInfo(TextView basinfo, TextView serviceInfo) {

        SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        //basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_green));
        // check se IP non settato check system configured
        if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissPublicIpConfigured()) {
            basinfo.setText(Html.fromHtml(context.getString(R.string.notconfigured)));
            //basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_red));
            return;
        }
        if (!opzioni.getCustomPref().contains("connectionName")) {
            basinfo.setText(context.getString(R.string.warn_connection));
            //basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_yellow));
            return;
        }
        if (!opzioni.isSoulissPublicIpConfigured()
                && !("WIFI".compareTo(opzioni.getCustomPref().getString("connectionName", "")) == 0)) {
            basinfo.setText(Html.fromHtml(context.getString(R.string.warn_wifi)));
            //	basinfoLine.setBackgroundColor(this.getResources().getColor(R.color.std_red));
            return;
        }
        String base = opzioni.getCachedAddress();
        Log.d(TAG, "cached Address: " + base + " backoff: " + opzioni.getBackoff());
        if (base != null && "".compareTo(base) != 0) {
            basinfo.setText(Html.fromHtml(context.getString(R.string.contact_at) + "<font color=\"#99CC00\"><b> " + base
                    + "</b></font> via <b>" + opzioni.getCustomPref().getString("connectionName", "ERROR") + "</b>"));
        } else if (base != null && context.getString(R.string.unavailable).compareTo(base) != 0) {
            basinfo.setText(context.getString(R.string.souliss_unavailable));
        } else {
            basinfo.setText(context.getString(R.string.contact_progress));
        }

        StringBuilder sb = new StringBuilder();
        //serviceinfoLine.setBackgroundColor(ContextCompat.getColor(this, R.color.std_green));
        /* SERVICE MANAGEMENT */
        if (!opzioni.isDataServiceEnabled()) {
            if (mBoundService != null) {// in esecuzione? strano
                sb.append("<br/><b>").append(context.getString(R.string.service_disabled)).append("!</b> ");
                // serviceinfoLine.setBackgroundColor(ContextCompat.getColor(this, R.color.std_red));
                if (opzioni.getTextFx()) {
                    Animation a2 = AnimationUtils.loadAnimation(context, R.anim.alpha_out);
                    a2.reset();
                    //serviceinfoLine.startAnimation(a2);
                }
                mBoundService.stopSelf();
            }

        } else {
            if (mBoundService != null) {
                sb.append("<b>").append(context.getString(R.string.service_lastexec)).append("</b> ").append(SoulissUtils.getTimeAgo(mBoundService.getLastupd())).append("<br/><b>");
                sb.append(context.getString(R.string.opt_serviceinterval)).append(":</b> ")
                        .append(SoulissUtils.getScaledTime(opzioni.getDataServiceIntervalMsec() / 1000));
            } else {
                sb.append(context.getString(R.string.service_warnbound));
                Intent serviceIntent = new Intent(context, SoulissDataService.class);
                Log.w(TAG, "Service not bound yet, restarting");
                context.startService(serviceIntent);
                //serviceinfoLine.setBackgroundColor(ContextCompat.getColor(context, R.color.std_yellow));
            }
        }
        serviceInfo.setText(Html.fromHtml(sb.toString()));
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        public final CardView container;

        public ViewHolder(View itemView) {
            super(itemView);
            container = (CardView) itemView.getRootView();
        }
        //enum componentEnum
    }


}