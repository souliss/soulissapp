package it.angelic.soulissclient.net;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.model.SoulissCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONHelper {
	private static final String LOG_TAG = "Souliss:JSONHelper";

	private static String server;
	private static int serverTimeout = 8000;

	public static int getServerTimeout() {
		return serverTimeout;
	}

	public static void setServerTimeout(int serverTimeout) {
		JSONHelper.serverTimeout = serverTimeout;
	}

	public static void setServer(String s) {
		server = s;
	}

	public static String getServer() {
		return server;
	}

	/**
	 * Issue a /status command and retrieves all nodes status
	 * 
	 * @return
	 */
	/*public static HashMap<Short, SoulissNode> getAllNodes() {

		// String tmpInt;
		SoulissPreferenceHelper op = new SoulissPreferenceHelper(SoulissClient.getAppContext());
		HashMap<Short, SoulissNode> aryActuator = new HashMap<Short, SoulissNode>();

		try {
			String url_login = "http://" + server + "/status?all";
			JSONArray nodesArray = getHttpJsonArray(url_login);

			// ip = jsonObject.optString("id");
			// JSONArray nodesArray = jsonObject.getJSONArray("id");
			// go through all the nodes
			for (short i = 0; i < nodesArray.length(); i++) {

				SoulissNode newSoulNode = populateNodeFromJSONArray(nodesArray, i, op);
				// aryActuator.add(newSoulNode);
				aryActuator.put(newSoulNode.getId(), newSoulNode);

			}

		} catch (JSONException e) {
			Log.e(LOG_TAG, e.toString());
		} catch (Exception e) {
			Log.e(LOG_TAG, " CAN'T DOWNLOAD NODES!! ", e);
		}
		Log.i(LOG_TAG, " getAllNodes size " + aryActuator.size());
		return aryActuator;

	}*/

	/*private static SoulissNode populateNodeFromJSONArray(JSONArray retrieved, short tgtIndex, SoulissPreferenceHelper op)
			throws Exception {
		JSONObject actuator;
		// SoulissPreferenceHelper op = new
		// SoulissPreferenceHelper(SoulissClient.getAppContext());
		try {
			actuator = retrieved.getJSONObject(tgtIndex);

			String tmpInt;
			SoulissNode newSoulNode = new SoulissNode((short) 0);

			tmpInt = actuator.optString("hlt");
			newSoulNode.setHealth(Short.parseShort(tmpInt));
			newSoulNode.setRefreshedAt(Calendar.getInstance());
			tmpInt = actuator.optString("id");

			newSoulNode.setId((short) Integer.parseInt(tmpInt));
			// prende lo slot del nodo
			JSONArray aryies = actuator.getJSONArray("slot");

			for (int j = 0; j < aryies.length(); j++) {
				SoulissTypical rest;
				JSONObject actualTyp = aryies.getJSONObject(j);
				tmpInt = actualTyp.optString("typ");
				SoulissTypicalDTO dto = new SoulissTypicalDTO();
				dto.setTypical(Short.parseShort(tmpInt));
				dto.setSlot((short) j);
				dto.setNodeId(tgtIndex);
				dto.setRefreshedAt(Calendar.getInstance());

				rest = SoulissTypical.typicalFactory(Short.parseShort(tmpInt), newSoulNode, dto, op);

				// rest.getTypicalDTO().setTypical(Byte.parseByte(tmpInt));
				// rest.getTypicalDTO().setNodeId(tgtIndex);
				// rest.getTypicalDTO().setSlot((short) j);

				// rest.getTypicalDTO().setRefreshedAt(Calendar.getInstance());
				tmpInt = actualTyp.optString("val");
				rest.getTypicalDTO().setOutput(Short.parseShort(tmpInt));

				// if (rest.getTypical() !=
				// Constants.Souliss_T_CurrentSensor_slave)
				newSoulNode.add(rest);
			}
			return newSoulNode;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}*/

	/**
	 * Issue a /status?id= command and retrieves specified node
	 * 
	 * @return null se non trovato
	 */
	/*public static SoulissNode getNode(int nodeid, SoulissPreferenceHelper op) {

		try {
			String url_login = "http://" + server + "/status?id=" + nodeid;
			// l'array contiene un solo nodo
			JSONArray nodesArray = getHttpJsonArray(url_login);
			// JSONArray nodesArray = jsonObject.getJSONArray("id");
			// go through all the nodes

			SoulissNode got = populateNodeFromJSONArray(nodesArray, (short) 0, op);
			if (nodeid != got.getId())
				throw new Exception("manage me");
			return got;
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.toString());
		} catch (Exception e) {
			Log.e(LOG_TAG, " NULL " + e.getMessage(), e);
		}
		// Log.i(LOG_TAG, " getAllActuator size " + aryActuator.size());
		return null;
	}*/

	/**
	 * Issue a reset command to Souliss, to reinitialize Souliss internal
	 * connections
	 * 
	 * @return JSON output string
	 */
	public static String issueSoulissReset() {
		return getHttp("http://" + server + "/force?rst");
	}

	/**
	 * Issue a massive command to Souliss, at specified coordinates
	 * 
	 * @param slot
	 * @param cmd
	 * @return JSON output string
	 */
	public static String issueSoulissMassiveCommand(String typ, String cmd) {

		StringBuilder cmdpars = new StringBuilder();

		cmdpars.append("?typ=");
		cmdpars.append(typ);
		cmdpars.append("&val=");
		cmdpars.append(cmd);

		return getHttp("http://" + server + "/force" + cmdpars);
	}

	/**
	 * Usato dal condizionatore fa cagar, andrebbe rifattorizzato
	 * 
	 * @param nodeId
	 * @param slot
	 * @param soulissT12Cmd
	 *            (XYZT X= powerON, Y PowerOFF, option1 , option2)
	 * @return
	 */
	@Deprecated
	public static boolean issueSoulissCommand(int nodeId, short slot, long soulissT12Cmd) {
		String pars = Long.toHexString(soulissT12Cmd);
		String ret;
		if (pars.length() <= 2)
			ret = issueSoulissCommand(String.valueOf(nodeId), String.valueOf(slot), String.valueOf(soulissT12Cmd));
		else {
			if (pars.length() == 3)
				pars = "0" + pars;
			String par1, par2;
			par1 = Integer.toString(Integer.parseInt(pars.substring(0, 2).toUpperCase(Locale.getDefault()), 16));
			par2 = Integer.toString(Integer.parseInt(pars.substring(2, 4).toUpperCase(Locale.getDefault()), 16));
			ret = issueSoulissCommand(String.valueOf(nodeId), String.valueOf(slot), par1, par2);
		}

		if ("\n({})\n".compareToIgnoreCase(ret) == 0)
			return true;

		return false;

	}

	public static boolean issueSoulissCommand(SoulissCommand in) {
		SoulissCommandDTO dto = in.getCommandDTO();

		if (dto.getType() != Constants.COMMAND_MASSIVE) {
			String ret = issueSoulissCommand(String.valueOf(dto.getNodeId()), String.valueOf(dto.getSlot()),
					String.valueOf(dto.getCommand()));
			if ("\n({})\n".compareToIgnoreCase(ret) == 0)
				return true;
		} else {
			String ret = issueSoulissMassiveCommand(String.valueOf(dto.getSlot()), String.valueOf(dto.getCommand()));
			if ("\n({})\n".compareToIgnoreCase(ret) == 0)
				return true;
		}
		return false;

	}

	/**
	 * Issue a command to Souliss, at specified coordinates
	 * 
	 * @param id
	 * @param slot
	 * @param cmd
	 * @return JSON output string
	 */
	public static String issueSoulissCommand(String id, String slot, String... cmd) {

		StringBuilder cmdpars = new StringBuilder();
		short order = 1;
		cmdpars.append("?id=");
		cmdpars.append(id);
		cmdpars.append("&slot=");
		cmdpars.append(slot);
		//
		for (String number : cmd) {
			cmdpars.append("&val");
			if (order > 1)
				cmdpars.append(order);
			cmdpars.append("=");
			cmdpars.append(number);
			order++;
		}

		// Log.i(Constants.TAG, "issuing: "+"http://" + server + "/force" +
		// cmdpars);
		return getHttp("http://" + server + "/force" + cmdpars);
	}

	public static JSONObject getHttpJson(String url) {
		JSONObject json = null;
		String result = getHttp(url);

		// la prima risposta di Souliss e` un pacco
		if (("\n").compareTo(result) == 0 && "".compareTo(url) != 0) {
			Log.w(LOG_TAG, "Empty Response received, trying again");
			return getHttpJson(url);
		}

		if (result != null && result.startsWith("\n")) {
			result = result.substring(2, result.length());
		}
		if (result != null && result.startsWith("(") && result.endsWith("")) {
			result = result.substring(1, result.length() - 1);
		}

		try {
			json = new JSONObject(result);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "There was a Json parsing based error", e);
		}
		return json;
	}

	public static JSONArray getHttpJsonArray(String url) {
		JSONArray json = null;
		String result = getHttp(url);

		// la prima risposta di Souliss e` un pacco
		if (("\n").compareTo(result) == 0 && "".compareTo(url) != 0) {
			Log.w(LOG_TAG, "Empty Response received, trying again");
			return getHttpJsonArray(url);
		}

		if (result != null && result.startsWith("\n")) {
			result = result.substring(1, result.length() - 1);
		}
		if (result != null && result.startsWith("({") && result.endsWith("})")) {
			result = result.substring(2, result.length() - 2);
		}

		try {
			json = new JSONArray(result);
		} catch (JSONException e) {
			Log.e(LOG_TAG, "There was a Json parsing based error" + e.getMessage());
		}
		return json;
	}

	/**
	 * Recupera la stringa JSON restituita dall'URL passata se ci sono molti
	 * nodi puo essere un problema ?
	 * 
	 * @param url
	 * @return
	 */
	public static String getHttp(String url) {
		Log.d(LOG_TAG, "getHttp : " + url);
		String result = "";
		HttpClient httpclient = new DefaultHttpClient();
		URI u;


		try {
			u = new URI(url);
			HttpGet httpget = new HttpGet(u);

			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = serverTimeout;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

			HttpResponse response;

			try {
				response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					result = convertStreamToString(instream);
					// Log.i(LOG_TAG, result);
					instream.close();
				}
			} catch (ClientProtocolException e) {
				Log.e(LOG_TAG, "There was a protocol based error", e);
			} catch (IOException e) {
				Log.e(LOG_TAG, "There was an IO Stream related error: ", e);
			} catch (Exception e) {
				Log.e(LOG_TAG, "There was an app error: ", e);
			}

		} catch (URISyntaxException e) {
			Log.e(LOG_TAG, "URI SYNTAX ", e);
		}

		return result;
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "There was an IO error", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static boolean checkSoulissHttp(String ip, int timeoutMsec) {
		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutMsec);
			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpGet request;
			Log.d(LOG_TAG, "checkSoulissHttp " + ip+" timeout: "+timeoutMsec);
			request = new HttpGet(new URI("http://" + ip));
			HttpResponse response = httpClient.execute(request);
			int status = response.getStatusLine().getStatusCode();
			// IP LOCALE
			if (status == HttpStatus.SC_OK) {
				return true;
			} else {
				Log.w(LOG_TAG, "checkSoulissHttp failed, status=" + status);
				return false;
			}
		} catch (ConnectTimeoutException ee) {
			Log.e(LOG_TAG, "Connection TIMEOUT!");
			return false;
		} catch (SocketException ef) {
			Log.e(LOG_TAG, "SocketException, maybe we're using wifi outside home? " + ef.getMessage());
			return false;
		} catch (Exception e) {
			Log.e(LOG_TAG, "There was an error checking connectivity " + e.getLocalizedMessage(), e);
			return false;
		}
	}

}
