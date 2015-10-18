package it.angelic.soulissclient.net;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.SoulissWidget;
import it.angelic.soulissclient.T4nFragWrapper;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBLowHelper;
import it.angelic.soulissclient.db.SoulissTypicalDTO;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTrigger;
import it.angelic.soulissclient.model.SoulissTypical;

import static it.angelic.soulissclient.Constants.Typicals.Souliss_T41_Antitheft_Main;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T4n_InAlarm;
import static junit.framework.Assert.assertEquals;

/**
 * Classe per il decode dei pacchetti nativi souliss
 * <p/>
 * This class decodes incoming Souliss packets, starting from decodevNet
 *
 * @author Ale
 */
public class UDPSoulissDecoder {

    SoulissPreferenceHelper opzioni;
    private SoulissDBLowHelper database;
    private SharedPreferences soulissSharedPreference;
    private Context context;
    private InetAddress localHost;

    public UDPSoulissDecoder(SoulissPreferenceHelper opts, Context ctx) {
        this.opzioni = opts;
        this.context = ctx;
        database = new SoulissDBLowHelper(ctx);
        soulissSharedPreference = opts.getContx().getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
        SoulissDBHelper.open();
        try {
            localHost = NetUtils.getInetLocalIpAddress();
        } catch (SocketException e) {
            Log.e(Constants.Net.TAG, "CANT GET LOCALADDR");
        }
    }

    /**
     * processa il pacchetto UDP ricevuto e agisce di condeguenza
     *
     * @param packet incoming datagram
     */
    public void decodeVNetDatagram(DatagramPacket packet) {
        int checklen = packet.getLength();
        // Log.d(Constants.TAG, "** Packet received");
        ArrayList<Short> mac = new ArrayList<>();
        for (int ig = 7; ig < checklen; ig++) {
            mac.add((short) (packet.getData()[ig] & 0xFF));
        }

        // 0xf 0xe 0x17 0x64 0x1 0x11 0x0 0x18 0x0 0x0 0x0 0x3 0xa 0x8 0xa
        // il primo byte dev essere la dimensione
        if (checklen != packet.getData()[0]) {
            StringBuilder dump = new StringBuilder();
            for (int ig = 0; ig < checklen; ig++) {
                // 0xFF & buf[index]
                dump.append("0x").append(Long.toHexString(0xFF & packet.getData()[ig])).append(" ");
                // dump.append(":"+packet.getData()[ig]);
            }
            Log.e(Constants.Net.TAG, "**WRONG PACKET SIZE: " + packet.getData()[0] + "bytes\n" + "Actual size: " + checklen
                    + "\n" + dump.toString());
        } else {
            decodeMacaco(mac);
        }

		/*
         * DEBUG PACCHETTO StringBuilder dump = new StringBuilder(); for (int ig
		 * = 0; ig < checklen; ig++) { // 0xFF & buf[index] dump.append("0x" +
		 * Long.toHexString(0xFF & packet.getData()[ig]) + " "); //
		 * dump.append(":"+packet.getData()[ig]); } Log.d(Constants.TAG, "***" +
		 * dump.toString());
		 */
        // Qualcosa ho ricevuto, invia broadcast
        Intent i = new Intent();
        i.putExtra("MACACO", mac);
        i.setAction(Constants.Net.CUSTOM_INTENT_SOULISS_RAWDATA);
        opzioni.getContx().sendBroadcast(i);
        // resetta backoff irraggiungibilit�
        opzioni.resetBackOff();
        //se era irraggiungibile, pinga
        if (!opzioni.isSoulissReachable())
            opzioni.setBestAddress();

    }

