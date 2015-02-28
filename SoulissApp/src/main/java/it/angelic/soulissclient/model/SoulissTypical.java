package it.angelic.soulissclient.model;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.db.SoulissTypicalDTO;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.typicals.Constants;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Modella un tipico, ovvero una classe di dispositivi
 * Per ogni nodo ce ne possono essere da 0 a n, con
 * n < MAX_TYP_PER_NODE
 * <p/>
 * has-a SoulissTypicalDTO che riflette il DB
 *
 * @author shine@angelic.it
 */
public class SoulissTypical implements Serializable, ISoulissTypical {
    /**
     *
     */
    private static final long serialVersionUID = -7375342157142543740L;
    // nodo di appartenenza
    protected SoulissNode parentNode;
    // contenitore dati specchio del DB
    protected SoulissTypicalDTO typicalDTO;
    //transient per evitare problemi di serializzazione
    protected transient SoulissPreferenceHelper prefs;
    private boolean isSlave = false;// indica se includerlo nelle liste
    private boolean isSensor = false;// indica se va loggato

    public SoulissTypical(SoulissPreferenceHelper pre) {
        super();
        prefs = pre;
        setTypicalDTO(new SoulissTypicalDTO());
    }

    @Override
    public String getNiceName() {
        if (typicalDTO.getName() != null)
            return typicalDTO.getName();
        return getDefaultName();

    }

    public Float getOutput() {
        return Float.valueOf(typicalDTO.getOutput());
    }

    @Override
    public void setIconResourceId(int resId) {
        typicalDTO.setIconId(resId);

    }

    @Override
    public void setName(String newName) {
        typicalDTO.setName(newName);
    }

    @Override
    public String getName() {
        return typicalDTO.getName();
    }

    public short getTypical() {
      return typicalDTO.getTypical();
    }

    public short getSlot() {
        return typicalDTO.getSlot();
    }

    public void setRelated(SoulissTypical in) {
        throw new RuntimeException("Can't call setRelated on a single generic typical");
    }

    public String getDefaultName() {
        short typical = typicalDTO.getTypical();
        assertTrue(typical != -1);

        int id;
        if (typical == Constants.Souliss_T11)
            id = R.string.Souliss_T11_desc;
        else if (typical == Constants.Souliss_T12)
            id = R.string.Souliss_T12_desc;
        else if (typical == Constants.Souliss_T1A)
            id = R.string.Souliss_T1A_desc;
        else if (typical == Constants.Souliss_T13)
            id = R.string.Souliss_T13_desc;
        else if (typical == Constants.Souliss_T14)
            id = R.string.Souliss_T14_desc;
        else if (typical == Constants.Souliss_T16)
            id = R.string.Souliss_T16_desc;
        else if (typical == Constants.Souliss_T18)
            id = R.string.Souliss_T18_desc;
        else if (typical == Constants.Souliss_T19)
            id = R.string.Souliss_T19_desc;
        else if (typical == Constants.Souliss_T21)
            id = R.string.Souliss_T21_desc;
        else if (typical == Constants.Souliss_T22)
            id = R.string.Souliss_T22_desc;
        else if (typical == Constants.Souliss_T31)
            id = R.string.Souliss_T31_desc;
        else if (typical == Constants.Souliss_T_CurrentSensor)
            id = R.string.Souliss_TCurrentSensor_desc;
        else if (typical == Constants.Souliss_T_TemperatureSensor)
            id = R.string.Souliss_TTemperature_desc;
        else if (typical == Constants.Souliss_T_HumiditySensor)
            id = R.string.Souliss_THumidity_desc;
        else if (typical == Constants.Souliss_T32_IrCom_AirCon)
            id = R.string.Souliss_TAircon_desc;
        else if (typical == Constants.Souliss_T1n_RGB)
            id = R.string.Souliss_TRGB_desc;
        else if (typical == Constants.Souliss_T41_Antitheft_Main)
            id = R.string.Souliss_T41_desc;
        else if (typical == Constants.Souliss_T42_Antitheft_Peer || typical == Constants.Souliss_T43_Antitheft_LocalPeer)
            id = R.string.Souliss_T42_desc;
        else if (typical == Constants.Souliss_T51)
            id = R.string.Souliss_T51_desc;
        else if (typical == Constants.Souliss_T52_TemperatureSensor)
            id = R.string.Souliss_TTemperature_desc;
        else if (typical == Constants.Souliss_T53_HumiditySensor)
            id = R.string.Souliss_THumidity_desc;
        else if (typical == Constants.Souliss_T54_LuxSensor)
            id = R.string.Souliss_T54_desc;
        else if (typical == Constants.Souliss_T55_VoltageSensor)
            id = R.string.Souliss_T55_desc;
        else if (typical == Constants.Souliss_T56_CurrentSensor)
            id = R.string.Souliss_T56_desc;
        else if (typical == Constants.Souliss_T57_PowerSensor)
            id = R.string.Souliss_T57_desc;
        else if (typical == Constants.Souliss_T58_PressureSensor)
            id = R.string.Souliss_T58_desc;
        else
            id = R.string.unknown_typical;

        Context cc = SoulissClient.getAppContext();
        return cc.getResources().getString(id);
    }

