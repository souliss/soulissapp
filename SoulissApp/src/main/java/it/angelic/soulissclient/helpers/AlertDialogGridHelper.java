package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.adapters.SoulissIconAdapter;
import it.angelic.soulissclient.adapters.TagRecyclerAdapter;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import us.feras.ecogallery.EcoGallery;

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
                            if (((SoulissTag) toRename).getTagId() <= SoulissDB.FAVOURITES_TAG_ID) {
                                Toast.makeText(cont, cont.getString(R.string.nodeleteFav), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(cont);
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (tagRecyclerAdapter != null) {
                                int tgtPos = -1;
                                List<SoulissTag> goer = dbt.getTags(SoulissApp.getAppContext());
                                SoulissTag[] tagArray = new SoulissTag[goer.size()];
                                tagArray = goer.toArray(tagArray);
                                tagRecyclerAdapter.setTagArray(tagArray);
                                try {
                                    for (int i = 0; i < tagArray.length; i++) {
                                        if (tagArray[i].getTagId().equals(((SoulissTag) toRename).getTagId())) {
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
    public static AlertDialog.Builder chooseIconDialog(final Context context, final TagRecyclerAdapter list,
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
                            SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissApp.getAppContext());
                            dbt.createOrUpdateTag((SoulissTag) toRename);
                            if (list != null) {
                             //   List<SoulissTag> goer = dbt.getTags(SoulissClient.getAppContext());
                                SoulissTag[] tagArray = list.getTagArray();
                               // tagArray = goer.toArray(tagArray);
                                //list.setTagArray(tagArray);
                                try {
                                    for (int i = 0; i < tagArray.length; i++) {
                                        if (tagArray[i].getTagId().equals(((SoulissTag) toRename).getTagId())) {
                                            ((SoulissTag)list.getTag(i)).setIconResourceId(toRename.getIconResourceId());
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

    /**
     * Remove a Scene
     *
     * @param ctx        used to invalidate views
     * @param datasource to store new value
     * @param toRename
     * @return
     */
    public static void removeTagDialog(final Context cont, final TagRecyclerAdapter ctx, final SoulissDBTagHelper datasource,
                                       final SoulissTag toRename, final SoulissPreferenceHelper opts) {
        Log.w(Constants.TAG, "Removing TAG:" + toRename.getNiceName() + " ID:" + toRename.getTagId());
        if (toRename.getTagId() <= SoulissDB.FAVOURITES_TAG_ID) {
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
                            SoulissTag[] tagArrBck = ctx.getTagArray();
                            for (int i = 0; i < tagArrBck.length; i++) {
                                if (tagArrBck[i].getTagId().equals(toRename.getTagId()))
                                    tgtPos = i;
                            }

                            // prendo dal DB
                            List<SoulissTag> goer = datasource.getTags(cont);
                            SoulissTag[] tagArr = new SoulissTag[goer.size()];
                            tagArr = goer.toArray(tagArr);
                            // targetScene.setCommandArray(goer);
                            // Adapter della lista
                            ctx.setTagArray(tagArr);
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
                        // Canceled.
                    }
                });
        alert.show();
    }

    public static AlertDialog tagOrderPickerDialog(final Context context, @Nullable final SoulissTag toUpdate, final int oldPosition, final TagRecyclerAdapter adapter) {
        final SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        // alert2.setTitle("Choose " + toRename.toString() + " icon");
        final AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);

        LayoutInflater factory = LayoutInflater.from(context);
        final View deleteDialogView = factory.inflate(R.layout.dialog_numberpicker, null, false);

        final NumberPicker low = (NumberPicker) deleteDialogView.findViewById(R.id.numberPicker1);
        low.setMinValue((int) (SoulissDB.FAVOURITES_TAG_ID + 1));

        low.setMaxValue(adapter == null ? 10 : adapter.getItemCount());

        Log.i(Constants.TAG, "Setting new TAG order:" + toUpdate.getName());

        deleteBuilder.setPositiveButton(context.getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SoulissDBTagHelper dbt = new SoulissDBTagHelper(context);
                        toUpdate.setTagOrder(low.getValue());//sempre
                        if (toUpdate != null && adapter != null) {
                            int newPosition = low.getValue() >= adapter.getItemCount() ? adapter.getItemCount() - 1 : low.getValue();
                            //swap elements
                            SoulissTag[] temp = adapter.getTagArray();
                            Assert.assertTrue(oldPosition<temp.length);
                            SoulissTag oldOne = temp[newPosition];
                            temp[newPosition] = toUpdate;
                            temp[oldPosition] = oldOne;
                            adapter.setTagArray(temp);
                            dbt.createOrUpdateTag(toUpdate);
                            //notify to move
                            adapter.notifyItemMoved(oldPosition, newPosition);
                        }
                    }
                }
        );

        deleteBuilder.setNegativeButton(context.getResources().getString(android.R.string.cancel),

                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }

        );
        final AlertDialog deleteDialog = deleteBuilder.create();

        deleteDialog.setView(deleteDialogView);

        deleteDialog.setTitle("Select Position");
        return deleteDialog;
    }
}
