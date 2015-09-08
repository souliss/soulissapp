package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.adapters.ProgramListAdapter;
import it.angelic.soulissclient.adapters.SoulissIconAdapter;
import it.angelic.soulissclient.adapters.TagRecyclerAdapter;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import us.feras.ecogallery.EcoGallery;

import static it.angelic.soulissclient.Constants.TAG;
import static junit.framework.Assert.assertTrue;

/**
 * Classe helper per i dialoghi riciclabili CHE USANO GRIDVIEW
 */
public class AlertDialogGridHelper {

    /**
     * Rename a node
     *
     * @param listV      used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return
     */
    public static AlertDialog.Builder renameSoulissObjectDialog(final Context cont, final TextView tgt,
                                                                final TagRecyclerAdapter listV, final SoulissDBHelper datasource, final ISoulissObject toRename) {
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
                                throw new RuntimeException("NOT IMPLEMENTED");
                            }

                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);
                            if (listV != null) {
                                throw new RuntimeException("NOT IMPLEMENTED");
                            }
                        } else if (toRename instanceof SoulissTag) {
                            if (((SoulissTag) toRename).getTagId() < Constants.TAG_DEFAULT_FAV) {
                                Toast.makeText(cont, cont.getString(R.string.nodeleteFav), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(cont);
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (listV != null) {
                                List<SoulissTag> goer = dbt.getTags(SoulissClient.getAppContext());
                                SoulissTag[] scenesArray = new SoulissTag[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                try {

                                    listV.setTagArray(scenesArray);
                                    listV.notifyDataSetChanged();
                                } catch (Exception e) {
                                    Log.w(Constants.TAG, "rename didn't find proper view to refresh");
                                }
                            }
                        } else {
                            if (listV != null) {
                                throw new RuntimeException("NOT IMPLEMENTED");
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
    public static AlertDialog.Builder chooseIconDialog(final Context context, final ImageView ctx, final TagRecyclerAdapter list,
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

                            }
                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);
                            if (list != null) {

                            }
                        } else if (toRename instanceof SoulissTag) {
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissClient.getAppContext());
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (list != null) {
                                List<SoulissTag> goer = dbt.getTags(SoulissClient.getAppContext());
                                SoulissTag[] scenesArray = new SoulissTag[goer.size()];
                                scenesArray = goer.toArray(scenesArray);
                                list.setTagArray(scenesArray);
                                list.notifyDataSetChanged();
                            }
                        } else {
                            ((SoulissTypical) toRename).getTypicalDTO().persist();
                            if (list != null) {

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
     * Remove a Scene
     *
     * @param ctx        used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return //TODO rivedere parametri
     */
    public static void removeTagDialog(final Context cont, final TagRecyclerAdapter ctx, final SoulissDBHelper datasource,
                                       final SoulissTag toRename, final SoulissPreferenceHelper opts) {
        Log.w(Constants.TAG, "Removing TAG:" + toRename.getNiceName() + " ID:" + toRename.getTagId());
        if (toRename.getTagId() < Constants.TAG_DEFAULT_FAV) {
            Toast.makeText(cont, R.string.cantRemoveDefault, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(cont);
        alert.setTitle(R.string.removeTag);
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setMessage(R.string.sureToRemoveTag);
        alert.setPositiveButton(cont.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        datasource.deleteTag(toRename);
                        if (ctx != null) {
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(cont);
                            // prendo dal DB
                            List<SoulissTag> goer = dbt.getTags(cont);
                            SoulissTag[] tagArr= new SoulissTag[goer.size()];
                            tagArr = goer.toArray(tagArr);
                            // targetScene.setCommandArray(goer);
                            // Adapter della lista
                            ctx.setTagArray(tagArr);
                            ctx.notifyDataSetChanged();
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
        ContextThemeWrapper wrapper = new ContextThemeWrapper(context, SoulissClient.getOpzioni().isLightThemeSelected() ? R.style.LightThemeSelector : R.style.DarkThemeSelector);
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
