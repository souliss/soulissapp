package it.angelic.soulissclient.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.SoulissTypicalFactory;

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

            dto.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_NAME)));
            dto.setIconId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ICONID)));
            dto.setImagePath(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_IMGPTH)));
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
    public int createOrUpdateTag(SoulissTag nodeIN) {
        ContentValues values = new ContentValues();

        if (nodeIN != null) {
            // wrap values from object
            values.put(SoulissDB.COLUMN_TAG_NAME, nodeIN.getName());
            values.put(SoulissDB.COLUMN_TAG_ICONID, nodeIN.getIconId());
            values.put(SoulissDB.COLUMN_TAG_IMGPTH, nodeIN.getImagePath());
            return database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + nodeIN.getTagId(),
                    null);
        } else {
            values.put(SoulissDB.COLUMN_TAG_ICONID, R.drawable.tv);
            // Inserisco e risetto il nome
            int ret = (int) database.insert(SoulissDB.TABLE_TAGS, null, values);
            values.put(SoulissDB.COLUMN_TAG_NAME,
                    SoulissClient.getAppContext().getResources().getString(R.string.tag) + " " + ret);
            database.update(SoulissDB.TABLE_TAGS, values, SoulissDB.COLUMN_TAG_ID + " = " + ret, null);
            return ret;
        }

    }

}
