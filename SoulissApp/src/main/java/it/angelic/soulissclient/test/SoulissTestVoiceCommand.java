package it.angelic.soulissclient.test;


import junit.framework.TestCase;

import it.angelic.soulissclient.VoiceCommandActivityNoDisplay;

import static androidx.test.InstrumentationRegistry.getContext;


/**
 * Created by shine@angelic.it on 02/09/2015.
 */
public class SoulissTestVoiceCommand extends TestCase {


    public void testVoiceCommand() {

        VoiceCommandActivityNoDisplay.interpretCommand(getContext(), "spegni luci");


    }


}
