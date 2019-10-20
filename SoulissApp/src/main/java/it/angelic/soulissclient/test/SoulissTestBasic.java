package it.angelic.soulissclient.test;

import android.util.Log;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.HalfFloatUtils;

/**
 * Created by shine@angelic.it on 02/09/2015.
 */
public class SoulissTestBasic extends junit.framework.TestCase {


    public void testHalfFloat() {
        Float testF = 16.72f;
        Log.i(Constants.TAG, "Testing float");
        int converted = HalfFloatUtils.fromFloat(testF);
        assertEquals(testF, round(HalfFloatUtils.toFloat(converted), 2));
    }

    /**
     * Testa il giro delirio degli half-float
     * Occhio all'endianess
     */
    public void testByteFloatBackAndForth() {
        Float testF = 66.75f;
        int converted = HalfFloatUtils.fromFloat(testF);

        String pars = Long.toHexString(converted);
        Log.i(Constants.TAG, "HEX String: 0x" + pars + " DECODE: " + Long.decode("0x" + pars));

        String first = Integer.toString(Integer.parseInt(pars.substring(0, 2), 16));
        String second = Integer.toString(Integer.parseInt(pars.substring(2, 4), 16));

        short TemperatureMeasuredValue2 = Short.parseShort(first);
        short TemperatureMeasuredValue = Short.parseShort(second);

        // ora ho i due bytes, li converto
        int shifted = TemperatureMeasuredValue2 << 8;
        Log.d(Constants.TAG, "first:" + Long.toHexString((long) TemperatureMeasuredValue)
                + " second:" + Long.toHexString(TemperatureMeasuredValue2)
                + " SENSOR Reading:" + Long.toHexString((long) shifted + TemperatureMeasuredValue)
                + " SHORT SUM: " + ((long) shifted + TemperatureMeasuredValue));

        Float compare = HalfFloatUtils.toFloat(shifted + TemperatureMeasuredValue);
        Log.i(Constants.TAG, "DECODED: " + compare);
        assertEquals(testF, compare);
    }




    private static float round(float value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (float) Math.round(value * scale) / scale;
    }
}
