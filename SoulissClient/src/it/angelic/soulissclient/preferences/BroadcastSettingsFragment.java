package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.PreferenceFragment;
/**
 * Per funzionare dovresti aggiungere in SoulissApp un menù di configurazione dove poter inserire i seguenti parametri:
1) Indirizzo IP da assegnare al Gateway
2) Subnetmask
3) Gateway
4) SSID Rete Wireless a cui il nodo deve collegarsi
5) Password rete wireless

Il frame va inviato su vNet in broadcast (come quello per la ricerca del gateway in automatico) con il payload formattato in questo modo:
indirizzo ip (4 byte) | subnetmask (4 byte) | gateway ip (4 byte) | lunghezza SSID (1 byte) | lunghezza password (1 byte) | SSID (lunghezza SSID byte) | password (lunghezza password byte).

Il functional code associato è SETIP 0x3B.

Questo frame va inviato solo su richiesta dell'utente e non ad ogni avvio e deve esser possibile lasciare qualunque dei parametri non compilato, in quel caso puoi utilizzare tutti zero.

Ad esempio, se dovessero esserci solo le informazioni IP e non quelle per il wifi il frame sarebbe:

indirizzo ip (4 byte) | subnetmask (4 byte) | gateway ip (4 byte) | lenghezza SSID (1 byte) = 1 | lunghezza password (1 byte) = 1 | SSID = 0 | password = 0.

Al contrario se dovessero esserci solo le informazioni del wifi,
0,0,0,0 (4 byte) | 0,0,0,0 (4 byte) | 0,0,0,0 (4 byte) | lunghezza SSID (1 byte) | lunghezza password (1 byte) | SSID | password.

Souliss si aspetta una lunghezza fissa per i primi 4 parametri (4+4+4 byte) ed una variabile per gli ultimi due parametri, con un minimo di 1 byte di lunghezza e valore zero per entrambi.
 * @author pegoraro
 *
 */
@TargetApi(11)
public class BroadcastSettingsFragment extends PreferenceFragment {

	private SoulissPreferenceHelper opzioni;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		//String settings;
		if (opzioni.isLightThemeSelected()) {
			getActivity().setTheme(R.style.LightThemeSelector);
		} else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings_broadcast);
		

	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
}