package it.angelic.soulissclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Adapter per gli scenari. Contiene comandi, come l'adapter dei programmi, ma i
 * comandi sono piu semplici
 *
 * @author Ale
 */
public class SceneCommandListAdapter extends BaseAdapter {
    SoulissCommand[] comandiScena;
    private Context context;
    private LayoutInflater mInflater;
    private SoulissPreferenceHelper opzioni;

    public SceneCommandListAdapter(Context context, SoulissCommand[] versio, SoulissPreferenceHelper op) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.comandiScena = versio;
        opzioni = op;
    }

    public int getCount() {
        // Hack lista vuota
        //if (comandiScena == null || comandiScena.length == 0)
        //	return 1;
        return comandiScena.length;
    }

    public Object getItem(int position) {
        return comandiScena[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public SoulissCommand[] getNodes() {
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
            //holder.evidenza = (View) convertView.findViewById(R.id.command_color);
            holder.image = (ImageView) convertView.findViewById(R.id.command_icon);
            if (comandiScena.length > 0)
                holder.data = comandiScena[position];

            convertView.setTag(holder);
        } else {
            holder = (CommandViewHolder) convertView.getTag();
        }

        if (opzioni.isLightThemeSelected()) {
            holder.textCmdWhen.setTextColor(context.getResources().getColor(R.color.black));
            holder.textCmd.setTextColor(context.getResources().getColor(R.color.black));
        }
        /* Scena vuota */
        if (comandiScena.length == 0) {
            holder.image.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.image.setColorFilter(context.getResources().getColor(R.color.aa_yellow),
                    android.graphics.PorterDuff.Mode.SRC_ATOP);
            holder.textCmd.setText(context.getResources().getString(R.string.scenes_empty));
            holder.textCmdWhen.setText(context.getResources().getString(R.string.scenes_empty_desc));
            //holder.evidenza.setBackgroundColor(context.getResources().getColor(color.trans_black));
        }
		/* comando singolo */
        else if (holder.data.getType() == Constants.COMMAND_SINGLE) {
            holder.image.setImageResource(holder.data.getIconResId());
            holder.image.setColorFilter(context.getResources().getColor(R.color.aa_yellow),
                    android.graphics.PorterDuff.Mode.SRC_ATOP);
            SoulissTypical appoggio = holder.data.getParentTypical();
            SoulissCommandDTO dto = holder.data.getCommandDTO();

            holder.textCmd.setText(holder.data.getNiceName());
            holder.textCmdWhen.setText(context.getResources().getString(R.string.scene_cmd_order) + " " + context.getResources().getQuantityString(R.plurals.seconds, dto.getInterval()/1000, dto.getInterval()/1000) +
                    " - " + context.getString(R.string.scene_cmd_massive));
			/* comando massivo */
        } else {
            RelativeLayout don = (RelativeLayout) convertView.findViewById(R.id.LinearLayout01);
            don.setBackgroundResource(R.drawable.list_rect_purple);
            holder.image.setImageResource(holder.data.getIconResId());
            holder.image.setColorFilter(context.getResources().getColor(R.color.aa_violet),
                    android.graphics.PorterDuff.Mode.SRC_ATOP);
            SoulissCommandDTO dto = holder.data.getCommandDTO();
            holder.textCmd.setText(holder.data.getNiceName());

            holder.textCmdWhen.setText(context.getString(R.string.scene_cmd_order) + " " + context.getResources().getQuantityString(R.plurals.seconds, dto.getInterval()/1000, dto.getInterval()/1000) +
                    " - " + context.getString(R.string.scene_cmd_massive));
        }
        // Nascondi, maf
        holder.textCmdInfo.setVisibility(View.GONE);

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
        ImageView image;
    }
}