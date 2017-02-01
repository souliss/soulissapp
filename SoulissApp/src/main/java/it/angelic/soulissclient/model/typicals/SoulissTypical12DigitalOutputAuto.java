package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class SoulissTypical12DigitalOutputAuto extends SoulissTypical implements ISoulissTypical {

    /**
     *
     */
    private static final long serialVersionUID = 4292781263370980816L;


    public SoulissTypical12DigitalOutputAuto(Context ctx, SoulissPreferenceHelper opts) {
        super(ctx, opts);
    }

    @Override
    public ArrayList<ISoulissCommand> getCommands(Context ctx) {
        // ritorna le bozze dei comandi, da riempire con la schermata addProgram
        ArrayList<ISoulissCommand> ret = new ArrayList<>();

        SoulissCommand t = new SoulissCommand(this);
        t.setCommand(Constants.Typicals.Souliss_T1n_OnCmd);
        t.setSlot(typicalDTO.getSlot());
        t.setNodeId(typicalDTO.getNodeId());
        ret.add(t);

        SoulissCommand tr = new SoulissCommand(this);
        tr.setCommand(Constants.Typicals.Souliss_T1n_OffCmd);
        tr.setSlot(typicalDTO.getSlot());
        tr.setNodeId(typicalDTO.getNodeId());
        ret.add(tr);

        SoulissCommand tj = new SoulissCommand(this);
        tj.setCommand(Constants.Typicals.Souliss_T1n_ToogleCmd);
        tj.setSlot(typicalDTO.getSlot());
        tj.setNodeId(typicalDTO.getNodeId());
        ret.add(tj);

        SoulissCommand td = new SoulissCommand(this);
        td.setCommand(Constants.Typicals.Souliss_T1n_AutoCmd);
        td.setSlot(typicalDTO.getSlot());
        td.setNodeId(typicalDTO.getNodeId());
        ret.add(td);

        return ret;
    }

    @Override
    public String getOutputDesc() {
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil)
            return context.getString(R.string.ON);
        else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil_Auto)
            return context.getString(R.string.ON) + " (AUTO)";
        else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil)
            return context.getString(R.string.OFF);
        else if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil_Auto)
            return "OFF (AUTO)";
        else
            return "UNKNOWN";
    }

    @Override
    public void setOutputDescView(@NonNull TextView textStatusVal) {
        textStatusVal.setText(getOutputDesc());
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil
                || "UNKNOWN".compareTo(getOutputDesc()) == 0
                || typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil_Auto) {
            textStatusVal.setTextColor(ContextCompat.getColor(context, R.color.std_red));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
        } else {
            textStatusVal.setTextColor(ContextCompat.getColor(context, R.color.std_green));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
        }
    }


    /**
     * Ottiene il layout del pannello comandi
     *
     */
    @Override
    public void getActionsLayout(Context ctx, LinearLayout cont) {
        cont.removeAllViews();

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        cont.addView(getQuickActionTitle());
        // cmd.setVisibility(View.GONE);

        final ListButton turnOnButton = new ListButton(ctx);
        turnOnButton.setText(ctx.getString(R.string.ON));
        cont.addView(turnOnButton);

        final ListButton turnOffButton = new ListButton(ctx);
        turnOffButton.setText(ctx.getString(R.string.OFF));
        cont.addView(turnOffButton);
        // disabilitazioni interlock
        if (typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil ||
                typicalDTO.getOutput() == Constants.Typicals.Souliss_T1n_OnCoil_Auto) {
            turnOnButton.setEnabled(false);
        } else {
            turnOffButton.setEnabled(false);
        }

        final ListButton tog = new ListButton(ctx);
        // final int tpos = position;
        //tog.setLayoutParams(lp);
        tog.setText("Auto");
        cont.addView(tog);

        turnOnButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
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
                Thread t = new Thread() {
                    public void run() {
                        UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
                                prefs, String.valueOf(Constants.Typicals.Souliss_T1n_AutoCmd));
                        // cmd.setText("Souliss command sent");
                    }
                };

                t.start();
            }

        });
    }


}
