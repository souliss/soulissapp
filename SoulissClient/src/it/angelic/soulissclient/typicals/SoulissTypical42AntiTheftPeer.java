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
import java.util.Calendar;

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
public class SoulissTypical42AntiTheftPeer extends SoulissTypical implements ISoulissTypical {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553488985062232592L;

	// Context ctx;

	public SoulissTypical42AntiTheftPeer(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

		//NO COMMANDS

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
		/*RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
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
		if (typicalDTO.getOutput() == Constants.Souliss_T4n_Antitheft) {
			turnOnButton.setEnabled(false);
		} else {
			turnOffButton.setEnabled(false);
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
								String.valueOf(Constants.Souliss_T4n_Armed));
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
								String.valueOf(Constants.Souliss_T4n_NotArmed));
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
								String.valueOf(Constants.Souliss_T4n_ReArm));
						// cmd.setText("Souliss command sent");

					}
				};

				t.start();

			}

		});*/

	}
	@Override
	public void setOutputDescView(TextView textStatusVal) {
		textStatusVal.setText(getOutputDesc());
		if (typicalDTO.getOutput() == Constants.Souliss_T4n_Alarm ||
				(Calendar.getInstance().getTime().getTime() - typicalDTO.getRefreshedAt().getTime().getTime() > (prefs.getDataServiceIntervalMsec()*3))) {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.borderedbackoff));
		} else {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_green));
			textStatusVal.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.borderedbackon));
		}
	}
	@Override
	public String getOutputDesc() {
		String ret;
		if (typicalDTO.getOutput() == Constants.Souliss_T4n_RstCmd)
			ret = "OK";
		else if (typicalDTO.getOutput() == Constants.Souliss_T4n_InAlarm || typicalDTO.getOutput() == Constants.Souliss_T4n_Alarm)
			ret = "ALARM";
		else
			ret = "UNKNOWN";
		
		if (Calendar.getInstance().getTime().getTime() - typicalDTO.getRefreshedAt().getTime().getTime() > (prefs.getDataServiceIntervalMsec()*3))
			ret += "(STALE)";
		return ret;
	}

}
