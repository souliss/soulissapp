package it.angelic.soulissclient.model.typicals;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.T15RGBIrActivity;
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

public class SoulissTypical15 extends SoulissTypical implements ISoulissTypical {

	// SoulissNode parentd = getParentNode();
	// SoulissTypical related =
	// parentd.getTypical((short)(getTypicalDTO().getSlot()+1));

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553488985062542092L;

	// Context ctx;

	public SoulissTypical15(SoulissPreferenceHelper pp) {
		super(pp);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

		SoulissCommand t = new SoulissCommand(ctx, this);
		t.getCommandDTO().setCommand(Constants.Souliss_T1n_RGB_OnCmd);
		t.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		t.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		SoulissCommand ff = new SoulissCommand(ctx, this);
		ff.getCommandDTO().setCommand(Constants.Souliss_T1n_RGB_OffCmd);
		ff.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		ff.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(ff);

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
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		cont.addView(getQuickActionTitle());
		/*
		 * 
		 * TRE BOTTONI: ON, OFF e TOGGLE
		 */
		final ListButton tog = new ListButton(ctx);
		// final int tpos = position;
		tog.setText("Rmt");
		tog.setLayoutParams(lp);
		cont.addView(tog);

		final ListButton turnON = new ListButton(ctx);
		turnON.setText("ON");
		turnON.setLayoutParams(lp);
		cont.addView(turnON);

		final ListButton turnOFF = new ListButton(ctx);
		turnOFF.setText("OFF");
		turnOFF.setLayoutParams(lp);
		cont.addView(turnOFF);

		tog.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						Intent nodeDatail = new Intent(ctx, T15RGBIrActivity.class);
						nodeDatail.putExtra("TIPICO", (SoulissTypical15) SoulissTypical15.this);
						ctx.startActivity(nodeDatail);
					}
				};

				t.start();

			}

		});

		turnON.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//tog.setEnabled(false);
				//turnON.setEnabled(false);
				//turnOFF.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
							UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
									prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE, String.valueOf(Constants.Souliss_T1n_RGB_OnCmd));
					}
				};

				t.start();

			}

		});

		turnOFF.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//tog.setEnabled(false);
				//turnON.setEnabled(false);
				//turnOFF.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
							UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
									prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE, String.valueOf(Constants.Souliss_T1n_RGB_OffCmd));
					}
				};

				t.start();

			}

		});

	}

	@Override
	public String getOutputDesc() {

		if (typicalDTO.getOutput() == Constants.Souliss_T1n_RGB_OffCmd)
			return "OFF";
		else
			return "ON";
	}

}
