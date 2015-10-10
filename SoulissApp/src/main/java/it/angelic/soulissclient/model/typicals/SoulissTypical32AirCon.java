package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;

import it.angelic.soulissclient.*;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;

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
	public ArrayList<ISoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<ISoulissCommand> ret = new ArrayList<>();

		SoulissCommand t = new SoulissCommand(this);
		t.getCommandDTO().setCommand(Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Auto_20);
		t.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		t.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		SoulissCommand ff = new SoulissCommand(this);
		ff.getCommandDTO().setCommand(Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Auto_24);
		ff.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		ff.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(ff);

		SoulissCommand cf = new SoulissCommand(this);
		cf.getCommandDTO().setCommand(Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Cool_18);
		cf.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		cf.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(cf);

		SoulissCommand df = new SoulissCommand(this);
		df.getCommandDTO().setCommand(Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Dry);
		df.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		df.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(df);

		SoulissCommand faf = new SoulissCommand( this);
		faf.getCommandDTO().setCommand(Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Fan);
		faf.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		faf.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(faf);

		SoulissCommand tt = new SoulissCommand(this);
		tt.getCommandDTO().setCommand(Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Off);
		tt.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		tt.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(tt);

		return ret;
	}

	@Override
	public void setOutputDescView(TextView textStatusVal) {
			textStatusVal.setText(getOutputDesc());
			if ((typicalDTO.getOutput() ==0 || typicalDTO.getOutput() >> 6 == 1)|| "UNKNOWN".compareTo(getOutputDesc()) == 0 || "NA".compareTo(getOutputDesc()) == 0) {
				textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_red));
				textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
			} else {

				textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_green));
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
		else if (fun == Constants.Typicals.Souliss_T_IrCom_AirCon_Fun_Auto)
			return "AUTO";
		else if (fun == Constants.Typicals.Souliss_T_IrCom_AirCon_Fun_Cool)
			return "COOL";
		else if (fun == Constants.Typicals.Souliss_T_IrCom_AirCon_Fun_Dry)
			return "DRY";
		else if (fun == Constants.Typicals.Souliss_T_IrCom_AirCon_Fun_Fan)
			return "FAN";
		else if (fun == Constants.Typicals.Souliss_T_IrCom_AirCon_Fun_Heat)
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
