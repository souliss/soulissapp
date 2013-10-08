package it.angelic.soulissclient.net.webserver;

import it.angelic.soulissclient.helpers.Base64;
import it.angelic.soulissclient.net.Constants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Zozzariello extends Thread {
	private static final String SERVER_NAME = "Zozzariello";
	private static final String ALL_PATTERN = "*";
	private static final String PATTERN_MESSAGE = "/message*";
	private static final String  PATTERN_STRUCTURE  = "/structure*";
	private static final String  PATTERN_STATUS  = "/status*";
	private static final String  PATTERN_FORCE  = "/force*";

	private boolean isRunning = false;
	private Context context = null;
	private int serverPort = 0;
	private InetAddress serverAddress;

	private BasicHttpProcessor httpproc = null;
	private BasicHttpContext httpContext = null;
	private HttpService httpService = null;
	private HttpRequestHandlerRegistry registry = null;
	private NotificationManager notifyManager = null;
	
	/*
	HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
	    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
	        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
	        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
	                ClientContext.CREDS_PROVIDER);
	        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
	       request.getHeaders("WWW-Authenticate");
	       
	       
	       
	        if (authState.getAuthScheme() == null) {
	            AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
	            Credentials creds = credsProvider.getCredentials(authScope);
	            if (creds != null) {
	                authState.setAuthScheme(new BasicScheme());
	                authState.setCredentials(creds);
	            }
	        }
	    }    
	};
*/
	public Zozzariello(Context context) {
		super(SERVER_NAME);

		this.setContext(context);
		this.setNotifyManager(notifyManager);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		serverPort = Integer.parseInt(pref.getString(Constants.PREF_SERVER_PORT, "" + Constants.WEB_SERVER_PORT));
		httpproc = new BasicHttpProcessor();
		httpContext = new BasicHttpContext();

		httpproc.addInterceptor(new ResponseDate());
		httpproc.addInterceptor(new ResponseServer());
		httpproc.addInterceptor(new ResponseContent());
		httpproc.addInterceptor(new ResponseConnControl());
	//	httpproc.addInterceptor(preemptiveAuth);

		httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
		
		registry = new HttpRequestHandlerRegistry();

		registry.register(ALL_PATTERN, new HomePageHandler(context));
		registry.register(PATTERN_STRUCTURE, new JSONStructureHandler(context));
		registry.register(PATTERN_STATUS, new JSONStatusHandler(context));
		registry.register(PATTERN_MESSAGE, new MessageCommandHandler(context));
		registry.register(PATTERN_FORCE, new JSONForceHandler(context));

		httpService.setHandlerResolver(registry);
	}
	
	@Override
	public void run() {
		super.run();

		try {
			ServerSocket serverSocket = new ServerSocket(serverPort);
			serverAddress = serverSocket.getInetAddress();
			serverSocket.setReuseAddress(true);

			while (isRunning) {
				try {
					final Socket socket = serverSocket.accept();

					DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

					serverConnection.bind(socket, new BasicHttpParams());

					httpService.handleRequest(serverConnection, httpContext);

					serverConnection.shutdown();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (HttpException e) {
					e.printStackTrace();
				}
			}

			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void startThread() {
		isRunning = true;

		super.start();
	}

	public synchronized void stopThread() {
		isRunning = false;
	}

	public void setNotifyManager(NotificationManager notifyManager) {
		this.notifyManager = notifyManager;
	}

	public NotificationManager getNotifyManager() {
		return notifyManager;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public int getServerPort() {
		return serverPort;
	}
	protected static void doLogin(HttpRequest request, HttpResponse response, HttpContext httpContext, SharedPreferences pref)
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

}