    /**
     * Decodes lower level MaCaCo packet
     *
     * @param macacoPck
     */
    private void decodeMacaco(ArrayList<Short> macacoPck) {
        int functionalCode = macacoPck.get(0);
        // int putIn = mac.get(1);
        // PUTIN :) 2 byte
        // STARTOFFSET 1 byte
        // NUMBEROF 1 byte
        int startOffset = macacoPck.get(3);
        int numberOf = macacoPck.get(4);
        Log.d(Constants.Net.TAG, "** Macaco IN: Start Offset:" + startOffset + ", Number of " + numberOf);
        switch (functionalCode) {
            case Constants.Net.Souliss_UDP_function_subscribe_data:
                Log.d(Constants.Net.TAG, "** Subscription answer");
                decodeStateRequest(macacoPck);
                break;
            case Constants.Net.Souliss_UDP_function_poll_resp:
                Log.d(Constants.Net.TAG, "** Poll answer");
                decodeStateRequest(macacoPck);
                processTriggers();
                processWidgets();
                break;
            case Constants.Net.Souliss_UDP_function_ping_resp:
                // assertEquals(mac.size(), 8);
                Log.d(Constants.Net.TAG, "** Ping response bytes " + macacoPck.size());
                decodePing(macacoPck);
                break;
            case Constants.Net.Souliss_UDP_function_ping_bcast_resp:
                // assertEquals(mac.size(), 8);
                Log.d(Constants.Net.TAG, "** Ping BROADCAST response bytes " + macacoPck.size());
                decodePing(macacoPck);
                break;
            case Constants.Net.Souliss_UDP_function_subscribe_resp:
                Log.d(Constants.Net.TAG, "** State request answer");
                decodeStateRequest(macacoPck);
                processTriggers();
                processWidgets();
                break;
            case Constants.Net.Souliss_UDP_function_typreq_resp:// Answer for assigned
                // typical logic
                Log.d(Constants.Net.TAG, "** TypReq answer");
                decodeTypRequest(macacoPck);
                break;
            case Constants.Net.Souliss_UDP_function_health_resp:// Answer nodes healty
                Log.d(Constants.Net.TAG, "** Health answer");
                decodeHealthRequest(macacoPck);
                break;
            case Constants.Net.Souliss_UDP_function_db_struct_resp:// Answer nodes
                assertEquals(macacoPck.size(), 9); // healty
                Log.w(Constants.Net.TAG, "** DB Structure answer");
                decodeDBStructRequest(macacoPck);
                break;
            case 0x83:
                Log.e(Constants.Net.TAG, "** (Functional code not supported)");
                break;
            case 0x84:
                Log.e(Constants.Net.TAG, "** (Data out of range)");
                break;
            case 0x85:
                Log.e(Constants.Net.TAG, "** (Subscription refused)");
                break;
            default:
                Log.e(Constants.Net.TAG, "** Unknown functional code: " + functionalCode);
                break;
        }

    }

