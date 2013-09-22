package it.angelic.soulissclient.model;

import static junit.framework.Assert.assertEquals;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.model.typicals.Constants;

import java.io.Serializable;

import android.content.Context;

public class SoulissCommand implements Serializable {

	private static final long serialVersionUID = -918392561828980547L;
	private SoulissCommandDTO commandDTO;
	private transient Context ctx;
	private SoulissTypical parentTypical;

	public SoulissCommandDTO getCommandDTO() {
		return commandDTO;
	}

	public SoulissCommand(Context ct, SoulissTypical parentTypical) {
		super();
		ctx = ct;
		this.commandDTO = new SoulissCommandDTO();
		commandDTO.setSlot(parentTypical.getTypicalDTO().getSlot());
		commandDTO.setNodeId(parentTypical.getParentNode().getId());
		this.parentTypical = parentTypical;
	}

	public SoulissCommand(Context ct, SoulissCommandDTO dto, SoulissTypical parentTypical) {
		super();
		ctx = ct;
		this.commandDTO = dto;
		this.parentTypical = parentTypical;
		if (parentTypical.getParentNode() != null)
			assertEquals(dto.getNodeId(), parentTypical.getParentNode().getId());
	}

	public SoulissCommand(Context ct, SoulissCommandDTO dto) {
		super();
		ctx = ct;
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
		if (typical == Constants.Souliss_T11) {
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
		} else if (typical == Constants.Souliss_T21)
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
		if (commandDTO.getType() == it.angelic.soulissclient.Constants.COMMAND_MASSIVE) {
			// comando massivo
			typical = commandDTO.getSlot();
		} else
			typical = parentTypical.getTypicalDTO().getTypical();
		long command = commandDTO.getCommand();
		int resId;
		if (typical == Constants.Souliss_T11) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.TurnON;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.string.TurnOFF;
			else if (command == Constants.Souliss_T1n_RstCmd)
				resId = R.string.Souliss_ResetCmd_desc;
			else if (command == Constants.Souliss_T1n_ToogleCmd)
				resId = R.string.Souliss_ToggleCmd_desc;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T12) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.TurnON;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.string.TurnOFF;
			else if (command == Constants.Souliss_T1n_RstCmd)
				resId = R.string.Souliss_ResetCmd_desc;
			else if (command == Constants.Souliss_T1n_AutoCmd)
				resId = R.string.Souliss_AutoCmd_desc;
			else if (command == Constants.Souliss_T1n_ToogleCmd)
				resId = R.string.Souliss_ToggleCmd_desc;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T13) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.TurnON;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.string.TurnOFF;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T14) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.Souliss_OpenCmd_desc;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		} else if (typical == Constants.Souliss_T16) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.TurnON;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.string.TurnOFF;
			else if (command == Constants.Souliss_T16_Red)
				resId = R.string.red;
			else if (command == Constants.Souliss_T16_Green)
				resId = R.string.green;
			else if (command == Constants.Souliss_T16_Blue)
				resId = R.string.blue;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		}else if (typical == Constants.Souliss_T19) {
			if (command == Constants.Souliss_T1n_OnCmd)
				resId = R.string.TurnON;
			else if (command == Constants.Souliss_T1n_OffCmd)
				resId = R.string.TurnOFF;
			else
				resId = R.string.Souliss_UndefinedCmd_desc;
		}  else if (typical == Constants.Souliss_T21)
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

		return this.ctx.getString(resId);
	}

	public SoulissTypical getParentTypical() {
		return parentTypical;
	}

	public Context getCtx() {
		return ctx;
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
	}

}
