package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 *
 * @author Ale
 */
public class SoulissDBTagHelper extends SoulissDBHelper {

    public SoulissDBTagHelper(Context context) {
        super(context);
    }

    // Database fields


    public static synchronized SQLiteDatabase getDatabase() {
        return database;
    }


    public List<SoulissTypical> getFavouriteTypicals() {

        SoulissTag fake = new SoulissTag();
        fake.setTagId(0);
        return getTagTypicals(fake);
    }

    /**
     * Crea un nuovo scenario vuoto
     *
     * @param tagIN
     * @return
     */
    public long createOrUpdateTag(SoulissTag tagIN) {
        ContentValues values = new ContentValues();
        long ret = -1;
        if (tagIN != null) {
            values.put(SoulissDB.COLUMN_TAG_NAME, tagIN.getName());
            values.put(SoulissDB.COLUMN_TAG_ICONID, tagIN.getIconResourceId());
            values.put(SoulissDB.COLUMN_TAG_ORDER, tagIN.getTagOrder());
            values.put(SoulissDB.COLUMN_TAG_IMGPTH, tagIN.getImagePath());

            ret = database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + tagIN.getTagId(),
                    null);
            Log.i(Constants.TAG, "UPD TAG " + tagIN.getTagId() + " just updated rows:" + ret);

            List<SoulissTypical> typs = tagIN.getAssignedTypicals();
            for (SoulissTypical nowT : typs) {
                createOrUpdateTagTypicalNode(nowT, tagIN, 0);
                Log.i(Constants.TAG, "INSERTED TAG->TYP" + nowT.toString() + " TO " + tagIN.getNiceName());
            }
            return ret;
        } else {//brand new
            values.put(SoulissDB.COLUMN_TAG_ICONID, 0);
            // Inserisco e risetto il nome e l'ordine
            ret = (int) database.insert(SoulissDB.TABLE_TAGS, null, values);
            values.put(SoulissDB.COLUMN_TAG_NAME,
                    context.getResources().getString(R.string.tag) + " " + ret);
            values.put(SoulissDB.COLUMN_TAG_ORDER, ret);
            database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + ret, null);
            return ret;
        }

    }



    /**
     * relazione coi typ
     *
     * @param nodeIN
     * @return
     */
    public int createOrUpdateTagTypicalNode(SoulissTypical nodeIN, SoulissTag toAssoc, Integer priority) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_TAG_TYP_NODE_ID, nodeIN.getNodeId());
        values.put(SoulissDB.COLUMN_TAG_TYP_SLOT, nodeIN.getSlot());
        values.put(SoulissDB.COLUMN_TAG_TYP_TAG_ID, toAssoc.getTagId());
        values.put(SoulissDB.COLUMN_TAG_TYP_PRIORITY, priority);
        int upd = database.update(SoulissDB.TABLE_TAGS_TYPICALS, values, SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + nodeIN.getNodeId()
                        + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + nodeIN.getSlot() + " AND " + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + toAssoc.getTagId(),
                null);
        if (upd == 0) {
            long insertId = database.insert(SoulissDB.TABLE_TAGS_TYPICALS, null, values);
        }

        return upd;
    }

    /**
     * relazione coi typ
     *
     * @param nodeIN
     * @return
     */
    public int deleteTagTypicalNode(SoulissTypical nodeIN, SoulissTag toAssoc) {

        int upd = database.delete(SoulissDB.TABLE_TAGS_TYPICALS, SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + nodeIN.getNodeId()
                + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + nodeIN.getSlot()
                + " AND " + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + toAssoc.getTagId(), null);
        Log.w(Constants.TAG, "DELETE TAG->TYP" + nodeIN.toString() + " TO " + toAssoc.getNiceName());
        return upd;
    }
    public List<SoulissTag> getTagsByTypicals(SoulissTypical parent) {

        List<SoulissTag> comments = new ArrayList<>();
        String MY_QUERY = "SELECT * FROM " + SoulissDB.TABLE_TAGS_TYPICALS + " a "
                + " WHERE a." + SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + parent.getNodeId()
                + " AND a." + SoulissDB.COLUMN_TAG_TYP_SLOT + " =  " + parent.getSlot();
        Cursor cursor = database.rawQuery(MY_QUERY, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int tagId = cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_TYP_TAG_ID));
            try {
                SoulissTag newTag = getTag(tagId);
                if (!comments.contains(newTag))
                    comments.add(newTag);
            } catch (SQLDataException e) {
                e.printStackTrace();
            }

            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }


}
