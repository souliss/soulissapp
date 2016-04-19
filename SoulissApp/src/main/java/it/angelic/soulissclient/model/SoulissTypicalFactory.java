package it.angelic.soulissclient.model;

import android.util.Log;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.db.SoulissTypicalDTO;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.model.typicals.SoulissTypical13DigitalInput;
import it.angelic.soulissclient.model.typicals.SoulissTypical14PulseOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical15;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.soulissclient.model.typicals.SoulissTypical1ALightsArray;
import it.angelic.soulissclient.model.typicals.SoulissTypical21;
import it.angelic.soulissclient.model.typicals.SoulissTypical22;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.model.typicals.SoulissTypical32AirCon;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical51AnalogueSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical52TemperatureSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical53HumiditySensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical54LuxSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical58PressureSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical5nCurrentVoltagePowerSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical6nAnalogue;

import static junit.framework.Assert.assertTrue;

public class SoulissTypicalFactory {
	/**
	 * get the concrete typical and instantiate it
	 * 
	 * @param typ
	 * @param parent
	 * @return
	 */
	public static SoulissTypical getTypical(short typ, SoulissNode parent, SoulissTypicalDTO dto,
			SoulissPreferenceHelper opts) {

		SoulissTypical rest = null;
		assertTrue(opts != null);
		switch (typ) {
		case Constants.Typicals.Souliss_T11:
			rest = new SoulissTypical11DigitalOutput(opts);
			break;
		case Constants.Typicals.Souliss_T12:
			rest = new SoulissTypical12DigitalOutputAuto(opts);
			break;
		case Constants.Typicals.Souliss_T13:
			rest = new SoulissTypical13DigitalInput(opts);
			break;
		case Constants.Typicals.Souliss_T14:
			rest = new SoulissTypical14PulseOutput(opts);
			break;
		case Constants.Typicals.Souliss_T15_RGB:
			rest = new SoulissTypical15(opts);
			break;
		case Constants.Typicals.Souliss_T16:
			rest = new SoulissTypical16AdvancedRGB(opts);
			break;
		case Constants.Typicals.Souliss_T18:
			rest = new SoulissTypical11DigitalOutput(opts);
			break;
		case Constants.Typicals.Souliss_T19:
			rest = new SoulissTypical19AnalogChannel(opts);
			break;
		case Constants.Typicals.Souliss_T1A:
			rest = new SoulissTypical1ALightsArray(opts);
			break;
			
		case Constants.Typicals.Souliss_T21:
			rest = new SoulissTypical21(opts);
			break;
		case Constants.Typicals.Souliss_T22:
			rest = new SoulissTypical22(opts);
			break;	
		case Constants.Typicals.Souliss_T31:
			rest = new SoulissTypical31Heating(opts);
			break;	
		case Constants.Typicals.Souliss_T32_IrCom_AirCon:
			rest = new SoulissTypical32AirCon(opts);
			break;
		case Constants.Typicals.Souliss_T41_Antitheft_Main:
			//set the isAntitheftConfigure option TRUE
			rest = new SoulissTypical41AntiTheft(opts);
			break;
		case Constants.Typicals.Souliss_T42_Antitheft_Peer:
			rest = new SoulissTypical42AntiTheftPeer(opts);
			break;
		case Constants.Typicals.Souliss_T43_Antitheft_LocalPeer:
			rest = new SoulissTypical43AntiTheftLocalPeer(opts);
			break;
		case Constants.Typicals.Souliss_T_related:
			rest = new SoulissTypical(opts);
			rest.setRelated(true);
			break;
		case Constants.Typicals.Souliss_T51:
			rest = new SoulissTypical51AnalogueSensor(opts);
			//rest.setSensor(true);
			break;
		case Constants.Typicals.Souliss_T52_TemperatureSensor:
			rest = new SoulissTypical52TemperatureSensor(opts);
			//rest.setSensor(true);
			break;
		case Constants.Typicals.Souliss_T53_HumiditySensor:
			rest = new SoulissTypical53HumiditySensor(opts);
			//rest.setSensor(true);
			break;
		case Constants.Typicals.Souliss_T54_LuxSensor:
			rest = new SoulissTypical54LuxSensor(opts);
			//rest.setSensor(true);
			break;
		case Constants.Typicals.Souliss_T55_VoltageSensor:
			rest = new SoulissTypical5nCurrentVoltagePowerSensor(opts,typ);
			//rest.setSensor(true);
			break;
		case Constants.Typicals.Souliss_T56_CurrentSensor:
			rest = new SoulissTypical5nCurrentVoltagePowerSensor(opts,typ);
			//rest.setSensor(true);
			break;			
		case Constants.Typicals.Souliss_T57_PowerSensor:
			rest = new SoulissTypical5nCurrentVoltagePowerSensor(opts,typ);
			//rest.setSensor(true);
			break;
		case Constants.Typicals.Souliss_T58_PressureSensor:
			rest = new SoulissTypical58PressureSensor(opts);
			//rest.setSensor(true);
			break;
			case Constants.Typicals.Souliss_T61:
				rest = new SoulissTypical6nAnalogue(opts);
				//rest.setSensor(true);
				break;
		default:
			Log.w(Constants.Typicals.TAG, "warning, unknown typical");
			rest = new SoulissTypical(opts);
			break;
		}
		rest.setTypicalDTO(dto);
		rest.setParentNode(parent);

		rest.setPrefs(opts);
		return rest;
	}
}
