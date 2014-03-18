package it.angelic.soulissclient.net;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical5nCurrentVoltagePowerSensor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class StaticUtils {
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			Log.e(Constants.TAG, "There was an IO error", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/*
	 * public static String convertStreamToString(InputStream is) {
	 * 
	 * if (is != null) { Writer writer = new StringWriter();
	 * 
	 * char[] buffer = new char[1024]; try { Reader reader = new
	 * BufferedReader(new InputStreamReader(is, "UTF-8")); int n; while ((n =
	 * reader.read(buffer)) != -1) { writer.write(buffer, 0, n); } } catch
	 * (UnsupportedEncodingException e) { e.printStackTrace(); } catch
	 * (IOException e) { e.printStackTrace(); } finally { try { is.close(); }
	 * catch (IOException e) { e.printStackTrace(); } }
	 * 
	 * return writer.toString(); } else { return ""; } }
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					System.out.println("ip1--:" + inetAddress);
					System.out.println("ip2--:" + inetAddress.getHostAddress());

					String ipv4;
					// for getting IPV4 format
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {

						String ip = inetAddress.getHostAddress().toString();
						System.out.println("ip---::" + ip);
						
						// return inetAddress.getHostAddress().toString();
						return ip;
					}
				}
			}
		} catch (Exception ex) {
			Log.e("IP Address", ex.toString());
		}
		return null;
	}/*
	 * public static String getLocalIpAddress() { try { for
	 * (Enumeration<NetworkInterface> en =
	 * NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	 * NetworkInterface intf = en.nextElement(); for (Enumeration<InetAddress>
	 * enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	 * InetAddress inetAddress = enumIpAddr.nextElement(); if
	 * (!inetAddress.isLoopbackAddress()) { return
	 * inetAddress.getHostAddress().toString(); } } } } catch (SocketException
	 * ex) { ex.printStackTrace(); } return null; }
	 */

	public static String openHTMLString(Context context, int id) {
		InputStream is = context.getResources().openRawResource(id);

		return StaticUtils.convertStreamToString(is);
	}

	public static String openHTMLStringfromURI(Context context, String id) throws FileNotFoundException {
		ContentResolver cr = context.getContentResolver();
		Log.d(Constants.TAG, "fileUriString = " + id);
		  Uri tempuri = Uri.parse(id);
		  InputStream is = cr.openInputStream(tempuri);

		return StaticUtils.convertStreamToString(is);
	}

	public static JSONObject getJSONSoulissDevice(SoulissTypical soulissTypical) {
		JSONObject objecttyp = new JSONObject();
		try {
			objecttyp.put("typ", Integer.toHexString(soulissTypical.getTypicalDTO().getTypical()));
			objecttyp.put("slo", soulissTypical.getTypicalDTO().getSlot());
						
			if(soulissTypical.getClass()== SoulissTypical5nCurrentVoltagePowerSensor.class){
				//se si tratta del tipico per la misurazione dei consumi allora per la restituzione del valore chiamo un metodo differente
				objecttyp.put("val", ((SoulissTypical5nCurrentVoltagePowerSensor) soulissTypical).getOutputFloat());
			} else {
				objecttyp.put("val", soulissTypical.getOutput());				
			}
			
			
			objecttyp.put("ddesc", soulissTypical.getNiceName());

			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objecttyp;
	}
	public static JSONObject getJSONSoulissLiveData(SoulissTypical soulissTypical) {
		JSONObject objecttyp = new JSONObject();
		try {
			objecttyp.put("typ", Integer.toHexString(soulissTypical.getTypicalDTO().getTypical()));
			
			if(soulissTypical.getClass()== SoulissTypical5nCurrentVoltagePowerSensor.class){
				//se si tratta del tipico per la misurazione dei consumi allora per la restituzione del valore chiamo un metodo differente
				objecttyp.put("val", ((SoulissTypical5nCurrentVoltagePowerSensor) soulissTypical).getOutputFloat());
			} else {
				objecttyp.put("val", soulissTypical.getOutput());				
			}
				


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objecttyp;
	}

}