    @Override
    public int getIconResourceId() {
        short typical = typicalDTO.getTypical();
        assertTrue(typical != -1);

        if (typicalDTO.getIconId() != 0)
            return typicalDTO.getIconId();
        if (typical == Constants.Souliss_T11)
            return R.drawable.light_on;
        else if (typical == Constants.Souliss_T12)
            return R.drawable.button;
        else if (typical == Constants.Souliss_T1A)
            return R.drawable.light_on;
        else if (typical == Constants.Souliss_T13)
            return R.drawable.light_on;
        else if (typical == Constants.Souliss_T14)
            return R.drawable.locked;
        else if (typical == Constants.Souliss_T16)
            return R.drawable.rgb;
        else if (typical == Constants.Souliss_T18)
            return R.drawable.power;
        else if (typical == Constants.Souliss_T19)
            return R.drawable.candle;
        else if (typical == Constants.Souliss_T21)
            return R.drawable.limit;
        else if (typical == Constants.Souliss_T22)
            return R.drawable.limit;
        else if (typical == Constants.Souliss_T31)
            return R.drawable.thermometer;
        else if (typical == Constants.Souliss_T41_Antitheft_Main)
            return R.drawable.shield;
        else if (typical == Constants.Souliss_T42_Antitheft_Peer || typical == Constants.Souliss_T43_Antitheft_LocalPeer)
            return R.drawable.shield;
        else if (typical == Constants.Souliss_T_TemperatureSensor)
            return R.drawable.thermometer;
        else if (typical == Constants.Souliss_T_related)
            return R.drawable.empty;
        else if (typical == Constants.Souliss_T_HumiditySensor)
            return R.drawable.raindrop;
        else if (typical == Constants.Souliss_T32_IrCom_AirCon)
            return R.drawable.snow;
        else if (typical == Constants.Souliss_T1n_RGB)
            return R.drawable.remote;
        else if (typical == Constants.Souliss_T51)
            return R.drawable.analog;
        else if (typical == Constants.Souliss_T52_TemperatureSensor)
            return R.drawable.thermometer;
        else if (typical == Constants.Souliss_T53_HumiditySensor)
            return R.drawable.raindrop;
        else if (typical == Constants.Souliss_T54_LuxSensor)
            return R.drawable.home;
        else if (typical == Constants.Souliss_T55_VoltageSensor)
            return R.drawable.lightning;
        else if (typical == Constants.Souliss_T56_CurrentSensor)
            return R.drawable.lightning;
        else if (typical == Constants.Souliss_T57_PowerSensor)
            return R.drawable.lightning;
        else if (typical == Constants.Souliss_T58_PressureSensor)
            return R.drawable.sun;
        else
            return R.drawable.empty_narrow;
    }

    public SoulissNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(@NonNull SoulissNode parentNode) {
        if (typicalDTO != null && parentNode.getId() != it.angelic.soulissclient.Constants.MASSIVE_NODE_ID)
            assertEquals(parentNode.getId(), typicalDTO.getNodeId());
        this.parentNode = parentNode;
    }

    public boolean isEmpty() {
        return typicalDTO.getTypical() == Constants.Souliss_T_empty;
    }

    public boolean isRelated() {
        return isSlave;
    }

    public void setRelated(boolean isSlave) {
        this.isSlave = isSlave;
    }

    public ArrayList<SoulissCommand> getCommands(Context ctx) {
        // to be overridden
        ArrayList<SoulissCommand> ret = new ArrayList<>();
        return ret;
    }

    public SoulissTypicalDTO getTypicalDTO() {
        return typicalDTO;
    }

    public void setTypicalDTO(SoulissTypicalDTO typicalDTO) {
        this.typicalDTO = typicalDTO;
    }

    @Override
    public String toString() {
        return getNiceName();
    }

    public boolean isSensor() {
        return isSensor;
    }

    public void setSensor(boolean isSensor) {
        this.isSensor = isSensor;
    }

    public SoulissPreferenceHelper getPrefs() {
        return prefs;
    }

    public void setPrefs(SoulissPreferenceHelper prefs) {
        this.prefs = prefs;
    }

    @Override
    public void getActionsLayout (final Context ctx, LinearLayout convertView){}

    protected TextView getQuickActionTitle() {
        // Infotext nascosto all'inizio
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        final TextView cmd = new TextView(SoulissClient.getAppContext());
        cmd.setText(/*SoulissClient.getAppContext().getString(R.string.actions)*/"");
        // cmd.setTextSize(ctx.getResources().getDimension(R.dimen.text_size));
        if (prefs != null && prefs.isLightThemeSelected())
            cmd.setTextColor(SoulissClient.getAppContext().getResources().getColor(R.color.black));

        cmd.setLayoutParams(lp);
        return cmd;
    }

    /**
     * Should be sub-implemented
     */
    public String getOutputDesc() {
        return "TOIMPLEMENT";
    }

    public void setOutputDescView(TextView textStatusVal) {
        textStatusVal.setText(getOutputDesc());
        if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil || "UNKNOWN".compareTo(getOutputDesc()) == 0 || "NA".compareTo(getOutputDesc()) == 0) {
            textStatusVal.setTextColor(SoulissClient.getAppContext().getResources().getColor(R.color.std_red));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackoff);
        } else {
            textStatusVal.setTextColor(SoulissClient.getAppContext().getResources().getColor(R.color.std_green));
            textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
        }
    }

    public short getNodeId() {
        return typicalDTO.getNodeId();
    }
}
