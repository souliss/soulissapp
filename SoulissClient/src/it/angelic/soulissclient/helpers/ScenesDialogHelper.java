package it.angelic.soulissclient.helpers;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.adapters.SceneCommandListAdapter;
import it.angelic.soulissclient.adapters.SceneListAdapter;
import it.angelic.soulissclient.db.SoulissCommandDTO;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.typicals.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Classe helper per i dialoghi riciclabili
 * 
 * 
 * 
 * 
 * */
public class ScenesDialogHelper {
	private static ProgressDialog progressDialog;

	/**
	 * Remove a command
	 * 
	 * @param ctx
	 *            used to invalidate views
	 * @param datasource
	 *            to store new value
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
							ArrayList<SoulissCommand> goer = datasource.getSceneCommands(cont, tgt.getId());
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
	 * @param ctx
	 *            used to invalidate views
	 * @param datasource
	 *            to store new value
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
	 * Sceglie nuova icona
	 * 
	 * @param context
	 * @param ctx
	 * @param list
	 * @param datasource
	 * @param opzioni
	 * @param toRename
	 *            puo essere nodo o Scenario
	 * @return
	 */
	public static AlertDialog.Builder addSceneCommandDialog(final Context context, final ListView list,
			final SoulissDBHelper datasource, final SoulissScene targetScene, final SoulissPreferenceHelper opzioni
			) {
		// prendo tipici dal DB
		List<SoulissNode> goer = datasource.getAllNodes();
		final SoulissNode[] nodiArray = new SoulissNode[goer.size() + 1];
		int q = 0;
		for (SoulissNode object : goer) {
			nodiArray[q++] = object;
		}
		SoulissNode fake = new SoulissNode((short) -1);//MASSIVO
		fake.setName(context.getString(R.string.allnodes));
		fake.setTypicals(datasource.getUniqueTypicals(fake));
		nodiArray[q] = fake;

		final AlertDialog.Builder alert2 = new AlertDialog.Builder(context);

		View dialoglayout = View.inflate(new ContextWrapper(context), R.layout.add_command_dialog, null);
		alert2.setTitle(context.getString(R.string.scene_add_command_to) + targetScene.toString());
		alert2.setIcon(android.R.drawable.ic_dialog_dialer);

		alert2.setView(dialoglayout);
		final Spinner outputNodeSpinner = (Spinner) dialoglayout.findViewById(R.id.spinner2);
		ArrayAdapter<SoulissNode> adapter = new ArrayAdapter<SoulissNode>(context,
				android.R.layout.simple_spinner_item, nodiArray);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		outputNodeSpinner.setAdapter(adapter);

		final Spinner outputTypicalSpinner = (Spinner) dialoglayout.findViewById(R.id.spinner3);
		final Spinner outputCommandSpinner = (Spinner) dialoglayout.findViewById(R.id.spinnerCommand);

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
				ArrayList<SoulissTypical> re = nodiArray[(int) outputNodeSpinner.getSelectedItemId()]
						.getActiveTypicals();
				if (re.size() > 0){ // node could be empty
					fillCommandSpinner(outputCommandSpinner, re.get(pos), context);
				}
				else{
					SoulissCommand[] strArray = new SoulissCommand[0];
					ArrayAdapter<SoulissCommand> adapter = new ArrayAdapter<SoulissCommand>(context,
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
						
						if (((SoulissNode) outputNodeSpinner.getSelectedItem()).getId() == -1) {// MASSIVE
							SoulissTypical model = (SoulissTypical) outputTypicalSpinner.getSelectedItem();
							if (model == null) {
								Toast.makeText(context, "Typical not selected", Toast.LENGTH_SHORT).show();
								return;
							}
							tull.getCommandDTO().setNodeId((short) Constants.MASSIVE_NODE_ID);
							tull.getCommandDTO().setType(Constants.COMMAND_MASSIVE);
							tull.getCommandDTO().setSlot(model.getTypicalDTO().getTypical());
						} else
							tull.getCommandDTO().setType(Constants.COMMAND_SINGLE);
						// lo metto dopo l'ultimo inserito
						tull.getCommandDTO().setInterval(targetScene.getCommandArray().size() + 1);
						tull.getCommandDTO().persistCommand(datasource);
						// setta comando singolo

						if (list != null) {
							// prendo comandi dal DB per questa scena
							ArrayList<SoulissCommand> goer = datasource.getSceneCommands(context, targetScene.getId());
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
	 * 
	 * @param tgt
	 * @param dec
	 */
	private static void setTypicalSpinner(Spinner tgt, SoulissNode ref, Context ctx) {
		try {

			SoulissTypical[] strArray = new SoulissTypical[ref.getActiveTypicals().size()];
			ref.getActiveTypicals().toArray(strArray);
			// set del contesto
			for (SoulissTypical soulissTypical : strArray) {
				soulissTypical.setCtx(ctx);
			}
			if (strArray.length == 0){//nodo vuoto
				SoulissTypical fake = new SoulissTypical(SoulissClient.getOpzioni());
				fake.setName(ctx.getString(R.string.node_empty));
				strArray = new SoulissTypical[1];
				strArray[0] = fake;
			}

			ArrayAdapter<SoulissTypical> adapter = new ArrayAdapter<SoulissTypical>(ctx, android.R.layout.simple_spinner_item, strArray);

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
	 * @param tgt
	 *            Spinner da riempire
	 * @param ref
	 *            tipico da cui ottenere i comandi
	 * 
	 */
	private static void fillCommandSpinner(Spinner tgt, SoulissTypical ref, Context ctx) {
		SoulissCommand[] strArray = new SoulissCommand[ref.getCommands(ctx).size()];
		ref.getCommands(ctx).toArray(strArray);
		// SoulissCommand[] etichette = new SoulissCommand[strArray.length];

		ArrayAdapter<SoulissCommand> adapter = new ArrayAdapter<SoulissCommand>(ctx,
				android.R.layout.simple_spinner_item, strArray);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tgt.setAdapter(adapter);
	}

	/**
	 * Dialogo esecuzione comandi
	 * 
	 * @param preferencesActivity
	 * @param ip
	 * @return
	 */
	public static void executeSceneDialog(final Activity preferencesActivity, final SoulissScene toExec,
			final SoulissPreferenceHelper opt) {

		progressDialog = ProgressDialog.show(preferencesActivity, "", "Executing Scene " + toExec.getNiceName());

		new Thread() {

			public void run() {
				Looper.prepare();
				// SoulissDBHelper datasource = new
				// SoulissDBHelper(preferencesActivity);
				ArrayList<SoulissCommand> gino = toExec.getCommandArray();
				// JSONHelper.setServer(ip);
				try {
					for (final SoulissCommand soulissCommand : gino) {
						SoulissCommandDTO dto = soulissCommand.getCommandDTO();
						if (soulissCommand.getCommandDTO().getType() == it.angelic.soulissclient.Constants.COMMAND_MASSIVE) {
							UDPHelper.issueMassiveCommand(String.valueOf(dto.getSlot()), opt,
									String.valueOf(dto.getCommand()));
						} else{
							String start = Long.toHexString(dto.getCommand());
							String[] laCosa = splitStringEvery(start,2);
							//String[] laCosa = start.split("(?<=\\G..)");
							
							for (int i = 0; i < laCosa.length; i++) {
								laCosa[i] = "0x"+laCosa[i];
							}
									
							UDPHelper.issueSoulissCommand(String.valueOf(dto.getNodeId()),
									String.valueOf(dto.getSlot()), opt, soulissCommand.getCommandDTO().getType(),
									//pura magia
									laCosa);
						}
						preferencesActivity.runOnUiThread(new Runnable() {
							public void run() {
								if (soulissCommand.getCommandDTO().getType() != Constants.COMMAND_MASSIVE)
									progressDialog.setMessage(preferencesActivity.getResources().getString(R.string.command_sent)+": " + soulissCommand.getCommandDTO().getInterval()
											+ " Issued to " + soulissCommand.getParentTypical().getNiceName());
								else
									progressDialog.setMessage("Massive command Issued to typicals "
											+ soulissCommand.getCommandDTO().getSlot());
							}
						});
						sleep(250);

					}

					preferencesActivity.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(preferencesActivity,
									"Execution of Scene " + toExec.toString() + " complete!", Toast.LENGTH_SHORT)
									.show();
						}
					});
				} catch (Exception e) {
					Log.e(ScenesDialogHelper.class.getName(), e.getMessage(), e);
				}

				/*
				 * if (!opt.isUdpMode()) { datasource.open(); final
				 * HashMap<Short, SoulissNode> names = JSONHelper.getAllNodes();
				 * 
				 * if (names == null || names.size() == 0) {
				 * preferencesActivity.runOnUiThread(new Runnable() { public
				 * void run() { Toast.makeText(preferencesActivity,
				 * "Souliss not reachable, network error",
				 * Toast.LENGTH_SHORT).show(); } }); } }
				 */
				progressDialog.dismiss();

			}

		}.start();

	}
	public static String[] splitStringEvery(String s, int interval) {
	    int arrayLength = (int) Math.ceil(((s.length() / (double)interval)));
	    String[] result = new String[arrayLength];

	    int j = 0;
	    int lastIndex = result.length - 1;
	    for (int i = 0; i < lastIndex; i++) {
	        result[i] = s.substring(j, j + interval);
	        j += interval;
	    } //Add the last bit
	    result[lastIndex] = s.substring(j);

	    return result;
	}
}
