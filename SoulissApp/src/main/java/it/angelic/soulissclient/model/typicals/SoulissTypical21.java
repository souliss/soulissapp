package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import java.util.ArrayList;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.views.ListButton;

public class SoulissTypical21 extends SoulissTypical implements ISoulissTypical {

	private static final long serialVersionUID = 4553652125062232032L;


	public SoulissTypical21(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public ArrayList<ISoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<ISoulissCommand> ret = new ArrayList<>();

		SoulissCommand t = new SoulissCommand(this);
		t.setCommand(Constants.Typicals.Souliss_T2n_ToogleCmd);
		t.setSlot(getTypicalDTO().getSlot());
		t.setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		return ret;
	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 */
	@Override
	public void getActionsLayout(Context ctx, LinearLayout cont) {
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
									prefs, String.valueOf(Constants.Typicals.Souliss_T2n_ToogleCmd));
					}
				};
				t.start();
			}
		});
	}

	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_Coil_Close)
			return "CLOSING";
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_LimSwitch_Open)
			return SoulissApp.getAppContext().getString(R.string.open);
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_LimSwitch_Close)
			return SoulissApp.getAppContext().getString(R.string.close);
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_Coil_Open)
			return SoulissApp.getAppContext().getString(R.string.open);
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_Coil_Stop)
			return SoulissApp.getAppContext().getString(R.string.stop);
		else
			return "UNKNOWN";
	}

}
