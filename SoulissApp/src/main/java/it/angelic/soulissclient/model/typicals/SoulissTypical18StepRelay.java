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
 *
 *         Handle one digital output based on hardware and software commands,
 *         output can be timed out.
 *         This logic can be used for lights, wall socket and all the devices
 *         that has an ON/OFF behaviour. It has a pulsed output, and can be
 *         used with bistable relay or similar devices.
 *         The actual state shall be reported with an external digital input
 *         such as an auxiliary contact or a current measure.
 *
 *         Hardware Command:
 *         If using a monostable wall switch (press and spring return),
 *         each press will toggle the output status.
 *         #define Souliss_T1n_ToggleCmd		0x01
 *
 *         If using a bistable wall switch (press without return), the two
 *         switch position can be associated with the ON and OFF commands
 *         #define Souliss_T1n_OnCmd			0x02
 *         #define Souliss_T1n_OffCmd			0x04
 *
 *         The actual state shall be reported using these two feedbacks in
 *         relevant INPUT slot.
 *         #define Souliss_T1n_OnFeedback			0x23
 *         #define Souliss_T1n_OffFeedback			0x24
 *
 *         Software Commands:
 *         From any available software interface, these commands will turn
 *         the light ON and OFF.
 *         #define Souliss_T1n_OnCmd			0x02
 *         #define Souliss_T1n_OffCmd			0x04
 *
 *         Command recap, using:
 *         -  1(hex) as command, toggle the output
 *         -  2(hex) as command, the output move to ON
 *         -  4(hex) as command, the output move to OFF
 *         -  0(hex) as command, no action
 *         Output status,
 *         -  0(hex) for state OFF,
 *         -  1(hex) for state ON,
 *         - A1(hex) for pulsed output.
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
        turnOnButton.setText(ctx.getString(R.string.ON));
        cont.addView(turnOnButton);

        final ListButton turnOffButton = new ListButton(ctx);
        turnOffButton.setText(ctx.getString(R.string.OFF));
        cont.addView(turnOffButton);

        turnOnButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs, String.valueOf(Constants.Typicals.Souliss_T1n_OnCmd));

                    }
                };
                t.start();
            }

        });

        turnOffButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs,
                                String.valueOf(Constants.Typicals.Souliss_T1n_OffCmd));
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

        SoulissCommand tt = new SoulissCommand(this);
        tt.setCommand(Constants.Typicals.Souliss_T1n_OffCmd);
        tt.setSlot(getTypicalDTO().getSlot());
        tt.setNodeId(getTypicalDTO().getNodeId());
        ret.add(tt);

        return ret;
    }

    @Override
    public String getOutputDesc() {

        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil || typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnFeedback)
            return context.getString(R.string.ON);
        else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil || typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffFeedback)
            return context.getString(R.string.OFF);
        return NOT_AVAILABLE;
    }

}