    /**
     * Si fa dare gli ID di eventuali widgets e li aggiorna
     * tramite sendBroadcast()
     */
    private void processWidgets() {
        try {
            int ids[] = AppWidgetManager.getInstance(SoulissApp.getAppContext()).getAppWidgetIds(new ComponentName(SoulissApp.getAppContext(), SoulissWidget.class));
            if (ids.length > 0) {
                Intent intent = new Intent(SoulissApp.getAppContext(), SoulissWidget.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
                // since it seems the onUpdate() is only fired on that:
                // int[] ids = {widgetId};
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                SoulissApp.getAppContext().sendBroadcast(intent);
            }
        } catch (Exception we) {
            Log.e(Constants.Net.TAG, "can't update widgets: " + we);
        }
    }

    private void processTriggers() {
        try {

            List<SoulissNode> ref = database.getAllNodes();
            List<SoulissTrigger> triggers = database.getAllTriggers(context);
            Log.i(Constants.Net.TAG, "checked triggers: " + triggers.size());
            // logThings(refreshedNodes);

            Map<Short, SoulissNode> refreshedNodes = new HashMap<>();

			/* Check antifurto */
            for (SoulissNode soulissNode : ref) {
                refreshedNodes.put(soulissNode.getId(), soulissNode);
                if (opzioni.isAntitheftPresent() && opzioni.isAntitheftNotify()) {
                    for (SoulissTypical ty : soulissNode.getTypicals()) {
                        // check Antitheft
                        if (ty.getTypicalDTO().getTypical() == Souliss_T41_Antitheft_Main
                                && ty.getTypicalDTO().getOutput() == Souliss_T4n_InAlarm) {
                            sendAntiTheftNotification(context, context.getString(R.string.antitheft_notify),
                                    context.getString(R.string.antitheft_notify_desc), R.drawable.shield, ty);
                            break;
                        }
                    }
                }
            }
            for (SoulissTrigger soulissTrigger : triggers) {
                SoulissTypical source = refreshedNodes.get(soulissTrigger.getInputNodeId()).getTypical(soulissTrigger.getInputSlot());
                //SoulissCommand command = new SoulissCommand(soulissTrigger.getCommandDto(),source);
                // SoulissTriggerDTO src = soulissTrigger.getTriggerDto();

                // SoulissTypical target =
                // refreshedNodes.get(command.getNodeId()).getTypical(command.getSlot());
                Calendar now = Calendar.getInstance();
                if (!soulissTrigger.getTriggerDto().isActivated()) {
                    // Descrizione programma
                    StringBuilder info = new StringBuilder(soulissTrigger.toString());
                    info.append(" slot ").append(soulissTrigger.getSlot());
                    if ("".compareTo(source.getNiceName()) != 0)
                        info.append(" (")
                                .append(source.getNiceName())
                                .append(")");
                    info.append(" on " ).append(source.getParentNode().getNiceName());

                    String op = soulissTrigger.getOp();
                    if (">".compareTo(op) == 0 && source.getTypicalDTO().getOutput() > soulissTrigger.getThreshVal()) {
                        Log.w(Constants.Net.TAG, "TRIGGERING COMMAND " + soulissTrigger.toString());
                        soulissTrigger.getTriggerDto().setActive(true);
                        soulissTrigger.execute();
                        soulissTrigger.getCommandDTO().setExecutedTime(now);
                        soulissTrigger.persist(database);
                        SoulissDataService.sendProgramNotification(context, SoulissApp.getAppContext().getResources().getString(R.string.programs_trigger_executed), info.toString(),
                                R.drawable.lighthouse, soulissTrigger);
                    } else if ("<".compareTo(op) == 0 && source.getTypicalDTO().getOutput() < soulissTrigger.getThreshVal()) {
                        Log.w(Constants.Net.TAG, "TRIGGERING COMMAND " + soulissTrigger.toString());
                        soulissTrigger.getTriggerDto().setActive(true);
                        soulissTrigger.execute();
                        soulissTrigger.getCommandDto().setExecutedTime(now);
                        soulissTrigger.persist(database);
                        SoulissDataService.sendProgramNotification(context, SoulissApp.getAppContext().getResources().getString(R.string.programs_trigger_executed), info.toString(),
                                R.drawable.lighthouse, soulissTrigger);
                    } else if ("=".compareTo(op) == 0 && source.getTypicalDTO().getOutput() == soulissTrigger.getThreshVal()) {
                        Log.w(Constants.Net.TAG, "TRIGGERING COMMAND " + soulissTrigger.toString());
                        soulissTrigger.execute();
                        soulissTrigger.getTriggerDto().setActive(true);
                        soulissTrigger.getCommandDto().setExecutedTime(now);
                        soulissTrigger.persist(database);
                        SoulissDataService.sendProgramNotification(context, SoulissApp.getAppContext().getResources().getString(R.string.programs_trigger_executed), info.toString(),
                                R.drawable.lighthouse, soulissTrigger);
                    }
                }
                // vedi se bisogna disattivare
                else {
                    String op = soulissTrigger.getOp();
                    if (">".compareTo(op) == 0 && source.getTypicalDTO().getOutput() <= soulissTrigger.getThreshVal()) {
                        Log.w(Constants.Net.TAG, "DEACTIVATE TRIGGER " + soulissTrigger.toString());
                        soulissTrigger.getTriggerDto().setActive(false);
                        soulissTrigger.persist(database);
                    } else if ("<".compareTo(op) == 0 && source.getTypicalDTO().getOutput() >= soulissTrigger.getThreshVal()) {
                        Log.w(Constants.Net.TAG, "DEACTIVATE TRIGGER " + soulissTrigger.toString());
                        soulissTrigger.getTriggerDto().setActive(false);
                        soulissTrigger.persist(database);
                    } else if ("=".compareTo(op) == 0 && source.getTypicalDTO().getOutput() != soulissTrigger.getThreshVal()) {
                        Log.w(Constants.Net.TAG, "DEACTIVATE TRIGGER " + soulissTrigger.toString());
                        soulissTrigger.getTriggerDto().setActive(false);
                        soulissTrigger.persist(database);
                    }
                }

            }
        } catch (IllegalStateException e) {
            Log.e(Constants.Net.TAG, "DB connection was closed, check trigger impossible");
        } catch (Exception e) {
            Log.e(Constants.Net.TAG, "check trigger impossible", e);
        }
    }

    /**
     * Alla ricezione di una risposta ping, aggiorna il cached address F e`
     * locale, se trovo B e` Remoto
     *
     * @param mac
     */
    private void decodePing(ArrayList<Short> mac) {
        // int nodes = mac.get(5);
        int putIn = mac.get(1);

        // se trovo F e` locale, se trovo B e` Remoto

        SharedPreferences.Editor editor = soulissSharedPreference.edit();
        // se ho gia` indirizzo privato che funziona (!= 0)
        boolean alreadyPrivate = (soulissSharedPreference.getString("cachedAddress", "").compareTo(
                opzioni.getPrefIPAddress()) == 0 && "".compareTo(opzioni.getPrefIPAddress()) != 0);
        if (putIn == 0xB && !alreadyPrivate) {// PUBBLICO
            opzioni.setCachedAddr(opzioni.getIPPreferencePublic());
            editor.putString("cachedAddress", opzioni.getIPPreferencePublic());
            Log.w(Constants.Net.TAG, "Set cached address: " + opzioni.getIPPreferencePublic());
        } else if (putIn == 0xF) {// PRIVATO
            opzioni.setCachedAddr(opzioni.getPrefIPAddress());
            editor.putString("cachedAddress", opzioni.getPrefIPAddress());
            Log.w(Constants.Net.TAG, "Set cached address: " + opzioni.getPrefIPAddress());
        } else if (putIn == 0x5) {// BROADCAST VA, USO QUELLA

            try {// sanity check
                final InetAddress toverify = NetUtils.extractTargetAddress(mac);
                Log.i(Constants.Net.TAG, "Parsed private IP: " + toverify.getHostAddress());
                /**
                 * deve essere determinato se l'indirizzo appartiene ad un nodo
                 * e se è all'interno della propria subnet. Se entrambe le
                 * verifiche hanno esito positivo, si può utilizzare tale
                 * indirizzo, altrimenti si continua ad usare broadcast.
                 */
                if (NetUtils.belongsToNode(toverify, NetUtils.intToInet(NetUtils.getDeviceSubnetMask(context)))
                        && NetUtils.belongsToSameSubnet(toverify, NetUtils.intToInet(NetUtils.getDeviceSubnetMask(context)),
                        localHost)) {
                    Log.d(Constants.Net.TAG, "BROADCAST detected, IP to verify: " + toverify);
                    Log.d(Constants.Net.TAG, "BROADCAST, subnet: " + NetUtils.intToInet(NetUtils.getDeviceSubnetMask(context)));
                    Log.d(Constants.Net.TAG, "BROADCAST, me: " + localHost);

                    Log.d(Constants.Net.TAG,
                            "BROADCAST, belongsToNode: "
                                    + NetUtils.belongsToNode(toverify, NetUtils.intToInet(NetUtils.getDeviceSubnetMask(context))));
                    Log.d(Constants.Net.TAG,
                            "BROADCAST, belongsToSameSubnet: "
                                    + NetUtils.belongsToSameSubnet(toverify,
                                    NetUtils.intToInet(NetUtils.getDeviceSubnetMask(context)), localHost));

                    opzioni.setCachedAddr(toverify.getHostAddress());
                    editor.putString("cachedAddress", toverify.getHostAddress());
                    if (!opzioni.isSoulissIpConfigured()) {// forse e` da
                        // togliere
                        Log.w(Constants.Net.TAG, "Auto-setting private IP: " + opzioni.getCachedAddress());
                        opzioni.setIPPreference(opzioni.getCachedAddress());
                    }
                } else {
                    throw new UnknownHostException("belongsToNode or belongsToSameSubnet = FALSE");
                }
            } catch (final Exception e) {
                Log.e(Constants.Net.TAG, "Error in address parsing, using BCAST address: " + e.getMessage(), e);
                opzioni.setCachedAddr(Constants.Net.BROADCASTADDR);
                editor.putString("cachedAddress", Constants.Net.BROADCASTADDR);

            }

        } else if (alreadyPrivate) {
            Log.w(Constants.Net.TAG,
                    "Local address already set. I'll NOT overwrite it: "
                            + soulissSharedPreference.getString("cachedAddress", ""));
        } else
            Log.e(Constants.Net.TAG, "Unknown putIn code: " + putIn);
        editor.commit();
    }

    /**
     * Sovrascrive la struttura I nodi e la struttura dei tipici e richiama
     * UDPHelper.typicalRequest(opzioni, nodes, 0);
     *
     * @param mac
     */
    private void decodeDBStructRequest(ArrayList<Short> mac) {
        // Threee static bytes
        assertEquals(4, (short) mac.get(4));
        final int nodes = mac.get(5);
        int maxnodes = mac.get(6);
        int maxTypicalXnode = mac.get(7);
        int maxrequests = mac.get(8);

        Log.i(Constants.Net.TAG, "DB Struct requested,nodes: " + nodes + " maxnodes: " + maxnodes + " maxrequests: "
                + maxrequests);
        SoulissDBHelper.open();
        database.createOrUpdateStructure(nodes, maxTypicalXnode);
        // Log.w(Constants.TAG, "Drop DB requested, response: " + mac);

        SharedPreferences.Editor editor = soulissSharedPreference.edit();
        // sistema configurato
        if (soulissSharedPreference.contains("numNodi"))
            editor.remove("numNodi");
        if (soulissSharedPreference.contains("TipiciXNodo"))
            editor.remove("TipiciXNodo");

        editor.putInt("numNodi", nodes);
        editor.putInt("TipiciXNodo", maxTypicalXnode);
        editor.apply();

        // FIXME centralizzare sta roba
        new Thread(new Runnable() {
            @Override
            public void run() {
                //ask for all typicals
                UDPHelper.typicalRequest(opzioni, nodes, 0);
                //first health req
                UDPHelper.healthRequest(opzioni, nodes, 0);
            }
        }).start();

    }

    /**
     * Definizione dei tipici
     *
     * @param mac
     */
    private void decodeTypRequest(ArrayList<Short> mac) {
        try {
            assertEquals(Constants.Net.Souliss_UDP_function_typreq_resp, (short) mac.get(0));
            SharedPreferences.Editor editor = soulissSharedPreference.edit();
            short tgtnode = mac.get(3);
            int numberOf = mac.get(4);
            int done = 0;
            // SoulissNode node = database.getSoulissNode(tgtnode);
            int typXnodo = soulissSharedPreference.getInt("TipiciXNodo", 1);
            Log.i(Constants.Net.TAG, "--DECODE MACACO TypRequest:" + tgtnode + " NUMOF:" + numberOf + " TYPICALSXNODE: "
                    + typXnodo);
            // creates Souliss nodes
            for (int j = 0; j < numberOf; j++) {
                if (mac.get(5 + j) != 0) {// create only not-empty typicals
                    SoulissTypicalDTO dto = new SoulissTypicalDTO();
                    dto.setTypical(mac.get(5 + j));
                    dto.setSlot(((short) (j % typXnodo)));// magia
                    dto.setNodeId((short) (j / typXnodo + tgtnode));
                    try {
                        dto.persist();
                        // conta solo i master
                        if (mac.get(5 + j) != it.angelic.soulissclient.Constants.Typicals.Souliss_T_related)
                            done++;
                        Log.d(Constants.Net.TAG, "---PERSISTED TYPICAL ON NODE:" + ((short) (j / typXnodo + tgtnode))
                                + " SLOT:" + ((short) (j % typXnodo)) + " TYP:" + (mac.get(5 + j)));
                    } catch (Exception ie) {
                        Log.e(Constants.Net.TAG, "---PERSIST ERROR:" + ie.getMessage() + " - " + ((short) (j / typXnodo + tgtnode))
                                + " SLOT:" + ((short) (j % typXnodo)) + " TYP:" + (mac.get(5 + j)));

                    }
                }
            }
            if (soulissSharedPreference.contains("numTipici"))
                editor.remove("numTipici");// unused
            editor.putInt("numTipici", database.countTypicals());
            editor.commit();
            Log.i(Constants.Net.TAG, "Refreshed " + numberOf + " typicals for node " + tgtnode);
        } catch (Exception uy) {
            Log.e(Constants.Net.TAG, "decodeTypRequest ERROR", uy);
        }
    }

    /**
     * puo giungere in seguito a state request oppure come subscription data
     * della publish. Semantica = a typical request. Aggiorna il DB solo se il
     * tipico esiste
     *
     * @param mac
     */
    private void decodeStateRequest(ArrayList<Short> mac) {
        try {
            List<SoulissNode> nodes = database.getAllNodes();
            Log.d(Constants.Net.TAG, "---Nodes on DB: " + nodes.size());
            int tgtnode = mac.get(3);
            int numberOf = mac.get(4);
            int typXnodo = soulissSharedPreference.getInt("TipiciXNodo", 8);
            Log.d(Constants.Net.TAG, "---DECODE MACACO OFFSET:" + tgtnode + " NUMOF:" + numberOf);
            SoulissTypicalDTO dto = new SoulissTypicalDTO();
            // refresh typicals
            for (short j = 0; j < numberOf; j++) {
                Log.v(Constants.Net.TAG, "---REFRESHING NODE:" + (j / typXnodo + tgtnode) + " SLOT:" + (j % typXnodo));
                try {
                    SoulissNode it = nodes.get(((int) j / typXnodo) + tgtnode);
                    SoulissTypical temp = it.getTypical((short) (j % typXnodo));
                    dto.setOutput(mac.get(5 + j));
                    dto.setSlot(((short) (j % typXnodo)));
                    dto.setNodeId((short) (j / typXnodo + tgtnode));
                    // sufficiente una refresh
                    dto.refresh(temp);
                } catch (NotFoundException e) {
                    // skipping unexistent typical");
                    //OK, può succedere
                } catch (Exception e) {
                    // FIXME nodes.get(((int) j / typXnodo) + tgtnode) è SBAGLIATO
                }
            }
            // Log.d(Constants.TAG, "Refreshed " + refreshed +
            // " typicals STATUS for node " + tgtnode);
        } catch (IllegalStateException e) {
            Log.e(Constants.Net.TAG, "DB connection was closed, impossible to finish");
            return;
        } catch (Exception uy) {
            Log.e(Constants.Net.TAG, "decodeStateRequest ERROR", uy);
        }

    }

    /**
     * Decodes a souliss nodes health request
     *
     * @param mac packet
     */
    private void decodeHealthRequest(ArrayList<Short> mac) {
        // Threee static bytes
        int tgtnode = mac.get(3);
        int numberOf = mac.get(4);

        ArrayList<Short> healths = new ArrayList<>();
        // build an array containing healths
        for (int i = 5; i < 5 + numberOf; i++) {
            healths.add(mac.get(i));
        }

        try {
            numberOf = database.refreshNodeHealths(healths, tgtnode);
            Log.d(Constants.Net.TAG, "Refreshed " + numberOf + " nodes' health");
        } catch (IllegalStateException e) {
            Log.e(Constants.Net.TAG, "DB connection closed! Can't update healths");
            return;
        }

    }

    /**
     * TODO Should be moved. Produces Android notification
     *
     * @param ctx
     * @param desc
     * @param longdesc
     * @param icon
     * @param ty
     */
    public static void sendAntiTheftNotification(Context ctx, String desc, String longdesc, int icon, SoulissTypical ty) {

        Intent notificationIntent = new Intent(ctx, T4nFragWrapper.class);
        notificationIntent.putExtra("TIPICO", ty);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);

        builder.setContentIntent(contentIntent).setSmallIcon(android.R.drawable.stat_sys_warning)
                .setLargeIcon(BitmapFactory.decodeResource(res, icon)).setTicker(desc)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(desc).setContentText(longdesc);
        Notification n = builder.build();
        nm.notify(664, n);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(ctx, notification);
            r.play();
        } catch (Exception e) {
            Log.e(Constants.Net.TAG, "Unable to play sounds:" + e.getMessage());
        }
    }

}
