package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.ListButton;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

public class SoulissTypical16AdvancedRGB extends SoulissTypical implements ISoulissTypical {

	// SoulissNode parentd = getParentNode();
	// SoulissTypical related =
	// parentd.getTypical((short)(getTypicalDTO().getSlot()+1));

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553421985062542092L;

	// Context ctx;

	public SoulissTypical16AdvancedRGB(SoulissPreferenceHelper pp) {
		super(pp);
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

		SoulissCommand ff = new SoulissCommand( this);
		ff.getCommandDTO().setCommand(Constants.Souliss_T1n_OffCmd);
		ff.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		ff.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(ff);
		
		SoulissCommand tf = new SoulissCommand( this);
		tf.getCommandDTO().setCommand(Constants.Souliss_T1n_ToogleCmd);
		tf.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		tf.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(tf);
		
		SoulissCommand fRed = new SoulissCommand( this);
		fRed.getCommandDTO().setCommand(Constants.Souliss_T16_Red);
		fRed.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		fRed.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(fRed);
		
		SoulissCommand fGreen = new SoulissCommand( this);
		fGreen.getCommandDTO().setCommand(Constants.Souliss_T16_Green);
		fGreen.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		fGreen.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(fGreen);
		
		SoulissCommand fBlue = new SoulissCommand(this);
		fBlue.getCommandDTO().setCommand(Constants.Souliss_T16_Blue);
		fBlue.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		fBlue.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(fBlue);

		return ret;
	}

	public int getColor(){
		int r = getParentNode().getTypical((short) (typicalDTO.getSlot() + 1))
				.getTypicalDTO().getOutput();
		int g = getParentNode().getTypical((short) (typicalDTO.getSlot() + 2))
				.getTypicalDTO().getOutput();
		int b = getParentNode().getTypical((short) (typicalDTO.getSlot() + 3))
				.getTypicalDTO().getOutput();
		return Color.rgb(r, g, b);
	}
	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 */
	@Override
	public void getActionsLayout(final Context ctx, LinearLayout cont) {

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
									prefs, String.valueOf(Constants.Souliss_T1n_OnCmd));
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
									prefs, String.valueOf(Constants.Souliss_T1n_OffCmd));
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
			textStatusVal.setTextColor(SoulissClient.getAppContext().getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
		} else {
			textStatusVal.setTextColor(getColor());
			textStatusVal.setBackgroundColor(getColor());
		}
	}
	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil)
			return "OFF";
		else
			return "ON";
	}
	/**************************************************************************
	 * Souliss RGB light command Souliss OUTPUT Data is:
	 * 
	 * 
	 * INPUT data 'read' from GUI
	 **************************************************************************/
	public void issueRGBCommand(final short val, final int r, final int g, final int b, final boolean togMulticast) {

		Thread t = new Thread() {
			public void run() {
				//Looper.prepare();

				if (togMulticast)//a tutti i nodi
					UDPHelper.issueMassiveCommand(""+Constants.Souliss_T16, prefs,""+val, "" + r, ""
							+ g, "" + b);
				else
				UDPHelper.issueSoulissCommand("" + getParentNode().getId(), ""
						+ getTypicalDTO().getSlot(), prefs,  "" + val, "" + r, ""
						+ g, "" + b);
			}
		};

		t.start();
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
	}

}
