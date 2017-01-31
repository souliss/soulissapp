package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.SoulissModelException;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.tagviewlib.SimpleTagViewUtils;

import static junit.framework.Assert.assertEquals;

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
            values.put(SoulissDB.COLUMN_TAG_FATHER_ID, tagIN.getFatherId());

            ret = database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + tagIN.getTagId(),
                    null);
            Log.i(Constants.TAG, "Update Tag " + tagIN.getTagId() + " in progress - just updated rows:" + ret + " father" + values.get(SoulissDB.COLUMN_TAG_FATHER_ID));

            List<SoulissTypical> typs = tagIN.getAssignedTypicals();
            int i = 0;
            for (SoulissTypical nowT : typs) {
                createOrUpdateTagTypicalNode(nowT, tagIN, i++);
                Log.i(Constants.TAG, "INSERTED TAG->TYP" + nowT.toString() + " TO " + tagIN.getNiceName());
            }

            List<SoulissTag> subTags = tagIN.getChildTags();
            for (SoulissTag nowTag :
                    subTags) {
                //recursive call
                Log.i(Constants.TAG, "INSERTED TAG->TAG" + nowTag.toString() + " TO " + tagIN.getNiceName());
                createOrUpdateTag(nowTag);
            }

            return ret;
        } else {//brand new
            values.put(SoulissDB.COLUMN_TAG_ICONID, SimpleTagViewUtils.getAwesomeNames(context).indexOf(FontAwesomeEnum.fa_tag.getFontName()));
            // Inserisco e risetto il nome e l'ordine
            ret = (int) database.insert(SoulissDB.TABLE_TAGS, null, values);
            values.put(SoulissDB.COLUMN_TAG_NAME, context.getResources().getString(R.string.tag) + " " + ret);
            //if (father != null)//forse non serve
            values.put(SoulissDB.COLUMN_TAG_FATHER_ID, (Long) null);
            values.put(SoulissDB.COLUMN_TAG_ORDER, ret);
            database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + ret, null);
            Log.i(Constants.TAG, "CREATED Tag " + ret + " in progress - just updated rows:" + ret);

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

    public List<SoulissTag> getAllTagsWithoutChildren(Context context) {
        List<SoulissTag> comments = new ArrayList<>();
        if (!database.isOpen())
            open();
        //solo radici
        Cursor cursor = database.query(SoulissDB.TABLE_TAGS, SoulissDB.ALLCOLUMNS_TAGS, null, null, null, null, SoulissDB.COLUMN_TAG_ORDER + ", " + SoulissDB.COLUMN_TAG_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTag dto = new SoulissTag();
            dto.setTagId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ID)));
            dto.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_NAME)));
            dto.setTagOrder(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ORDER)));
            dto.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ICONID)));
            dto.setImagePath(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_IMGPTH)));
            dto.setFatherId(null);
            Log.i(Constants.TAG, "retrieving ROOT TAG:" + dto.getTagId() + " ORDER:" + dto.getTagOrder());
            dto.setAssignedTypicals(getTagTypicals(dto));

            comments.add(dto);
            cursor.moveToNext();
        }
        cursor.close();
        return comments;
    }

    public List<SoulissTypical> getFavouriteTypicals() {

        SoulissTag fake = new SoulissTag();
        fake.setTagId(0);
        return getTagTypicals(fake);
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

    /**
     * Order-only UPDATE
     *
     * @param nodeIN
     * @return
     */
    public int refreshTag(SoulissTag nodeIN) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_TAG_ORDER, nodeIN.getTagOrder());
        long upd = database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + nodeIN.getTagId(),
                null);

        assertEquals(upd, 1);
        return (int) upd;
    }

    public int refreshTagTypical(SoulissTypical nodeIN, SoulissTag father, @NonNull Integer priority) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_TAG_TYP_PRIORITY, priority);
        long upd = database.update(SoulissDB.TABLE_TAGS_TYPICALS, values, SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + nodeIN.getNodeId()
                        + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + nodeIN.getSlot()
                        + " AND " + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + father.getTagId(),
                null);

        assertEquals(upd, 1);
        return (int) upd;
    }

    /**
     * Come me li dai li setto...
     * <p>
     * Tutti gli oggetti che possono essere typ o tag vengono
     * ri-settati in base alla loro posizione in freshTagTyps
     *
     * @param freshTagTyps
     * @param fatherTag
     */
    public void updateTagTypicalsOrder(List<ISoulissObject> freshTagTyps, SoulissTag fatherTag) {
        for (int i = 0; i < freshTagTyps.size(); i++) {
            if (freshTagTyps.get(i) instanceof SoulissTypical) {
                refreshTagTypical((SoulissTypical) freshTagTyps.get(i), fatherTag, i);
                // createOrUpdateTagTypicalNode((SoulissTypical) freshTagTyps.get(i), fatherTag, i);
            } else if (freshTagTyps.get(i) instanceof SoulissTag) {
                SoulissTag sorting = (SoulissTag) freshTagTyps.get(i);
                sorting.setTagOrder(i);
                refreshTag(sorting);
            } else
                throw new SoulissModelException("E ADESSO DOVE SI VA?");
        }
    }
}
