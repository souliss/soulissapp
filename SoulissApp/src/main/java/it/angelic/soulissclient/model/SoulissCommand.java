package it.angelic.soulissclient.model;

import android.content.Context;
import android.support.annotation.DrawableRes;

import java.io.Serializable;
import java.util.Calendar;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
import it.angelic.soulissclient.net.UDPHelper;

import static junit.framework.Assert.assertEquals;

/**
 * Il comando e` nato per riflettere qualcosa da inviare
 * poi e` stato esteso ai massivi
 * poi agli scenari. Un programma puo` infatti voler
 * eseguire uno scenario, e persisterlo
 * <p/>
 * nel caso scenario, nodeId = -2
 * nel caso massivo, -1
 */
public class SoulissCommand implements Serializable, ISoulissCommand {

    private static final long serialVersionUID = -918392561828980547L;
    private SoulissCommandDTO commandDTO;
    private SoulissTypical parentTypical;
    private SoulissScene targetScene;

    public SoulissCommand(SoulissTypical parentTypical) {
        super();
        this.commandDTO = new SoulissCommandDTO();
        commandDTO.setSlot(parentTypical.getTypicalDTO().getSlot());
        commandDTO.setNodeId(parentTypical.getParentNode().getId());
        this.parentTypical = parentTypical;
        if (parentTypical.getParentNode() != null)
            assertEquals(commandDTO.getNodeId(), parentTypical.getParentNode().getId());
    }

    public SoulissCommand(SoulissCommandDTO dto, SoulissTypical parentTypical) {
        this(dto);
        this.parentTypical = parentTypical;
        if (parentTypical.getParentNode() != null)
            assertEquals(dto.getNodeId(), parentTypical.getParentNode().getId());

    }

    public SoulissCommand(SoulissCommandDTO dto) {
        super();
        this.commandDTO = dto;
        // falso se trigger assertEquals(true, dto.getSceneId() != 0);
        if (dto.getNodeId() == it.angelic.soulissclient.Constants.COMMAND_FAKE_SCENE) {
            SoulissDBHelper db = new SoulissDBHelper(SoulissApp.getAppContext());
            targetScene = db.getScene(dto.getSlot());
            commandDTO.setSceneId(null);
        }
    }

    /**
     * puo capitare che parentScene non sia nulla e nel DTO invece scene_ID sia null
     * infatti se nel DB SECENEID e` diverso da null, si tratta di comandi che definiscono uno
     * scenario. qui invece ParentScene
     *
     * @return
     */
    public SoulissScene getTargetScene() {
        return targetScene;
    }

    public void setTargetScene(SoulissScene parentScene) {
        this.targetScene = parentScene;

    }

    /*Quando targetScene non e` nullo, lo e` parentTypical, e nodeId nel DTO vale -2
    * In pratica targetScene e typical sono mutuali, in base a cosa controlla il comando */
    public SoulissTypical getParentTypical() {
        return parentTypical;
    }

    public void setParentTypical(SoulissTypical parentTypical) {
        this.parentTypical = parentTypical;
    }

    public int getType() {
        return commandDTO.getType();
    }

    public SoulissCommandDTO getCommandDTO() {
        return commandDTO;
    }

