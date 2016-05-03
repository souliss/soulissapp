package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.adapters.SceneCommandListAdapter;
import it.angelic.soulissclient.adapters.SceneListAdapter;
import it.angelic.soulissclient.adapters.TagListAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Classe helper per i dialoghi riciclabili
 */
public class ScenesDialogHelper {
    private static ProgressDialog progressDialog;

    /**
     * Remove a command
     *
     * @param ctx        used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return //TODO rivedere parametri
     */
    public static void removeCommandDialog(final Context cont, final ListView ctx, final SoulissDBHelper datasource,
                                           final SoulissScene tgt, final SoulissCommand toRename, final SoulissPreferenceHelper opzioni) {

        // se lo scenario e` default ci sono solo 2 COMANDI !!!
        if (toRename.getCommandDTO().getSceneId() < 3 && toRename.getCommandDTO().getInterval() < 3) {
            Toast.makeText(cont, "Can't remove default commands", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(cont);
        alert.setTitle("Remove Command");
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setMessage("Are you sure you want to delete this command from " + tgt.toString() + "?");
        alert.setPositiveButton(cont.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        datasource.deleteCommand(toRename);
                        if (ctx != null) {
                            // prendo comandi dal DB
                            ArrayList<SoulissCommand> goer = datasource.getSceneCommands( tgt.getId());
                            SoulissCommand[] programsArray = new SoulissCommand[goer.size()];
                            programsArray = goer.toArray(programsArray);
                            tgt.setCommandArray(goer);
                            SceneCommandListAdapter progsAdapter = new SceneCommandListAdapter(cont, programsArray,
                                    opzioni);
                            // Adapter della lista
                            ctx.setAdapter(progsAdapter);
                            ctx.invalidateViews();
                        }
                    }
                });

        alert.setNegativeButton(cont.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        alert.show();
    }

    /**
     * Remove a Scene
     *
     * @param ctx        used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return //TODO rivedere parametri
     */
    public static void removeSceneDialog(final Context cont, final ListView ctx, final SoulissDBHelper datasource,
                                         final SoulissScene toRename, final SoulissPreferenceHelper opts) {

        if (toRename.getId() < 3) {
            Toast.makeText(cont, "Can't remove default scenes", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(cont);
        alert.setTitle("Remove Scene");
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setMessage("Are you sure you want to delete this scene ?");
        alert.setPositiveButton(cont.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        datasource.deleteScene(toRename);
                        if (ctx != null) {
                            // prendo comandi dal DB
                            LinkedList<SoulissScene> goer = datasource.getScenes(cont);
                            SoulissScene[] programsArray = new SoulissScene[goer.size()];
                            programsArray = goer.toArray(programsArray);
                            // targetScene.setCommandArray(goer);
                            SceneListAdapter progsAdapter = new SceneListAdapter(cont, programsArray, opts);
                            // Adapter della lista
                            ctx.setAdapter(progsAdapter);
                            ctx.invalidateViews();
                        }
                    }
                });

        alert.setNegativeButton(cont.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        alert.show();
    }

    /**
     * Remove a Scene
     *
     * @param ctx        used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return //TODO rivedere parametri
     */
    public static void removeTagDialog(final Context cont, final ListView ctx, final SoulissDBHelper datasource,
                                       final SoulissTag toRename, final SoulissPreferenceHelper opts) {
        Log.w(Constants.TAG, "Removing TAG:" + toRename.getNiceName() + " ID:" + toRename.getTagId());
        if (toRename.getTagId() < 2) {
            Toast.makeText(cont, "Can't remove default Favourite cuneyt.Tag", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(cont);
        alert.setTitle("Remove cuneyt.Tag");
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setMessage("Are you sure you want to delete this tag ?");
        alert.setPositiveButton(cont.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        datasource.deleteTag(toRename);
                        if (ctx != null) {
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(cont);
                            // prendo comandi dal DB
                            List<SoulissTag> goer = dbt.getTags(cont);
                            SoulissTag[] programsArray = new SoulissTag[goer.size()];
                            programsArray = goer.toArray(programsArray);
                            // targetScene.setCommandArray(goer);
                            TagListAdapter progsAdapter = new TagListAdapter(cont, programsArray, opts);
                            // Adapter della lista
                            ctx.setAdapter(progsAdapter);
                            ctx.invalidateViews();
                        }
                    }
                });

        alert.setNegativeButton(cont.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        alert.show();
    }

    /**
     * Sceglie nuova icona
     *
     * @param context
     * @param list
     * @param datasource
     * @param opzioni    puo essere nodo o Scenario
     * @return
     */
    public static AlertDialog.Builder addSceneCommandDialog(final Context context, final ListView list,
                                                            final SoulissDBHelper datasource, final SoulissScene targetScene, final SoulissPreferenceHelper opzioni) {
        // prendo tipici dal DB
        List<SoulissNode> goer = datasource.getAllNodes();
        final SoulissNode[] nodiArray = new SoulissNode[goer.size() + 1];
        int q = 0;
        for (SoulissNode object : goer) {
            nodiArray[q++] = object;
        }
        SoulissNode fake = new SoulissNode(Constants.MASSIVE_NODE_ID);// MASSIVO
        fake.setName(context.getString(R.string.allnodes));
        fake.setTypicals(datasource.getUniqueTypicals(fake));
        nodiArray[q] = fake;

        final AlertDialog.Builder alert2 = new AlertDialog.Builder(context);

        View dialoglayout = View.inflate(new ContextWrapper(context), R.layout.add_command_dialog, null);
        alert2.setTitle(context.getString(R.string.scene_add_command_to) + " " + targetScene.toString());
        alert2.setIcon(android.R.drawable.ic_dialog_dialer);

        alert2.setView(dialoglayout);
        final Spinner outputNodeSpinner = (Spinner) dialoglayout.findViewById(R.id.spinner2);
        ArrayAdapter<SoulissNode> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, nodiArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputNodeSpinner.setAdapter(adapter);

        final Spinner outputTypicalSpinner = (Spinner) dialoglayout.findViewById(R.id.spinner3);
        final Spinner outputCommandSpinner = (Spinner) dialoglayout.findViewById(R.id.spinnerCommand);
        final Spinner outputDelaySpinner = (Spinner) dialoglayout.findViewById(R.id.spinnerCommandDelay);

		/* Cambiando nodo, cambia i tipici */
        OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                setTypicalSpinner(outputTypicalSpinner, nodiArray[pos], context);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        outputNodeSpinner.setOnItemSelectedListener(lit);
        /* Cambiando tipico, cambia i comandi */
        OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                List<SoulissTypical> re = nodiArray[(int) outputNodeSpinner.getSelectedItemId()]
                        .getActiveTypicals();
                if (re.size() > 0) { // node could be empty
                    fillCommandSpinner(outputCommandSpinner, re.get(pos), context);
                } else {
                    SoulissCommand[] strArray = new SoulissCommand[0];
                    ArrayAdapter<SoulissCommand> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_item, strArray);
                    outputCommandSpinner.setAdapter(adapter);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        outputTypicalSpinner.setOnItemSelectedListener(lib);

        alert2.setPositiveButton(context.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Aggiungi comando
                        SoulissCommand tull = (SoulissCommand) outputCommandSpinner.getSelectedItem();
                        if (tull == null) {
                            Toast.makeText(context, "Command not selected", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // collega il comando alla scena
                        tull.getCommandDTO().setSceneId(targetScene.getId());

                        if (((SoulissNode) outputNodeSpinner.getSelectedItem()).getNodeId() == Constants.MASSIVE_NODE_ID) {// MASSIVE
                            SoulissTypical model = (SoulissTypical) outputTypicalSpinner.getSelectedItem();
                            if (model == null) {
                                Toast.makeText(context, "Typical not selected", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            tull.getCommandDTO().setNodeId(Constants.MASSIVE_NODE_ID);
                            //tull.getCommandDTO().setType(Constants.COMMAND_MASSIVE);
                            tull.getCommandDTO().setSlot(model.getTypicalDTO().getTypical());
                        } else
                            tull.getCommandDTO().setType(Constants.COMMAND_SINGLE);
                        // lo metto dopo l'ultimo inserito
                        int[] mefisto = context.getResources().getIntArray(R.array.delayIntervalValues);
                        tull.getCommandDTO().setInterval(mefisto[outputDelaySpinner.getSelectedItemPosition()]  );
                        Log.w(Constants.TAG,"Saving new command with delay:"+ context.getResources().getIntArray(R.array.delayIntervalValues)[outputDelaySpinner.getSelectedItemPosition()]  );
                        tull.getCommandDTO().persistCommand();

                        if (list != null) {//refresh
                            // prendo comandi dal DB per questa scena
                            ArrayList<SoulissCommand> goer = datasource.getSceneCommands(targetScene.getId());
                            SoulissCommand[] scenesArr = new SoulissCommand[goer.size()];
                            scenesArr = goer.toArray(scenesArr);
                            targetScene.setCommandArray(goer);
                            // TODO usare notifydschange e togliere new
                            // Adapter()
                            SceneCommandListAdapter progsAdapter = new SceneCommandListAdapter(context, scenesArr,
                                    opzioni);
                            list.setAdapter(progsAdapter);
                            list.invalidateViews();
                        }
                    }
                });

        alert2.setNegativeButton(context.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        return alert2;
    }

    /**
     * popola spinner tipici in base al nodo. per es. la V non ha il neutro
     */
    private static void setTypicalSpinner(Spinner tgt, SoulissNode ref, Context ctx) {
        try {

            SoulissTypical[] strArray = new SoulissTypical[ref.getActiveTypicals().size()];
            ref.getActiveTypicals().toArray(strArray);

            if (strArray.length == 0) {// nodo vuoto
                SoulissTypical fake = new SoulissTypical(SoulissApp.getOpzioni());
                fake.setName(ctx.getString(R.string.node_empty));
                strArray = new SoulissTypical[1];
                strArray[0] = fake;
            }

            ArrayAdapter<SoulissTypical> adapter = new ArrayAdapter<>(ctx,
                    android.R.layout.simple_spinner_item, strArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            tgt.setAdapter(adapter);

        } catch (Exception e) {
            Log.e("add DIA", "Errore in setTypicalSpinner:" + e.getMessage(), e);
        }
    }

    /**
     * popola spinner comandi in base al tipico. Mette nell'adapter i comandi
     * ottenuti da getCommands
     *
     * @param tgt Spinner da riempire
     * @param ref tipico da cui ottenere i comandi
     */
    private static void fillCommandSpinner(Spinner tgt, SoulissTypical ref, Context ctx) {
        ISoulissCommand[] strArray = new SoulissCommand[ref.getCommands(ctx).size()];
        ref.getCommands(ctx).toArray(strArray);
        // SoulissCommand[] etichette = new SoulissCommand[strArray.length];

        ArrayAdapter<ISoulissCommand> adapter = new ArrayAdapter<>(ctx,
                android.R.layout.simple_spinner_item, strArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tgt.setAdapter(adapter);
    }

    /**
     * Dialogo esecuzione comandi
     *
     * @param preferencesActivity
     * @return
     */
    public static void executeSceneDialog(final Activity preferencesActivity, final SoulissScene toExec ) {

        toExec.execute();
        preferencesActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(preferencesActivity,toExec.getName() + " " + preferencesActivity.getString(R.string.command_sent), Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }

    private static List<String> getParts(String string, int partitionSize) {
        List<String> parts = new ArrayList<>();
        int len = string.length();
        for (int i = 0; i < len; i += partitionSize) {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    public static String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        } // Add the last bit
        result[lastIndex] = s.substring(j);

        return result;
    }
}
