package it.angelic.soulissclient.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
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


    public List<SoulissTag> getTags(Context context) {
        List<SoulissTag> comments = new ArrayList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_TAGS, SoulissDB.ALLCOLUMNS_TAGS,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTag dto = new SoulissTag();
            dto.setTagId(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ID));
            dto.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_NAME)));
            dto.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ICONID)));
            dto.setImagePath(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_IMGPTH)));
            //TODO fill list of typs

            // if (newTyp.getTypical() !=
            // Constants.Souliss_T_CurrentSensor_slave)
            comments.add(dto);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    /**
     * Crea un nuovo scenario vuoto
     *
     * @param nodeIN
     * @return
     */
    public long createOrUpdateTag(SoulissTag nodeIN) {
        ContentValues values = new ContentValues();
        long ret = -1;
        if (nodeIN != null) {
            // wrap values from object
            values.put(SoulissDB.COLUMN_TAG_NAME, nodeIN.getName());
            values.put(SoulissDB.COLUMN_TAG_ICONID, nodeIN.getIconResourceId());
            values.put(SoulissDB.COLUMN_TAG_IMGPTH, nodeIN.getImagePath());
            if (nodeIN.getTagId() != null) {
                ret = database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + nodeIN.getTagId(),
                        null);
            } else {
                ret = database.insert(SoulissDB.TABLE_TAGS, null, values);
            }
            List<SoulissTypical> typs = nodeIN.getAssignedTypicals();
            for (SoulissTypical nowT : typs) {
                createOrUpdateTagTypicalNode(nowT, nodeIN, 0);
            }
            return ret;
        } else {
            values.put(SoulissDB.COLUMN_TAG_ICONID, R.drawable.tv);
            // Inserisco e risetto il nome
            ret = (int) database.insert(SoulissDB.TABLE_TAGS, null, values);
            values.put(SoulissDB.COLUMN_TAG_NAME,
                    SoulissClient.getAppContext().getResources().getString(R.string.tag) + " " + ret);
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
    public int createOrUpdateTagTypicalNode(SoulissTypical nodeIN, SoulissTag toAssoc, Integer pri) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_TAG_TYP_NODE_ID, nodeIN.getNodeId());
        values.put(SoulissDB.COLUMN_TAG_TYP_SLOT, nodeIN.getSlot());
        values.put(SoulissDB.COLUMN_TAG_TYP_TAG_ID, toAssoc.getTagId());
        values.put(SoulissDB.COLUMN_TAG_TYP_PRIORITY, pri);
        int upd = database.update(SoulissDB.TABLE_TAGS_TYPICALS, values, SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + nodeIN.getNodeId()
                        + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + nodeIN.getSlot() + " AND " + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + toAssoc.getTagId(),
                null);
        if (upd == 0) {
            long insertId = database.insert(SoulissDB.TABLE_TAGS_TYPICALS, null, values);
        }

        return upd;
    }

}
