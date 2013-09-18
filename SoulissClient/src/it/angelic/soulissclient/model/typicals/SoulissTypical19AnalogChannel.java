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
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SoulissTypical19AnalogChannel extends SoulissTypical implements ISoulissTypical {

	// SoulissNode parentd = getParentNode();
	// SoulissTypical related =
	// parentd.getTypical((short)(getTypicalDTO().getSlot()+1));

	/**
	 * 
	 */
	private static final long serialVersionUID = 45534215562542092L;

	// Context ctx;

	public SoulissTypical19AnalogChannel(SoulissPreferenceHelper pp) {
		super(pp);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

		SoulissCommand t = new SoulissCommand(ctx, this);
		t.getCommandDTO().setCommand(Constants.Souliss_T1n_OnCmd);
		t.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		t.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		SoulissCommand ff = new SoulissCommand(ctx, this);
		ff.getCommandDTO().setCommand(Constants.Souliss_T1n_OffCmd);
		ff.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		ff.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(ff);
		return ret;
	}

	public int getIntensity(){
		int r = ((SoulissTypical) getParentNode().getTypical((short) (typicalDTO.getSlot() + 1)))
				.getTypicalDTO().getOutput();
		return r;
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

		final ListButton turnON = new ListButton(ctx);
		turnON.setText("ON");
		turnON.setLayoutParams(lp);
		cont.addView(turnON);

		final ListButton turnOFF = new ListButton(ctx);
		turnOFF.setText("OFF");
		turnOFF.setLayoutParams(lp);
		cont.addView(turnOFF);

		turnON.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				turnON.setEnabled(false);
				turnOFF.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
							UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
									prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE, String.valueOf(Constants.Souliss_T1n_OnCmd));
					}
				};

				t.start();

			}

		});

		turnOFF.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				turnON.setEnabled(false);
				turnOFF.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
							UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
									prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE, String.valueOf(Constants.Souliss_T1n_OffCmd));
					}
				};
				t.start();
			}

		});

	}

	@Override
	public void setOutputDescView(TextView textStatusVal) {
		textStatusVal.setText(getOutputDesc());
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil || "UNKNOWN".compareTo(getOutputDesc()) == 0) {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
		} else {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_green));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
		}
	}
	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil)
			return ctx.getString(R.string.OFF);
		else
			return ctx.getString(R.string.ON);
	}
	/**************************************************************************
	 * Souliss RGB light command Souliss OUTPUT Data is:
	 * 
	 * 
	 * INPUT data 'read' from GUI
	 **************************************************************************/
	public void issueAnalogCommand(final short val, final int r, final boolean togMulticast) {

		Thread t = new Thread() {
			public void run() {
				Looper.prepare();

				if (togMulticast)//a tutti i nodi
					UDPHelper.issueMassiveCommand(""+Constants.Souliss_T16, prefs,""+val, "" + r);
				else
				UDPHelper.issueSoulissCommand("" + getParentNode().getId(), ""
						+ getTypicalDTO().getSlot(), prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE, "" + val, "" + r);
			}
		};

		t.start();
		return;
	}
	public void issueRefresh() {

		Thread t = new Thread() {
			public void run() {
				Looper.prepare();
					//refresh data for typical's node
					UDPHelper.pollRequest(prefs, 1, getParentNode().getId());
			}
		};

		t.start();
		return;
	}

}
