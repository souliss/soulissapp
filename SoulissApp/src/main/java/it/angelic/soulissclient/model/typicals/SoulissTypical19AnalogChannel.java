package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
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

public class SoulissTypical19AnalogChannel extends SoulissTypical implements ISoulissTypical {

    // SoulissNode parentd = getParentNode();
    // SoulissTypical related =
    // parentd.getTypical((short)(getTypicalDTO().getSlot()+1));

    /**
     *
     */
    private static final long serialVersionUID = 45534215562542092L;

    // Context ctx;

    public SoulissTypical19AnalogChannel(SoulissPreferenceHelper pp) {
        super(pp);
    }

    @Override
    public ArrayList<ISoulissCommand> getCommands(Context ctx) {
        // ritorna le bozze dei comandi, da riempire con la schermata addProgram
        ArrayList<ISoulissCommand> ret = new ArrayList<>();

        SoulissCommand t = new SoulissCommand( this);
        t.setCommand(Constants.Typicals.Souliss_T1n_OnCmd);
        t.setSlot(getTypicalDTO().getSlot());
        t.setNodeId(getTypicalDTO().getNodeId());
        ret.add(t);

        SoulissCommand ff = new SoulissCommand( this);
        ff.setCommand(Constants.Typicals.Souliss_T1n_OffCmd);
        ff.setSlot(getTypicalDTO().getSlot());
        ff.setNodeId(getTypicalDTO().getNodeId());
        ret.add(ff);

        SoulissCommand ft = new SoulissCommand( this);
        ft.setCommand(Constants.Typicals.Souliss_T1n_ToogleCmd);
        ft.setSlot(getTypicalDTO().getSlot());
        ft.setNodeId(getTypicalDTO().getNodeId());
        ret.add(ft);

        SoulissCommand ftt = new SoulissCommand( this);
        ftt.setCommand(Constants.Typicals.Souliss_T19_Min);
        ftt.setSlot(getTypicalDTO().getSlot());
        ftt.setNodeId(getTypicalDTO().getNodeId());
        ret.add(ftt);

        SoulissCommand fttd = new SoulissCommand( this);
        fttd.setCommand(Constants.Typicals.Souliss_T19_Med);
        fttd.setSlot(getTypicalDTO().getSlot());
        fttd.setNodeId(getTypicalDTO().getNodeId());
        ret.add(fttd);

        SoulissCommand ftts = new SoulissCommand( this);
        ftts.setCommand(Constants.Typicals.Souliss_T19_Max);
        ftts.setSlot(getTypicalDTO().getSlot());
        ftts.setNodeId(getTypicalDTO().getNodeId());
        ret.add(ftts);
        return ret;
    }

    public int getIntensity() {
        int r = getParentNode().getTypical((short) (typicalDTO.getSlot() + 1))
                .getTypicalDTO().getOutput();
        return r;
    }

    public String getIntensityPercent(){
        return ""+String.valueOf((int)(Float.valueOf(getIntensity() / 255f) * 100)) + "%";
    }

    /**
     * Ottiene il layout del pannello comandi
     *
     * @param ctx contesto
     * @param convertView la View da popolare
     */
    @Override
    public void getActionsLayout(final Context ctx,
                                 LinearLayout convertView) {

        LinearLayout cont = convertView;
        cont.removeAllViews();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        cont.addView(getQuickActionTitle());
        /*
		 * 
		 * TRE BOTTONI: ON, OFF e TOGGLE
		 */

        final ListButton turnON = new ListButton(ctx);
        turnON.setText("ON");
        turnON.setLayoutParams(lp);
        cont.addView(turnON);

        final ListButton turnOFF = new ListButton(ctx);
        turnOFF.setText("OFF");
        turnOFF.setLayoutParams(lp);
        cont.addView(turnOFF);

        turnON.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                turnON.setEnabled(false);
                turnOFF.setEnabled(false);
                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs, String.valueOf(Constants.Typicals.Souliss_T1n_OnCmd));
                    }
                };

                t.start();

            }

        });

        turnOFF.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                turnON.setEnabled(false);
                turnOFF.setEnabled(false);
                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs, String.valueOf(Constants.Typicals.Souliss_T1n_OffCmd));
                    }
                };
                t.start();
            }

        });

    }

    @Override
    public void setOutputDescView(TextView textStatusVal) {
        textStatusVal.setText(getOutputDesc());
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil || "UNKNOWN".compareTo(getOutputDesc()) == 0) {
            textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_red));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
        } else {
            textStatusVal.setTextColor(SoulissApp.getAppContext().getResources().getColor(R.color.std_green));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
        }
    }

    @Override
    public String getOutputDesc() {
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil)
            return SoulissApp.getAppContext().getString(R.string.OFF);
        else
            return SoulissApp.getAppContext().getString(R.string.ON) + " "  +getIntensityPercent();
    }

    /**
     * ***********************************************************************
     * Souliss RGB light command Souliss OUTPUT Data is:
     * <p/>
     * <p/>
     * INPUT data 'read' from GUI
     * ************************************************************************
     */
    public void issueSingleChannelCommand(final short command, final int intensity, final boolean togMulticast) {

        Thread t = new Thread() {
            public void run() {
                Looper.prepare();

                if (togMulticast)//a tutti i nodi
                    UDPHelper.issueMassiveCommand("" + Constants.Typicals.Souliss_T19, prefs, "" + command, "" + intensity);
                else
                    UDPHelper.issueSoulissCommand("" + getParentNode().getNodeId(), ""
                            + getTypicalDTO().getSlot(), prefs, "" + command, "" + intensity);
            }
        };

        t.start();
    }
    /**
     * ***********************************************************************
     * Souliss RGB light command Souliss OUTPUT Data is:
     * <p/>
     * <p/>
     * INPUT data 'read' from GUI
     * ************************************************************************
     */
    public void issueSingleChannelCommand(final short command,  final boolean togMulticast) {

        Thread t = new Thread() {
            public void run() {
                Looper.prepare();

                if (togMulticast)//a tutti i nodi
                    UDPHelper.issueMassiveCommand("" + Constants.Typicals.Souliss_T19, prefs, "" + command);
                else
                    UDPHelper.issueSoulissCommand("" + getParentNode().getNodeId(), ""
                            + getTypicalDTO().getSlot(), prefs, "" + command );
            }
        };

        t.start();
    }

    public void issueRefresh() {

        Thread t = new Thread() {
            public void run() {
                Looper.prepare();
                //refresh data for typical's node
                UDPHelper.pollRequest(prefs, 1, getParentNode().getNodeId());
            }
        };

        t.start();
    }

}
