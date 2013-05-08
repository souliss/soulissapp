package it.angelic.soulissclient.typicals;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.helpers.ListButton;
import it.angelic.soulissclient.helpers.ListToggleButton;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Handle one digital output based on hardware and software commands, output can
 * be timed out.
 * 
 * This logic can be used for lights, wall socket and all the devices that has
 * an ON/OFF behavior.
 * 
 * @author Ale
 * 
 */
public class SoulissTypical11 extends SoulissTypical implements ISoulissTypical {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553488985062232592L;

	// Context ctx;

	public SoulissTypical11(SoulissPreferenceHelper fg) {
		super(fg);
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

		SoulissCommand tt = new SoulissCommand(ctx, this);
		tt.getCommandDTO().setCommand(Constants.Souliss_T1n_OffCmd);
		tt.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		tt.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(tt);

		SoulissCommand ter = new SoulissCommand(ctx, this);
		ter.getCommandDTO().setCommand(Constants.Souliss_T1n_ToogleCmd);
		ter.getCommandDTO().setSlot(typicalDTO.getSlot());
		ter.getCommandDTO().setNodeId(typicalDTO.getNodeId());
		ret.add(ter);

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
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		cont.setGravity(Gravity.CENTER);

		cont.addView(getQuickActionTitle());
		// cmd.setVisibility(View.GONE);

		final ListToggleButton tog = new ListToggleButton(ctx);
		// final int tpos = position;
		tog.setLayoutParams(lp);
		tog.setTextOff("I/O");
		tog.setTextOn("I/O");
		cont.addView(tog);

		final ListButton turnOnButton = new ListButton(ctx);
		turnOnButton.setText(ctx.getString(R.string.ON));
		cont.addView(turnOnButton);

		final ListButton turnOffButton = new ListButton(ctx);
		turnOffButton.setText(ctx.getString(R.string.OFF));
		cont.addView(turnOffButton);
		// disabilitazioni interlock
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OnCoil) {
			turnOnButton.setEnabled(false);
			tog.setChecked(true);
		} else {
			turnOffButton.setEnabled(false);
			tog.setChecked(false);
		}

		turnOnButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tog.setEnabled(false);
				turnOnButton.setEnabled(false);
				turnOffButton.setEnabled(false);
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
				tog.setEnabled(false);
				turnOnButton.setEnabled(false);
				turnOffButton.setEnabled(false);
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
				tog.setEnabled(false);
				turnOnButton.setEnabled(false);
				turnOffButton.setEnabled(false);

				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T1n_ToogleCmd));
						// cmd.setText("Souliss command sent");

					}
				};

				t.start();

			}

		});

	}
	@Override
	public void setOutputDescView(TextView textStatusVal) {
		textStatusVal.setText(getOutputDesc());
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil ||
				"UNKNOWN".compareTo(getOutputDesc()) == 0 ||
				"NA".compareTo(getOutputDesc()) == 0) {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.borderedbackoff));
		} else {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_green));
			textStatusVal.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.borderedbackon));
		}
	}
	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OnCoil)
			return "ON";
		else if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil)
			return "OFF";
		else if (typicalDTO.getOutput() >= Constants.Souliss_T1n_Timed)
			return ctx.getString(R.string.Souliss_TRGB_sleep);
		else
			return "UNKNOWN";
	}

}
