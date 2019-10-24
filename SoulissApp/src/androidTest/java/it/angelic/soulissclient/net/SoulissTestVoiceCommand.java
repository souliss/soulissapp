package it.angelic.soulissclient.net;


import android.content.Context;
import android.os.Looper;

import junit.framework.TestCase;

import androidx.test.platform.app.InstrumentationRegistry;
import it.angelic.soulissclient.VoiceCommandActivityNoDisplay;


/**
 * Created by shine@angelic.it on 02/09/2015.
 */
public class SoulissTestVoiceCommand extends junit.framework.TestCase{


    public void testVoiceCommand() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Looper.prepare();
        VoiceCommandActivityNoDisplay.interpretCommand(context, "spegni luci");
    }


}
