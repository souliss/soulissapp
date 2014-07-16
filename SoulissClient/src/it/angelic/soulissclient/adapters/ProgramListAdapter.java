package it.angelic.soulissclient.adapters;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissTriggerDTO;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProgramListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	SoulissCommand[] programmi;
	SparseArray<SoulissTriggerDTO> triggers;
	private SoulissPreferenceHelper opzioni;

	public ProgramListAdapter(Context context, SoulissCommand[] versio, SparseArray<SoulissTriggerDTO> trigs,
			SoulissPreferenceHelper optss) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.programmi = versio;
		this.triggers = trigs;
		opzioni = optss;

	}

	public int getCount() {
		return (int) programmi.length;
	}

	public Object getItem(int position) {
		return programmi[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public SoulissCommand[] getNodes() {
		return programmi;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		CommandViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_program, parent, false);
			holder = new CommandViewHolder();
			holder.textCmd = (TextView) convertView.findViewById(R.id.TextViewCommand);
			holder.textCmdWhen = (TextView) convertView.findViewById(R.id.TextViewCommandWhen);
			holder.textCmdInfo = (TextView) convertView.findViewById(R.id.TextViewCommandInfo);
			holder.image = (ImageView) convertView.findViewById(R.id.program_icon);
			holder.data = programmi[position];
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
		SoulissTypical appoggio = holder.data.getParentTypical();
		SoulissCommandDTO dto = holder.data.getCommandDTO();
		// Descrizione programma
		StringBuilder info = new StringBuilder(holder.data.toString());
		info.append(" slot " + dto.getSlot());
		if ("".compareTo(appoggio.getNiceName()) != 0)
			info.append(" (" + appoggio.getNiceName() + ")");
		info.append(" on " + appoggio.getParentNode().getNiceName());
		if ("".compareTo(appoggio.getParentNode().getNiceName()) != 0)
			holder.textCmd.setText(info.toString());
		/* programma temporale */
		if (holder.data.getType() == Constants.COMMAND_TIMED) {
			RelativeLayout don = (RelativeLayout) convertView.findViewById(R.id.LinearLayout01);
			don.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.list_rect_purple));
			//holder.evidenza.setBackgroundColor(context.getResources().getColor(color.std_purple_shadow));
			holder.image.setImageResource(R.drawable.clock);
			holder.image.setColorFilter(context.getResources().getColor(color.aa_violet),
					android.graphics.PorterDuff.Mode.SRC_ATOP);

			holder.textCmdWhen
					.setText("Execution at: " + Constants.hourFormat.format(dto.getScheduledTime().getTime()));
			if (holder.data.getCommandDTO().getInterval() > 0){
				String strMeatFormat = context.getString(R.string.programs_every);
				holder.textCmdInfo.setText(String.format(strMeatFormat,  dto.getInterval()));
			}else{
				
			
				holder.textCmdInfo.setText(context.getString(R.string.programs_recursive));
			}
			/* Dimensioni del testo settate dalle opzioni */
			// holder.textCmdWhen.setTextSize(TypedValue.COMPLEX_UNIT_SP,
			// opzioni.getListDimensTesto());

		}/* programma POSIZIONALE */
		else if (holder.data.getType() == Constants.COMMAND_COMEBACK_CODE
				|| holder.data.getType() == Constants.COMMAND_GOAWAY_CODE) {
			RelativeLayout don = (RelativeLayout) convertView.findViewById(R.id.LinearLayout01);
			don.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.list_rect_blue));
			//holder.evidenza.setBackgroundColor(context.getResources().getColor(color.aa_blue));
			holder.image.setImageResource(R.drawable.exit);
			holder.image.setColorFilter(context.getResources().getColor(color.aa_blue), PorterDuff.Mode.SRC_ATOP);
			// se gia eseguito, dico quando
			if (holder.data.getCommandDTO().getExecutedTime() != null) {
				holder.textCmdWhen.setText(context.getString(R.string.last_exec)
						+ Constants.hourFormat.format(holder.data.getCommandDTO().getExecutedTime().getTime()));
			} else {
				holder.textCmdWhen.setText(context.getString(R.string.programs_notyet));
			}
			if (holder.data.getType() == Constants.COMMAND_GOAWAY_CODE)
				holder.textCmdInfo.setText(context.getString(R.string.programs_leave));
			else
				holder.textCmdInfo.setText(context.getString(R.string.programs_come));

			/* Dimensioni del testo settate dalle opzioni */
			holder.textCmdWhen.setTextSize(TypedValue.COMPLEX_UNIT_SP, opzioni.getListDimensTesto());

			/* COMANDO TRIGGERED */
		} else if (holder.data.getType() == Constants.COMMAND_TRIGGERED) {
			RelativeLayout don = (RelativeLayout) convertView.findViewById(R.id.LinearLayout01);
			don.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.list_rect_red));
			//holder.evidenza.setBackgroundColor(context.getResources().getColor(color.std_red_shadow));
			holder.image.setImageResource(R.drawable.lighthouse);
			holder.image.setColorFilter(context.getResources().getColor(color.aa_red), PorterDuff.Mode.SRC_ATOP);

			SoulissTriggerDTO intrig = triggers.get((int) holder.data.getCommandDTO().getProgramId());

			/* Dimensioni del testo settate dalle opzioni */
			holder.textCmdWhen.setTextSize(TypedValue.COMPLEX_UNIT_SP, opzioni.getListDimensTesto());
			holder.textCmdInfo.setText(context.getString(R.string.programs_when) + " " + intrig.getInputSlotlot()
					+ " on Node " + intrig.getInputNodeId() + context.getString(R.string.is) + " " + intrig.getOp()
					+ " " + intrig.getThreshVal());
			if (holder.data.getCommandDTO().getExecutedTime() != null)
				holder.textCmdWhen.setText(context.getString(R.string.programs_leave)
						+ Constants.hourFormat.format((holder.data.getCommandDTO().getExecutedTime().getTime())));
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
		//public View evidenza;
		TextView textCmd;
		TextView textCmdWhen;
		TextView textCmdInfo;
		ImageView image;
		public SoulissCommand data;
	}
}