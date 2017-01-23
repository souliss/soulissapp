package it.angelic.soulissclient.model;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
import it.angelic.soulissclient.model.db.SoulissCommandDTO;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
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
 *
 * @author shine@angelic.it
 */
public class SoulissCommand implements Serializable, ISoulissCommand {

    private static final long serialVersionUID = -918392561828980547L;
    private SoulissCommandDTO commandDTO;
    private SoulissTypical parentTypical;
    private SoulissScene targetScene;
    protected Context context;

    public SoulissCommand(SoulissTypical parentTypical) {
        super();
        this.context = parentTypical.getContext();
        this.commandDTO = new SoulissCommandDTO();
        commandDTO.setSlot(parentTypical.getTypicalDTO().getSlot());
        commandDTO.setNodeId(parentTypical.getParentNode().getNodeId());
        this.parentTypical = parentTypical;
        if (parentTypical.getParentNode() != null)
            assertEquals(commandDTO.getNodeId(), parentTypical.getParentNode().getNodeId());
    }

    public SoulissCommand(Context context, SoulissCommandDTO dto, SoulissTypical parentTypical) {
        this(context, dto);
        this.parentTypical = parentTypical;
        if (parentTypical.getParentNode() != null)
            assertEquals(dto.getNodeId(), parentTypical.getParentNode().getNodeId());

    }

    public SoulissCommand(Context c, SoulissCommandDTO dto) {
        super();
        this.commandDTO = dto;
        // falso se trigger assertEquals(true, dto.getSceneId() != 0);
        if (dto.getNodeId() == it.angelic.soulissclient.Constants.COMMAND_FAKE_SCENE) {
            SoulissDBHelper db = new SoulissDBHelper(c);
            targetScene = db.getScene(dto.getSlot());
            commandDTO.setSceneId(null);
        }
    }

