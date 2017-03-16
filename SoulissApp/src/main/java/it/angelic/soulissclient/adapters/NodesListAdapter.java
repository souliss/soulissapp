package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.SoulissUtils;

public class NodesListAdapter extends BaseAdapter {
    private List<SoulissNode> nodi;
    private Activity context;
    private LayoutInflater mInflater;
    private SoulissPreferenceHelper opzioni;

    public NodesListAdapter(Activity context, List<SoulissNode> versio, SoulissPreferenceHelper opts) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.nodi = versio;
        opzioni = opts;
    }

    public int getCount() {
        // Hack lista vuota
        if (nodi == null || nodi.size() == 0)
            return 1;
        return nodi.size();
    }

    public Object getItem(int position) {
        return nodi.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public List<SoulissNode> getNodes() {
        return nodi;
    }

    public void setNodes(List<SoulissNode> in) {
        nodi = in;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        NodeViewHolder holder;

        if (convertView == null) {
            // ProgressBar sfumata
            final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(Constants.roundedCorners, null, null));
            final LinearGradient gradient = new LinearGradient(0, 0, 250, 0, ContextCompat.getColor(context, color.aa_red), ContextCompat.getColor(context, R.color.aa_green),
                    android.graphics.Shader.TileMode.CLAMP);

            convertView = mInflater.inflate(R.layout.listview, parent, false);
            holder = new NodeViewHolder();

            holder.text = (TextView) convertView.findViewById(R.id.TextView01);
            holder.textTyp = (TextView) convertView.findViewById(R.id.TextViewTypicals);
            holder.textHlt = (TextView) convertView.findViewById(R.id.TextViewHealth);
            holder.image = (TextView) convertView.findViewById(R.id.node_icon);
            holder.hlt = (ProgressBar) convertView.findViewById(R.id.progressBarHealth);
            holder.hlt.setIndeterminate(false);
            holder.hlt.setMax(50);
            holder.hlt.setProgress(20);
            holder.hlt.setProgress(0);
            holder.hlt.setMax(Constants.MAX_HEALTH);
            holder.hlt.setBackgroundResource(android.R.drawable.progress_horizontal);


            // pgDrawable.getPaint().setStrokeWidth(3);
            pgDrawable.getPaint().setDither(true);
            pgDrawable.getPaint().setShader(gradient);

            ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);

            // Rect bounds = holder.hlt.getProgressDrawable().getBounds();
            holder.hlt.setProgressDrawable(progress);
            // holder.hlt.getProgressDrawable().setBounds(bounds);

            convertView.setTag(holder);
        } else {
            holder = (NodeViewHolder) convertView.getTag();
        }

        if (nodi.size() == 0) {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_exclamation_triangle.getFontName());
            //holder.image.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.image.setTextColor(context.getResources().getColor(R.color.aa_yellow));
            holder.text.setText(context.getResources().getString(R.string.node_empty));
            holder.textTyp.setText(context.getResources().getString(R.string.dialog_notinited_db));
            holder.hlt.setVisibility(View.INVISIBLE);
            //holder.evidenza.setBackgroundColor(context.getResources().getColor(color.trans_black));
            return convertView;
        }
        holder.data = nodi.get(position);
        holder.text.setText(nodi.get(position).getNiceName());
        // holder.text.setTextAppearance(context, R.style.CodeFontTitle);

        // Progress = health
        holder.hlt.setProgress(0);
        holder.hlt.setProgress(nodi.get(position).getHealth());
        /* Dimensioni del testo settate dalle opzioni */
        // holder.textTyp.setTextSize(TypedValue.COMPLEX_UNIT_SP,holder.textTyp.getTextSize()
        // + opzioni.getListDimensTesto());
        // holder.textTyp.setTextAppearance(context, R.style.CodeFontMain);

        holder.textTyp.setText(context.getResources().getQuantityString(R.plurals.Devices,
                nodi.get(position).getActiveTypicals().size(), nodi.get(position).getActiveTypicals().size())
                + " - " + context.getString(R.string.update) + " " + SoulissUtils.getTimeAgo(nodi.get(position).getRefreshedAt()));

        if (opzioni.isLightThemeSelected()) {
            holder.textTyp.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.text.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.textHlt.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        /* Icona del nodo */
        //if (holder.data.getIconResourceId() != 0)
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, holder.data.getIconResourceId());
        //else
        //    FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_microchip.getFontName());
        if (opzioni.getTextFx()) {
            Animation a2 = AnimationUtils.loadAnimation(context, R.anim.alpha);
            a2.reset();
            // a2.setStartTime(System.currentTimeMillis() + 400 * position);
            a2.setStartOffset(250 * position);
            // Animazione immagine holder.image.clearAnimation();
            holder.image.startAnimation(a2);
            // holder.text.clearAnimation();
            // holder.text.startAnimation(a2);
        }


        return convertView;
    }

    public static class NodeViewHolder {
        public SoulissNode data;
        TextView text;
        TextView textTyp;
        TextView textHlt;
        ProgressBar hlt;
        TextView image;
        int imageRes;
    }
}