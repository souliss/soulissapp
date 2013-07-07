package it.angelic.soulissclient.typicals;

import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.db.SoulissTypicalDTO;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SoulissTypical implements Serializable, ISoulissObject, ISoulissTypical {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7375342157142543740L;
	// nodo di appartenenza
	private SoulissNode parentNode;
	// contenitore dati specchio del DB
	SoulissTypicalDTO typicalDTO;

	private boolean isSlave = false;// indica se includerlo nelle liste
	private boolean isSensor = false;// indica se va loggato

	transient Context ctx;
	transient SoulissPreferenceHelper prefs;

	@Override
	public String getNiceName() {
		if (typicalDTO.getName() != null)
			return typicalDTO.getName();

		return getDefaultName();

	}

	@Override
	public void setIconResourceId(int resId) {
		typicalDTO.setIconId(resId);

	}

	@Override
	public void setName(String newName) {
		typicalDTO.setName(newName);
	}

	public void setRelated(SoulissTypical in) {
		Log.e(Constants.TAG, "Called setRealted on a single typical");
	}

	public SoulissTypical(SoulissPreferenceHelper pre) {
		super();
		prefs = pre;
		setTypicalDTO(new SoulissTypicalDTO());
	}

	/**
	 * get the concrete typical
	 * 
	 * @param typ
	 * @param parent
	 * @return
	 */
	public static SoulissTypical typicalFactory(short typ, SoulissNode parent, SoulissTypicalDTO dto,
			SoulissPreferenceHelper opts) {

		SoulissTypical rest = null;
		assertTrue(opts != null);
		switch (typ) {
		case Constants.Souliss_T11:
			rest = new SoulissTypical11(opts);
			break;
		case Constants.Souliss_T12:
			rest = new SoulissTypical12(opts);
			break;
		case Constants.Souliss_T13:
			rest = new SoulissTypical13(opts);
			break;
		case Constants.Souliss_T14:
			rest = new SoulissTypical14(opts);
			break;
		case Constants.Souliss_T1n_RGB:
			rest = new SoulissTypical15(opts);
			break;
		case Constants.Souliss_T16:
			rest = new SoulissTypical16AdvancedRGB(opts);
			break;
		case Constants.Souliss_T21:
			rest = new SoulissTypical21(opts);
			break;
		case Constants.Souliss_T22:
			rest = new SoulissTypical22(opts);
			break;
		case Constants.Souliss_T_CurrentSensor:
			rest = new SoulissTypicalCurrentSensor(opts);
			rest.setSensor(true);
			break;
		case 66:
			rest = new SoulissTypical(opts);
			rest.setRelated(true);
			break;
		case Constants.Souliss_T_TemperatureSensor:
			rest = new SoulissTypicalTemperatureSensor(opts);
			rest.setSensor(true);
			break;
		case Constants.Souliss_T_HumiditySensor:
			rest = new SoulissTypicalHumiditySensor(opts);
			rest.setSensor(true);
			break;
		case Constants.Souliss_T32_IrCom_AirCon:
			rest = new SoulissTypical32AirCon(opts);
			break;
		case Constants.Souliss_T_related:
			rest = new SoulissTypical(opts);
			rest.setRelated(true);
			// if (dto != null)
			// rest.setRelated(parent.getTypical((short)(dto.getSlot()-1)));
			break;
		case Constants.Souliss_T51:
			rest = new SoulissTypical51AnalogueSensor(opts);
			rest.setSensor(true);
			break;
		case Constants.Souliss_T54_LuxSensor:
			rest = new SoulissTypical54LuxSensor(opts);
			rest.setSensor(true);
			break;
		case Constants.Souliss_T52:
			rest = new SoulissTypical5n(opts);
			((SoulissTypical5n) rest).setTransientVal(dto.getOutput());
			break;
		case Constants.Souliss_T53:
			rest = new SoulissTypical5n(opts);
			((SoulissTypical5n) rest).setTransientVal(dto.getOutput());
			break;
		default:
			rest = new SoulissTypical(opts);
			break;
		}
		/*
		 * if (dto == null) { rest.setTypicalDTO(new SoulissTypicalDTO()); }
		 * else
		 */
		rest.setTypicalDTO(dto);
		rest.setParentNode(parent);

		rest.setPrefs(opts);
		// rest.getTypicalDTO().setNodeId(parent.getId());
		return rest;
	}

	public String getDefaultName() {
		short typical = typicalDTO.getTypical();
		assertTrue(typical != -1);

		int id;
		if (typical == Constants.Souliss_T11)
			id = R.string.Souliss_T11_desc;
		else if (typical == Constants.Souliss_T12)
			id = R.string.Souliss_T12_desc;
		else if (typical == Constants.Souliss_T13)
			id = R.string.Souliss_T13_desc;
		else if (typical == Constants.Souliss_T14)
			id = R.string.Souliss_T14_desc;
		else if (typical == Constants.Souliss_T16)
			id = R.string.Souliss_T16_desc;
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
		else if (typical == Constants.Souliss_T51)
			id = R.string.Souliss_T51_desc;
		else if (typical == Constants.Souliss_T52)
			id = R.string.Souliss_T52_desc;
		else if (typical == Constants.Souliss_T53)
			id = R.string.Souliss_T53_desc;
		else if (typical == Constants.Souliss_T54_LuxSensor)
			id = R.string.Souliss_T54_desc;
		else
			id = R.string.unknown_typical;

		Context cc = SoulissClient.getAppContext();
		return cc.getResources().getString(id);
	}

	@Override
	public int getDefaultIconResourceId() {
		short typical = typicalDTO.getTypical();
		assertTrue(typical != -1);

		if (typicalDTO.getIconId() != 0)
			return typicalDTO.getIconId();
		if (typical == Constants.Souliss_T11)
			return R.drawable.light_on;
		else if (typical == Constants.Souliss_T12)
			return R.drawable.button;
		else if (typical == Constants.Souliss_T13)
			return R.drawable.shock;
		else if (typical == Constants.Souliss_T14)
			return R.drawable.locked;
		else if (typical == Constants.Souliss_T16)
			return R.drawable.rgb;
		else if (typical == Constants.Souliss_T21)
			return R.drawable.limit;
		else if (typical == Constants.Souliss_T22)
			return R.drawable.limit;
		else if (typical == Constants.Souliss_T31)
			return R.drawable.thermometer;
		else if (typical == Constants.Souliss_T_CurrentSensor)
			return R.drawable.lightning;
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
		else if (typical == Constants.Souliss_T52)
			return R.drawable.analog;
		else if (typical == Constants.Souliss_T53)
			return R.drawable.analog;
		else if (typical == Constants.Souliss_T54_LuxSensor)
			return R.drawable.home;
		else
			return R.drawable.empty_narrow;
	}

	public SoulissNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(SoulissNode parentNode) {
		this.parentNode = parentNode;
	}

	public boolean isEmpty() {
		if (typicalDTO.getTypical() == Constants.Souliss_T_empty)
			return true;
		else
			return false;
	}

	public boolean isRelated() {
		return isSlave;
	}

	public void setRelated(boolean isSlave) {
		this.isSlave = isSlave;
	}

	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// to be overridden

		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();
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

	public Context getCtx() {
		return ctx;
	}

	public void setCtx(Context ctx) {
		this.ctx = ctx;
		// setPrefs(new SoulissPreferenceHelper(ctx));
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
	public void getActionsLayout(TypicalsListAdapter ble, Context ctx, Intent parentIntent, View convertView,
			ViewGroup parent) {
		//Log.e(Constants.TAG,"Should be implemented elsewhere or not called at all");

	}

	protected TextView getQuickActionTitle() {
		// Infotext nascosto all'inizio
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		final TextView cmd = new TextView(ctx);
		cmd.setText(ctx.getString(R.string.actions));
		// cmd.setTextSize(ctx.getResources().getDimension(R.dimen.text_size));
		if (prefs != null && prefs.isLightThemeSelected())
			cmd.setTextColor(ctx.getResources().getColor(R.color.black));

		cmd.setLayoutParams(lp);
		return cmd;
	}

	public String getOutputDesc() {
		return "TOIMPLEMENT";
	}
	
	public void setOutputDescView(TextView textStatusVal) {
		textStatusVal.setText(getOutputDesc());
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil || "UNKNOWN".compareTo(getOutputDesc()) == 0 || "NA".compareTo(getOutputDesc()) == 0) {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_red));
			textStatusVal.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.borderedbackoff));
		} else {
			textStatusVal.setTextColor(ctx.getResources().getColor(R.color.std_green));
			textStatusVal.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.borderedbackon));
		}
	}
}
