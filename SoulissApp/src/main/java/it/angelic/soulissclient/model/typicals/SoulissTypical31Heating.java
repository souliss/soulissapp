package it.angelic.soulissclient.model.typicals;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.helpers.HalfFloatUtils;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Typical 31 : Temperature control with cooling and heating mode
 * 
 * It compare an internal setpoint with a measured value to control a digital
 * output for cooling or heating. Actual temperature and desired setpoint are
 * stored and available for user interface or any other node in the network.
 * 
 * 
 * This typical use five (5) memory slot, arranged as follow:
 * 
 * Temperature Control User Commands (IN / OUT) SLOT +0
 * 
 * Temperature Measured Value (IN / OUT) SLOT +1, SLOT +2
 * 
 * Temperature Setpoint Value (IN / OUT) SLOT +3, SLOT +4
 * 
 * all values shall be in half-precision floating point, automatic conversion is
 * done if using Souliss_AnalogIn
 * 
 * @author Ale
 * 
 */
public class SoulissTypical31Heating extends SoulissTypical implements ISoulissTypical {

	// SoulissNode parentd = getParentNode();
	// SoulissTypical TemperatureMeasuredValue =
	// parentd.getTypical((short)(getTypicalDTO().getSlot()+1));

	/**
	 * 
	 */
	private static final long serialVersionUID = 1113488985342542012L;
	private int statusByte;
	// private SoulissNode parentd;
	// Context ctx;
	private SoulissTypical TemperatureSetpointValue;
	private SoulissTypical TemperatureSetpointValue2;

	private float TemperatureMeasuredVal;

	public SoulissTypical31Heating(SoulissPreferenceHelper pp) {
		super(pp);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

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

	}

	@Override
	public void setOutputDescView(TextView textStatusVal) {
		textStatusVal.setText(getOutputDesc());
		if ((typicalDTO.getOutput() == 0 || typicalDTO.getOutput() >> 6 == 1)
				|| "UNKNOWN".compareTo(getOutputDesc()) == 0 || "NA".compareTo(getOutputDesc()) == 0) {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
		} else {

			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_green));
			textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
		}
	}

	@Override
	/**
	 * Outpu as follows
	 * 
	 * BIT 0   Not used
	                    BIT 1   (0 Heating OFF , 1 Heating ON)
	                    BIT 2   (0 Cooling OFF , 1 Cooling ON)
	                    BIT 3   (0 Fan 1 OFF   , Fan 1 ON)
	                    BIT 4   (0 Fan 2 OFF   , Fan 2 ON)
	                    BIT 5   (0 Fan 3 OFF   , Fan 3 ON)
	                    BIT 6   (0 Manual Mode , 1 Automatic Mode for Fan)
	                    BIT 7   (0 Heating Mode, 1 Cooling Mode)       
	                    
	 */
	public String getOutputDesc() {
		statusByte = getTypicalDTO().getOutput();
		short TemperatureMeasuredValue = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 1))
				.getTypicalDTO().getOutput();
		short TemperatureMeasuredValue2 = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 2))
				.getTypicalDTO().getOutput();

		// ora ho i due bytes, li converto
		int shifted = TemperatureMeasuredValue2 << 8;

		TemperatureMeasuredVal = HalfFloatUtils.toFloat(shifted + TemperatureMeasuredValue);

		Log.i(Constants.TAG,
				"first:" + Long.toHexString((long) TemperatureMeasuredValue) + " second:"
						+ Long.toHexString((long) TemperatureMeasuredValue2) + "SENSOR Reading:"
						+ TemperatureMeasuredVal);

		// Serve solo per dare comandi, da togliere
		TemperatureSetpointValue = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 3));
		TemperatureSetpointValue2 = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 4));

		/*
		 * Log.d(Constants.TAG, "AirCon State: 0x" +
		 * Integer.toHexString(typicalDTO.getOutput()) + " " +
		 * Integer.toHexString
		 * (TemperatureMeasuredValue.getTypicalDTO().getOutput()));
		 */
		StringBuilder strout = new StringBuilder();
		// int fun = TemperatureMeasuredValue.getTypicalDTO().getOutput() >> 4;
		Log.i(Constants.TAG, "HEATING status: " + Integer.toBinaryString(typicalDTO.getOutput()));

		if (typicalDTO.getOutput() >> 6 == 1)
			strout.append("HEAT");
		else if (typicalDTO.getOutput() >> 5 == 1)
			strout.append("COOL");
		else 
			strout.append("OFF");

		if (typicalDTO.getOutput() >> 4 == 1)
			strout.append(" - FAN LOw");
		else if (typicalDTO.getOutput() >> 3 == 1)
			strout.append(" - FAN MED");
		else if (typicalDTO.getOutput() >> 2 == 1)
			strout.append(" - FAN HIG");

		strout.append(" " + TemperatureMeasuredVal + "°");
		return strout.toString();
	}

	public void issueCommand(final int function, final Float temp) {
		Thread t = new Thread() {
			public void run() {
				if (temp == null) {
					Log.i(Constants.TAG, "ISSUE COMMAND:" + String.valueOf((float) function));
					UDPHelper.issueSoulissCommand("" + getParentNode().getId(), "" + getTypicalDTO().getSlot(),
							SoulissClient.getOpzioni(), Constants.COMMAND_SINGLE, "" + function);
				
				} else {
					int re = HalfFloatUtils.fromFloat(temp);
					String pars = Long.toHexString(re);
					String first = Integer.toString(Integer.parseInt(pars.substring(0, 2), 16));
					String second = Integer.toString(Integer.parseInt(pars.substring(2, 4), 16));
					String[] cmd = { String.valueOf(function), "0", "0", first, second };
					Log.i(Constants.TAG, "ISSUE COMMAND:" + String.valueOf(function) + " 0 0 "+first+" "+second);
					UDPHelper.issueSoulissCommand("" + getParentNode().getId(), "" + getTypicalDTO().getSlot(),
							SoulissClient.getOpzioni(), Constants.COMMAND_SINGLE, cmd);
				}
			}
		};
		t.start();
	}

}
