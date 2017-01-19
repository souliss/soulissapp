package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.views.ListButton;
import it.angelic.soulissclient.views.ListSwitchCompat;

/**
 * Handle one digital output based on hardware and software commands, output can
 * be timed out.
 * <p/>
 * This logic can be used for lights, wall socket and all the devices that has
 * an ON/OFF behavior.
 *
 * @author Ale
 */
public class SoulissTypical11DigitalOutput extends SoulissTypical implements ISoulissTypical {

    /**
     *
     */
    private static final long serialVersionUID = 4553488985062232592L;

    // Context ctx;

    public SoulissTypical11DigitalOutput(SoulissPreferenceHelper fg) {
        super(fg);
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

        SoulissCommand ter = new SoulissCommand(this);
        ter.setCommand(Constants.Typicals.Souliss_T1n_ToogleCmd);
        ter.setSlot(typicalDTO.getSlot());
        ter.setNodeId(typicalDTO.getNodeId());
        ret.add(ter);

        return ret;
    }

    /**
     * Ottiene il layout del pannello comandi
     */
    @Override
    public void getActionsLayout(Context ctx, LinearLayout cont) {
        cont.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        cont.addView(getQuickActionTitle());
        // cmd.setVisibility(View.GONE);

        final ListSwitchCompat tog = new ListSwitchCompat(ctx);
        // final int tpos = position;
        tog.setLayoutParams(lp);
        tog.setTextOff("I/O");
        tog.setTextOn("I/O");
        cont.addView(tog);

        final ListButton turnOnButton = new ListButton(ctx);
        turnOnButton.setLayoutParams(lp);
        turnOnButton.setText(ctx.getString(R.string.ON));
        cont.addView(turnOnButton);

        final ListButton turnOffButton = new ListButton(ctx);
        turnOffButton.setLayoutParams(lp);
        turnOffButton.setText(ctx.getString(R.string.OFF));
        cont.addView(turnOffButton);
        // disabilitazioni interlock
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil || typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnFeedback) {
            turnOnButton.setEnabled(false);
            tog.setChecked(true);
        } else {
            turnOffButton.setEnabled(false);
            tog.setChecked(false);
        }

        turnOnButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //tog.setEnabled(false);
                //turnOnButton.setEnabled(false);
                //turnOffButton.setEnabled(false);
                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs,
                                String.valueOf(Constants.Typicals.Souliss_T1n_OnCmd));
                    }
                };
                t.start();
            }

        });

        turnOffButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //tog.setEnabled(false);
                //turnOnButton.setEnabled(false);
                //turnOffButton.setEnabled(false);
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

        tog.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //tog.setEnabled(false);
                //turnOnButton.setEnabled(false);
                //turnOffButton.setEnabled(false);

                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs,
                                String.valueOf(Constants.Typicals.Souliss_T1n_ToogleCmd));
                        // cmd.setText("Souliss command sent");
                    }
                };
                t.start();
            }
        });

    }

    @Override
    public void setOutputDescView(TextView textStatusVal) {
        textStatusVal.setText(getOutputDesc());
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil || typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffFeedback ||
                "UNKNOWN".compareTo(getOutputDesc()) == 0 ||
                "NA".compareTo(getOutputDesc()) == 0) {
            textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_red));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
            //textStatusVal.setBackground(ctx.getResources().getDrawable(R.drawable.borderedbackoff));
        } else {
            textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_green));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
            //textStatusVal.setBackground(ctx.getResources().getDrawable(R.drawable.borderedbackon));
        }
    }

    @Override
    public String getOutputDesc() {
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil || typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnFeedback)
            return SoulissApp.getAppContext().getString(R.string.ON);
        else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil || typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffFeedback)
            return SoulissApp.getAppContext().getString(R.string.OFF);
        else if (typicalDTO.getOutput() >= Constants.Typicals.Souliss_T1n_Timed)
            return "" + typicalDTO.getOutput();
            //return ctx.getString(R.string.Souliss_TRGB_sleep);
        else
            return "UNKNOWN";
    }

}
