package it.angelic.soulissclient;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
	public static final String TAG = "SoulissApp";

    public static final int WATCH_ONLY_ID = 2;
    public static final int PHONE_ONLY_ID = 3;
    public static final int BOTH_ID = 4;

    public static final String BOTH_PATH = "/both";
    public static final String WATCH_ONLY_PATH = "/watch-only";
    public static final String KEY_NOTIFICATION_ID = "notification-id";

    public static final String ACTION_DISMISS
            = "com.example.android.wearable.synchronizednotifications.DISMISS";
    public static final String CUSTOM_INTENT_SOULISS_RAWDATA = "it.angelic.soulissclient.RAW_MACACO_DATA";
    public static final String CUSTOM_INTENT_SOULISS_TIMEOUT = "it.angelic.soulissclient.RAW_TIMEOUT";
    static final String ACTION_SEND_SOULISS_COMMAND = "it.angelic.soulissclient.WEAR_VOICE_COMMAND";
    public static final String ACTION_OPEN_SOULISS = "it.angelic.soulissclient.OPEN_SOULISSAPP";
    public static final String ACTION_SEND_COMMAND = "it.angelic.soulissclient.SEND_COMMAND";


	public static final int SEC_IN_A_MIN = 60;
	public static final int MSEC_IN_A_SEC = 1000;
	public static final int MIN_IN_A_HOUR = 60;


    public static final int MAX_USER_IDX = 0x64;
	public static final int MAX_NODE_IDX = 0xFE;
	public static final int MAX_HEALTH = 255;
	public static final int ICON_REQUEST = 1;
	public static final float[] roundedCorners = new float[] { 5,5,5,5,5,5,5,5 };
    public static final int sdkVersionNumber = Integer.valueOf(android.os.Build.VERSION.SDK_INT);

	public static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
	public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public static final int TAG_INSERT_POINT = 1;
    static final int NOTIFICATION_ID = 3113;


    public static DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");
	public static DecimalFormat gpsDecimalFormat = new DecimalFormat("#.######");
	//public static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyyMMdd");
	public static final long POSITION_UPDATE_INTERVAL = 10 * 1000;//5 seconds
	public static final long POSITION_UPDATE_MIN_DIST = 25;
	public static final int GUI_UPDATE_INTERVAL = 5000;
	public static final int VOICE_REQUEST_OK = 9876;
	//public static final int   CHECK_STATUS_PAUSE_MSEC = 250;
	
	//public static final long TEXT_SIZE_TITLE_OFFSET = 10;

	public static final int COMMAND_TIMED = 0;
	// Interpretati come Calendar
	public static final int COMMAND_COMEBACK_CODE = 1;
	public static final int COMMAND_GOAWAY_CODE = 2;
	public static final int COMMAND_TRIGGERED = 3;
	public static final int COMMAND_SINGLE = 4;
	//public static final int COMMAND_MASSIVE = 5;
    public static final short COMMAND_FAKE_SCENE = -2;
	public static final short MASSIVE_NODE_ID = -1;
	public static final int CONNECTION_NONE = -1;
	//Souliss Data Intent
	public static final String CUSTOM_INTENT = "it.angelic.soulissclient.GOT_DATA";

    private static final String[] ROM = {"X\u0305", "V\u0305", "M", "D", "C", "L", "X", "V", "I"};
	private static final int MAX = 10000; // value of R[0], must be a power of 10

	private static final int[][] DIGITS = {
	    {},{0},{0,0},{0,0,0},{0,1},{1},
	    {1,0},{1,0,0},{1,0,0,0},{0,2}};
	public static final int POSITION_DEADZONE_METERS = 27;


	/**
	 * Utility per numeri Romani
	 * @param number
	 * @return
	 */
	public static String int2roman(int number) {
	    if (number < 0 || number >= MAX*4) throw new IllegalArgumentException(
	            "int2roman: " + number + " is not between 0 and " + (MAX*4-1));
	    if (number == 0) return "O";
	    StringBuilder sb = new StringBuilder();
	    int i = 0, m = MAX;
	    while (number > 0) {
	        int[] d = DIGITS[number / m];
	        for (int n: d) sb.append(ROM[i-n]);
	        number %= m;
	        m /= 10;
	        i += 2;
	    }
	    return sb.toString();
	}
	


	public static class Typicals {
        public static final String TAG = "SoulissApp:Typicals";

        /**
         *
         * @author Ale
         *
         */
        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            Field[] consts = getClass().getDeclaredFields();
            for (Field aConst : consts) {
                if ((aConst.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0) {
                    try {
                        ret.append(aConst.getName());
                        ret.append(" = ");
                        ret.append(aConst.get(null)).append("<br/>");
                    } catch (Exception e) {
                        Log.e(it.angelic.soulissclient.Constants.TAG, "Can't build parameter's list" + e);
                    }
                }
            }
            return ret.toString();
        }

        /**
         * /** // Defines for Typicals C LIBRARY
         *
         * #define Souliss_T31 31 // Temperature control #define Souliss_T41 41 //
         * Anti-theft integration (Main) #define Souliss_T42 42 // Anti-theft
         * integration (Peer)
         */
        public static final short Souliss_T_empty = 0;
        public static final short Souliss_T_related = 0xFF;
        // Defines for Typicals
        public static final short Souliss_T11 = 0x11;
        public static final short Souliss_T12 = 0x12;
        public static final short Souliss_T13 = 0x13;
        public static final short Souliss_T14 = 0x14;
        public static final short Souliss_T15_RGB = 0x15;// RGB Light
        public static final short Souliss_T16 = 0x16;
        public static final short Souliss_T18 = 0x18;
        public static final short Souliss_T19 = 0x19;
        public static final short Souliss_T1A = 0x1A;

        public static final short Souliss_T21 = 0x21;// Motorized devices with limit
                                                        // switches
        public static final short Souliss_T22 = 0x22;// Motorized devices with limit
                                                        // switches and middle
                                                        // position
        public static final short Souliss_T31 = 0x31;//HEATING
        public static final short Souliss_T32_IrCom_AirCon = 0x32;

        public static final short Souliss_T42_Antitheft_Group = 0x40; // Anti-theft
                                                                        // group
                                                                        // (used w/
                                                                        // massive
                                                                        // commands)
        public static final short Souliss_T41_Antitheft_Main = 0x41; // Anti-theft
                                                                        // integration
                                                                        // (Main)
        public static final short Souliss_T42_Antitheft_Peer = 0x42; // Anti-theft
                                                                        // integration
                                                                        // (Peer)
        public static final short Souliss_T43_Antitheft_LocalPeer = 0x43;

        /*
         * Souliss_Logic_T52 - Temperature measure (-20, +50) °C Souliss_Logic_T53 -
         * Humidity measure (0, 100) % Souliss_Logic_T54 - Light Sensor (0, 40) kLux
         * Souliss_Logic_T55 - Voltage (0, 400) V Souliss_Logic_T56 - Current (0,
         * 25) A Souliss_Logic_T57 - Power (0, 6500) W Souliss_Logic_T58 - Pressure
         * measure (0, 1500) hPa
         */
        public static final short Souliss_T51 = 0x51;
        public static final short Souliss_T52_TemperatureSensor = 0x52;
        public static final short Souliss_T53_HumiditySensor = 0x53;
        public static final short Souliss_T54_LuxSensor = 0x54;
        public static final short Souliss_T55_VoltageSensor = 0x55;
        public static final short Souliss_T56_CurrentSensor = 0x56;
        public static final short Souliss_T57_PowerSensor = 0x57;
        public static final short Souliss_T58_PressureSensor = 0x58;

        // #define Souliss_T3n_AirCon_OnCmd 0xF0
        // #define Souliss_T3n_AirCon_OffCmd 0xFC
        // #define Souliss_T3n_AirCon_RstCmd 0x00

        // #define Souliss_T3n_AirCon_Normal 0x71
        // #define Souliss_T3n_AirCon_Eco 0x01
        // #define Souliss_T3n_AirCon_Turbo 0x11

        // customized (remote) AirCon commands
        public static final int Souliss_T_IrCom_AirCon_Pow_On = 0x8FFE;
        public static final int Souliss_T_IrCom_AirCon_Pow_Auto_20 = 0x8FFD;
        public static final int Souliss_T_IrCom_AirCon_Pow_Auto_24 = 0x8FFE;
        public static final int Souliss_T_IrCom_AirCon_Pow_Cool_18 = 0x807B;
        public static final int Souliss_T_IrCom_AirCon_Pow_Cool_22 = 0x8079;
        public static final int Souliss_T_IrCom_AirCon_Pow_Cool_26 = 0x807A;
        public static final int Souliss_T_IrCom_AirCon_Pow_Fan = 0x8733;
        public static final int Souliss_T_IrCom_AirCon_Pow_Dry = 0x87BE;
        public static final int Souliss_T_IrCom_AirCon_Pow_Off = 0x70FE;

        // Souliss Aircon Temperature

        public static final short Souliss_T_IrCom_AirCon_temp_16C = 0xF;
        public static final short Souliss_T_IrCom_AirCon_temp_17C = 0x7;
        public static final short Souliss_T_IrCom_AirCon_temp_18C = 0xB;
        public static final short Souliss_T_IrCom_AirCon_temp_19C = 0x3;
        public static final short Souliss_T_IrCom_AirCon_temp_20C = 0xD;
        public static final short Souliss_T_IrCom_AirCon_temp_21C = 0x5;
        public static final short Souliss_T_IrCom_AirCon_temp_22C = 0x9;
        public static final short Souliss_T_IrCom_AirCon_temp_23C = 0x1;
        public static final short Souliss_T_IrCom_AirCon_temp_24C = 0xE;
        public static final short Souliss_T_IrCom_AirCon_temp_25C = 0x6;
        public static final short Souliss_T_IrCom_AirCon_temp_26C = 0xA;
        public static final short Souliss_T_IrCom_AirCon_temp_27C = 0x2;
        public static final short Souliss_T_IrCom_AirCon_temp_28C = 0xC;
        public static final short Souliss_T_IrCom_AirCon_temp_29C = 0x4;
        public static final short Souliss_T_IrCom_AirCon_temp_30C = 0x8;

        // Souliss conditioner Function

        public static final short Souliss_T_IrCom_AirCon_Fun_Auto = 0xF;
        public static final short Souliss_T_IrCom_AirCon_Fun_Dry = 0xB;
        public static final short Souliss_T_IrCom_AirCon_Fun_Fan = 0x3;
        public static final short Souliss_T_IrCom_AirCon_Fun_Heat = 0xD;
        public static final short Souliss_T_IrCom_AirCon_Fun_Cool = 0x7;

        // #define Souliss_T3n_AirCon_Opt1 0x2D
        // #define Souliss_T3n_AirCon_Opt2 0x77

        public static final short Souliss_T_IrCom_AirCon_Fan_Auto = 0x7;
        public static final short Souliss_T_IrCom_AirCon_Fan_High = 0x2;
        public static final short Souliss_T_IrCom_AirCon_Fan_Medium = 0x6;
        public static final short Souliss_T_IrCom_AirCon_Fan_Low = 0x5;

        // optional switches. May be used to toggle
        // custom aircon functions as air deflector, ionizer, turbomode, etc.
        public static final short Souliss_T_IrCom_AirCon_Opt1 = 0x2D;
        public static final short Souliss_T_IrCom_AirCon_Opt2 = 0x77;

        public static final short Souliss_T_IrCom_AirCon_Reset = 0x00;

        // General defines for T1n
        public static final short Souliss_T1n_ToogleCmd = 0x01;
        public static final short Souliss_T1n_OnCmd = 0x02;
        public static final short Souliss_T1n_OffCmd = 0x04;
        public static final short Souliss_T1n_AutoCmd = 0x08;
        public static final short Souliss_T1n_Timed = 0x30;
        public static final short Souliss_T1n_RstCmd = 0x00;
        public static final short Souliss_T1n_OnCoil = 0x01;
        public static final short Souliss_T1n_OffCoil = 0x00;
        public static final short Souliss_T1n_OnCoil_Auto = 0xF1;
        public static final short Souliss_T1n_OffCoil_Auto = 0xF0;
        public static final short Souliss_T1n_Set = 0x22; // Set a state
        public static final short Souliss_T1n_BrightUp = 0x10; // Increase Light
        public static final short Souliss_T1n_BrightDown = 0x20; // Decrease Light
        public static final short Souliss_T1n_Flash = 0x21; // Flash Light

        public static final short Souliss_T1n_OnFeedback = 0x23;
        public static final short Souliss_T1n_OffFeedback = 0x24;

        public static final long Souliss_T16_Red = 0x22FF0000; // Set a state
        public static final long Souliss_T16_Green = 0x2200FF00;
        public static final long Souliss_T16_Blue = 0x220000FF;
        public static final long Souliss_T18_Pulse = 0xA1;

        public static final short Souliss_T19_Min = 0x2255; // 85 Dec, un terzo
        public static final short Souliss_T19_Med = 0x22AA; // 170 Dec
        public static final short Souliss_T19_Max = 0x22FF; // Set a state
        /*
         * IR RGB Typical
         */
        public static final short Souliss_T1n_RGB_OnCmd = 0x1;
        public static final short Souliss_T1n_RGB_OffCmd = 0x9;

        // Souliss RGB main colours
        public static final short Souliss_T1n_RGB_R = 0x2;
        public static final short Souliss_T1n_RGB_G = 0x3;
        public static final short Souliss_T1n_RGB_B = 0x4;
        public static final short Souliss_T1n_RGB_W = 0x5;
        // Souliss RGB Controls
        public static final short Souliss_T_IrCom_RGB_bright_up = 0x6;
        public static final short Souliss_T_IrCom_RGB_bright_down = 0x7;
        // MODES;
        public static final short Souliss_T_IrCom_RGB_mode_flash = 0xA1;
        public static final short Souliss_T_IrCom_RGB_mode_strobe = 0xA2;
        public static final short Souliss_T_IrCom_RGB_mode_fade = 0xA3;
        public static final short Souliss_T_IrCom_RGB_mode_smooth = 0xA4;

        public static final short Souliss_T1n_RGB_R2 = 0xB1;
        public static final short Souliss_T1n_RGB_R3 = 0xB2;
        public static final short Souliss_T1n_RGB_R4 = 0xB3;
        public static final short Souliss_T1n_RGB_R5 = 0xB4;
        public static final short Souliss_T1n_RGB_G2 = 0xC1;
        public static final short Souliss_T1n_RGB_G3 = 0xC2;
        public static final short Souliss_T1n_RGB_G4 = 0xC3;
        public static final short Souliss_T1n_RGB_G5 = 0xC4;
        public static final short Souliss_T1n_RGB_B2 = 0xD1;
        public static final short Souliss_T1n_RGB_B3 = 0xD2;
        public static final short Souliss_T1n_RGB_B4 = 0xD3;
        public static final short Souliss_T1n_RGB_B5 = 0xD4;

        public static final short Souliss_T1n_RGB_RstCmd = 0x00;

        // Defines for Typical 2n
        public static final short Souliss_T2n_CloseCmd = 0x01;
        public static final short Souliss_T2n_OpenCmd = 0x02;
        public static final short Souliss_T2n_StopCmd = 0x04;
        public static final short Souliss_T2n_ToogleCmd = 0x08;
        public static final short Souliss_T2n_RstCmd = 0x00;
        public static final short Souliss_T2n_Timer_Val = 0x1F;
        public static final short Souliss_T2n_Timer_Off = 0x10;
        public static final short Souliss_T2n_LimSwitch_Close = 0x08;
        public static final short Souliss_T2n_LimSwitch_Open = 0x10;
        public static final short Souliss_T2n_NoLimSwitch = 0x20;
        public static final short Souliss_T2n_Coil_Close = 0x01;
        public static final short Souliss_T2n_Coil_Open = 0x02;
        public static final short Souliss_T2n_Coil_Stop = 0x03;
        public static final short Souliss_T2n_Coil_Off = 0x00;

        // General defines for T3n
        public static final short Souliss_T3n_IncSetPoint = 0x01;
        public static final short Souliss_T3n_DecSetPoint = 0x02;
        public static final short Souliss_T3n_AsMeasured = 0x03;
        public static final short Souliss_T3n_Cooling = 0x04;
        public static final short Souliss_T3n_Heating = 0x05;
        public static final short Souliss_T3n_FanOff = 0x06;
        public static final short Souliss_T3n_FanLow = 0x07;
        public static final short Souliss_T3n_FanMed = 0x08;
        public static final short Souliss_T3n_FanHigh = 0x09;
        public static final short Souliss_T3n_FanAuto = 0x0A;
        public static final short Souliss_T3n_FanManual = 0x0B;
        public static final short Souliss_T3n_Set = 0x0C;
        public static final short Souliss_T3n_ShutOff = 0x0D;

        // General defines for T4n
        public static final short Souliss_T4n_Alarm = 0x01; // Alarm Condition
                                                            // Detected (Input)
        public static final short Souliss_T4n_RstCmd = 0x00;
        public static final short Souliss_T4n_ReArm = 0x03; // Silence and Arm
                                                            // Command
        public static final short Souliss_T4n_NotArmed = 0x04; // Anti-theft not
                                                                // Armed Command
        public static final short Souliss_T4n_Armed = 0x05; // Anti-theft Armed
                                                            // Command
        public static final short Souliss_T4n_Antitheft = 0x01; // Anti-theft Armed
                                                                // Feedback
        public static final short Souliss_T4n_NoAntitheft = 0x00; // Anti-theft not
                                                                    // Armed
                                                                    // Feedback
        public static final short Souliss_T4n_InAlarm = 0x03; // Anti-theft in Alarm

        public static final short Souliss_RstCmd = 0x00;
        public static final short Souliss_NOTTRIGGED = 0x00;
        public static final short Souliss_TRIGGED = 0x01;

        // Defines for current sensor
        public static final short Souliss_T_CurrentSensor = 0x65;

        // REMOVE THESE
        public static final short Souliss_T_TemperatureSensor = 0x67;
        public static final byte Souliss_T_TemperatureSensor_refresh = 0x02;

        public static final short Souliss_T_HumiditySensor = 0x69;
        public static final byte Souliss_T_HumiditySensor_refresh = 0x03;

    }

    /**
     * Network constants
     *
     * @author Ale
     *
     */
    public static class Net {

        public static final String TAG = "SoulissApp";

        public static final int DEFAULT_SOULISS_PORT = 230;
        public static final int SERVERPORT = 23000;
        public static final int WEB_SERVER_PORT = 8080;

        public static final String BROADCASTADDR = "255.255.255.255";

        public static final byte Souliss_UDP_function_force = 51;
        public static final byte Souliss_UDP_function_force_massive = 52;
        public static final byte Souliss_UDP_function_subscribe = 0x21;
        public static final byte Souliss_UDP_function_poll = 0x27;
        public static final byte Souliss_UDP_function_poll_resp = 0x37;
        public static final byte Souliss_UDP_function_subscribe_resp = 0x31;
        public static final byte Souliss_UDP_function_subscribe_data = 0x15;
        public static final byte Souliss_UDP_function_typreq = 0x22;
        public static final byte Souliss_UDP_function_typreq_resp = 0x32;
        public static final byte Souliss_UDP_function_healthReq = 0x25;
        public static final byte Souliss_UDP_function_health_resp = 0x35;

        public static final byte Souliss_UDP_function_ping = 0x8;
        public static final byte Souliss_UDP_function_ping_resp = 0x18;
        public static final byte Souliss_UDP_function_broadcast_configure = 0x2D;
        public static final byte Souliss_UDP_function_broadcast_configure_wifissid = 0x2E;
        public static final byte Souliss_UDP_function_broadcast_configure_wifipass = 0x2F;

        public static final byte Souliss_UDP_function_ping_bcast = 0x28;
        public static final byte Souliss_UDP_function_ping_bcast_resp = 0x38;

        public static final int Souliss_UDP_function_db_struct = 0x26;
        public static final int Souliss_UDP_function_db_struct_resp = 0x36;

        public static final Byte[] PING_PAYLOAD = { Souliss_UDP_function_ping, 0, 0, 0, 0 };
        public static final Byte[] PING_BCAST_PAYLOAD = { Souliss_UDP_function_ping_bcast, 0, 0, 0, 0 };
        public static final Byte[] DBSTRUCT_PAYLOAD = { Souliss_UDP_function_db_struct, 0, 0, 0, 0 };

        //WEBSERVER
        public static final String PREF_SERVER_PORT = "prefServerPort";

    }
}
