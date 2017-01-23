package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import java.util.ArrayList;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.views.ListButton;

public class SoulissTypical22 extends SoulissTypical implements ISoulissTypical {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1116356816188011036L;
	Context ctx;


	public SoulissTypical22(Context ctx, SoulissPreferenceHelper opts) {

		super(ctx, opts);
	}

	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_Coil_Close
				|| typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_LimSwitch_Close)
			return context.getString(R.string.close).toUpperCase();
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_Coil_Open
				|| typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_LimSwitch_Open)
			return context.getString(R.string.open).toUpperCase();
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_Coil_Stop)
			return context.getString(R.string.stop).toUpperCase();
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T2n_NoLimSwitch)
			return "MIDDLE";
		else
			return "ERR";
	}

	@Override
	public ArrayList<ISoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<ISoulissCommand> ret = new ArrayList<>();

		SoulissCommand t = new SoulissCommand(this);
		t.setCommand(Constants.Typicals.Souliss_T2n_CloseCmd);
		t.setSlot(typicalDTO.getSlot());
		t.setNodeId(typicalDTO.getNodeId());
		ret.add(t);

		SoulissCommand rset = new SoulissCommand(this);
		rset.setCommand(Constants.Typicals.Souliss_T2n_OpenCmd);
		rset.setSlot(typicalDTO.getSlot());
		rset.setNodeId(typicalDTO.getNodeId());
		ret.add(rset);

		SoulissCommand rsest = new SoulissCommand(this);
		rsest.setCommand(Constants.Typicals.Souliss_T2n_StopCmd);
		rsest.setSlot(typicalDTO.getSlot());
		rsest.setNodeId(typicalDTO.getNodeId());
		ret.add(rsest);

		return ret;
	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 */
	@Override
	public void getActionsLayout(final Context ctx, final LinearLayout cont) {
		cont.removeAllViews();
		// LinearLayout.LayoutParams lp = new
		// LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.MATCH_PARENT);

		cont.addView(getQuickActionTitle());
		/*
		 * 
		 * TRE BOTTONI: ON, OFF e TOGGLE
		 */
		final ListButton closeButton = new ListButton(ctx);
		closeButton.setText(ctx.getString(R.string.close));
		cont.addView(closeButton);

		final ListButton openButton = new ListButton(ctx);
		openButton.setText(ctx.getString(R.string.open));
		cont.addView(openButton);

		final ListButton stopButton = new ListButton(ctx);
		stopButton.setText(ctx.getString(R.string.stop));
		cont.addView(stopButton);

		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs,
								String.valueOf(Constants.Typicals.Souliss_T2n_CloseCmd));
					}
				};
				t.start();
			}

		});

		openButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs,
								String.valueOf(Constants.Typicals.Souliss_T2n_OpenCmd));
					}
				};
				t.start();
			}

		});

		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs,
								String.valueOf(Constants.Typicals.Souliss_T2n_StopCmd));
					}
				};
				t.start();
			}

		});

	}
}
