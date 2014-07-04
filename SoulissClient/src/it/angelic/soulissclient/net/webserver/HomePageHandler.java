package it.angelic.soulissclient.net.webserver;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.net.NetUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class HomePageHandler implements HttpRequestHandler {
	private Context context = null;
	final String contentType = "text/html; charset=UTF-8";
	private SharedPreferences pref;
	private SoulissPreferenceHelper opzioni;

	public HomePageHandler(Context context) {
		this.context = context;
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		opzioni = new SoulissPreferenceHelper(context);
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException,
			IOException {
		// LOGIN STUFF
		try {
			Zozzariello.doLogin(request, response, httpContext, pref);
		} catch (AuthenticationException e) {
			if ("".compareTo(pref.getString("webUser", "")) == 0) {
				// user disabilitata nelle opzioni
			} else {
				// non si passa
				response.addHeader("WWW-Authenticate", "Basic");
				response.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
				return;
			}
		}

		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			public void writeTo(final OutputStream outstream) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
				String resp;
				try {
					resp = NetUtils.openHTMLStringfromURI(context, opzioni.getChosenHtmlRootfile());
				} catch (Exception e) {
					resp = NetUtils.openHTMLString(context, R.raw.json_manual);
					Log.e(Constants.TAG, e.getMessage());
				}
				writer.write(resp);
				writer.flush();
			}
		});

		((EntityTemplate) entity).setContentType(contentType);

		response.setEntity(entity);

	}

}
