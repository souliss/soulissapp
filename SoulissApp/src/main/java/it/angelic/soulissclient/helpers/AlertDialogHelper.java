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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.LinkedList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SettingsActivity;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.adapters.FontAwesomeTagListAdapter;
import it.angelic.soulissclient.adapters.NodesListAdapter;
import it.angelic.soulissclient.adapters.SceneListAdapter;
import it.angelic.soulissclient.adapters.SoulissFontAwesomeAdapter;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.model.db.SoulissDBOpenHelper;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.preferences.NetSettingsFragment;
import it.angelic.soulissclient.preferences.ServiceSettingsFragment;
import it.angelic.soulissclient.programmi.ProgramListAdapter;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.LauncherElementEnum;
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
        AlertDialog.Builder alert = new AlertDialog.Builder(source,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setTitle(source.getResources().getString(R.string.notconfigured));
        alert.setMessage(source.getResources().getString(R.string.dialog_notinited_ip));

        alert.setPositiveButton(source.getResources().getString(R.string.proceed),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final Intent preferencesActivity = new Intent(source.getBaseContext(),
                                SettingsActivity.class);

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
        AlertDialog.Builder alert = new AlertDialog.Builder(source,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
        if (!opts.getDontShowAgain(source.getResources().getString(R.string.dialog_disabled_db))) {
            final CheckBox checkBox = new CheckBox(source);
            checkBox.setText(source.getResources().getString(R.string.dialog_dontshowagain));

            LinearLayout linearLayout = new LinearLayout(source);
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            lParams.setMargins(15, 15, 15, 15);
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
                                    SettingsActivity.class);
                            preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
                            preferencesActivity.setAction("db_setup");
                            preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            source.startActivity(preferencesActivity);
                            //visto che DB vuoto, anticipa
                            new Thread() {
                                public void run() {
                                    Looper.prepare();
                                    UDPHelper.dbStructRequest(opts);
                                }
                            }.start();
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
        ContextThemeWrapper wrapper = new ContextThemeWrapper(source, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.LightThemeSelector : R.style.DarkThemeSelector);
        AlertDialog.Builder alert = new AlertDialog.Builder(wrapper,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);

        final CheckBox checkBox = new CheckBox(wrapper);
        TextView textView = new TextView(wrapper);
        if (!opts.getDontShowAgain(source.getResources().getString(R.string.dialog_disabled_service))) {
            checkBox.setText(source.getResources().getString(R.string.dialog_dontshowagain));
            alert.setMessage(source.getResources().getString(R.string.dialog_notinited_service));

            LinearLayout linearLayout = new LinearLayout(wrapper);
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
                                    SettingsActivity.class);
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
        ContextThemeWrapper wrapper = new ContextThemeWrapper(source, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.LightThemeSelector : R.style.DarkThemeSelector);
        AlertDialog.Builder alert = new AlertDialog.Builder(wrapper,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        final SharedPreferences soulissCust = PreferenceManager.getDefaultSharedPreferences(source);

        alert.setTitle(source.getResources().getString(R.string.dialog_warn_db));
        alert.setMessage(source.getResources().getString(R.string.dialog_drop_db));

        alert.setPositiveButton(source.getResources().getString(R.string.proceed),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        datasource.close();
                        if (source.deleteDatabase(SoulissDBOpenHelper.DATABASE_NAME)) {
                            SharedPreferences.Editor editor = soulissCust.edit();
                            // tolgo db dalle prefs
                            if (soulissCust.contains("numNodi"))
                                editor.remove("numNodi");
                            if (soulissCust.contains("numTipici"))
                                editor.remove("numTipici");
                            editor.commit();
                            Log.w(TAG, "Souliss DB dropped");
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
    public static AlertDialog.Builder removeCommandDialog(final Activity cont, final ListView ctx,
                                                          final SoulissDBHelper datasource, final SoulissCommand toRename) {
        AlertDialog.Builder alert = new AlertDialog.Builder(cont,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
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
                            LinkedList<SoulissCommand> programsArray = datasource.getUnexecutedCommands(cont);

                            ProgramListAdapter progsAdapter = new ProgramListAdapter(cont, programsArray, datasource
                                    .getTriggerMap(), new SoulissPreferenceHelper(cont.getApplicationContext()));
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
    public static AlertDialog.Builder renameSoulissObjectDialog(final Activity cont, final TextView textViewLabel,
                                                                final ListView listV, final SoulissDBHelper datasource, final ISoulissObject toRename) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(cont,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
        final SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(cont);
        assertTrue("chooseIconDialog: NOT instanceof", toRename instanceof SoulissNode
                || toRename instanceof SoulissScene || toRename instanceof SoulissTypical || toRename instanceof SoulissTag);
        alert.setIcon(R.drawable.ic_mode_edit_24dp);
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
                                NodesListAdapter nodesAdapter = new NodesListAdapter(cont, goer, opzioni);
                                // Adapter della lista
                                listV.setAdapter(nodesAdapter);
                                listV.invalidateViews();
                            }

                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);
                            if (listV != null) {
                                LinkedList<SoulissScene> goer = datasource.getScenes();
                                try {
                                    SceneListAdapter sa = (SceneListAdapter) listV.getAdapter();

                                    sa.setScenes(goer);
                                    sa.notifyDataSetChanged();
                                    // listV.setAdapter(sa);
                                    listV.invalidateViews();
                                } catch (Exception e) {
                                    Log.w(Constants.TAG, "rename didn't find proper view to refresh");
                                }
                            }
                        } else if (toRename instanceof SoulissTag) {
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(cont);
                            dbt.createOrUpdateTag((SoulissTag) toRename);//non aggiorno il campo fath, non serve
                            if (listV != null) {
                                List<SoulissTag> goer = dbt.getRootTags();
                                SoulissTag[] scenesArray = new SoulissTag[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                try {
                                    FontAwesomeTagListAdapter sa = (FontAwesomeTagListAdapter) listV.getAdapter();
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
                        } else {//Typical
                            if (listV != null) {
                                ((SoulissTypical) toRename).getTypicalDTO().persist();
                                TypicalsListAdapter ta = (TypicalsListAdapter) listV.getAdapter();
                                ta.notifyDataSetChanged();
                                listV.invalidateViews();
                            }
                        }
                        if (cont instanceof Activity && !(toRename instanceof SoulissTypical))
                            cont.setTitle(toRename.getNiceName());
                        if (textViewLabel != null) {
                            textViewLabel.setText(value);
                            textViewLabel.setText(toRename.getNiceName());
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

    public static AlertDialog.Builder deleteConfigDialog(final Context cont, final Spinner toUpdate) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(cont, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.LightThemeSelector : R.style.DarkThemeSelector);

        final AlertDialog.Builder alert = new AlertDialog.Builder(wrapper, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight : R.style.MyAlertDialogTheme);
        final String bckConfig = (String) toUpdate.getSelectedItem();
        alert.setIcon(R.drawable.ic_cancel_24dp);
        alert.setTitle(cont.getString(R.string.delete) + " " + bckConfig + "?");

        alert.setPositiveButton(cont.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //occhio all'ordine, sto rimuovendo
                        ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) toUpdate.getAdapter();
                        toUpdate.setSelection(0);
                        Log.w(Constants.TAG, "deleting:" + bckConfig);
                        //FIXME con http://stackoverflow.com/questions/21747917/undesired-onitemselected-calls/21751327#21751327
                        spinnerAdapter.remove(bckConfig);
                        spinnerAdapter.notifyDataSetChanged();
                        if (SoulissApp.getCurrentConfig().equals(bckConfig))
                            SoulissApp.setCurrentConfig("");//reset w/o saving
                        SoulissApp.deleteConfiguration(bckConfig);
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

    public static AlertDialog.Builder renameConfigDialog(final Context cont, final Spinner toUpdate) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(cont, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.LightThemeSelector : R.style.DarkThemeSelector);

        final AlertDialog.Builder alert = new AlertDialog.Builder(wrapper, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight : R.style.MyAlertDialogTheme);
        //final SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(cont);
        final String bckConfig = (String) toUpdate.getSelectedItem();
        alert.setIcon(R.drawable.ic_mode_edit_24dp);
        alert.setTitle(cont.getString(R.string.rename) + " " + bckConfig);

        // Set an EditText view to get user input
        final EditText input = new EditText(wrapper);
        alert.setView(input);
        input.setText(bckConfig);
        alert.setPositiveButton(wrapper.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        SoulissApp.deleteConfiguration(bckConfig);
                        if (SoulissApp.getCurrentConfig().equals(bckConfig)) {
                            Log.w(Constants.TAG, "Sobstitute current config:");
                            SoulissApp.setCurrentConfig(value);
                        }
                        SoulissApp.addConfiguration(value);
                        ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) toUpdate.getAdapter();

                        spinnerAdapter.remove(bckConfig);
                        spinnerAdapter.add(value);
                        spinnerAdapter.notifyDataSetChanged();
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

    public static AlertDialog equalizerDialog(final Context context, @Nullable final TextView toUpdate, final Fragment canvas, final FragmentActivity act) {
        final SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        // alert2.setTitle("Choose " + toRename.toString() + " icon");
        final AlertDialog.Builder equalizerBuilder = new AlertDialog.Builder(context,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);

        LayoutInflater factory = LayoutInflater.from(context);
        final View deleteDialogView = factory.inflate(R.layout.dialog_equalizer, null, false);
        final Spinner aufioChan = deleteDialogView.findViewById(R.id.spinnerInputChannel);
        final SeekBar low = deleteDialogView.findViewById(R.id.seekBarLow);
        final SeekBar med = deleteDialogView.findViewById(R.id.seekBarMed);
        final SeekBar hi = deleteDialogView.findViewById(R.id.seekBarHigh);

        final SeekBar lowRange = deleteDialogView.findViewById(R.id.seekBarRangeLow);
        final SeekBar medRange = deleteDialogView.findViewById(R.id.seekBarRangeMed);
        final SeekBar hiRange = deleteDialogView.findViewById(R.id.seekBarRangeHigh);
        low.setProgress(Float.valueOf(opzioni.getEqLow() * 100f).intValue());
        med.setProgress(Float.valueOf(opzioni.getEqMed() * 100f).intValue());
        hi.setProgress(Float.valueOf(opzioni.getEqHigh() * 100f).intValue());
        aufioChan.setSelection(opzioni.getAudioInputChannel());
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
                        final int[] spinnerFunVal = context.getResources().getIntArray(R.array.inputChanValues);
                        Log.w(Constants.TAG, "Setting input channel:" + spinnerFunVal[aufioChan.getSelectedItemPosition()] + "pos:" + aufioChan.getSelectedItemPosition());
                        //spinnerFunVal[aufioChan.getSelectedItemPosition() non funziona? faccio cosi, va ben lo stesso
                        //perche 1 e` il mic, e 0 il default audio session
                        opzioni.setAudioInputChannel(aufioChan.getSelectedItemPosition());
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
                        if (toUpdate != null) {
                           /* act.getSupportFragmentManager()
                                    .beginTransaction()
                                    .detach(canvas)
                                    .attach(canvas)
                                    .commit();*/
                            toUpdate.setText(strDisease2Msg);
                        }
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
     * @param iconImageView
     * @param list
     * @param datasource
     * @param toRename      puo essere nodo o Scenario
     * @return
     */
    public static AlertDialog.Builder chooseIconDialog(final Activity context, @Nullable final TextView iconImageView, final ListView list,
                                                       final SoulissDBHelper datasource, final ISoulissObject toRename) {
        final int savepoint = toRename.getIconResourceId();
        final SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(context);
        assertTrue("chooseIconDialog: NOT instanceof", toRename instanceof SoulissNode
                || toRename instanceof SoulissScene || toRename instanceof SoulissTypical || toRename instanceof SoulissTag);
        final AlertDialog.Builder alert2 = new AlertDialog.Builder(context,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
        // alert2.setTitle("Choose " + toRename.toString() + " icon");
        alert2.setTitle(context.getString(R.string.dialog_choose_icon) + " " + toRename.getNiceName());

        alert2.setIcon(R.drawable.ic_mode_edit_24dp);
        // loads gallery and requires icon selection*/
        final EcoGallery gallery = new EcoGallery(context);
        gallery.setAdapter(new SoulissFontAwesomeAdapter(context));
        alert2.setView(gallery);


        alert2.setPositiveButton(context.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int pos = gallery.getSelectedItemPosition();

                        SoulissFontAwesomeAdapter ad = (SoulissFontAwesomeAdapter) gallery.getAdapter();
                        toRename.setIconResourceId(FontAwesomeUtil.getCodeIndexByFontName(context, FontAwesomeEnum.values()[pos].getFontName()));
                        if (toRename instanceof SoulissNode) {
                            datasource.createOrUpdateNode((SoulissNode) toRename);
                            if (list != null) {
                                List<SoulissNode> goer = datasource.getAllNodes();
                                NodesListAdapter nodesAdapter = new NodesListAdapter(context, goer, opzioni);
                                // Adapter della lista
                                list.setAdapter(nodesAdapter);
                                list.invalidateViews();
                            }
                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);
                            if (list != null) {
                                LinkedList<SoulissScene> goer = datasource.getScenes();

                                SceneListAdapter progsAdapter = new SceneListAdapter(context, goer, opzioni);
                                // Adapter della lista
                                list.setAdapter(progsAdapter);
                                list.invalidateViews();
                            }
                        } else if (toRename instanceof SoulissTag) {
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissApp.getAppContext());
                            dbt.createOrUpdateTag((SoulissTag) toRename);//non aggiorno il campo fath, non serve
                            if (list != null) {
                                List<SoulissTag> goer = dbt.getRootTags();
                                SoulissTag[] scenesArray = new SoulissTag[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                FontAwesomeTagListAdapter progsAdapter = new FontAwesomeTagListAdapter(context, scenesArray, opzioni);
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
                        if (iconImageView != null) {
                            FontAwesomeUtil.prepareFontAweTextView(context, iconImageView, toRename.getIconResourceId());
                            // iconImageView.setText(FontAwesomeUtil.translateAwesomeCode(context, ));
                            iconImageView.invalidate();
                        }

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
        AlertDialog.Builder alert = new AlertDialog.Builder(preferencesActivity,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
        // final SharedPreferences customSharedPreference =
        // preferencesActivity.getSharedPreferences("SoulissPrefs",
        // Activity.MODE_PRIVATE);
        alert.setTitle(preferencesActivity.getResources().getString(R.string.dialog_warn_db));
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        if (opts.isSoulissReachable()) {
            // alert.setIcon()
            alert.setMessage(preferencesActivity.getResources().getString(R.string.dialog_create_db) + ": " + ip
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
     * @param toRebuild           node to request the refresh for
     * @return
     */
    public static AlertDialog.Builder rebuildNodeDialog(final Activity preferencesActivity, final SoulissNode toRebuild,
                                                        final SoulissPreferenceHelper opts) {
        AlertDialog.Builder alert = new AlertDialog.Builder(preferencesActivity,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);
        alert.setTitle(preferencesActivity.getResources().getString(R.string.menu_changenodeRebuild));
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        if (opts.isSoulissReachable()) {
            // alert.setIcon()
            alert.setMessage(preferencesActivity.getResources().getString(R.string.menu_changenodeRebuild_desc));
            alert.setPositiveButton(preferencesActivity.getResources().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            new Thread() {
                                public void run() {
                                    Looper.prepare();
                                    UDPHelper.typicalRequest(opts, 1, toRebuild.getNodeId());
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
    public static AlertDialog.Builder addToCommandDialog(final Context context,
                                                         final SoulissDBTagHelper datasource,
                                                         final SoulissTypical toadd,
                                                         @Nullable final SoulissTag parentTag,
                                                         @Nullable final ListView toReferesh) {
        // prendo tag dal DB
        List<SoulissTag> goer = datasource.getAllTagsWithoutChildren(context);
        final SoulissTag[] tagArray = new SoulissTag[goer.size()];
        int q = 0;
        for (SoulissTag object : goer) {
            tagArray[q++] = object;
        }
        ContextThemeWrapper wrapper = new ContextThemeWrapper(context, SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.LightThemeSelector : R.style.DarkThemeSelector);
        final AlertDialog.Builder alert2 = new AlertDialog.Builder(wrapper,SoulissApp.getOpzioni().isLightThemeSelected() ? R.style.MyAlertDialogThemeLight:R.style.MyAlertDialogTheme);

        View dialoglayout = View.inflate(new ContextWrapper(context), R.layout.dialog_add_to_, null);
        alert2.setView(dialoglayout);
        alert2.setTitle(context.getString(R.string.scene_add_to));
        alert2.setIcon(android.R.drawable.ic_menu_add);

        final RadioButton dashbRadio = dialoglayout.findViewById(R.id.radioButtonDashboardTag);
        final RadioButton tagRadio = dialoglayout.findViewById(R.id.radioButtonTag);
        final RadioButton newTagRadio = dialoglayout.findViewById(R.id.radioButtonNewTag);
        final EditText editNewTag = dialoglayout.findViewById(R.id.editTextNewTag);

        final Spinner outputNodeSpinner = dialoglayout.findViewById(R.id.spinnerTags);
        ArrayAdapter<SoulissTag> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, tagArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputNodeSpinner.setAdapter(adapter);

        /* INTERLOCK */
        View.OnClickListener db_radio_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputNodeSpinner.setEnabled(false);
                editNewTag.setEnabled(false);
                newTagRadio.setChecked(false);
                tagRadio.setChecked(false);
                dashbRadio.setChecked(true);
            }
        };
        dashbRadio.setOnClickListener(db_radio_listener);
        View.OnClickListener se_radio_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputNodeSpinner.setEnabled(true);
                editNewTag.setEnabled(false);
                newTagRadio.setChecked(false);
                dashbRadio.setChecked(false);
            }
        };
        tagRadio.setOnClickListener(se_radio_listener);

        View.OnClickListener te_radio_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputNodeSpinner.setEnabled(false);
                editNewTag.setEnabled(true);
                tagRadio.setChecked(false);
                dashbRadio.setChecked(false);
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
                        if (dashbRadio.isChecked()) {
                            SoulissDBLauncherHelper dbl = new SoulissDBLauncherHelper(context);
                            LauncherElement nodeLauncher = new LauncherElement();
                            nodeLauncher.setComponentEnum(LauncherElementEnum.TYPICAL);
                            nodeLauncher.setLinkedObject(toadd);
                            dbl.addElement(nodeLauncher);
                            Toast.makeText(context, toadd.getNiceName() + context.getString(R.string.added_to_dashboard), Toast.LENGTH_SHORT).show();
                        } else if (tagRadio.isChecked()) {
                            it = (SoulissTag) outputNodeSpinner.getSelectedItem();
                            if (!it.getAssignedTypicals().contains(toadd))
                                it.getAssignedTypicals().add(toadd);
                            if (it.getTagId() == SoulissDBOpenHelper.FAVOURITES_TAG_ID)
                                toadd.getTypicalDTO().setFavourite(true);
                            else
                                toadd.getTypicalDTO().setTagged(true);
                            toadd.getTypicalDTO().persist();
                            datasource.createOrUpdateTag(it);//non aggiorno il campo fath, non serve
                        } else if (newTagRadio.isChecked()) {
                            if (editNewTag.getText() == null || editNewTag.getText().length() == 0) {
                                Toast.makeText(context, context.getString(R.string.input_tag_name), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            it = new SoulissTag();
                            long newId = datasource.createOrUpdateTag(null);
                            it.setFatherId(parentTag != null ? parentTag.getTagId() : null);
                            it.setTagId(newId);
                            it.setName(editNewTag.getText().toString());
                            it.setIconResourceId(FontAwesomeUtil.getCodeIndexByFontName(context, FontAwesomeEnum.fa_tag.getFontName()));
                            it.getAssignedTypicals().add(toadd);

                            toadd.getTypicalDTO().setTagged(true);
                            toadd.getTypicalDTO().persist();
                            datasource.createOrUpdateTag(it);
                            Toast.makeText(context, "TAG" + ": " + it.getNiceName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Select " + context.getString(R.string.existing_tag) + " " + context.getString(R.string.or)
                                    + " " + context.getString(R.string.new_tag), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (toReferesh != null) {

                            TypicalsListAdapter ta = (TypicalsListAdapter) toReferesh.getAdapter();
                            ta.notifyDataSetChanged();
                            toReferesh.invalidateViews();
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
