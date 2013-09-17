package it.angelic.soulissclient.model.typicals;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.helpers.ListButton;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SoulissTypical21 extends SoulissTypical implements ISoulissTypical {

	private static final long serialVersionUID = 4553652125062232032L;


	public SoulissTypical21(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

		SoulissCommand t = new SoulissCommand(ctx, this);
		t.getCommandDTO().setCommand(Constants.Souliss_T2n_ToogleCmd);
		t.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		t.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

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
	public void getActionsLayout(final TypicalsListAdapter ble, Context ctx, final Intent parentIntent,
			View convertView, final ViewGroup parent) {
		LinearLayout cont = (LinearLayout) convertView.findViewById(R.id.linearLayoutButtons);
		cont.removeAllViews();
		
	//	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
		//		RelativeLayout.LayoutParams.MATCH_PARENT);
		
		cont.addView(getQuickActionTitle());

		final ListButton turnOnButton = new ListButton(ctx);
		turnOnButton.setText(ctx.getString(R.string.toggle));

		cont.addView(turnOnButton);

		turnOnButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				turnOnButton.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
							UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
									prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE, String.valueOf(Constants.Souliss_T2n_ToogleCmd));
					}
				};
				t.start();
			}
		});
	}

	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Souliss_T2n_Coil_Close )
			return "CLOSING";
		else if (typicalDTO.getOutput() == Constants.Souliss_T2n_LimSwitch_Open)
			return "OPENED";
		else if (typicalDTO.getOutput() == Constants.Souliss_T2n_LimSwitch_Close)
			return "CLOSED";
		else if (typicalDTO.getOutput() == Constants.Souliss_T2n_Coil_Open)
			return "OPENING";
		else if (typicalDTO.getOutput() == Constants.Souliss_T2n_Coil_Stop)
			return "STOP";
		else
			return "UNKNOWN";
	}

}
