package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

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
public class SoulissTypical41AntiTheft extends SoulissTypical implements ISoulissTypical {

	private static final long serialVersionUID = 4553488985062232592L;

	// Context ctx;

	public SoulissTypical41AntiTheft(SoulissPreferenceHelper fg) {
		super(fg);
		//se lo istanzio, ce l'ho
		fg.setAntitheftPresent(true);//
	}

	@Override
	public ArrayList<ISoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<ISoulissCommand> ret = new ArrayList<>();

		SoulissCommand t = new SoulissCommand( this);
		t.setCommand(Constants.Typicals.Souliss_T4n_Armed);
		t.setSlot(getTypicalDTO().getSlot());
		t.setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		SoulissCommand tt = new SoulissCommand( this);
		tt.setCommand(Constants.Typicals.Souliss_T4n_NotArmed);
		tt.setSlot(getTypicalDTO().getSlot());
		tt.setNodeId(getTypicalDTO().getNodeId());
		ret.add(tt);

		SoulissCommand ter = new SoulissCommand( this);
		ter.setCommand(Constants.Typicals.Souliss_T4n_ReArm);
		ter.setSlot(typicalDTO.getSlot());
		ter.setNodeId(typicalDTO.getNodeId());
		ret.add(ter);

		return ret;
	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 */
	@Override
	public void getActionsLayout(Context ctx, LinearLayout cont) {
		cont.removeAllViews();
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		//LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
			//	LinearLayout.LayoutParams.WRAP_CONTENT);
		cont.setGravity(Gravity.CENTER);

		cont.addView(getQuickActionTitle());
		// cmd.setVisibility(View.GONE);

		final ListButton tog = new ListButton(ctx);
		// final int tpos = position;
		tog.setLayoutParams(lp);
		tog.setText("RST");

		final ListButton turnOnButton = new ListButton(ctx);
		turnOnButton.setText(ctx.getString(R.string.ON));

		final ListButton turnOffButton = new ListButton(ctx);
		turnOffButton.setText(ctx.getString(R.string.OFF));
		
		cont.addView(turnOnButton);
		cont.addView(turnOffButton);
		cont.addView(tog);
		
		// disabilitazioni interlock
		if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T4n_Antitheft) {
			turnOnButton.setEnabled(false);
		} else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T4n_NoAntitheft) {
			turnOffButton.setEnabled(false);
		} else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T4n_InAlarm) {
			
		}

		turnOnButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tog.setEnabled(false);
				turnOnButton.setEnabled(false);
				turnOffButton.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs,
								String.valueOf(Constants.Typicals.Souliss_T4n_Armed));
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
								prefs,
								String.valueOf(Constants.Typicals.Souliss_T4n_NotArmed));
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
						/*UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE,
								String.valueOf(Constants.Souliss_T4n_ReArm));*/
						
						//FIXES #40 ?
						UDPHelper.issueMassiveCommand(String.valueOf(getTypicalDTO().getTypical()), prefs, String.valueOf(Constants.Typicals.Souliss_T4n_ReArm));
						
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
		if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T4n_NoAntitheft ||
				(Calendar.getInstance().getTime().getTime() - typicalDTO.getRefreshedAt().getTime().getTime() > (prefs.getDataServiceIntervalMsec()*3)) ||
				typicalDTO.getOutput() == Constants.Typicals.Souliss_T4n_InAlarm) {
			textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
		} else {
			textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_green));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
		}
	}
	@Override
	public String getOutputDesc() {
		String ret;
		if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T4n_Antitheft)
			ret = "ARMED";
		else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T4n_NoAntitheft)
			ret = "NOT ARMED";
		else if (typicalDTO.getOutput() >= Constants.Typicals.Souliss_T4n_InAlarm)
			ret = "ALARM";
		else
			ret= "UNKNOWN";
		
		if (Calendar.getInstance().getTime().getTime() - getTypicalDTO().getRefreshedAt().getTime().getTime() > (prefs.getDataServiceIntervalMsec()*3))
			ret += "(STALE)";
		
		return ret;
	}

}
