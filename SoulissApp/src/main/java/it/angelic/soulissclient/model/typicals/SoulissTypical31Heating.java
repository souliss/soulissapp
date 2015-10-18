package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import junit.framework.Assert;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.HalfFloatUtils;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

/**
 * Typical 31 : Temperature control with cooling and heating mode
 * <p/>
 * It compare an internal setpoint with a measured value to control a digital
 * output for cooling or heating. Actual temperature and desired setpoint are
 * stored and available for user interface or any other node in the network.
 * <p/>
 * <p/>
 * This typical use five (5) memory slot, arranged as follow:
 * <p/>
 * Temperature Control User Commands (IN / OUT) SLOT +0
 * <p/>
 * Temperature Measured Value (IN / OUT) SLOT +1, SLOT +2
 * <p/>
 * Temperature Setpoint Value (IN / OUT) SLOT +3, SLOT +4
 * <p/>
 * all values shall be in half-precision floating point, automatic conversion is
 * done if using Souliss_AnalogIn
 *
 * @author Ale
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
    private float TemperatureSetpointVal;

    //AUTOF
    public SoulissTypical31Heating(SoulissPreferenceHelper pp) {
        super(pp);
    }

    @Override
    public ArrayList<ISoulissCommand> getCommands(Context ctx) {
        // ritorna le bozze dei comandi, da riempire con la schermata addProgram
        ArrayList<ISoulissCommand> ret = new ArrayList<>();

        return ret;
    }

    @Override
    public String getOutputDesc() {

        if (Calendar.getInstance().getTime().getTime() - typicalDTO.getRefreshedAt().getTime().getTime() < (prefs.getDataServiceIntervalMsec()*3))
        {
            statusByte = getTypicalDTO().getOutput();
            if (isStatusByteSet(statusByte, 0)) {
                if (isCoolMode())
                    return(SoulissApp.getAppContext().getResources().getStringArray(R.array.HeatingFunction)[0]);
                else
                    return(SoulissApp.getAppContext().getResources().getStringArray(R.array.HeatingFunction)[1]);
            } else
                return SoulissApp.getAppContext().getString(R.string.OFF);
        }
        else
            return SoulissApp.getAppContext().getString(R.string.stale);
    }

    public String getOutputLongDesc() {
        statusByte = getTypicalDTO().getOutput();
        short TemperatureMeasuredValue = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 1))
                .getTypicalDTO().getOutput();
        short TemperatureMeasuredValue2 = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 2))
                .getTypicalDTO().getOutput();

        // Serve solo per dare comandi, da togliere
        TemperatureSetpointValue = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 3));
        TemperatureSetpointValue2 = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 4));
        short TemperatureSetpointValue = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 3)).getTypicalDTO().getOutput();
        short TemperatureSetpointValue2 = getParentNode().getTypical((short) (getTypicalDTO().getSlot() + 4)).getTypicalDTO().getOutput();

        // ora ho i due bytes, li converto
        int shifted = TemperatureMeasuredValue2 << 8;
        float celsius = HalfFloatUtils.toFloat(shifted + TemperatureMeasuredValue);
        TemperatureMeasuredVal = prefs.isFahrenheitChosen() ? it.angelic.soulissclient.helpers.Utils.celsiusToFahrenheit(celsius) : celsius;

        Log.i(Constants.TAG,
                "first:" + Long.toHexString((long) TemperatureMeasuredValue) + " second:"
                        + Long.toHexString((long) TemperatureMeasuredValue2) + " SENSOR Reading:"
                        + TemperatureMeasuredVal);

        // ora ho i due bytes, li converto
        int shifteds = TemperatureSetpointValue2 << 8;

        TemperatureSetpointVal = HalfFloatUtils.toFloat(shifteds + TemperatureSetpointValue);
        Log.i(Constants.TAG,
                "first:" + Long.toHexString((long) TemperatureSetpointValue) + " second:"
                        + Long.toHexString((long) TemperatureSetpointValue2) + "SENSOR Setpoint:"
                        + TemperatureSetpointVal);
        /*
         * Log.d(Constants.TAG, "AirCon State: 0x" +
		 * Integer.toHexString(typicalDTO.getOutput()) + " " +
		 * Integer.toHexString
		 * (TemperatureMeasuredValue.getTypicalDTO().getOutput()));
		 */
        StringBuilder strout = new StringBuilder();
        // int fun = TemperatureMeasuredValue.getTypicalDTO().getOutput() >> 4;
        Log.i(Constants.TAG, "HEATING status: " + Integer.toBinaryString(statusByte));
        final ByteBuffer buf = ByteBuffer.allocate(4); // sizeof(int)
        buf.putInt(statusByte);
