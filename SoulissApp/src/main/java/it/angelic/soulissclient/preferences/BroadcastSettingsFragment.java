package it.angelic.soulissclient.preferences;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.net.Constants;
import it.angelic.soulissclient.net.NetUtils;
import it.angelic.soulissclient.net.UDPHelper;

/**
 * Per funzionare dovresti aggiungere in SoulissApp un menù di configurazione
 * dove poter inserire i seguenti parametri: 1) Indirizzo IP da assegnare al
 * Gateway 2) Subnetmask 3) Gateway 4) SSID Rete Wireless a cui il nodo deve
 * collegarsi 5) Password rete wireless
 * 
 * Il frame va inviato su vNet in broadcast (come quello per la ricerca del
 * gateway in automatico) con il payload formattato in questo modo: indirizzo ip
 * (4 byte) | subnetmask (4 byte) | gateway ip (4 byte) | lunghezza SSID (1
 * byte) | lunghezza password (1 byte) | SSID (lunghezza SSID byte) | password
 * (lunghezza password byte).
 * 
 * Il functional code associato è SETIP 0x3B.
 * 
 * Questo frame va inviato solo su richiesta dell'utente e non ad ogni avvio e
 * deve esser possibile lasciare qualunque dei parametri non compilato, in quel
 * caso puoi utilizzare tutti zero.
 * 
 * Ad esempio, se dovessero esserci solo le informazioni IP e non quelle per il
 * wifi il frame sarebbe:
 * 
 * indirizzo ip (4 byte) | subnetmask (4 byte) | gateway ip (4 byte) | lenghezza
 * SSID (1 byte) = 1 | lunghezza password (1 byte) = 1 | SSID = 0 | password =
 * 0.
 * 
 * Al contrario se dovessero esserci solo le informazioni del wifi, 0,0,0,0 (4
 * byte) | 0,0,0,0 (4 byte) | 0,0,0,0 (4 byte) | lunghezza SSID (1 byte) |
 * lunghezza password (1 byte) | SSID | password.
 * 
 * Souliss si aspetta una lunghezza fissa per i primi 4 parametri (4+4+4 byte)
 * ed una variabile per gli ultimi due parametri, con un minimo di 1 byte di
 * lunghezza e valore zero per entrambi.
 * 
 * @author Del Pex
 * 
 */
@TargetApi(11)
public class BroadcastSettingsFragment extends PreferenceFragment {

	private SoulissPreferenceHelper opzioni;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		opzioni = SoulissClient.getOpzioni();
		// String settings;
		if (opzioni.isLightThemeSelected()) {
			getActivity().setTheme(R.style.LightThemeSelector);
		} else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_broadcast);
		final EditTextPreference bcast_IP = (EditTextPreference) findPreference("bcast_IP");
		final EditTextPreference bcast_subnet = (EditTextPreference) findPreference("bcast_subnet");
		final EditTextPreference bcast_gateway = (EditTextPreference) findPreference("bcast_gateway");
		final EditTextPreference bcast_ssid = (EditTextPreference) findPreference("bcast_ssid");
		final EditTextPreference bcast_passwd = (EditTextPreference) findPreference("bcast_passwd");
		final Preference sndBcast = findPreference("sndBcast");

		bcast_subnet.setText(NetUtils.getDeviceSubnetMaskString(getActivity()));
		bcast_subnet.setSummary(getString(R.string.bridge_subnet) + ": " + bcast_subnet.getText());

		bcast_gateway.setText(NetUtils.getDeviceGatewayString(getActivity()));
		bcast_gateway.setSummary(getString(R.string.bridge_gateway) + ": " + bcast_gateway.getText());

		sndBcast.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {

				final List<Byte> bcastPayload = new ArrayList<>();
				bcastPayload.add(Constants.Souliss_UDP_function_broadcast_configure);

				try {
					if (bcast_IP.getText().length() == 0)
						throw new Exception("empty parameter: IP address");
					InetAddress i4 = InetAddress.getByName(bcast_IP.getText());
					for (int tper = 0; tper < i4.getAddress().length; tper++) {
						bcastPayload.add(i4.getAddress()[tper]);
					}
				} catch (Exception e) {
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
				}
				// SUBNET
				try {
					InetAddress is4 = InetAddress.getByName(bcast_subnet.getText());
					for (int tper = 0; tper < is4.getAddress().length; tper++) {
						bcastPayload.add(is4.getAddress()[tper]);
					}
				} catch (Exception e) {
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
				}
				// GATEWAY
				try {
					InetAddress ig4 = InetAddress.getByName(bcast_gateway.getText());
					for (int tper = 0; tper < ig4.getAddress().length; tper++) {
						bcastPayload.add(ig4.getAddress()[tper]);
					}
				} catch (Exception e) {
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
					bcastPayload.add((byte) 0);
				}
				byte[] wifi = { 0x0 };
				try {
					wifi = hexStringToByteArray(bcast_ssid.getText());
					bcastPayload.add((byte) wifi.length);
				} catch (Exception e) {
					bcastPayload.add((byte) 1);// lunghezza
				}
				byte[] pass = { 0x0 };
				try {
					pass = hexStringToByteArray(bcast_passwd.getText());
					bcastPayload.add((byte) pass.length);
				} catch (Exception e) {
					bcastPayload.add((byte) 1);// lunghezza
				}

				for (int i = 0; i < wifi.length; i++) {
					byte b = wifi[i];
					bcastPayload.add(b);// lunghezza
				}

                for (byte b : pass) {
                    bcastPayload.add(b);// lunghezza
                }

				new Thread(new Runnable() {
					@Override
					public void run() {
						UDPHelper.issueBroadcastConfigure(opzioni, bcastPayload);
					}
				}).start();
				
				return true;
			}
		});

	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

}