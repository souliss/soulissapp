package it.angelic.soulissclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

public class VoiceCommandActivityNoDisplay extends Activity {


    public static void interpretCommand(final Context context, @NonNull final String yesMan) {
        final StringBuilder comandToSend = new StringBuilder();
        final SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();

        if (opzioni.getCachedAddress() == null) {
            context.startService(new Intent(context, SoulissDataService.class));
            Log.w(Constants.TAG, "Started Emrg service");
        }

        //capisci scena, eseguila e ciao
        SoulissDBHelper db = new SoulissDBHelper(context);
        SoulissDBHelper.open();
        if (yesMan.toLowerCase().contains("ping")) {

            opzioni.setBestAddress();
            Log.i(Constants.TAG, "Voice PING Command SENT");

            return;
        }
        for (SoulissScene scenario : db.getScenes(context)) {
            if (yesMan.toLowerCase().contains(scenario.getName().toLowerCase())) {
                Log.w(Constants.TAG, "Voice activated Scenario:!! :" + scenario.getName());
                Toast.makeText(context, scenario.getName() + " " + context.getString(R.string.command_sent), Toast.LENGTH_SHORT).show();
                scenario.execute();
                return;
            }
        }
        if (isContainedInArray(yesMan, context.getResources().getStringArray(R.array.TurnON_strarray))) {
            comandToSend.append("" + Constants.Typicals.Souliss_T1n_OnCmd);
        } else if (isContainedInArray(yesMan, context.getResources().getStringArray(R.array.TurnOFF_strarray))) {
            comandToSend.append("" + Constants.Typicals.Souliss_T1n_OffCmd);
        } else if (isContainedInArray(yesMan, context.getResources().getStringArray(R.array.toggle_strarray))) {
            comandToSend.append("" + Constants.Typicals.Souliss_T1n_ToogleCmd);
        } else if (isContainedInArray(yesMan, context.getResources().getStringArray(R.array.open_strarray))) {
            comandToSend.append("" + Constants.Typicals.Souliss_T2n_OpenCmd);
        } else if (isContainedInArray(yesMan, context.getResources().getStringArray(R.array.close_strarray))) {
            comandToSend.append("" + Constants.Typicals.Souliss_T2n_CloseCmd);
        } else if (yesMan.toLowerCase().contains(context.getResources().getString(R.string.red).toLowerCase())) {
            comandToSend.append("" + Constants.Typicals.Souliss_T16_Red);
        } else if (yesMan.toLowerCase().contains(context.getResources().getString(R.string.green).toLowerCase())) {
            comandToSend.append("" + Constants.Typicals.Souliss_T16_Green);
        } else if (yesMan.toLowerCase().contains(context.getResources().getString(R.string.blue).toLowerCase())) {
            comandToSend.append("" + Constants.Typicals.Souliss_T16_Blue);
        }
        SoulissNode nodeMatch = null;
        boolean typMatch = false;
        boolean cmdSent = false;
        if (comandToSend.length() > 0) {//se c'e un comando
            Log.i(Constants.TAG, "Command recognized:" + yesMan);
            // SoulissDBHelper db = new SoulissDBHelper(AbstractStatusedFragmentActivity.this);
            List<SoulissNode> nodes = db.getAllNodes();
            for (final SoulissNode premio : nodes) {
                if (premio.getName() != null && yesMan.contains(premio.getName().toLowerCase()))
                    nodeMatch = premio;
            }
            for (final SoulissNode premio : nodes) {
                List<SoulissTypical> tippi = premio.getTypicals();
                for (final SoulissTypical treppio : tippi) {
                    if (treppio.getName() != null && yesMan.contains(treppio.getName().toLowerCase())) {
                        typMatch = true;
                        if (yesMan.contains(context.getString(R.string.all))) {
                            cmdSent = true;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    UDPHelper.issueMassiveCommand("" + treppio.getTypical(), opzioni, comandToSend.toString());
                                    Log.i(Constants.TAG, "Voice MASSIVE Command SENT: " + treppio.getName());
                                }
                            }).start();
                            break;//uno basta e avanza
                        } else if (nodeMatch == null || yesMan.contains(nodeMatch.getName().toLowerCase())) {
                            cmdSent = true;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    UDPHelper.issueSoulissCommand("" + treppio.getNodeId(), "" + treppio.getSlot(), opzioni, comandToSend.toString());
                                    Log.i(Constants.TAG, "Voice Command SENT: " + treppio.getName());
                                }
                            }).start();
                        } else {
                            Log.i(Constants.TAG, "Potential match found, but waiting for the right node");
                        }
                    }
                }
            }
            if (cmdSent) {
                Toast.makeText(context, yesMan + " " + context.getString(R.string.command_sent), Toast.LENGTH_SHORT).show();
            } else if (typMatch) {
                //Error, doveva mandare
                Log.e(Constants.TAG, "Potential match NOT found, has waited for the right node");
                Toast.makeText(context, context.getString(R.string.command_node_error) + ": " + yesMan, Toast.LENGTH_SHORT).show();
            } else if (nodeMatch != null) {
                Toast.makeText(context, context.getString(R.string.command_typ_error) + ": " + yesMan, Toast.LENGTH_SHORT).show();
            } else {//command found, but not device
                Toast.makeText(context, context.getString(R.string.command_typ_error) + ": " + yesMan, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, yesMan + " - " + context.getString(R.string.err_command_not_recognized), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Compare input words with a serie of synonims
     *
     * @param In       to be included in array
     * @param synonyms
     * @return
     */
    private static boolean isContainedInArray(String In, String[] synonyms) {
        for (String it : synonyms) {
            if (In.contains(it.toLowerCase()))
                return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        // startService(new Intent(this, SoulissDataService.class));
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(Constants.TAG, "onCreate WrapperActivity");

        ArrayList<String> thingsYouSaid = getIntent().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (thingsYouSaid != null && thingsYouSaid.size() > 0) {
            Log.w(Constants.TAG, "GOT VOICE COMMAND: " + thingsYouSaid.get(0));
            interpretCommand(this, thingsYouSaid.get(0).toLowerCase());
        } else {
            //try to read command elsewhere
        }
        finish();
    }
}