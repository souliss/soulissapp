package it.angelic.soulissclient;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import it.angelic.soulissclient.fragments.T16RGBAdvancedFragment;
import it.angelic.soulissclient.fragments.T19SingleChannelLedFragment;
import it.angelic.soulissclient.fragments.T1nGenericLightFragment;
import it.angelic.soulissclient.fragments.T31HeatingFragment;
import it.angelic.soulissclient.fragments.T32AirConFragment;
import it.angelic.soulissclient.fragments.T4nFragment;
import it.angelic.soulissclient.fragments.T5nSensorFragment;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.model.typicals.SoulissTypical14PulseOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical15;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.model.typicals.SoulissTypical32AirCon;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;

import static junit.framework.Assert.assertTrue;

/**
 * Wrapper per poter aprire dall'esterno direttamente un dettaglio
 */
public class TypicalDetailFragWrapper extends AbstractStatusedFragmentActivity {
    private SoulissTypical collected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        // recuper nodo da extra
        setContentView(R.layout.main_detailwrapper);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            supportFinishAfterTransition();
            return;
        }
        Bundle extras = getIntent().getExtras();
        // collected.setCtx(getActivity());

        if (extras != null && extras.get("TIPICO") != null)
            collected = (SoulissTypical) extras.get("TIPICO");
        assertTrue("TIPICO NULLO", collected != null);



            Fragment NewFrag = null;
            Log.w(Constants.TAG, "TypicalDetailFragWrapper should not be used like this: Legacy support");
            if (collected.isSensor())
                NewFrag = T5nSensorFragment.newInstance(collected.getTypicalDTO().getSlot(), collected);
            else if (collected instanceof SoulissTypical16AdvancedRGB)
                NewFrag = T16RGBAdvancedFragment.newInstance(collected.getTypicalDTO().getSlot(), collected);
            else if (collected instanceof SoulissTypical19AnalogChannel)
                NewFrag = T19SingleChannelLedFragment.newInstance(collected.getTypicalDTO().getSlot(), collected);
            else if (collected instanceof SoulissTypical31Heating)
                NewFrag = T31HeatingFragment.newInstance(collected.getTypicalDTO().getSlot(), collected);
            else if (collected instanceof SoulissTypical11DigitalOutput || collected instanceof SoulissTypical12DigitalOutputAuto)
                NewFrag = T1nGenericLightFragment.newInstance(collected.getTypicalDTO().getSlot(), collected);
            else if (collected instanceof SoulissTypical41AntiTheft || collected instanceof SoulissTypical42AntiTheftPeer || collected instanceof SoulissTypical43AntiTheftLocalPeer)
                NewFrag = T4nFragment.newInstance(collected.getTypicalDTO().getSlot(), collected);
            else if (collected instanceof SoulissTypical32AirCon)
                NewFrag = T32AirConFragment.newInstance(collected.getTypicalDTO().getSlot(), collected);
            else if (collected instanceof SoulissTypical14PulseOutput) {
                //no detail, notice user and return
                Toast.makeText(this,
                        getString(R.string.status_souliss_nodetail), Toast.LENGTH_SHORT)
                        .show();
                return;
            } else {
                //TODO transform these in Frags
                 if (collected instanceof SoulissTypical15) {
                    Intent nodeDatail = new Intent(this, T15RGBIrActivity.class);
                    nodeDatail.putExtra("TIPICO", collected);
                    startActivity(nodeDatail);
                }else{
                    Log.e(Constants.TAG, "SERIOUS: Unknowsn typical");
                }
                supportFinishAfterTransition();
                return;
            }
            // During initial setup, plug in the details fragment.
            //T1nGenericLightFragment details = T1nGenericLightFragment.newInstance(collected.getTypicalDTO().getSlot(),
            //		collected);
            NewFrag.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.detailPane, NewFrag).commit();
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(collected == null ? getString(R.string.scenes_title) : collected.getNiceName());
    }


}
