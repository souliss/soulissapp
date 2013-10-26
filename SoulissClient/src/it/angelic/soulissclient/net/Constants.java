package it.angelic.soulissclient.net;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

/**
 * Network constants
 * 
 * @author Ale
 *
 */
public class Constants {
	public static final String TAG = "SoulissApp";
	public static final String CUSTOM_INTENT_SOULISS_RAWDATA = "it.angelic.soulissclient.RAW_MACACO_DATA";
	public static final String CUSTOM_INTENT_SOULISS_TIMEOUT = "it.angelic.soulissclient.RAW_TIMEOUT";
	
	public static final int SOULISSPORT = 230;
	public static final int SERVERPORT = 23000;
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
	
	public static final byte Souliss_UDP_function_ping_bcast = 0x28;
	public static final byte Souliss_UDP_function_ping_bcast_resp = 0x38;

	public static final int Souliss_UDP_function_db_struct = 0x26;
	public static final int Souliss_UDP_function_db_struct_resp = 0x36;
	
	public static final Byte[] PING_PAYLOAD = { Souliss_UDP_function_ping, 0, 0, 0, 0 };
	public static final Byte[] PING_BCAST_PAYLOAD = { Souliss_UDP_function_ping_bcast, 0, 0, 0, 0 };
	public static final Byte[] DBSTRUCT_PAYLOAD = { Souliss_UDP_function_db_struct, 0, 0, 0, 0 };
	
	public static int getSubnet(Context ctx){
		WifiManager wifii= (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();

       /* s_dns1="DNS 1: "+String.valueOf(d.dns1);
        s_dns2="DNS 2: "+String.valueOf(d.dns2);    
        s_gateway="Default Gateway: "+String.valueOf(d.gateway);    
        s_ipAddress="IP Address: "+String.valueOf(d.ipAddress); 
        s_leaseDuration="Lease Time: "+String.valueOf(d.leaseDuration);     */
        return d.netmask;    
    
	}
}