/*
            BIT 0	(0 System  OFF,  1 System  ON)
			BIT 1	(0 Heating OFF , 1 Heating ON)
			BIT 2	(0 Cooling OFF , 1 Cooling ON)
			BIT 3	(0 Fan 1 OFF   , 1 Fan 1 ON)
			BIT 4	(0 Fan 2 OFF   , 1 Fan 2 ON)
			BIT 5	(0 Fan 3 OFF   , 1 Fan 3 ON)
			BIT 6	(0 Manual Mode , 1 Automatic Mode for Fan)
			BIT 7	(0 Heating Mode, 1 Cooling Mode)
 */
        if (isStatusByteSet(statusByte, 0)) {
            if (isCoolMode())
                strout.append("COOL");
            else
                strout.append("HEAT");
        } else
            strout.append("OFF");

        if (isStatusByteSet(statusByte, 6))
            strout.append(" - Fan Auto");
        else
            strout.append(" - Fan Manual");


        strout.append(" ").append(TemperatureMeasuredVal).append("°").append(prefs.isFahrenheitChosen()?"F":"C")
                .append(" (").append(TemperatureSetpointVal).append("°").append(prefs.isFahrenheitChosen()?"F":"C").append(")");
        return strout.toString();
    }

    public float getTemperatureMeasuredVal() {
        return TemperatureMeasuredVal;
    }

    public void setTemperatureMeasuredVal(float temperatureMeasuredVal) {
        TemperatureMeasuredVal = temperatureMeasuredVal;
    }

    public float getTemperatureSetpointVal() {
        return TemperatureSetpointVal;
    }

    public void setTemperatureSetpointVal(float temperatureSetpointVal) {
        TemperatureSetpointVal = temperatureSetpointVal;
    }

    public boolean isCoolMode() {
        return isStatusByteSet(statusByte, 7);

    }

    public boolean isFannTurnedOn(int fan) {
        if (fan < 1 || fan > 3)
            return false;
        //fan start from
        return isStatusByteSet(statusByte, 2 + fan);
    }

    private boolean isHeatMode() {
        return !isCoolMode();

    }

    private boolean isStatusByteSet(int b, int n) {
        return ((b & (1L << n)) != 0);
    }

    public void issueCommand(final int function, final Float temp) {
        Thread t = new Thread() {
            public void run() {
                if (temp == null) {
                    Log.i(Constants.TAG, "ISSUE COMMAND W/O TEMP:" + String.valueOf((float) function));
                    UDPHelper.issueSoulissCommand("" + getParentNode().getId(), "" + getTypicalDTO().getSlot(),
                            SoulissApp.getOpzioni(), "" + function);

                } else {
                    int re = HalfFloatUtils.fromFloat(temp);
                    String first, second;
                    String pars = Long.toHexString(re);
                    Log.i(Constants.TAG, "PARSED SETPOINT TEMP: 0x" + pars);

                    try {
                        second = Integer.toString(Integer.parseInt(pars.substring(0, 2), 16));
                    } catch (StringIndexOutOfBoundsException sie) {
                        second = "0";
                    }
                    try {
                        first = Integer.toString(Integer.parseInt(pars.substring(2, 4), 16));
                    } catch (StringIndexOutOfBoundsException sie) {
                        first = "0";
                    }
                    //INVERTITI? Occhio
                    String[] cmd = {String.valueOf(function), "0", "0", first, second};
                    //verifyCommand(temp, first, second);
                    Log.i(Constants.TAG, "ISSUE COMMAND:" + String.valueOf(function) + " 0 0 " + first + " " + second);
                    UDPHelper.issueSoulissCommand("" + getParentNode().getId(), "" + getTypicalDTO().getSlot(),
                            SoulissApp.getOpzioni(), cmd);
                }
            }
        };
        t.start();
    }

    @Override
    public void setOutputDescView(TextView textStatusVal) {
        textStatusVal.setText(getOutputDesc());
        if ((typicalDTO.getOutput() == 0 || typicalDTO.getOutput() >> 6 == 1)
                || "UNKNOWN".compareTo(getOutputLongDesc()) == 0 || "NA".compareTo(getOutputLongDesc()) == 0) {
            textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_red));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
        } else {

            textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_green));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
        }
    }

    //TODO move in tests
    private void verifyCommand(Float toSend, String byteOne, String byteTwo) {
        int re = HalfFloatUtils.fromFloat(toSend);
        String pars = Long.toHexString(re);
        Log.d(Constants.TAG, "SetPoint" + toSend + ", in HEX:" + pars);
        String first = Integer.toString(Integer.parseInt(pars.substring(0, 2), 16));
        String second = Integer.toString(Integer.parseInt(pars.substring(2, 4), 16));
        Log.d(Constants.TAG, "Splitted (DEC) - first:" + first + " - second:" + second);

        short TemperatureMeasuredValueShift = Short.parseShort(first);
        short TemperatureMeasuredValue = Short.parseShort(second);
        int shifted = TemperatureMeasuredValueShift << 8;

        float reconv = HalfFloatUtils.toFloat(shifted + TemperatureMeasuredValue);
        Log.d(Constants.TAG, "Reconverted:" + reconv);
        Assert.assertTrue(byteOne.equals(first));
        Assert.assertTrue(byteTwo.equals(second));
    }

}
