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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SoulissTypical12DigitalOutputAuto extends SoulissTypical implements ISoulissTypical {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4292781263370980816L;

	public SoulissTypical12DigitalOutputAuto(SoulissPreferenceHelper fg) {
		super(fg);

	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

		SoulissCommand t = new SoulissCommand(ctx, this);
		t.getCommandDTO().setCommand(Constants.Souliss_T1n_OnCmd);
		t.getCommandDTO().setSlot(typicalDTO.getSlot());
		t.getCommandDTO().setNodeId(typicalDTO.getNodeId());
		ret.add(t);

		SoulissCommand tr = new SoulissCommand(ctx, this);
		tr.getCommandDTO().setCommand(Constants.Souliss_T1n_OffCmd);
		tr.getCommandDTO().setSlot(typicalDTO.getSlot());
		tr.getCommandDTO().setNodeId(typicalDTO.getNodeId());
		ret.add(tr);

		SoulissCommand td = new SoulissCommand(ctx, this);
		td.getCommandDTO().setCommand(Constants.Souliss_T1n_AutoCmd);
		td.getCommandDTO().setSlot(typicalDTO.getSlot());
		td.getCommandDTO().setNodeId(typicalDTO.getNodeId());
		ret.add(td);

		return ret;
	}

	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OnCoil)
			return ctx.getString(R.string.ON);
		else if	(typicalDTO.getOutput() == Constants.Souliss_T1n_OnCoil_Auto)
			return ctx.getString(R.string.ON)+" (AUTO)";
		else if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil)
			return ctx.getString(R.string.OFF);
		else if( typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil_Auto)
			return "OFF (AUTO)";
		else
			return "UNKNOWN";
	}
	@Override
	public void setOutputDescView(TextView textStatusVal) {
		textStatusVal.setText(getOutputDesc());
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil || "UNKNOWN".compareTo(getOutputDesc()) == 0 || typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil_Auto) {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
		} else {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_green));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
		}
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
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		cont.addView(getQuickActionTitle());
		// cmd.setVisibility(View.GONE);

		final ListButton turnOnButton = new ListButton(ctx);
		turnOnButton.setText(ctx.getString(R.string.ON));
		cont.addView(turnOnButton);

		final ListButton turnOffButton = new ListButton(ctx);
		turnOffButton.setText(ctx.getString(R.string.OFF));
		cont.addView(turnOffButton);
		// disabilitazioni interlock
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OnCoil || 
				typicalDTO.getOutput() == Constants.Souliss_T1n_OnCoil_Auto) {
			turnOnButton.setEnabled(false);
		} else {
			turnOffButton.setEnabled(false);
		}

		final ListButton tog = new ListButton(ctx);
		// final int tpos = position;
		tog.setLayoutParams(lp);
		tog.setText("Auto");
		cont.addView(tog);

		turnOnButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {

						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T1n_OnCmd));

					}
				};
				t.start();
			}

		});

		turnOffButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T1n_OffCmd));

					}

				};

				t.start();

			}

		});

		tog.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T1n_AutoCmd));
						// cmd.setText("Souliss command sent");
					}
				};

				t.start();
			}

		});
	}


}
