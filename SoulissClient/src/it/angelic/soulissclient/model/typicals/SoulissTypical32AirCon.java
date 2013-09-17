package it.angelic.soulissclient.model.typicals;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SoulissTypical32AirCon extends SoulissTypical implements ISoulissTypical {

	// SoulissNode parentd = getParentNode();
	// SoulissTypical related =
	// parentd.getTypical((short)(getTypicalDTO().getSlot()+1));

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553488985062542092L;
	private SoulissTypical related;
	//private SoulissNode parentd;
	// Context ctx;

	public SoulissTypical32AirCon(SoulissPreferenceHelper pp) {
		super(pp);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

		SoulissCommand t = new SoulissCommand(ctx, this);
		t.getCommandDTO().setCommand(Constants.Souliss_T_IrCom_AirCon_Pow_Auto_20);
		t.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		t.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		SoulissCommand ff = new SoulissCommand(ctx, this);
		ff.getCommandDTO().setCommand(Constants.Souliss_T_IrCom_AirCon_Pow_Auto_24);
		ff.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		ff.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(ff);

		SoulissCommand cf = new SoulissCommand(ctx, this);
		cf.getCommandDTO().setCommand(Constants.Souliss_T_IrCom_AirCon_Pow_Cool_18);
		cf.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		cf.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(cf);

		SoulissCommand df = new SoulissCommand(ctx, this);
		df.getCommandDTO().setCommand(Constants.Souliss_T_IrCom_AirCon_Pow_Dry);
		df.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		df.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(df);

		SoulissCommand faf = new SoulissCommand(ctx, this);
		faf.getCommandDTO().setCommand(Constants.Souliss_T_IrCom_AirCon_Pow_Fan);
		faf.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		faf.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(faf);

		SoulissCommand tt = new SoulissCommand(ctx, this);
		tt.getCommandDTO().setCommand(Constants.Souliss_T_IrCom_AirCon_Pow_Off);
		tt.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		tt.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(tt);

		return ret;
	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 * @param ble
	 * @param ctx
	 * @param parentIntent
	 * @param convertView
	 * @param parent
	 */
	@Override
	public void getActionsLayout(final TypicalsListAdapter ble, final Context ctx, final Intent parentIntent,
			View convertView, final ViewGroup parent) {

		/*LinearLayout cont = (LinearLayout) convertView.findViewById(R.id.linearLayoutButtons);
		cont.removeAllViews();
		final TextView cmd = new TextView(ctx);
		cmd.setText(ctx.getString(R.string.actions));
		if (prefs.isLightThemeSelected())
			cmd.setTextColor(ctx.getResources().getColor(R.color.black));

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		cmd.setLayoutParams(lp);
		// cmd.setGravity(Gravity.TOP);
		cont.addView(cmd);
*/
		

	}

	@Override
	public void setOutputDescView(TextView textStatusVal) {
			textStatusVal.setText(getOutputDesc());
			if ((typicalDTO.getOutput() ==0 || typicalDTO.getOutput() >> 6 == 1)|| "UNKNOWN".compareTo(getOutputDesc()) == 0 || "NA".compareTo(getOutputDesc()) == 0) {
				textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_red));
				textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
			} else {

				textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_green));
				textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
			}
		}
	@Override
	public String getOutputDesc() {
		related = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 1));

		/*Log.d(Constants.TAG,
				"AirCon State: 0x" + Integer.toHexString(typicalDTO.getOutput()) + " "
						+ Integer.toHexString(related.getTypicalDTO().getOutput()));*/
		int fun = related.getTypicalDTO().getOutput() >> 4;

		if (typicalDTO.getOutput() ==0 || typicalDTO.getOutput() >> 6 == 1)
			return "OFF";
		else if (fun == Constants.Souliss_T_IrCom_AirCon_Fun_Auto)
			return "AUTO";
		else if (fun == Constants.Souliss_T_IrCom_AirCon_Fun_Cool)
			return "COOL";
		else if (fun == Constants.Souliss_T_IrCom_AirCon_Fun_Dry)
			return "DRY";
		else if (fun == Constants.Souliss_T_IrCom_AirCon_Fun_Fan)
			return "FAN";
		else if (fun == Constants.Souliss_T_IrCom_AirCon_Fun_Heat)
			return "HEAT";
		else
			return "UNKNOWN";
	}

	public SoulissTypical getRelated() {
		return related;
	}

	@Override
	public void setRelated(SoulissTypical related) {
		this.related = related;
	}
}
