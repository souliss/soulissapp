package it.angelic.soulissclient.helpers;

import it.angelic.soulissclient.Constants;

/**
 * Helper class to conver half precision float to int
 * int are used on analogue typicals (2 bytes)
 *  and should be reversed because of endianess
 * @author http://stackoverflow.com/users/237321/x4u
 * 
 *
 */
public class TimeHourSpinnerUtils {

	final static Float vals[]= {0f,0.25f,0.5f,0.75f,1f,2f,3f,4f,5f,6f,8f,12f,24f};

	public static int getTimeArrayPos(int warnDelayMsec) {
		for (int g=0;g<vals.length;g++)
		{
			if ((int)(vals[g]* Constants.MIN_IN_A_HOUR) * Constants.MSEC_IN_A_SEC * Constants.SEC_IN_A_MIN == warnDelayMsec)
				return g;
		}

		return 0;
	}

	public static int getTimeArrayValMsec(int arrayPos){
		return ((int)(vals[arrayPos]*Constants.MIN_IN_A_HOUR) * Constants.MSEC_IN_A_SEC * Constants.SEC_IN_A_MIN);


	}
}
