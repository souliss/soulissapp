package it.angelic.soulissclient.model;

import static junit.framework.Assert.assertEquals;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.ScenesDialogHelper;
import it.angelic.soulissclient.model.typicals.Constants;
import it.angelic.soulissclient.net.UDPHelper;

import java.io.Serializable;
import java.util.Calendar;

import android.content.Context;
import android.util.Log;

/**
 * Il comando e` nato per riflettere qualcosa da inviare
 * poi e` stato esteso ai massivi
 * poi agli scenari. Un programma puo` infatti voler
 * eseguire uno scenario, e persisterlo
 */
public class SoulissCommand implements Serializable , ISoulissExecutable{

	private static final long serialVersionUID = -918392561828980547L;
	private SoulissCommandDTO commandDTO;
	private SoulissTypical parentTypical;

	public SoulissCommandDTO getCommandDTO() {
		return commandDTO;
	}

	public SoulissCommand(SoulissTypical parentTypical) {
		super();
		this.commandDTO = new SoulissCommandDTO();
		commandDTO.setSlot(parentTypical.getTypicalDTO().getSlot());
		commandDTO.setNodeId(parentTypical.getParentNode().getId());
		this.parentTypical = parentTypical;
	}

	public SoulissCommand(SoulissCommandDTO dto, SoulissTypical parentTypical) {
		super();
		this.commandDTO = dto;
		this.parentTypical = parentTypical;
		if (parentTypical.getParentNode() != null)
			assertEquals(dto.getNodeId(), parentTypical.getParentNode().getId());
	}

	public SoulissCommand(SoulissCommandDTO dto) {
		super();
		this.commandDTO = dto;
		// falso se trigger assertEquals(true, dto.getSceneId() != 0);
	}

	public int getType() {
		return commandDTO.getType();
	}

