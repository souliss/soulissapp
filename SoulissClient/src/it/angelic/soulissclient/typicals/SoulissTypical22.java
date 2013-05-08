package it.angelic.soulissclient.typicals;

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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SoulissTypical22 extends SoulissTypical implements ISoulissTypical {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1116356816188011036L;
	Context ctx;

	public SoulissTypical22(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Souliss_T2n_Coil_Close )
			return "CLOSED";
		else if (typicalDTO.getOutput() == Constants.Souliss_T2n_Coil_Open)
			return "OPENED";
		else if (typicalDTO.getOutput() == Constants.Souliss_T2n_Coil_Stop)
			return "STOP";
		else
			return "UNKNOWN";
	}
	
	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();
		
		SoulissCommand t = new SoulissCommand(ctx, this);
		t.getCommandDTO().setCommand(Constants.Souliss_T2n_CloseCmd);
		t.getCommandDTO().setSlot(typicalDTO.getSlot());
		t.getCommandDTO().setNodeId(typicalDTO.getNodeId());
		ret.add(t);
		
		
		SoulissCommand rset = new SoulissCommand(ctx,this);
		rset.getCommandDTO().setCommand(Constants.Souliss_T2n_OpenCmd);
		rset.getCommandDTO().setSlot(typicalDTO.getSlot());
		rset.getCommandDTO().setNodeId(typicalDTO.getNodeId());
		ret.add(rset);
		
		
		SoulissCommand rsest = new SoulissCommand(ctx,this);
		rsest.getCommandDTO().setCommand(Constants.Souliss_T2n_StopCmd);
		rsest.getCommandDTO().setSlot(typicalDTO.getSlot());
		rsest.getCommandDTO().setNodeId(typicalDTO.getNodeId());
		ret.add(rsest);
		
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
		LinearLayout cont = (LinearLayout) convertView.findViewById(R.id.linearLayoutButtons);
		cont.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		
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
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T2n_CloseCmd));
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
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T2n_OpenCmd));
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
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T2n_StopCmd));
					}
				};
				t.start();
			}

		});


	}
}
