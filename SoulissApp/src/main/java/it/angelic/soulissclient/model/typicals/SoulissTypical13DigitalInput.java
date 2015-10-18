package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.angelic.soulissclient.*;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Handle one digital output based on hardware and software commands, output can
 * be timed out.
 * 
 * This logic can be used for lights, wall socket and all the devices that has
 * an ON/OFF behavior.
 * @author Ale
 * 
 */
public class SoulissTypical13DigitalInput extends SoulissTypical implements ISoulissTypical {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553488985062232592L;

	// Context ctx;

	public SoulissTypical13DigitalInput(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public ArrayList<ISoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		return new ArrayList<ISoulissCommand>();
	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 */
	@Override
	public void getActionsLayout(Context ctx, LinearLayout cont) {
		cont.removeAllViews();
		//RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
			//	RelativeLayout.LayoutParams.WRAP_CONTENT);

		// Infotext nascosto all'inizio
		final TextView cmd = getQuickActionTitle();
		//cmd.setTextSize(ctx.getResources().getDimension(R.dimen.text_size));
		cmd.setText(ctx.getString(R.string.Souliss_T13_desc)+ " has no software commands");
		cont.addView(cmd);
		//cmd.setVisibility(View.GONE);

		
	}

	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil)
			return "ON";
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil)
			return "OFF";
		else
			return "UNKNOWN";
	}

}
