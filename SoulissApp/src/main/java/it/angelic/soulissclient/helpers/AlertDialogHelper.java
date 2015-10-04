package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.PreferencesActivity;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.adapters.NodesListAdapter;
import it.angelic.soulissclient.adapters.ProgramListAdapter;
import it.angelic.soulissclient.adapters.SceneListAdapter;
import it.angelic.soulissclient.adapters.SoulissIconAdapter;
import it.angelic.soulissclient.adapters.TagListAdapter;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.preferences.NetSettingsFragment;
import it.angelic.soulissclient.preferences.ServiceSettingsFragment;
import us.feras.ecogallery.EcoGallery;

import static it.angelic.soulissclient.Constants.TAG;
import static junit.framework.Assert.assertTrue;

/**
 * Classe helper per i dialoghi riciclabili
 */
public class AlertDialogHelper {
    // private static ProgressDialog progressDialog;

    /**
     * Mostra warning che il sistema non ha l'IP settato
     *
     * @param source
     * @return
     */
    public static AlertDialog.Builder sysNotInitedDialog(final Activity source) {
        AlertDialog.Builder alert = new AlertDialog.Builder(source);
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setTitle(source.getResources().getString(R.string.notconfigured));
        alert.setMessage(source.getResources().getString(R.string.dialog_notinited_ip));

        alert.setPositiveButton(source.getResources().getString(R.string.proceed),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final Intent preferencesActivity = new Intent(source.getBaseContext(),
                                PreferencesActivity.class);

                        preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, NetSettingsFragment.class.getName());
                        // preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
                        preferencesActivity.setAction("network_setup");
                        preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        //preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        source.startActivity(preferencesActivity);
                    }
                });
        alert.setNegativeButton(source.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        return alert;
    }

    public static void dbNotInitedDialog(final Context source) {
        final SoulissPreferenceHelper opts = new SoulissPreferenceHelper(source);
        AlertDialog.Builder alert = new AlertDialog.Builder(source);
        if (!opts.getDontShowAgain(source.getResources().getString(R.string.dialog_disabled_db))) {
            final CheckBox checkBox = new CheckBox(source);
            checkBox.setText(source.getResources().getString(R.string.dialog_dontshowagain));

            LinearLayout linearLayout = new LinearLayout(source);
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            lParams.setMargins(15,15,15,15);
            linearLayout.setLayoutParams(lParams);

            linearLayout.setOrientation(LinearLayout.VERTICAL);
            alert.setMessage(source.getResources().getString(R.string.dialog_notinited_db));
            linearLayout.addView(checkBox);
            alert.setView(linearLayout);

            alert.setIcon(android.R.drawable.ic_dialog_alert);
            alert.setTitle(source.getResources().getString(R.string.dialog_disabled_db));

            alert.setPositiveButton(source.getResources().getString(R.string.proceed),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (checkBox.isChecked()) {
                                opts.setDontShowAgain(source.getResources().getString(R.string.dialog_disabled_db),
                                        true);
                            }
                            final Intent preferencesActivity = new Intent(source,
                                    PreferencesActivity.class);
                            preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
                            preferencesActivity.setAction("db_setup");
                            preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            source.startActivity(preferencesActivity);
                        }
                    });
            alert.setNegativeButton(source.getResources().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (checkBox.isChecked()) {
                                opts.setDontShowAgain(source.getResources().getString(R.string.dialog_disabled_db),
                                        true);
                            }
                        }
                    });

            alert.show();
        }
    }

    public static void serviceNotActiveDialog(final Activity source) {
        final SoulissPreferenceHelper opts = SoulissApp.getOpzioni();
        AlertDialog.Builder alert = new AlertDialog.Builder(source);

        final CheckBox checkBox = new CheckBox(source);
        TextView textView = new TextView(source);
        if (!opts.getDontShowAgain(source.getResources().getString(R.string.dialog_disabled_service))) {
            checkBox.setText(source.getResources().getString(R.string.dialog_dontshowagain));
            alert.setMessage(source.getResources().getString(R.string.dialog_notinited_service));

            LinearLayout linearLayout = new LinearLayout(source);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(textView);
            linearLayout.addView(checkBox);
            alert.setView(linearLayout);

            alert.setIcon(android.R.drawable.ic_dialog_alert);
            alert.setTitle(source.getResources().getString(R.string.dialog_disabled_service));

            alert.setPositiveButton(source.getResources().getString(R.string.proceed),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (checkBox.isChecked()) {
                                opts.setDontShowAgain(
                                        source.getResources().getString(R.string.dialog_disabled_service), true);
                            }
                            final Intent preferencesActivity = new Intent(source.getBaseContext(),
                                    PreferencesActivity.class);
                            preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ServiceSettingsFragment.class.getName());
                            preferencesActivity.setAction("service_setup");
                            preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            source.startActivity(preferencesActivity);
                        }
                    });
            alert.setNegativeButton(source.getResources().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (checkBox.isChecked()) {
                                opts.setDontShowAgain(
                                        source.getResources().getString(R.string.dialog_disabled_service), true);
                            }
                        }
                    });

            alert.show();
        }
    }

    public static AlertDialog.Builder dropSoulissDBDialog(final Activity source, final SoulissDBHelper datasource) {
        AlertDialog.Builder alert = new AlertDialog.Builder(source);
        // AlertDialog.Builder alert;
        // alert = new AlertDialog.Builder(new ContextThemeWrapper(source,
        // R.style.AboutDialog));
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        final SharedPreferences soulissCust = source.getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
        alert.setTitle(source.getResources().getString(R.string.dialog_warn_db));
        alert.setMessage(source.getResources().getString(R.string.dialog_drop_db));

        alert.setPositiveButton(source.getResources().getString(R.string.proceed),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        datasource.close();
                        if (source.deleteDatabase(SoulissDB.DATABASE_NAME)) {
                            SharedPreferences.Editor editor = soulissCust.edit();
                            // tolgo db dalle prefs
                            if (soulissCust.contains("numNodi"))
                                editor.remove("numNodi");
                            if (soulissCust.contains("numTipici"))
                                editor.remove("numTipici");
                            editor.commit();
                            Log.w(TAG, "Souliss DB dropped");
                            // source.finish();
                            final Intent preferencesActivity = new Intent(source.getBaseContext(),
                                    PreferencesActivity.class);

                            preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
                            preferencesActivity.setAction("db_setup");
                            preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            source.startActivity(preferencesActivity);

                        } else {
                            Log.e(TAG, "Unable to DROP DB");
                            Toast.makeText(source, "Unable to DROP DB", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        alert.setNegativeButton(source.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        return alert;
    }

    /**
     * Remove a command
     *
     * @param ctx        used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return
     */
    public static AlertDialog.Builder removeCommandDialog(final Context cont, final ListView ctx,
                                                          final SoulissDBHelper datasource, final SoulissCommand toRename) {
        AlertDialog.Builder alert = new AlertDialog.Builder(cont);
        alert.setTitle(cont.getString(R.string.dialog_remove_title));
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setMessage(cont.getString(R.string.dialog_remove_cmd));
        alert.setPositiveButton(cont.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        SoulissDBHelper.open();
                        int res = datasource.deleteCommand(toRename);
                        Log.i(TAG, "SoulissCommand deletion returned: " + res);
                        if (ctx != null) {
                            // prendo comandi dal DB
                            LinkedList<SoulissCommand> goer = datasource.getUnexecutedCommands(cont);
                            SoulissCommand[] programsArray = new SoulissCommand[goer.size()];
                            programsArray = goer.toArray(programsArray);

                            ProgramListAdapter progsAdapter = new ProgramListAdapter(cont, programsArray, datasource
                                    .getTriggerMap(cont), new SoulissPreferenceHelper(cont.getApplicationContext()));
                            // Adapter della lista
                            ctx.setAdapter(progsAdapter);
                            ctx.invalidateViews();
                        }
                        datasource.close();
                    }
                });

        alert.setNegativeButton(cont.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        return alert;
    }

    /**
     * Rename a node
     *
     * @param listV      used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return
     */
    public static AlertDialog.Builder renameSoulissObjectDialog(final Context cont, final TextView tgt,
                                                                final ListView listV, final SoulissDBHelper datasource, final ISoulissObject toRename) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(cont);
        final SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(cont);
        assertTrue("chooseIconDialog: NOT instanceof", toRename instanceof SoulissNode
                || toRename instanceof SoulissScene || toRename instanceof SoulissTypical || toRename instanceof SoulissTag);
        alert.setIcon(android.R.drawable.ic_dialog_dialer);
        alert.setTitle(cont.getString(R.string.rename) + " " + toRename.getNiceName());

        // Set an EditText view to get user input
        final EditText input = new EditText(cont);
        alert.setView(input);
        input.setText(toRename.getNiceName());
        alert.setPositiveButton(cont.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        toRename.setName(value);
                        SoulissDBHelper.open();
                        if (toRename instanceof SoulissNode) {
                            datasource.createOrUpdateNode((SoulissNode) toRename);
                            if (listV != null) {
                                List<SoulissNode> goer = datasource.getAllNodes();
                                SoulissNode[] nodiArray = new SoulissNode[goer.size()];
                                nodiArray = goer.toArray(nodiArray);
                                NodesListAdapter nodesAdapter = new NodesListAdapter(cont, nodiArray, opzioni);
                                // Adapter della lista
                                listV.setAdapter(nodesAdapter);
                                listV.invalidateViews();
                            }

                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);
                            if (listV != null) {
                                LinkedList<SoulissScene> goer = datasource.getScenes(SoulissApp.getAppContext());
                                SoulissScene[] scenesArray = new SoulissScene[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                try {
                                    SceneListAdapter sa = (SceneListAdapter) listV.getAdapter();
                                    // SceneListAdapter progsAdapter = new
                                    // SceneListAdapter(cont, scenesArray,
                                    // opzioni);
                                    // Adapter della lista
                                    sa.setScenes(scenesArray);
                                    sa.notifyDataSetChanged();
                                    // listV.setAdapter(sa);
                                    listV.invalidateViews();
                                } catch (Exception e) {
                                    Log.w(Constants.TAG, "rename didn't find proper view to refresh");
                                }
                            }
                        } else if (toRename instanceof SoulissTag) {
                            if (((SoulissTag) toRename).getTagId() < 2) {
                                Toast.makeText(cont, cont.getString(R.string.nodeleteFav), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(cont);
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (listV != null) {
                                List<SoulissTag> goer = dbt.getTags(SoulissApp.getAppContext());
                                SoulissTag[] scenesArray = new SoulissTag[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                try {
                                    TagListAdapter sa = (TagListAdapter) listV.getAdapter();
                                    // SceneListAdapter progsAdapter = new
                                    // SceneListAdapter(cont, scenesArray,
                                    // opzioni);
                                    // Adapter della lista
                                    sa.setScenes(scenesArray);
                                    sa.notifyDataSetChanged();
                                    // listV.setAdapter(sa);
                                    listV.invalidateViews();
                                } catch (Exception e) {
                                    Log.w(Constants.TAG, "rename didn't find proper view to refresh");
                                }
                            }
                        } else {
                            if (listV != null) {
                                ((SoulissTypical) toRename).getTypicalDTO().persist();
                                TypicalsListAdapter ta = (TypicalsListAdapter) listV.getAdapter();
                                ta.notifyDataSetChanged();
                                listV.invalidateViews();
                            }
                        }
                        if (cont instanceof Activity && !(toRename instanceof SoulissTypical))
                            ((Activity) cont).setTitle(toRename.getNiceName());
                        if (tgt != null) {
                            tgt.setText(value);
                            tgt.setText(toRename.getNiceName());
                        }

                    }
                });

        alert.setNegativeButton(cont.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        return alert;
    }

    public static AlertDialog equalizerDialog(final Context context, @Nullable final TextView toUpdate) {
        final SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        // alert2.setTitle("Choose " + toRename.toString() + " icon");
        final AlertDialog.Builder equalizerBuilder = new AlertDialog.Builder(context);

        LayoutInflater factory = LayoutInflater.from(context);
        final View deleteDialogView = factory.inflate(R.layout.dialog_equalizer, null, false);

        final SeekBar low = (SeekBar) deleteDialogView.findViewById(R.id.seekBarLow);
        final SeekBar med = (SeekBar) deleteDialogView.findViewById(R.id.seekBarMed);
        final SeekBar hi = (SeekBar) deleteDialogView.findViewById(R.id.seekBarHigh);

        final SeekBar lowRange = (SeekBar) deleteDialogView.findViewById(R.id.seekBarRangeLow);
        final SeekBar medRange = (SeekBar) deleteDialogView.findViewById(R.id.seekBarRangeMed);
        final SeekBar hiRange = (SeekBar) deleteDialogView.findViewById(R.id.seekBarRangeHigh);
        low.setProgress(Float.valueOf(opzioni.getEqLow() * 100f).intValue());
        med.setProgress(Float.valueOf(opzioni.getEqMed() * 100f).intValue());
        hi.setProgress(Float.valueOf(opzioni.getEqHigh() * 100f).intValue());
        //Range wideness
        lowRange.setProgress(Float.valueOf(opzioni.getEqLowRange() * 100f).intValue());
        medRange.setProgress(Float.valueOf(opzioni.getEqMedRange() * 100f).intValue());
        hiRange.setProgress(Float.valueOf(opzioni.getEqHighRange() * 100f).intValue());
        Log.i("SoulissEqualizer", "Setting new eq low:" + opzioni.getEqLow() + " med: " + opzioni.getEqMed()
                + " high: " + opzioni.getEqHigh());
        Log.i("SoulissEqualizer", "Setting new eq low RANGE:" + opzioni.getEqLowRange() + " med: " + opzioni.getEqMedRange()
                + " high: " + opzioni.getEqHighRange());

        equalizerBuilder.setPositiveButton(context.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        opzioni.setEqLow(low.getProgress() / 100f);
                        opzioni.setEqMed((float) med.getProgress() / 100f);
                        opzioni.setEqHigh(hi.getProgress() / 100f);
                        String strDisease2Format = context.getString(R.string.Souliss_TRGB_eq);
                        String strDisease2Msg = String.format(strDisease2Format,
                                Constants.twoDecimalFormat.format(opzioni.getEqLow()),
                                Constants.twoDecimalFormat.format(opzioni.getEqMed()),
                                Constants.twoDecimalFormat.format(opzioni.getEqHigh()));
                        opzioni.setEqLowRange(lowRange.getProgress() / 100f);
                        opzioni.setEqMedRange(medRange.getProgress() / 100f);
                        opzioni.setEqHighRange(hiRange.getProgress() / 100f);
                        if (toUpdate != null)
                            toUpdate.setText(strDisease2Msg);
                    }
                });

        equalizerBuilder.setNegativeButton(context.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
        final AlertDialog deleteDialog = equalizerBuilder.create();

        deleteDialog.setView(deleteDialogView);

        deleteDialog.setTitle("Global equalizer");
        return deleteDialog;
    }

    /**
     * Sceglie nuova icona
     *
     * @param context
     * @param ctx
     * @param list
     * @param datasource
     * @param toRename   puo essere nodo o Scenario
     * @return
     */
    public static AlertDialog.Builder chooseIconDialog(final Context context, final ImageView ctx, final ListView list,
                                                       final SoulissDBHelper datasource, final ISoulissObject toRename) {
        final int savepoint = toRename.getIconResourceId();
        final SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(context);
        assertTrue("chooseIconDialog: NOT instanceof", toRename instanceof SoulissNode
                || toRename instanceof SoulissScene || toRename instanceof SoulissTypical || toRename instanceof SoulissTag);
        final AlertDialog.Builder alert2 = new AlertDialog.Builder(context);
        // alert2.setTitle("Choose " + toRename.toString() + " icon");
        alert2.setTitle(context.getString(R.string.dialog_choose_icon) + " " + toRename.getNiceName());

        alert2.setIcon(android.R.drawable.ic_dialog_dialer);
        // loads gallery and requires icon selection*/
        final EcoGallery gallery = new EcoGallery(context);
        // final Gallery gallery = new Gallery(context);
        // Gallery gallery = (Gallery) findViewById(R.id.gallery);
        // gallery.setMinimumHeight(300);
        // gallery.setLayoutParams(new Layo);
        gallery.setAdapter(new SoulissIconAdapter(context));
        alert2.setView(gallery);


        alert2.setPositiveButton(context.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int pos = gallery.getSelectedItemPosition();
                        SoulissIconAdapter ad = (SoulissIconAdapter) gallery.getAdapter();
                        toRename.setIconResourceId(ad.getItemResId(pos));
                        if (toRename instanceof SoulissNode) {
                            datasource.createOrUpdateNode((SoulissNode) toRename);
                            if (list != null) {
                                List<SoulissNode> goer = datasource.getAllNodes();
                                SoulissNode[] nodiArray = new SoulissNode[goer.size()];
                                nodiArray = goer.toArray(nodiArray);
                                NodesListAdapter nodesAdapter = new NodesListAdapter(context, nodiArray, opzioni);
                                // Adapter della lista
                                list.setAdapter(nodesAdapter);
                                list.invalidateViews();
                            }
                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);
                            if (list != null) {
                                LinkedList<SoulissScene> goer = datasource.getScenes(SoulissApp.getAppContext());
                                SoulissScene[] scenesArray = new SoulissScene[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                SceneListAdapter progsAdapter = new SceneListAdapter(context, scenesArray, opzioni);
                                // Adapter della lista
                                list.setAdapter(progsAdapter);
                                list.invalidateViews();
                            }
                        } else if (toRename instanceof SoulissTag) {
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissApp.getAppContext());
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (list != null) {
                                List<SoulissTag> goer = dbt.getTags(SoulissApp.getAppContext());
                                SoulissTag[] scenesArray = new SoulissTag[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                TagListAdapter progsAdapter = new TagListAdapter(context, scenesArray, opzioni);
                                // Adapter della lista
                                list.setAdapter(progsAdapter);
                                list.invalidateViews();
                            }
                        } else {
                            ((SoulissTypical) toRename).getTypicalDTO().persist();
                            if (list != null) {
                                TypicalsListAdapter ta = (TypicalsListAdapter) list.getAdapter();
                                ta.notifyDataSetChanged();
                                list.invalidateViews();
                            }
                        }
                        ctx.setImageResource(toRename.getIconResourceId());
                        ctx.invalidate();

                    }
                });

        alert2.setNegativeButton(context.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        toRename.setIconResourceId(savepoint);
                    }
                });

        return alert2;
    }

    /**
     * Dialogo creazione DB
     *
     * @param preferencesActivity
     * @param ip
     * @return
     */
    public static AlertDialog.Builder updateSoulissDBDialog(final Activity preferencesActivity, final String ip,
                                                            final SoulissPreferenceHelper opts) {
        // ProgressDialog.Builder alert = new
        // ProgressDialog.Builder(preferencesActivity);
        AlertDialog.Builder alert = new AlertDialog.Builder(preferencesActivity);
        // final SharedPreferences customSharedPreference =
        // preferencesActivity.getSharedPreferences("SoulissPrefs",
        // Activity.MODE_PRIVATE);
        alert.setTitle(preferencesActivity.getResources().getString(R.string.dialog_warn_db));
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        if (opts.isSoulissReachable()) {
            // alert.setIcon()
            alert.setMessage(preferencesActivity.getResources().getString(R.string.dialog_create_db) + ip
                    + preferencesActivity.getResources().getString(R.string.dialog_create_db2));
            alert.setPositiveButton(preferencesActivity.getResources().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            new Thread() {
                                public void run() {
                                    Looper.prepare();
                                    UDPHelper.dbStructRequest(opts);
                                }
                            }.start();

                        }
                    });
        } else {
            alert.setMessage(preferencesActivity.getResources().getString(R.string.souliss_unavailable));

        }

        alert.setNegativeButton(SoulissApp.getAppContext().getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        return alert;

    }

    /**
     * Dialogo creazione singolo nodo
     * Rebuilds a single node's devices
     *
     * @param preferencesActivity
     * @param toRebuild node to request the refresh for
     * @return
     */
    public static AlertDialog.Builder rebuildNodeDialog(final Activity preferencesActivity, final SoulissNode toRebuild,
                                                            final SoulissPreferenceHelper opts) {
        AlertDialog.Builder alert = new AlertDialog.Builder(preferencesActivity);
        alert.setTitle(preferencesActivity.getResources().getString(R.string.menu_changenodeRebuild ));
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        if (opts.isSoulissReachable()) {
            // alert.setIcon()
            alert.setMessage(preferencesActivity.getResources().getString(R.string.menu_changenodeRebuild_desc) );
            alert.setPositiveButton(preferencesActivity.getResources().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            new Thread() {
                                public void run() {
                                    Looper.prepare();
                                    UDPHelper.typicalRequest(opts, 1, toRebuild.getId());
                                }
                            }.start();
                        }
                    });
        } else {
            alert.setMessage(preferencesActivity.getResources().getString(R.string.souliss_unavailable));
        }

        alert.setNegativeButton(SoulissApp.getAppContext().getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        return alert;

    }


    /**
     * Sceglie nuova icona
     * <p/>
     * puo essere nodo o Scenario
     *
     * @return
     */
    public static AlertDialog.Builder addTagCommandDialog(final Context context,
                                                          final SoulissDBTagHelper datasource,
                                                          final SoulissTypical toadd,
                                                          @Nullable final ListView toReferesh) {
        // prendo tag dal DB
        List<SoulissTag> goer = datasource.getTags(context);
        final SoulissTag[] tagArray = new SoulissTag[goer.size()];
        int q = 0;
        for (SoulissTag object : goer) {
            tagArray[q++] = object;
        }
        ContextThemeWrapper wrapper = new ContextThemeWrapper(context, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.LightThemeSelector : R.style.DarkThemeSelector);
        final AlertDialog.Builder alert2 = new AlertDialog.Builder(wrapper);

        View dialoglayout = View.inflate(new ContextWrapper(context), R.layout.add_to_dialog, null);
        alert2.setView(dialoglayout);
        // alert2.setInverseBackgroundForced( true );
        alert2.setTitle(context.getString(R.string.scene_add_to));
        //alert2.setIcon(android.R.drawable.ic_dialog_map);

        final RadioButton tagRadio = (RadioButton) dialoglayout.findViewById(R.id.radioButtonTag);
        final RadioButton newTagRadio = (RadioButton) dialoglayout.findViewById(R.id.radioButtonNewTag);
        final EditText editNewTag = (EditText) dialoglayout.findViewById(R.id.editTextNewTag);

        final Spinner outputNodeSpinner = (Spinner) dialoglayout.findViewById(R.id.spinnerTags);
        ArrayAdapter<SoulissTag> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, tagArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputNodeSpinner.setAdapter(adapter);

        /* INTERLOCK */
        View.OnClickListener se_radio_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputNodeSpinner.setEnabled(true);
                editNewTag.setEnabled(false);
                newTagRadio.setChecked(false);
            }
        };
        tagRadio.setOnClickListener(se_radio_listener);

        View.OnClickListener te_radio_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputNodeSpinner.setEnabled(false);
                editNewTag.setEnabled(true);
                tagRadio.setChecked(false);
            }
        };
        newTagRadio.setOnClickListener(te_radio_listener);


		/* Cambiando nodo, cambia i tipici */
        AdapterView.OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //setTypicalSpinner(outputTypicalSpinner, nodiArray[pos], context);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        outputNodeSpinner.setOnItemSelectedListener(lit);


        alert2.setPositiveButton(context.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SoulissTag it;
                        if (tagRadio.isChecked()) {
                            it = (SoulissTag) outputNodeSpinner.getSelectedItem();
                            if (!it.getAssignedTypicals().contains(toadd))
                                it.getAssignedTypicals().add(toadd);
                            datasource.createOrUpdateTag(it);
                        } else if (newTagRadio.isChecked()) {
                            if (editNewTag.getText() == null || editNewTag.getText().length() == 0) {
                                Toast.makeText(context, context.getString(R.string.input_tag_name), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            it = new SoulissTag();
                            long newId = datasource.createOrUpdateTag(null);
                            it.setTagId(newId);
                            it.setName(editNewTag.getText().toString());
                            it.setIconResourceId(R.drawable.tv);
                            it.getAssignedTypicals().add(toadd);
                            datasource.createOrUpdateTag(it);
                            Toast.makeText(context, "TAG" + ": " + it.getNiceName(), Toast.LENGTH_SHORT).show();

                            return;
                        } else {

                            Toast.makeText(context, "Select " + context.getString(R.string.existing_tag) + " " + context.getString(R.string.or)
                                    + " " + context.getString(R.string.new_tag), Toast.LENGTH_SHORT).show();
                            return;
                        }


                    }
                });

        alert2.setNegativeButton(context.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        tagRadio.performClick();

        editNewTag.requestFocus();

//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //      imm.toggleSoftInput(InputMethodManager.HI, 0);
        return alert2;
    }

}