    // FIXME ritorna alla cazzo, rivedere le icone dei comandi
    public
    @DrawableRes
    int getIconResId() {
        if (targetScene != null) {
            return targetScene.getIconResourceId();
        } else if (commandDTO.getNodeId() == it.angelic.soulissclient.Constants.MASSIVE_NODE_ID) {
            // comando massivo
            return R.drawable.arrowmove1;
        }
        short typical = parentTypical.getTypicalDTO().getTypical();
        long command = commandDTO.getCommand();
        int resId;
        if (typical == Constants.Typicals.Souliss_T11 || typical == Constants.Typicals.Souliss_T18) {
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.drawable.light_on;
            else if (command == Constants.Typicals.Souliss_T1n_OffCmd)
                resId = R.drawable.light_off;
            else if (command == Constants.Typicals.Souliss_T1n_RstCmd)
                resId = R.drawable.sos;
            else if (command == Constants.Typicals.Souliss_T1n_ToogleCmd)
                resId = R.drawable.button;
            else
                resId = R.drawable.bell;
        } else if (typical == Constants.Typicals.Souliss_T12) {
            resId = R.drawable.sos;
        } else if (typical == Constants.Typicals.Souliss_T13) {
            resId = R.drawable.sos;
        } else if (typical == Constants.Typicals.Souliss_T14) {
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.drawable.lock;
            else
                resId = R.drawable.sos;
        } else if (typical == Constants.Typicals.Souliss_T16) {
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.drawable.light_on;
            else if (command == Constants.Typicals.Souliss_T1n_OffCmd)
                resId = R.drawable.light_off;
            else
                resId = R.drawable.rgb;
        } else if (typical == Constants.Typicals.Souliss_T19)
            resId = R.drawable.candle;
        else if (typical == Constants.Typicals.Souliss_T21)
            resId = R.drawable.sos;
        else if (typical == Constants.Typicals.Souliss_T22)
            resId = R.drawable.sos;
        else if (typical == Constants.Typicals.Souliss_T31){
            resId = R.drawable.sos;
        }
        else if (typical == Constants.Typicals.Souliss_T_CurrentSensor)
            resId = R.drawable.sos;
        else if (typical == Constants.Typicals.Souliss_T_TemperatureSensor)
            resId = R.drawable.sos;
        else if (typical == Constants.Typicals.Souliss_T_HumiditySensor)
            resId = R.drawable.sos;
        else
            resId = R.drawable.empty;

        return resId;
    }


    @Override
    public void execute() {
        SoulissCommandDTO dto = getCommandDTO();
        Calendar now = Calendar.getInstance();
        if (dto.getNodeId() == it.angelic.soulissclient.Constants.COMMAND_FAKE_SCENE) {
            //in realta devo eseguire una scena, non questo comando
            //salvato adalla Addprogram activity
            targetScene.execute();
            return;
        } else if (dto.getNodeId() == it.angelic.soulissclient.Constants.MASSIVE_NODE_ID) {
            String intero = Long.toHexString(dto.getCommand());
            String[] laCosa = ScenesDialogHelper.splitStringEvery(intero, 2);
            //codice che funziona ma non so perche`
            for (int i = 0; i < laCosa.length; i++) {
                laCosa[i] = "0x" + laCosa[i];
            }
            //split the command if longer
            UDPHelper.issueMassiveCommand(String.valueOf(dto.getSlot()), SoulissApp.getOpzioni(), laCosa);
        } else {// COMANDO SINGOLO
            String start = Long.toHexString(dto.getCommand());
            String[] laCosa = ScenesDialogHelper.splitStringEvery(start, 2);
            // String[] laCosa = start.split("(?<=\\G..)");
            for (int i = 0; i < laCosa.length; i++) {
                laCosa[i] = "0x" + laCosa[i];
            }
            //codice che funziona ma non so perche`
            UDPHelper.issueSoulissCommand(String.valueOf(dto.getNodeId()),
                    String.valueOf(dto.getSlot()), SoulissApp.getOpzioni(),
                    // pura magia della decode
                    laCosa);
        }
        //in base al tipo, segno ultima esecuzione
        if (getType() == it.angelic.soulissclient.Constants.COMMAND_TIMED
                && now.after(getCommandDTO().getScheduledTime())) {
            getCommandDTO().setExecutedTime(now);
        } else if (getType() == it.angelic.soulissclient.Constants.COMMAND_COMEBACK_CODE) {
            getCommandDTO().setExecutedTime(now);
            getCommandDTO().setSceneId(null);
        } else if (getType() == it.angelic.soulissclient.Constants.COMMAND_GOAWAY_CODE) {
            getCommandDTO().setExecutedTime(now);
            getCommandDTO().setSceneId(null);
        }
    }

