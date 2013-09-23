package it.angelic.soulissclient.net.webserver;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.helpers.Base64;
import it.angelic.soulissclient.net.StaticUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

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

public class HomePageHandler implements HttpRequestHandler {
	private Context context = null;
	final String contentType = "text/html; charset=UTF-8";
	private SharedPreferences pref;

	public HomePageHandler(Context context) {
		this.context = context;
		pref = PreferenceManager.getDefaultSharedPreferences(context);

	}

	private void doLogin(HttpRequest request, HttpResponse response, HttpContext httpContext)
			throws AuthenticationException {
		if (request.getHeaders("Authorization").length == 0) {

			throw new AuthenticationException();
		} else {
			String auth = request.getHeaders("Authorization")[0].getValue();
			if (!auth.startsWith("Basic "))
				throw new AuthenticationException();
			auth = auth.substring(6);
			byte[] in = Base64.decode(auth, Base64.DEFAULT);
			try {
				String o = new String(in, "UTF-8");
				if (o.compareTo(pref.getString("webUser", "") + ":" + pref.getString("webPass", "")) != 0)
					throw new AuthenticationException();
				// Login Success!!
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new AuthenticationException();
			}
			// byte[] in = {'u','s','e','r',':'};
			byte[] out = Base64.encode(in, Base64.DEFAULT);
		}
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException,
			IOException {
		//LOGIN STUFF
		try {
			doLogin(request, response, httpContext);
		} catch (AuthenticationException e) {
			if ("".compareTo(pref.getString("webUser", "")) == 0) {
				// user disabilitata nelle opzioni
			} else {
				//non si passa
				response.addHeader("WWW-Authenticate", "Basic");
				response.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
				return;
			}
		}

		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			public void writeTo(final OutputStream outstream) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
				String resp = StaticUtils.openHTMLString(context, R.raw.json_manual);

				writer.write(resp);
				writer.flush();
			}
		});

		((EntityTemplate) entity).setContentType(contentType);

		response.setEntity(entity);

	}

}
