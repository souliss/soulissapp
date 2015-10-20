package it.angelic.soulissclient.net;

import android.content.ContentResolver;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical5nCurrentVoltagePowerSensor;

public class NetUtils {

    public static InetAddress getInetLocalIpAddress() throws SocketException {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                //Log.d(Constants.TAG, "ip1--:" + inetAddress);
                //System.out.println("ip2--:" + inetAddress.getHostAddress());

                // for getting IPV4 format
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    // return inetAddress.getHostAddress().toString();
                    return inetAddress;
                }
            }
        }
        return null;
    }

    public static String getLocalIpAddress() {
        try {

            return getInetLocalIpAddress().getHostAddress();
            //System.out.println("ip---::" + ip);
            // return inetAddress.getHostAddress().toString();

        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    public static InetAddress getDeviceSubnetMask(Context ctx) {
        WifiManager wifii = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();
        //A volte non funziona e torna 0.
        if (d.netmask == 0) {
            InetAddress localHost = null;
            try {
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(NetUtils.intToInet(d.ipAddress));
                //get(1) e` IP4
                byte[] subDario = subnetLenghtToSubnetAddressDario(networkInterface.getInterfaceAddresses().get(1).getNetworkPrefixLength());
                Log.w(Constants.TAG, "Emergency subnet recovery from IP Address mask lenght:" + networkInterface.getInterfaceAddresses().get(1).getNetworkPrefixLength());
                return Inet4Address.getByAddress(subDario);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return intToInet(d.netmask);
    }


    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public static String getDeviceSubnetMaskString(Context ctx) {
        byte[] bytes = getDeviceSubnetMask(ctx).getAddress();
        NetUtils.reverse(bytes);
        InetAddress address;
        try {
            address = InetAddress.getByAddress(bytes);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            Log.e(it.angelic.soulissclient.Constants.TAG, e.toString());
            return null;
        }
    }

    public static int getDeviceGateway(Context ctx) {
        WifiManager wifii = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();

        return d.gateway;
    }

    public static String getDeviceGatewayString(Context ctx) {
        byte[] bytes = BigInteger.valueOf(getDeviceGateway(ctx)).toByteArray();
        NetUtils.reverse(bytes);
        InetAddress address;
        try {
            address = InetAddress.getByAddress(bytes);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            Log.e("getDeviceGateway: ", e.toString());
            return null;
        }
    }

    /**
     * Se la and bit a bit del indirizzo IP da verificare con l'inversa della
     * subnet mask non ha almeno un byte diverso da zero, l'indirizzo IP non può
     * essere utilizzato.
     *
     * @param toverify
     */
    public static boolean belongsToNode(InetAddress toverify, InetAddress subnetMask) {
        // int sub = subnetMask.getAddress();//Constants.getSubnet(context);
        // verificare inversione
        // InetAddress subnet = intToInet(sub);
        Log.d(Constants.Net.TAG, "testing belongsToNode subnetMask[]:" + subnetMask.getHostName() + " toverify[]:" + toverify.getHostName());
        byte[] subnetAddr = subnetMask.getAddress();
        byte[] actual = toverify.getAddress();
        for (int i = 0; i < subnetAddr.length; i++) {
            if ((subnetAddr[i] & ~actual[i]) != 0)
                return true;
        }
        return false;
    }

    /**
     * @param subnet
     * @return
     * @author Dario di Maio
     */
    private static byte[] subnetLenghtToSubnetAddressDario(int subnet) {
        int low_bit = 32 - subnet;
        byte[] submask = new byte[4];

        for (int i = 0; i < 8; i++) {
            if (i >= low_bit) submask[3] += Math.pow(2, i);
            if (i + 8 >= low_bit) submask[2] += Math.pow(2, i);
            if (i + 16 >= low_bit) submask[1] += Math.pow(2, i);
            if (i + 24 >= low_bit) submask[0] += Math.pow(2, i);
        }
        return submask;
    }

    /**
     * Verifica della subnet : Un indirizzo IP appartiene alla propria subnet
     * se, fatta la and bit a bit dell'indirizzo IP da verificare con la
     * subnetmask il risultato ottenuto è uguale alla and bit a bit del proprio
     * indirizzo IP con la subnet mask.
     *
     * @param toverify
     */
    public static boolean belongsToSameSubnet(InetAddress toverify, InetAddress subnetMask, InetAddress localH) {
        // int sub = Constants.getSubnet(context);

        // byte[] bytes = BigInteger.valueOf(sub).toByteArray();
        // InetAddress subnet = intToInet(sub);
        Log.d(Constants.Net.TAG, "testing subnet[]:" + subnetMask.getHostName() + " toverify[]:" + toverify.getHostName() + " localH[]:" + localH.getHostName());
        byte[] subnetAddr = subnetMask.getAddress();
        byte[] actual = toverify.getAddress();
        byte[] local = localH.getAddress();
        for (int i = 0; i < subnetAddr.length; i++) {

            if ((subnetAddr[i] & actual[i]) != (subnetAddr[i] & local[i]))
                return false;
        }
        return true;

    }

    public static InetAddress extractTargetAddress(ArrayList<Short> mac) throws UnknownHostException {
        Short[] parsed = mac.subList(5, 9).toArray(new Short[4]);
        byte[] good = new byte[4];
        // [56, 5, 0, 0, 4, 192, 168, 0, 17]
        for (int i = 0; i < good.length; i++) {
            good[i] = parsed[i].byteValue();
        }
        final InetAddress checkIPt = InetAddress.getByAddress(good);
        return checkIPt;
    }

    public static byte byteOfInt(int value, int which) {
        int shift = which * 8;
        return (byte) (value >> shift);
    }

    public static InetAddress intToInet(int value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = byteOfInt(value, i);
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // This only happens if the byte array has a bad length
            return null;
        }
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(Constants.Net.TAG, "There was an IO error", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String openHTMLString(Context context, int id) {
        InputStream is = context.getResources().openRawResource(id);

        return convertStreamToString(is);
    }

    public static String openHTMLStringfromURI(Context context, String id) throws FileNotFoundException {
        ContentResolver cr = context.getContentResolver();
        Log.d(Constants.Net.TAG, "fileUriString = " + id);
        Uri tempuri = Uri.parse(id);
        InputStream is = cr.openInputStream(tempuri);

        return convertStreamToString(is);
    }

    public static JSONObject getJSONSoulissDevice(SoulissTypical soulissTypical) {
        JSONObject objecttyp = new JSONObject();
        try {
            objecttyp.put("typ", Integer.toHexString(soulissTypical.getTypicalDTO().getTypical()));
            objecttyp.put("slo", soulissTypical.getTypicalDTO().getSlot());

            if (soulissTypical.getClass() == SoulissTypical5nCurrentVoltagePowerSensor.class) {
                // se si tratta del tipico per la misurazione dei consumi allora
                // per la restituzione del valore chiamo un metodo differente
                objecttyp.put("val", ((SoulissTypical5nCurrentVoltagePowerSensor) soulissTypical).getOutputFloat());
            } else {
                objecttyp.put("val", soulissTypical.getOutput());
            }

            objecttyp.put("ddesc", soulissTypical.getNiceName());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return objecttyp;
    }

    public static JSONObject getJSONSoulissLiveData(SoulissTypical soulissTypical) {
        JSONObject objecttyp = new JSONObject();
        try {
            objecttyp.put("typ", Integer.toHexString(soulissTypical.getTypicalDTO().getTypical()));

            if (soulissTypical.getClass() == SoulissTypical5nCurrentVoltagePowerSensor.class) {
                // se si tratta del tipico per la misurazione dei consumi allora
                // per la restituzione del valore chiamo un metodo differente
                objecttyp.put("val", ((SoulissTypical5nCurrentVoltagePowerSensor) soulissTypical).getOutputFloat());
            } else {
                objecttyp.put("val", soulissTypical.getOutput());
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return objecttyp;
    }

}
