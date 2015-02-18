package it.angelic.soulissclient.model.typicals;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.helpers.ListButton;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
/**
 * 
 * Typical 14 : Pulse Digital Output
 * 
 * One way trigger switch. The only command available will turn it on. 
 * Souliss will turn it off after some cycle individually
 * 
 * @author shine@angelic.it
 *
 */
public class SoulissTypical14PulseOutput extends SoulissTypical implements ISoulissTypical {

	private static final long serialVersionUID = 4553488325062232092L;


	public SoulissTypical14PulseOutput(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<>();

		SoulissCommand t = new SoulissCommand( this);
		t.getCommandDTO().setCommand(Constants.Souliss_T1n_OnCmd);
		t.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		t.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		return ret;
	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 */
	@Override
	public void getActionsLayout( Context ctx, LinearLayout cont) {
		cont.removeAllViews();
		
		//RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
			//	RelativeLayout.LayoutParams.MATCH_PARENT);
		// cmd.setGravity(Gravity.TOP);
		cont.addView(getQuickActionTitle());

		final ListButton turnOnButton = new ListButton(ctx);
		turnOnButton.setText(ctx.getString(R.string.open));

		cont.addView(turnOnButton);

		turnOnButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//turnOnButton.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs, String.valueOf(Constants.Souliss_T1n_OnCmd));

					}
				};
				t.start();
			}

		});
	}

	@Override
	public String getOutputDesc() {
		return "NA";
	}

}
