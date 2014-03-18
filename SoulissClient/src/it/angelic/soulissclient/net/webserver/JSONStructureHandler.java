package it.angelic.soulissclient.net.webserver;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.StaticUtils;

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

public class JSONStructureHandler implements HttpRequestHandler {

	private Context context = null;
	private SoulissDBHelper db;

	public JSONStructureHandler(Context context) {
		this.context = context;
		db = new SoulissDBHelper(context);
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException,
			IOException {
		int mellow = -1;// nodesid sempe positivi
		try {

			String uriString = request.getRequestLine().getUri();
			Uri uri = Uri.parse(uriString);
			String message = URLDecoder.decode(uri.getQueryParameter("id"), "UTF-8");
			mellow = Integer.parseInt(message);
			Log.i(it.angelic.soulissclient.Constants.TAG, "URI: " + uriString);
			Log.i(it.angelic.soulissclient.Constants.TAG, "Decoded ID: " + message);
		} catch (Exception e) {
			Log.e(it.angelic.soulissclient.Constants.TAG, e.getMessage(), e);
		}
		final int target = mellow;

		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			public void writeTo(final OutputStream outstream) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
				// Log.v(it.angelic.soulissclient.Constants.TAG,writeJSON(target));
				writer.write(writeJSON(target));
				writer.flush();
			}
		});
		// Log.d(it.angelic.soulissclient.Constants.TAG,
		// "Encoding:"+entity.getContentEncoding());
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		response.setEntity(entity);
	}

	/**
	 * Write the entire network structure if target < 0
	 * 
	 * @param target
	 * @return
	 */
	private String writeJSON(int target) {
		db.open();
		List<SoulissNode> nodes;
		// Se target < 0, allora metto tutto
		if (target < 0)
			nodes = db.getAllNodes();
		else {
			nodes = new ArrayList<SoulissNode>();
			nodes.add(db.getSoulissNode(target));
		}
		JSONObject glob = new JSONObject();
		JSONArray nodesArr = new JSONArray();
		for (SoulissNode soulissNode : nodes) {
			JSONObject object = new JSONObject();
			JSONArray typArr = new JSONArray();

			ArrayList<SoulissTypical> tipici = soulissNode.getActiveTypicals();
			for (SoulissTypical soulissTypical : tipici) {
				typArr.put(StaticUtils.getJSONSoulissDevice(soulissTypical));
			}

			try {
				object.put("slot", typArr);
				object.put("hlt", soulissNode.getHealth());
				object.put("ndesc", soulissNode.getNiceName());
			} catch (JSONException e) {
				Log.e(Constants.TAG, "Zozzariello ERROR:", e);
			}

			nodesArr.put(object);

		}
		try {
			glob.put("id", nodesArr);
		} catch (JSONException e) {
			Log.e(Constants.TAG, "Zozzariello ERROR:", e);
		}
		
		return glob.toString();
	}
}
