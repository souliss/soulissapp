package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import java.util.ArrayList;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.views.ListButton;

/**
 * Typical 18 : Step Relay Output
 * <p>
 * One way trigger switch. The only command available will turn it on.
 * Souliss will turn it off after some cycle individually
 *
 * @author shine@angelic.it
 */
public class SoulissTypical18StepRelay extends SoulissTypical implements ISoulissTypical {

    private static final long serialVersionUID = 4553488325062232092L;

    public SoulissTypical18StepRelay(Context context, SoulissPreferenceHelper pre) {
        super(context, pre);
    }


    /**
     * Ottiene il layout del pannello comandi
     */
    @Override
    public void getActionsLayout(Context ctx, LinearLayout cont) {
        cont.removeAllViews();

        //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
        //	RelativeLayout.LayoutParams.MATCH_PARENT);
        // cmd.setGravity(Gravity.TOP);
        cont.addView(getQuickActionTitle());

        final ListButton turnOnButton = new ListButton(ctx);
        turnOnButton.setText(ctx.getString(R.string.step));

        cont.addView(turnOnButton);

        turnOnButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //turnOnButton.setEnabled(false);
                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs, String.valueOf(Constants.Typicals.Souliss_T1n_OnCmd));

                    }
                };
                t.start();
            }

        });
    }

    @Override
    public ArrayList<ISoulissCommand> getCommands(Context ctx) {
        // ritorna le bozze dei comandi, da riempire con la schermata addProgram
        ArrayList<ISoulissCommand> ret = new ArrayList<>();

        SoulissCommand t = new SoulissCommand(this);
        t.setCommand(Constants.Typicals.Souliss_T1n_OnCmd);
        t.setSlot(getTypicalDTO().getSlot());
        t.setNodeId(getTypicalDTO().getNodeId());
        ret.add(t);

        return ret;
    }

    @Override
    public String getOutputDesc() {
        return "NA";
    }

}
