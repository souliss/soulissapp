package it.angelic.soulissclient.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class NetUtils {
	
	public static InetAddress getLocalIpAddress() throws SocketException {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					System.out.println("ip1--:" + inetAddress);
					System.out.println("ip2--:" + inetAddress.getHostAddress());

					// for getting IPV4 format
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
						// return inetAddress.getHostAddress().toString();
						return inetAddress;
					}
				}
			}
		return null;
	}
	
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
	/**
	 * Se la and bit a bit del indirizzo IP da verificare con l'inversa della
	 * subnet mask non ha almeno un byte diverso da zero, l'indirizzo IP non può
	 * essere utilizzato.
	 * 
	 * @param toverify
	 */
	public static boolean belongsToNode(InetAddress toverify, InetAddress subnetMask) {
		//int sub = subnetMask.getAddress();//Constants.getSubnet(context);
		// verificare inversione
		//InetAddress subnet = intToInet(sub);
		byte[] subnetAddr = subnetMask.getAddress();
		byte[] actual = toverify.getAddress();
		for (int i = 0; i < subnetAddr.length; i++) {
			if ((subnetAddr[i] & ~actual[i]) != 0)
				return true;
		}
		return false;
	}

	/**
	 * Verifica della subnet : Un indirizzo IP appartiene alla propria subnet
	 * se, fatta la and bit a bit dell'indirizzo IP da verificare con la
	 * subnetmask il risultato ottenuto è uguale alla and bit a bit del proprio
	 * indirizzo IP con la subnet mask.
	 * 
	 * @param toverify
	 */
	public static boolean belongsToSameSubnet(InetAddress toverify, InetAddress subnetMask, InetAddress localH) {
		//int sub = Constants.getSubnet(context);
		
		// byte[] bytes = BigInteger.valueOf(sub).toByteArray();
		//InetAddress subnet = intToInet(sub);

		byte[] subnetAddr = subnetMask.getAddress();
		byte[] actual = toverify.getAddress();
		byte[] local = localH.getAddress();
		for (int i = 0; i < subnetAddr.length; i++) {
			if ((subnetAddr[i] & actual[i]) != (subnetAddr[i] & local[i]))
				return false;
		}
		return true;

	}

	public static InetAddress extractTargetAddress(ArrayList<Short> mac) throws UnknownHostException {
		Short[] parsed = mac.subList(5, 9).toArray(new Short[4]);
		byte[] good = new byte[4];
		// [56, 5, 0, 0, 4, 192, 168, 0, 17]
		for (int i = 0; i < good.length; i++) {
			good[i] = parsed[i].byteValue();
		}
		final InetAddress checkIPt = InetAddress.getByAddress(good);
		return checkIPt;
	}


	public static byte byteOfInt(int value, int which) {
		int shift = which * 8;
		return (byte) (value >> shift);
	}

	public static InetAddress intToInet(int value) {
		byte[] bytes = new byte[4];
		for (int i = 0; i < 4; i++) {
			bytes[i] = byteOfInt(value, i);
		}
		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			// This only happens if the byte array has a bad length
			return null;
		}
	}

}
