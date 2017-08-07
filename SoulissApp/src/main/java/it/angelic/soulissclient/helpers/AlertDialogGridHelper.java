package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.adapters.TagRecyclerAdapter;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBOpenHelper;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;

import static junit.framework.Assert.assertTrue;

/**
 * Classe helper per i dialoghi riciclabili CHE USANO GRIDVIEW
 */
public class AlertDialogGridHelper {

    /**
     * Rename a node
     *
     * @param tagRecyclerAdapter used to invalidate views
     * @param datasource         to store new value
     * @param toRename
     * @return
     */
    public static AlertDialog.Builder renameSoulissObjectDialog(final Context cont, final TextView tgt,
                                                                final TagRecyclerAdapter tagRecyclerAdapter, final SoulissDBHelper datasource, final ISoulissObject toRename) {
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
                            if (tagRecyclerAdapter != null) {
                                throw new RuntimeException("NOT IMPLEMENTED");
                            }

                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);
                            if (tagRecyclerAdapter != null) {
                                throw new RuntimeException("NOT IMPLEMENTED");
                            }
                        } else if (toRename instanceof SoulissTag) {
                            if (((SoulissTag) toRename).getTagId() <= SoulissDBOpenHelper.FAVOURITES_TAG_ID) {
                                Toast.makeText(cont, cont.getString(R.string.nodeleteFav), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(cont);
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (tagRecyclerAdapter != null) {
                                int tgtPos = -1;
                                List<SoulissTag> goer = dbt.getRootTags();

                                tagRecyclerAdapter.setTagArray(goer);
                                try {
                                    for (int i = 0; i < goer.size(); i++) {
                                        if (goer.get(i).getTagId().equals(((SoulissTag) toRename).getTagId())) {
                                            tgtPos = i;
                                            tagRecyclerAdapter.notifyItemChanged(tgtPos);
                                            Log.w(Constants.TAG, "notifiedAdapter of change on index " + tgtPos);
                                        }
                                    }


                                } catch (Exception e) {
                                    Log.w(Constants.TAG, "rename didn't find proper view to refresh");
                                }
                            }
                        } else {
                            if (tagRecyclerAdapter != null) {
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
     * @param list
     * @param datasource
     * @param toRename   puo essere nodo o Scenario
     * @return
     */
  /*  public static AlertDialog.Builder chooseIconDialog(final Context context, final TagRecyclerAdapter list,
                                                       final SoulissDBHelper datasource, final ISoulissObject toRename) {
        final int savepoint = toRename.getIconResourceId();
        final SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(context);
        assertTrue("chooseIconDialog: NOT instanceof", toRename instanceof SoulissNode
                || toRename instanceof SoulissScene || toRename instanceof SoulissTypical || toRename instanceof SoulissTag);
        final AlertDialog.Builder alert2 = new AlertDialog.Builder(context);
        // alert2.setTitle("Choose " + toRename.toString() + " icon");
        alert2.setTitle(context.getString(R.string.dialog_choose_icon) + " " + toRename.getNiceName());

        alert2.setIcon(android.R.drawable.ic_dialog_dialer);
        // loads gallery and requires icon selection
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

                        SoulissFontAwesomeAdapter ad = (SoulissFontAwesomeAdapter) gallery.getAdapter();
                        toRename.setIconResourceId(FontAwesomeUtil.getCodeIndexByFontName(context, FontAwesomeEnum.values()[pos].getFontName()));


                        if (toRename instanceof SoulissNode) {
                            datasource.createOrUpdateNode((SoulissNode) toRename);

                        } else if (toRename instanceof SoulissScene) {
                            datasource.createOrUpdateScene((SoulissScene) toRename);

                        } else if (toRename instanceof SoulissTag) {
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissApp.getAppContext());
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (list != null) {
                                //   List<SoulissTag> goer = dbt.getRootTags(SoulissClient.getAppContext());
                                List<SoulissTag> tagArray = list.getTagArray();
                                // tagArray = goer.toArray(tagArray);
                                //list.setTagArray(tagArray);
                                try {
                                    for (int i = 0; i < tagArray.size(); i++) {
                                        if (tagArray.get(i).getTagId().equals(((SoulissTag) toRename).getTagId())) {
                                            list.getTag(i).setIconResourceId(toRename.getIconResourceId());
                                            list.notifyItemChanged(i);
                                            Log.w(Constants.TAG, "notifiedAdapter of change on index " + i);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(Constants.TAG, "rename didn't find proper view to refresh");
                                }
                            }
                        } else {
                            ((SoulissTypical) toRename).getTypicalDTO().persist();
                            if (list != null) {

                            }
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
*/
    /**
     * Remove a Scene
     *
     * @param ctx        used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return
     */
    public static void removeTagDialog(final Context cont, final TagRecyclerAdapter ctx, final SoulissDBTagHelper datasource,
                                       final SoulissTag toRename) {
        Log.w(Constants.TAG, "Removing TAG:" + toRename.getNiceName() + " ID:" + toRename.getTagId());
        if (toRename.getTagId() <= SoulissDBOpenHelper.FAVOURITES_TAG_ID) {
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
                        int tgtPos = -1;
                        datasource.deleteTag(toRename);
                        if (ctx != null) {
                            List<SoulissTag> tagArrBck = ctx.getTagArray();
                            for (int i = 0; i < tagArrBck.size(); i++) {
                                if (tagArrBck.get(i).getTagId().equals(toRename.getTagId()))
                                    tgtPos = i;
                            }

                            // prendo dal DB
                            List<SoulissTag> goer = datasource.getRootTags();
                            ctx.setTagArray(goer);
                            //shift visivo
                            ctx.notifyItemRemoved(tgtPos);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                //brutta pezza per riallineare position
                                public void run() {
                                    ctx.notifyDataSetChanged();
                                }
                            }, 500);  // 1500 seconds
                        }
                    }
                });

        alert.setNegativeButton(cont.getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int tgtPos = -1;
                        if (ctx != null) {
                            List<SoulissTag> tagArrBck = ctx.getTagArray();
                            for (int i = 0; i < tagArrBck.size(); i++) {
                                if (tagArrBck.get(i).getTagId().equals(toRename.getTagId()))
                                    tgtPos = i;
                            }

                        }
                        if (tgtPos != -1) {
                            ctx.notifyItemChanged(tgtPos);
                        }
                    }
                });
        alert.show();
    }


}