    @Override
    public void execute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                if (commandDTO.getNodeId() == it.angelic.soulissclient.Constants.COMMAND_FAKE_SCENE) {
                    //in realta devo eseguire una scena, non questo comando
                    //salvato adalla Addprogram activity
                    targetScene.execute();
                    return;
                } else if (commandDTO.getNodeId() == it.angelic.soulissclient.Constants.MASSIVE_NODE_ID) {
                    String intero = Long.toHexString(commandDTO.getCommand());
                    String[] laCosa = ScenesDialogHelper.splitStringEvery(intero, 2);
                    //codice che funziona ma non so perche`
                    for (int i = 0; i < laCosa.length; i++) {
                        laCosa[i] = "0x" + laCosa[i];
                    }
                    //split the command if longer
                    UDPHelper.issueMassiveCommand(String.valueOf(commandDTO.getSlot()), SoulissApp.getOpzioni(), laCosa);
                } else {// COMANDO SINGOLO
                    String start = Long.toHexString(commandDTO.getCommand());
                    String[] laCosa = ScenesDialogHelper.splitStringEvery(start, 2);
                    // String[] laCosa = start.split("(?<=\\G..)");
                    for (int i = 0; i < laCosa.length; i++) {
                        laCosa[i] = "0x" + laCosa[i];
                    }
                    //codice che funziona ma non so perche`
                    UDPHelper.issueSoulissCommand(String.valueOf(commandDTO.getNodeId()),
                            String.valueOf(commandDTO.getSlot()), SoulissApp.getOpzioni(),
                            // pura magia della decode
                            laCosa);
                }
                //in base al tipo, segno ultima esecuzione
                if (getType() == it.angelic.soulissclient.Constants.COMMAND_TIMED
                        && now.after(commandDTO.getScheduledTime())) {
                    setExecutedTime(now);
                } else if (getType() == it.angelic.soulissclient.Constants.COMMAND_COMEBACK_CODE) {
                    setExecutedTime(now);
                    commandDTO.setSceneId(null);
                } else if (getType() == it.angelic.soulissclient.Constants.COMMAND_GOAWAY_CODE) {
                    setExecutedTime(now);
                    commandDTO.setSceneId(null);
                }
            }
        }).start();
    }

    public long getCommand() {
        return commandDTO.getCommand();
    }

    //protetto per i trigger
    protected SoulissCommandDTO getCommandDTO() {
        return commandDTO;
    }

    public long getCommandId() {
        return commandDTO.getCommandId();
    }

    public Calendar getExecutedTime() {
        Calendar ret = Calendar.getInstance();
        ret.setTime(new Date(commandDTO.getScheduledTime()));
        return ret;
    }

    public int getSceneId() {
        return commandDTO.getSceneId();
    }

    public void setCommandId(long commandId) {
        commandDTO.setCommandId(commandId);
    }


    public void setExecutedTime(Calendar executedTime) {
        if (executedTime == null)
            commandDTO.setExecutedTime(null);
        else
            commandDTO.setExecutedTime(executedTime.getTime().getTime());
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
                resId = R.drawable.button1;
            else
                resId = R.drawable.bell1;
        } else if (typical == Constants.Typicals.Souliss_T12) {
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.drawable.light_on;
            else if (command == Constants.Typicals.Souliss_T1n_OffCmd)
                resId = R.drawable.light_off;
            else
            resId = R.drawable.sos;
        } else if (typical == Constants.Typicals.Souliss_T13) {
            resId = R.drawable.sos;
        } else if (typical == Constants.Typicals.Souliss_T14) {
            if (command == Constants.Typicals.Souliss_T1n_OnCmd)
                resId = R.drawable.lock1;
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
            resId = R.drawable.candle1;
        else if (typical == Constants.Typicals.Souliss_T21)
            resId = R.drawable.sos;
        else if (typical == Constants.Typicals.Souliss_T22)
            resId = R.drawable.sos;
        else if (typical == Constants.Typicals.Souliss_T31) {
            resId = R.drawable.sos;
        } else
            resId = R.drawable.empty;

        return resId;
    }

    @Override
    public
    @DrawableRes
    int getIconResourceId() {
        return getIconResId();
    }

    @Override
    public void setIconResourceId(@DrawableRes int resId) {
        //should not be called
        Log.w(Constants.TAG, "set() invalid, command has no icon");
    }

    public int getInterval() {
        return commandDTO.getInterval();
    }

    @Override
    public String getName() {
        short typical;
        if (targetScene != null) {
            return context.getString(R.string.execute)
                    + " " + context.getString(R.string.scene)
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

        return context.getString(resId);
    }

    public void setInterval(int interval) {
        commandDTO.setInterval(interval);
    }

    @Override
    public void setName(String newName) {
        throw new Error("Commands don't support custom names");
    }

    @Override
    public String getNiceName() {
        StringBuilder info = new StringBuilder();
        Context ctx = context;
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

    public short getNodeId() {
        return commandDTO.getNodeId();
    }

    public void setNodeId(short id) {
        commandDTO.setNodeId(id);
    }

    /**
     * Quando targetScene non e` nullo, lo e` parentTypical, e nodeId nel DTO vale -2
     * In pratica targetScene e typical sono mutuali, in base a cosa controlla il comando
     *
     * @return
     */
    public SoulissTypical getParentTypical() {
        return parentTypical;
    }

    public void setParentTypical(SoulissTypical parentTypical) {
        this.parentTypical = parentTypical;
    }

    public Calendar getScheduledTime() {
        Calendar ret = Calendar.getInstance();
        ret.setTime(new Date(commandDTO.getScheduledTime()));
        return ret;
    }

    public void setScheduledTime(Calendar baseNow) {
        if (baseNow == null)
            commandDTO.setScheduledTime(null);
        else
            commandDTO.setScheduledTime(baseNow.getTime().getTime());
    }

    public short getSlot() {
        return commandDTO.getSlot();
    }

    public void setSlot(short typical) {
        commandDTO.setSlot(typical);
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

    public int getType() {
        return commandDTO.getType();
    }

    public void setType(int commandSingle) {
        commandDTO.setType(commandSingle);
    }

    public void setCommand(long souliss_t4n_armed) {
        commandDTO.setCommand(souliss_t4n_armed);
    }

    public void persistCommand() {
        commandDTO.persistCommand();
    }

    public void setSceneId(Integer id) {
        commandDTO.setSceneId(id);
    }

    public void setStep(int size) {
        if (!(parentTypical == null)) {
            Log.e(Constants.TAG, "ERRORE strutturale, setStep con parent non nullo");
        }
        commandDTO.setScheduledTime((long) size);
    }

    @Override
    public String toString() {
        return getName();
    }

    /*
    holder.textCmd.setText(context.getResources().getString(R.string.scene_send_command) + " \""
					+ holder.data.toString() + "\" " + context.getResources()
					+ context.getResources().getString(R.string.compatible) + " ("
					+ holder.data.getParentTypical().getNiceName() + ")");
		*/

}
