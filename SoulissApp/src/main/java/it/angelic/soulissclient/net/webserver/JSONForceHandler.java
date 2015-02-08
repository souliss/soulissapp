package it.angelic.soulissclient.net.webserver;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.NetUtils;
import it.angelic.soulissclient.net.UDPHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class JSONForceHandler implements HttpRequestHandler {

	private Context context = null;
	private SoulissDBHelper db;

	public JSONForceHandler(Context context) {
		this.context = context;
		db = new SoulissDBHelper(context);
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException,
			IOException {
		int mellow = -1;// nodesid sempe positivi
		try {
			//HTTP GET /force?id=0&slot=1&val=22+01+05+01
			String uriString = request.getRequestLine().getUri();
			Uri uri = Uri.parse(uriString);
			final String message = URLDecoder.decode(uri.getQueryParameter("id"), "UTF-8");
			final String slot = URLDecoder.decode(uri.getQueryParameter("slot"), "UTF-8");
			final String val = URLDecoder.decode(uri.getQueryParameter("val"), "UTF-8");
			final String[] parsed = val.split("\\+");
			mellow = Integer.parseInt(message);
			Log.i(it.angelic.soulissclient.Constants.TAG, "URI: " + uriString);
			Log.i(it.angelic.soulissclient.Constants.TAG, "Decoded ID: " + message);
			
			HttpEntity entity = new EntityTemplate(new ContentProducer() {
				public void writeTo(final OutputStream outstream) throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
					//Log.v(it.angelic.soulissclient.Constants.TAG,writeJSON(target));
					writer.write(UDPHelper.issueSoulissCommand(message, slot, SoulissClient.getOpzioni(), parsed));
					writer.flush();
				}
			});
			//Log.d(it.angelic.soulissclient.Constants.TAG, "Encoding:"+entity.getContentEncoding());
			response.setHeader("Content-Type", "text/html; charset=UTF-8");
			response.setEntity(entity);
		} catch (Exception e) {
			Log.e(it.angelic.soulissclient.Constants.TAG, e.getMessage(),e);
		}
		final int target = mellow;
		// HTTP GET /force?id=0&slot=1&val=22

		
	}
	/**
	 * Write the entire live data if target < 0
	 * 
	 * @param target
	 * @return
	 */
	private String writeJSON(int target) {
		db.open();
		List<SoulissNode> nodes;
		//Se target < 0, allora metto tutto
		if (target < 0)
			nodes = db.getAllNodes();
		else {
			nodes = new ArrayList<SoulissNode>();
			nodes.add(db.getSoulissNode(target));
		}
		JSONArray nodesArr = new JSONArray();
		for (SoulissNode soulissNode : nodes) {
			JSONObject object = new JSONObject();
			JSONArray typArr = new JSONArray();

			List<SoulissTypical> tipici = soulissNode.getActiveTypicals();
			for (SoulissTypical soulissTypical : tipici) {
				typArr.put(NetUtils.getJSONSoulissLiveData(soulissTypical));
			}

			try {
				object.put("slot", typArr);
				object.put("hlt", soulissNode.getHealth());
			} catch (JSONException e) {
				Log.e(Constants.TAG, "Zozzariello ERROR:",e);
			}
			
			//nodesArr.put("id",soulissNode.getId() );
			JSONObject glob = new JSONObject();
			try {
				glob.put("id", object);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nodesArr.put(glob);
		}

		/*
		 * JSONObject object = new JSONObject(); try { object.put("name",
		 * "Jack Hack"); object.put("score", new Integer(200));
		 * object.put("current", new Double(152.32)); object.put("nickname",
		 * "Hacker"); } catch (JSONException e) { e.printStackTrace(); }
		 */
		return nodesArr.toString();
	}
}
