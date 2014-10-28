package it.angelic.soulissclient.net;

import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Looper;
import android.util.Log;

/**
 * Apre una porta sul 23000 e si mette in ascolto per le risposte.
 * 
 * Come farlo "a due teste"? Ne vale la pena?
 * 
 * @author Ale
 * 
 */
public class UDPRunnable implements Runnable {

	// implements Runnable so it can be created as a new thread
	private static final String TAG = "Souliss:UDP";
	private DatagramSocket socket;
	private SoulissPreferenceHelper opzioni;
	// private Context context;
	private ThreadPoolExecutor tpe;

	final int maxThreads = 8;

	public UDPRunnable(SoulissPreferenceHelper opzioni) {
		super();
		this.opzioni = opzioni;
		// this.context = ctx;
		tpe = new ThreadPoolExecutor(
				maxThreads/2, // core thread pool size
				maxThreads, // maximum thread pool size
				53, // time to wait before resizing pool
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(maxThreads, true),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	public void run() {
		// Souliss listens on port 23000
		Looper.prepare();
		// lifecycle
		
		//final UDPSoulissDecoder decoder = new UDPSoulissDecoder(opzioni, SoulissClient.getAppContext());
		
		while (true) {
			try {
				// InetAddress serverAddr = InetAddress.getByName(SOULISSIP);
				DatagramChannel channel = DatagramChannel.open();
				socket = channel.socket();

				// socket = new DatagramSocket();
				socket.setReuseAddress(true);
				socket.setBroadcast(true);
				// port to receive souliss board data
				InetSocketAddress sa = new InetSocketAddress(Constants.SERVERPORT);
				socket.bind(sa);

				// create a buffer to copy packet contents into
				byte[] buf = new byte[200];
				// create a packet to receive
				final DatagramPacket packet = new DatagramPacket(buf, buf.length);
				int to = opzioni.getDataServiceIntervalMsec();
				Log.d(TAG, "***Waiting on packet, timeout=" + to);
				socket.setSoTimeout(to);
				// wait to receive the packet
				socket.receive(packet);
				// spawn a decoder and go on
				tpe.execute(new Runnable() {
					@Override
					public void run() {
						UDPSoulissDecoder decoder = new UDPSoulissDecoder(opzioni, SoulissClient.getAppContext());
						Log.d("UDP", "***Created decoder:" + decoder.toString());
						decoder.decodeVNetDatagram(packet);
					}
				});
				Log.d(TAG, "***ThreadPool, active=" + tpe.getActiveCount()+", completed:"+tpe.getCompletedTaskCount()+", poolsize:"+tpe.getPoolSize());
				
				socket.close();

			} catch (BindException e) {
				Log.e(TAG, "***UDP Port busy, Souliss already listening? " + e.getMessage());
				e.printStackTrace();
				try {
					//Thread.sleep(opzioni.getDataServiceIntervalMsec());
					socket.close();
				} catch (Exception e1) {
					Log.e(TAG, "***UDP close failed" + e1.toString());
				}
			} catch (SocketTimeoutException e2) {
				Log.w(TAG, "***UDP SocketTimeoutException close!" + e2);
				socket.close();
			} catch (Exception ee) {
				Log.e(TAG, "***UDP unhandled!" + ee.getMessage());
			}
		}
	}
}