	// FIXME ritorna alla cazzo, rivedere le icone dei comandi
	public int getIconResId() {
		if (commandDTO.getType() == it.angelic.soulissclient.Constants.COMMAND_MASSIVE) {
			// comando massivo
			return R.drawable.arrowmove;
		}
		short typical = parentTypical.getTypicalDTO().getTypical();
		long command = commandDTO.getCommand();
		int resId;
		 if (typical == Constants.Souliss_T11 || typical == Constants.Souliss_T18) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.drawable.light_on;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.drawable.light_off;
			else if (command == Constants.Souliss_T1n_RstCmd)
				resId = R.drawable.sos;
			else if (command == Constants.Souliss_T1n_ToogleCmd)
				resId = R.drawable.button;
			else
				resId = R.drawable.bell;
		} else if (typical == Constants.Souliss_T12) {
			resId = R.drawable.sos;
		} else if (typical == Constants.Souliss_T13) {
			resId = R.drawable.sos;
		} else if (typical == Constants.Souliss_T14) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.drawable.light_on;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.drawable.light_off;
			else
				resId = R.drawable.sos;
		} else if (typical == Constants.Souliss_T16) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.drawable.light_on;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.drawable.light_off;
			else
				resId = R.drawable.rgb;
		} else if (typical == Constants.Souliss_T19)
			resId = R.drawable.candle;
		else if (typical == Constants.Souliss_T21)
			resId = R.drawable.sos;
		else if (typical == Constants.Souliss_T22)
			resId = R.drawable.sos;
		else if (typical == Constants.Souliss_T31)
			resId = R.drawable.sos;
		else if (typical == Constants.Souliss_T_CurrentSensor)
			resId = R.drawable.sos;
		else if (typical == Constants.Souliss_T_TemperatureSensor)
			resId = R.drawable.sos;
		else if (typical == Constants.Souliss_T_HumiditySensor)
			resId = R.drawable.sos;
		else
			resId = R.drawable.empty;

		return resId;
	}

	@Override
	public String toString() {
		short typical;
        if (commandDTO.getNodeId() == it.angelic.soulissclient.Constants.COMMAND_FAKE_SCENE){
            return "Scene execution";
        }
		if (commandDTO.getType() == it.angelic.soulissclient.Constants.COMMAND_MASSIVE) {
			// comando massivo
			typical = commandDTO.getSlot();
		} else
			typical = parentTypical.getTypicalDTO().getTypical();
		long command = commandDTO.getCommand();
		int resId;
		if (typical == Constants.Souliss_T11 || typical == Constants.Souliss_T12 || typical == Constants.Souliss_T13 ||
		typical == Constants.Souliss_T18|| typical == Constants.Souliss_T19) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.TurnON;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.string.TurnOFF;
			else if (command == Constants.Souliss_T1n_RstCmd)
				resId = R.string.Souliss_ResetCmd_desc;
			else if (command == Constants.Souliss_T1n_ToogleCmd)
				resId = R.string.Souliss_ToggleCmd_desc;
			else if (command == Constants.Souliss_T1n_AutoCmd)
				resId = R.string.Souliss_AutoCmd_desc;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T14) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.Souliss_OpenCmd_desc;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T16) {//RGB
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.TurnON;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.string.TurnOFF;
			else if (command == Constants.Souliss_T1n_ToogleCmd)
				resId = R.string.toggle;
			else if (command == Constants.Souliss_T16_Red)
				resId = R.string.red;
			else if (command == Constants.Souliss_T16_Green)
				resId = R.string.green;
			else if (command == Constants.Souliss_T16_Blue)
				resId = R.string.blue;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T21)
			if (command == Constants.Souliss_T2n_CloseCmd)
				resId = R.string.Souliss_CloseCmd_desc;
			else if (command == Constants.Souliss_T2n_OpenCmd)
				resId = R.string.Souliss_OpenCmd_desc;
			else if (command == Constants.Souliss_T2n_StopCmd)
				resId = R.string.Souliss_StopCmd_desc;
			else if (command == Constants.Souliss_T2n_ToogleCmd)
				resId = R.string.Souliss_ToggleCmd_desc;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		else if (typical == Constants.Souliss_T22) {
			if (command == Constants.Souliss_T2n_CloseCmd)
				resId = R.string.Souliss_CloseCmd_desc;
			else if (command == Constants.Souliss_T2n_OpenCmd)
				resId = R.string.Souliss_OpenCmd_desc;
			else if (command == Constants.Souliss_T2n_StopCmd)
				resId = R.string.Souliss_StopCmd_desc;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T31)
			resId = R.string.Souliss_T31_desc;
		else if (typical == Constants.Souliss_T_CurrentSensor)
			resId = R.string.Souliss_TCurrentSensor_desc;
		else if (typical == Constants.Souliss_T_TemperatureSensor)
			resId = R.string.Souliss_TTemperature_desc;
		else if (typical == Constants.Souliss_T_HumiditySensor)
			resId = R.string.Souliss_THumidity_desc;
		else if (typical == it.angelic.soulissclient.model.typicals.Constants.Souliss_T32_IrCom_AirCon) {
			if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Auto_20)
				resId = R.string.Souliss_T_IrCom_AirCon_Pow_Auto_20_desc;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Auto_24)
				resId = R.string.Souliss_T_IrCom_AirCon_Pow_Auto_24_desc;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Cool_18)
				resId = R.string.Souliss_T_IrCom_AirCon_Pow_Cool_18_desc;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Cool_22)
				resId = R.string.Souliss_T_IrCom_AirCon_Pow_Cool_22_desc;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Cool_26)
				resId = R.string.Souliss_T_IrCom_AirCon_Pow_Cool_26_desc;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Dry)
				resId = R.string.Souliss_T_IrCom_AirCon_Pow_Dry_desc;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Fan)
				resId = R.string.Souliss_T_IrCom_AirCon_Pow_Fan_desc;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_Pow_Off)
				resId = R.string.TurnOFF;
			else
				resId = R.string.Souliss_emptycmd_desc;
		} else if (typical == it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_RGB) {
			if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_RGB_OnCmd)
				resId = R.string.TurnON;
			else if (command == it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_RGB_OffCmd)
				resId = R.string.TurnOFF;
			else
				resId = R.string.Souliss_emptycmd_desc;
		} else
			resId = R.string.Souliss_emptycmd_desc;

		return SoulissClient.getAppContext().getString(resId);
	}

	public SoulissTypical getParentTypical() {
		return parentTypical;
	}


    @Override
    public void execute() {
        SoulissCommandDTO dto = getCommandDTO();
        Context ctx = SoulissClient.getAppContext();
        Calendar now = Calendar.getInstance();
        if (dto.getNodeId() == it.angelic.soulissclient.Constants.COMMAND_FAKE_SCENE){
            int sceneId = dto.getSlot();
            SoulissDBHelper db = new SoulissDBHelper(ctx);
            SoulissScene casino = db.getScenes(ctx,sceneId);
            casino.execute();
            return;
        }else if (dto.getNodeId()  == it.angelic.soulissclient.Constants.COMMAND_MASSIVE) {
            String intero = Long.toHexString(dto.getCommand());
            String[] laCosa =  ScenesDialogHelper.splitStringEvery(intero, 2);
            for (int i = 0; i < laCosa.length; i++) {
                laCosa[i] = "0x" + laCosa[i];
            }
            //split the command if longer
            UDPHelper.issueMassiveCommand(String.valueOf(dto.getSlot()), SoulissClient.getOpzioni(), laCosa);

            //UDPHelper.issueMassiveCommand(""+Constants.Souliss_T16, prefs,""+val, "" + r, ""+ g, "" + b);
        }else if (getType() == it.angelic.soulissclient.Constants.COMMAND_TIMED
                && now.after(getCommandDTO().getScheduledTime())) {
            // esegui comando
            Log.w(Constants.TAG, "issuing command: " + toString());
            UDPHelper.issueSoulissCommand(this, SoulissClient.getOpzioni());
            getCommandDTO().setExecutedTime(now);

        }else if (getType() == it.angelic.soulissclient.Constants.COMMAND_COMEBACK_CODE) {
            Log.w(Constants.TAG, "issuing COMEBACK command: " + toString());
            UDPHelper.issueSoulissCommand(this, SoulissClient.getOpzioni());
            getCommandDTO().setExecutedTime(now);
            getCommandDTO().setSceneId(null);
        }else if (getType() == it.angelic.soulissclient.Constants.COMMAND_GOAWAY_CODE) {
            Log.w(Constants.TAG, "issuing GOAWAY command: " + toString());
            UDPHelper.issueSoulissCommand(this, SoulissClient.getOpzioni());
            getCommandDTO().setExecutedTime(now);
            getCommandDTO().setSceneId(null);
        } else {// COMANDO SINGOLO
            String start = Long.toHexString(dto.getCommand());
            String[] laCosa = ScenesDialogHelper.splitStringEvery(start, 2);
            // String[] laCosa = start.split("(?<=\\G..)");
            for (int i = 0; i < laCosa.length; i++) {
                laCosa[i] = "0x" + laCosa[i];
            }

            UDPHelper.issueSoulissCommand(String.valueOf(dto.getNodeId()),
                    String.valueOf(dto.getSlot()), SoulissClient.getOpzioni(), dto.getType(),
                    // pura magia della decode
                    laCosa);
        }
    }

    public short getNodeId() {
        return  commandDTO.getNodeId();
    }


    public short getSlot() {
        return commandDTO.getSlot();
    }

    public long getCommand() {
        return commandDTO.getCommand();
    }
}
