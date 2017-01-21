package it.angelic.soulissclient.net;

import android.os.Looper;
import android.util.Log;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * Apre una porta sul 23000 e si mette in ascolto per le risposte.
 * usa un thread pool executor, anche se active count in realta non va mai a superare uno
 *
 * @author Ale
 */
public class UDPRunnable implements Runnable {

    // implements Runnable so it can be created as a new thread
    private static final String TAG = "Souliss:UDP";
    final int MAX_THREADS = 8;
    private SoulissPreferenceHelper opzioni;
    private DatagramSocket socket;
    // private Context context;
    private ThreadPoolExecutor threadExecutor;

    public UDPRunnable(SoulissPreferenceHelper opzioni) {
        super();
        this.opzioni = opzioni;
        Log.d("UDP", "***UDPRunnable Created");
        // this.context = ctx;
        threadExecutor = new ThreadPoolExecutor(
                MAX_THREADS / 2, // core thread pool size
                MAX_THREADS, // maximum thread pool size
                53, // time to wait before resizing pool
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(MAX_THREADS, true),
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
                InetSocketAddress sa = new InetSocketAddress(Constants.Net.SERVERPORT);
                socket.bind(sa);

                // create a buffer to fileCopy packet contents into
                byte[] buf = new byte[200];
                // create a packet to receive
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                int to = opzioni.getDataServiceIntervalMsec();
                Log.d(TAG, "***Waiting on packet, timeout=" + to);
                socket.setSoTimeout(to);
                // wait to receive the packet
                socket.receive(packet);
                Log.d(TAG, "***Packet received, spawning decoder. Recvd bytes=" + packet.getLength());
                // spawn a decoder and go on
                threadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        UDPSoulissDecoder decoder = new UDPSoulissDecoder(opzioni, SoulissApp.getAppContext());
                        Log.d(TAG, "***Created decoder:" + decoder.toString());
                        decoder.decodeVNetDatagram(packet);

                    }
                });
                Log.d(TAG, "***ThreadPool, active=" + threadExecutor.getActiveCount() + ", completed:" + threadExecutor.getCompletedTaskCount() + ", poolsize:" + threadExecutor.getPoolSize());
                socket.close();

            } catch (BindException e) {
                Log.e(TAG, "***UDP Port busy, Souliss already listening? " + e.getMessage());
                e.printStackTrace();
                try {
                    //Thread.sleep(opzioni.getDataServiceIntervalMsec());
                    socket.close();
                } catch (Exception e1) {
                    Log.e(TAG, "***UDP socket close failed: " + e1.getMessage());
                }
            } catch (SocketTimeoutException e2) {
                Log.w(TAG, "***UDP SocketTimeoutException close!" + e2);
                socket.close();
            } catch (ClosedByInterruptException xc) {
                xc.printStackTrace();
                Log.e(TAG, "***UDP runnable interrupted!");
                socket.close();
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ee) {
                ee.printStackTrace();
                Log.e(TAG, "***UDP unhandled error!" + ee.getMessage() + " of class " + ee.getClass());
                socket.close();
                Thread.currentThread().interrupt();
            }
        }
    }


}
