package it.angelic.soulissclient.test;

import android.test.AndroidTestCase;

import it.angelic.soulissclient.VoiceCommandActivityNoDisplay;

/**
 * Created by shine@angelic.it on 02/09/2015.
 */
public class SoulissTestVoiceCommand extends AndroidTestCase {


    public void testVoiceCommand() {

        VoiceCommandActivityNoDisplay.interpretCommand(getContext(), "spegni luci");


    }


}