    public short getNodeId() {
        return commandDTO.getNodeId();
    }

    public short getSlot() {
        return commandDTO.getSlot();
    }

    public long getCommand() {
        return commandDTO.getCommand();
    }

    @Override
    public void setIconResourceId(@DrawableRes int resId) {
    }

    @Override
    public
    @DrawableRes
    int getIconResourceId() {
        return getIconResId();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        short typical;
        if (targetScene != null) {
            return SoulissApp.getAppContext().getString(R.string.execute)
                    + " " + SoulissApp.getAppContext().getString(R.string.scene)
                    + " " + targetScene.getNiceName();
        } else
            typical = parentTypical.getTypical();
        long command = commandDTO.getCommand();
        int resId;
        if (typical == Constants.Typicals.Souliss_T11 || typical == Constants.Typicals.Souliss_T12 || typical == Constants.Typicals.Souliss_T13 ||
                typical == Constants.Typicals.Souliss_T18 || typical == Constants.Typicals.Souliss_T19) {
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.string.TurnON;
            else if (command == Constants.Typicals.Souliss_T1n_OffCmd)
                resId = R.string.TurnOFF;
            else if (command == Constants.Typicals.Souliss_T1n_RstCmd)
                resId = R.string.Souliss_ResetCmd_desc;
            else if (command == Constants.Typicals.Souliss_T1n_ToogleCmd)
                resId = R.string.Souliss_ToggleCmd_desc;
            else if (command == Constants.Typicals.Souliss_T1n_AutoCmd)
                resId = R.string.Souliss_AutoCmd_desc;
            else if (command == Constants.Typicals.Souliss_T19_Min)
                resId = R.string.Souliss_T19_Min_desc;
            else if (command == Constants.Typicals.Souliss_T19_Med)
                resId = R.string.Souliss_T19_Med_desc;
            else if (command == Constants.Typicals.Souliss_T19_Max)
                resId = R.string.Souliss_T19_Max_desc;
            else
                resId = R.string.Souliss_UndefinedCmd_desc;
        } else if (typical == Constants.Typicals.Souliss_T14) {
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.string.Souliss_OpenCmd_desc;
            else
                resId = R.string.Souliss_UndefinedCmd_desc;
        } else if (typical == Constants.Typicals.Souliss_T16) {//RGB
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.string.TurnON;
            else if (command == Constants.Typicals.Souliss_T1n_OffCmd)
                resId = R.string.TurnOFF;
            else if (command == Constants.Typicals.Souliss_T1n_ToogleCmd)
                resId = R.string.toggle;
            else if (command == Constants.Typicals.Souliss_T16_Red)
                resId = R.string.red;
            else if (command == Constants.Typicals.Souliss_T16_Green)
                resId = R.string.green;
            else if (command == Constants.Typicals.Souliss_T16_Blue)
                resId = R.string.blue;
            else
                resId = R.string.Souliss_UndefinedCmd_desc;
        } else if (typical == Constants.Typicals.Souliss_T21)
            if (command == Constants.Typicals.Souliss_T2n_CloseCmd)
                resId = R.string.Souliss_CloseCmd_desc;
            else if (command == Constants.Typicals.Souliss_T2n_OpenCmd)
                resId = R.string.Souliss_OpenCmd_desc;
            else if (command == Constants.Typicals.Souliss_T2n_StopCmd)
                resId = R.string.Souliss_StopCmd_desc;
            else if (command == Constants.Typicals.Souliss_T2n_ToogleCmd)
                resId = R.string.Souliss_ToggleCmd_desc;
            else
                resId = R.string.Souliss_UndefinedCmd_desc;
        else if (typical == Constants.Typicals.Souliss_T22) {
            if (command == Constants.Typicals.Souliss_T2n_CloseCmd)
                resId = R.string.Souliss_CloseCmd_desc;
            else if (command == Constants.Typicals.Souliss_T2n_OpenCmd)
                resId = R.string.Souliss_OpenCmd_desc;
            else if (command == Constants.Typicals.Souliss_T2n_StopCmd)
                resId = R.string.Souliss_StopCmd_desc;
            else
                resId = R.string.Souliss_UndefinedCmd_desc;
        } else if (typical == Constants.Typicals.Souliss_T31)
            resId = R.string.Souliss_T31_desc;
        else if (typical == Constants.Typicals.Souliss_T_CurrentSensor)
            resId = R.string.Souliss_TCurrentSensor_desc;
        else if (typical == Constants.Typicals.Souliss_T_TemperatureSensor)
            resId = R.string.Souliss_TTemperature_desc;
        else if (typical == Constants.Typicals.Souliss_T_HumiditySensor)
            resId = R.string.Souliss_THumidity_desc;
        else if (typical == Constants.Typicals.Souliss_T32_IrCom_AirCon) {
            if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Auto_20)
                resId = R.string.Souliss_T_IrCom_AirCon_Pow_Auto_20_desc;
            else if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Auto_24)
                resId = R.string.Souliss_T_IrCom_AirCon_Pow_Auto_24_desc;
            else if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Cool_18)
                resId = R.string.Souliss_T_IrCom_AirCon_Pow_Cool_18_desc;
            else if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Cool_22)
                resId = R.string.Souliss_T_IrCom_AirCon_Pow_Cool_22_desc;
            else if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Cool_26)
                resId = R.string.Souliss_T_IrCom_AirCon_Pow_Cool_26_desc;
            else if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Dry)
                resId = R.string.Souliss_T_IrCom_AirCon_Pow_Dry_desc;
            else if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Fan)
                resId = R.string.Souliss_T_IrCom_AirCon_Pow_Fan_desc;
            else if (command == Constants.Typicals.Souliss_T_IrCom_AirCon_Pow_Off)
                resId = R.string.TurnOFF;
            else
                resId = R.string.Souliss_emptycmd_desc;
        } else if (typical == Constants.Typicals.Souliss_T15_RGB) {
            if (command == Constants.Typicals.Souliss_T1n_RGB_OnCmd)
                resId = R.string.TurnON;
            else if (command == Constants.Typicals.Souliss_T1n_RGB_OffCmd)
                resId = R.string.TurnOFF;
            else
                resId = R.string.Souliss_emptycmd_desc;
        } else
            resId = R.string.Souliss_emptycmd_desc;

        return SoulissApp.getAppContext().getString(resId);
    }

    @Override
    public void setName(String newName) {
        throw new Error("Commands don't support custom names");
    }

    @Override
    public String getNiceName() {
        StringBuilder info = new StringBuilder();
        Context ctx = SoulissApp.getAppContext();
        info.append(ctx.getString(R.string.scene_send_command));
        info.append(" ");
        info.append(getName()).append(" ");
        if (getParentTypical() != null) {
            SoulissTypical appo = getParentTypical();
            if (appo.getNodeId() == it.angelic.soulissclient.Constants.MASSIVE_NODE_ID) {
                info.append(ctx.getString(R.string.to_all)).append(" ");
                info.append(ctx.getString(R.string.compatible)).append(" (");
                info.append(appo.getNiceName()).append(" )");
            } else {
                info.append(ctx.getString(R.string.to)).append(" ");
                // Descrizione programma
                if ("".compareTo(appo.getNiceName()) != 0)
                    info.append(" ").append(appo.getNiceName());
                if ("".compareTo(appo.getParentNode().getNiceName()) != 0)
                    info.append(" (").append(appo.getParentNode().getNiceName()).append(")");
            }
        } else {
            return getName();
        }
        return info.toString();
    }

    /*
    holder.textCmd.setText(context.getResources().getString(R.string.scene_send_command) + " \""
					+ holder.data.toString() + "\" " + context.getResources()
					+ context.getResources().getString(R.string.compatible) + " ("
					+ holder.data.getParentTypical().getNiceName() + ")");
		*/

}
