package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;

/**
 * Adapter per gli scenari. Contiene comandi, come l'adapter dei programmi, ma i
 * comandi sono piu semplici
 *
 * @author Ale
 */
public class SceneCommandListAdapter extends BaseAdapter {
    List<SoulissCommand> comandiScena;
    private Activity context;
    private LayoutInflater mInflater;
    private SoulissPreferenceHelper opzioni;

    public SceneCommandListAdapter(Activity context, List<SoulissCommand> versio, SoulissPreferenceHelper op) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.comandiScena = versio;
        opzioni = op;
    }

    public int getCount() {
        // Hack lista vuota
        if (comandiScena == null || comandiScena.size() == 0)
            return 1;
        return comandiScena.size();
    }

    public Object getItem(int position) {
        return comandiScena.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public List<SoulissCommand> getSceneCommands() {
        return comandiScena;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CommandViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_scene_detail, parent, false);
            holder = new CommandViewHolder();
            holder.textCmd = (TextView) convertView.findViewById(R.id.TextViewCommand);
            holder.textCmdWhen = (TextView) convertView.findViewById(R.id.TextViewCommandWhen);
            holder.textCmdInfo = (TextView) convertView.findViewById(R.id.TextViewCommandInfo);
            holder.image = (TextView) convertView.findViewById(R.id.command_icon);
            holder.tileLie = convertView.findViewById(R.id.tileLie);
            if (comandiScena.size() > 0)
                holder.data = comandiScena.get(position);

            convertView.setTag(holder);
        } else {
            holder = (CommandViewHolder) convertView.getTag();
        }

        if (opzioni.isLightThemeSelected()) {
            holder.textCmdWhen.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.textCmd.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        /* Scena vuota */
        if (comandiScena.size() == 0) {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_exclamation_triangle.getFontName());
            //holder.image.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.image.setTextColor(context.getResources().getColor(R.color.aa_yellow));
            holder.textCmd.setText(context.getResources().getString(R.string.scenes_empty));
            holder.textCmdWhen.setText(context.getResources().getString(R.string.scenes_empty_desc));
            //holder.evidenza.setBackgroundColor(context.getResources().getColor(color.trans_black));
            return convertView;
        }
		/* comando singolo */
        else if (holder.data.getType() == Constants.COMMAND_SINGLE) {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, holder.data.getIconResId());
            //holder.image.setImageResource(holder.data.getIconResId());
            holder.image.setTextColor(context.getResources().getColor(R.color.md_yellow_600));
            holder.tileLie.setBackgroundColor(ContextCompat.getColor(context, R.color.md_yellow_600));
            SoulissTypical appoggio = holder.data.getParentTypical();

            holder.textCmd.setText(holder.data.getName());
            String strVal = Float.valueOf(holder.data.getInterval() / 1000f).toString();
            holder.textCmdWhen.setText(holder.data.getNiceName());
            holder.textCmdInfo.setText(context.getResources().getString(R.string.scene_cmd_order) + " " + context.getResources().getQuantityString(R.plurals.seconds, holder.data.getInterval() / 1000, strVal));
            /* comando massivo */
        } else {
            RelativeLayout don = (RelativeLayout) convertView.findViewById(R.id.LinearLayout01);
            //don.setBackgroundResource(R.drawable.list_rect_purple);
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_arrows_alt.getFontName());
            holder.tileLie.setBackgroundColor(ContextCompat.getColor(context, R.color.md_yellow_900));
            holder.image.setTextColor(context.getResources().getColor(R.color.md_yellow_900));
            //holder.image.setImageResource(holder.data.getIconResId());
            //holder.image.setColorFilter(context.getResources().getColor(R.color.aa_violet),
            //        android.graphics.PorterDuff.Mode.SRC_ATOP);
            holder.textCmd.setText(context.getString(R.string.scene_cmd_massive));
            String strVal = Float.valueOf(holder.data.getInterval() / 1000f).toString();
            holder.textCmdWhen.setText(holder.data.getNiceName());
            holder.textCmdInfo.setText(context.getString(R.string.scene_cmd_order) + " " + context.getResources().getQuantityString(R.plurals.seconds, holder.data.getInterval() / 1000, strVal) +
                    " - " + context.getString(R.string.scene_cmd_massive));
        }
        // Nascondi, maf
        //holder.textCmdInfo.setVisibility(View.GONE);

		/* Dimensioni del testo settate dalle opzioni */
        // holder.textCmdWhen.setTextSize(TypedValue.COMPLEX_UNIT_SP,
        // opzioni.getListDimensTesto());
        if (opzioni.getTextFx()) {
            Animation a2 = AnimationUtils.loadAnimation(context, R.anim.scalerotale);
            a2.reset();
            a2.setStartOffset(100 * position);
            // Animazione immagine holder.image.clearAnimation();
            holder.image.startAnimation(a2);
        }

        return convertView;
    }


    public static class CommandViewHolder {
        public SoulissCommand data;
        //public View evidenza;
        TextView textCmd;
        TextView textCmdWhen;
        TextView textCmdInfo;
        TextView image;
        View tileLie;
    }
}