package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.db.SoulissTriggerDTO;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;

public class ProgramListAdapter extends BaseAdapter {
    private List<SoulissCommand> programmi = new ArrayList<>();
    private SparseArray<SoulissTriggerDTO> triggers;
    private Activity context;
    private LayoutInflater mInflater;
    private SoulissPreferenceHelper opzioni;

    public ProgramListAdapter(Activity context, List<SoulissCommand> versio, SparseArray<SoulissTriggerDTO> trigs,
                              SoulissPreferenceHelper optss) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.programmi = versio;
        this.triggers = trigs;
        opzioni = optss;

    }

    public List<SoulissCommand> getCommands() {
        return programmi;
    }

    public int getCount() {
        if (programmi == null)
            return 0;
        return programmi.size();
    }

    public Object getItem(int position) {
        return programmi.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CommandViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_program, parent, false);
            holder = new CommandViewHolder();
            holder.textCmd = convertView.findViewById(R.id.TextViewCommand);
            holder.textCmdWhen = convertView.findViewById(R.id.TextViewCommandWhen);
            holder.textCmdInfo = convertView.findViewById(R.id.TextViewCommandInfo);
            holder.image = convertView.findViewById(R.id.program_icon);
            holder.line = convertView.findViewById(R.id.StaticTileLine);
            holder.data = programmi.get(position);
            convertView.setTag(holder);
        } else {
            holder = (CommandViewHolder) convertView.getTag();
        }
        // holder.data.getCommand().getNodeId()

        if (opzioni.isLightThemeSelected()) {
            holder.textCmdWhen.setTextColor(context.getResources().getColor(R.color.black));
            holder.textCmd.setTextColor(context.getResources().getColor(R.color.black));
            holder.textCmdInfo.setTextColor(context.getResources().getColor(R.color.black));
        }
        //StringBuilder info = new StringBuilder(holder.data.toString());

        holder.textCmd.setText(holder.data.getNiceName());
        /* programma temporale */
        if (holder.data.getType() == Constants.COMMAND_TIMED) {
            RelativeLayout don = convertView.findViewById(R.id.LinearLayout01);
            //don.setBackgroundResource(R.drawable.list_rect_purple);
            //holder.evidenza.setBackgroundColor(context.getResources().getColor(color.std_purple_shadow));
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_clock_o.getFontName());
            holder.image.setTextColor(ContextCompat.getColor(context, color.md_light_blue_200));
            holder.line.setBackgroundColor(ContextCompat.getColor(context, color.md_light_blue_200));
            holder.textCmdWhen
                    .setText(context.getString(R.string.execute_at) + " " + Constants.hourFormat.format(holder.data.getScheduledTime().getTime()));
            if (holder.data.getInterval() > 0) {
                String strMeatFormat = context.getString(R.string.programs_every);
                holder.textCmdInfo.setText(String.format(strMeatFormat, holder.data.getInterval()));
            } else {
                holder.textCmdInfo.setText(context.getString(R.string.programs_recursive));
            }

        }/* programma POSIZIONALE */ else if (holder.data.getType() == Constants.COMMAND_COMEBACK_CODE
                || holder.data.getType() == Constants.COMMAND_GOAWAY_CODE) {
            RelativeLayout don = convertView.findViewById(R.id.LinearLayout01);

            if (holder.data.getExecutedTime() != null) {
                holder.textCmdWhen.setText(context.getString(R.string.last_exec)
                        + " " + Constants.hourFormat.format(holder.data.getExecutedTime().getTime()));
            } else {
                holder.textCmdWhen.setText(context.getString(R.string.programs_notyet));
            }
            if (holder.data.getType() == Constants.COMMAND_GOAWAY_CODE) {
                holder.textCmdInfo.setText(context.getString(R.string.programs_leave));
                FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_sign_out.getFontName());
                holder.image.setTextColor(ContextCompat.getColor(context, color.md_light_blue_400));
                holder.line.setBackgroundColor(ContextCompat.getColor(context, color.md_light_blue_400));
            } else {
                holder.textCmdInfo.setText(context.getString(R.string.programs_come));
                FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_sign_in.getFontName());
                holder.image.setTextColor(ContextCompat.getColor(context, color.md_light_blue_500));
                holder.line.setBackgroundColor(ContextCompat.getColor(context, color.md_light_blue_500));
            }
            /* Dimensioni del testo settate dalle opzioni */
            holder.textCmdWhen.setTextSize(TypedValue.COMPLEX_UNIT_SP, opzioni.getListDimensTesto());

			/* COMANDO TRIGGERED */
        } else if (holder.data.getType() == Constants.COMMAND_TRIGGERED) {
            RelativeLayout don = convertView.findViewById(R.id.LinearLayout01);
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_puzzle_piece.getFontName());
            //rosso
            holder.image.setTextColor(ContextCompat.getColor(context, color.md_light_blue_900));
            holder.line.setBackgroundColor(ContextCompat.getColor(context, color.md_light_blue_900));
            SoulissTriggerDTO intrig = triggers.get((int) holder.data.getCommandId());
			/* Dimensioni del testo settate dalle opzioni */
            holder.textCmdWhen.setTextSize(TypedValue.COMPLEX_UNIT_SP, opzioni.getListDimensTesto());
            holder.textCmdInfo.setText(context.getString(R.string.programs_when) + " " + intrig.getInputSlot()
                    + " on Node " + intrig.getInputNodeId() + " " + context.getString(R.string.is) + " " + intrig.getOp()
                    + " " + intrig.getThreshVal());
            if (holder.data.getExecutedTime() != null)
                holder.textCmdWhen.setText(context.getString(R.string.last_exec)
                        + " " + Constants.hourFormat.format((holder.data.getExecutedTime().getTime())));
            else
                holder.textCmdWhen.setText(context.getString(R.string.programs_notyet));
        }
        if (opzioni.getTextFx()) {
            Animation a2 = AnimationUtils.loadAnimation(context, R.anim.scalerotale);
            a2.reset();
            a2.setStartOffset(100 * position);
            // Animazione immagine holder.image.clearAnimation();
            holder.image.startAnimation(a2);
            // holder.text.clearAnimation();
            // holder.text.startAnimation(a2);
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
        public View line;
    }
}