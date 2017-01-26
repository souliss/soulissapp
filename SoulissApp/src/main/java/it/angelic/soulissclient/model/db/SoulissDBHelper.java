package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;

import com.dacer.androidcharts.ClockPieHelper;

import java.io.File;
import java.sql.SQLDataException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import it.angelic.soulissclient.BuildConfig;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.fragments.TimeRangeEnum;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTrigger;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.SoulissTypicalFactory;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;
import it.angelic.soulissclient.util.FontAwesomeUtil;

import static it.angelic.soulissclient.Constants.MASSIVE_NODE_ID;
import static it.angelic.soulissclient.Constants.TAG;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 *
 * @author Ale
 */
public class SoulissDBHelper {

    // Database fields
    protected static SQLiteDatabase database;
    protected static SoulissDB soulissDatabase;
    protected final Context context;
    protected SoulissPreferenceHelper opts;

    public static synchronized SQLiteDatabase getDatabase() {
        return database;
    }

    public long getSize() {
        return new File(database.getPath()).length();
    }

    public SoulissDBHelper(Context context) {
        soulissDatabase = new SoulissDB(context);
        opts = SoulissApp.getOpzioni();
        this.context = context;
    }

    public static synchronized void open() throws SQLException {
        if (database == null || !database.isOpen())
            database = soulissDatabase.getWritableDatabase();
    }

    public void close() {
        soulissDatabase.close();
        if (database != null && database.isOpen())
            database.close();
        else
            Log.w(TAG, "DB already closed");
    }

    /**
     * presuppone che il nodo esista, asserError altrimenti Light update, solo
     * data ed health, pensato per JSON
     *
     * @param nodeIN
     * @return
     */
    @Deprecated
    public int refreshNode(SoulissNode nodeIN) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
        values.put(SoulissDB.COLUMN_NODE_HEALTH, nodeIN.getHealth());
        long upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + nodeIN.getNodeId(),
                null);

        assertEquals(upd, 1);
        return (int) upd;
    }

    public void clean() {
        if (database != null && database.isOpen()) {
            database.execSQL("VACUUM");
        } else
            Log.w(TAG, "DB closed, clean() failed");
    }

    /**
     * presuppone che il nodo esista, asserError altrimenti Light update, solo
     * data ed health, pensato per JSON. Skippa refresh se tipico vuoto
     *
     * @param nodeIN
     * @return
     */
    @Deprecated
    public int refreshNodeAndTypicals(SoulissNode nodeIN) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
        values.put(SoulissDB.COLUMN_NODE_HEALTH, nodeIN.getHealth());
        long upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + nodeIN.getNodeId(),
                null);

        assertEquals(upd, 1);

        List<SoulissTypical> tips = nodeIN.getTypicals();
        for (SoulissTypical x : tips) {
            if (!x.isEmpty())
                x.refresh();
        }

        return (int) upd;
    }

    /**
     * campi singoli altrimenti side effects
     *
     * @param nodeIN
     * @return
     */
    public int createOrUpdateNode(SoulissNode nodeIN) {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_NODE_NAME, nodeIN.getName());
        values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
        values.put(SoulissDB.COLUMN_NODE_ID, nodeIN.getNodeId());
        values.put(SoulissDB.COLUMN_NODE_HEALTH, nodeIN.getHealth());
        values.put(SoulissDB.COLUMN_NODE_ICON, nodeIN.getIconResourceId());
        int upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + nodeIN.getNodeId(),
                null);
        if (upd == 0) {
            long insertId = database.insert(SoulissDB.TABLE_NODES, null, values);
        }

        return upd;
    }

    /**
     * Crea un nuovo scenario vuoto
     *
     * @param nodeIN
     * @return
     */
    public int createOrUpdateScene(SoulissScene nodeIN) {
        ContentValues values = new ContentValues();

        if (nodeIN != null) {
            // wrap values from object
            values.put(SoulissDB.COLUMN_SCENE_ID, nodeIN.getId());
            values.put(SoulissDB.COLUMN_SCENE_ICON, nodeIN.getIconResourceId());
            values.put(SoulissDB.COLUMN_SCENE_NAME, nodeIN.toString());
            return database.update(SoulissDB.TABLE_SCENES, values, SoulissDB.COLUMN_SCENE_ID + " = " + nodeIN.getId(),
                    null);
        } else {
            values.put(SoulissDB.COLUMN_SCENE_ICON, FontAwesomeUtil.getCodeIndexByFontName(context, "fa-moon-o"));
            // Inserisco e risetto il nome
            int ret = (int) database.insert(SoulissDB.TABLE_SCENES, null, values);
            values.put(SoulissDB.COLUMN_SCENE_NAME,
                    context.getResources().getString(R.string.scene) + " " + ret);
            database.update(SoulissDB.TABLE_SCENES, values, SoulissDB.COLUMN_SCENE_ID + " = " + ret, null);
            return ret;
        }

    }

    public SoulissNode getSoulissNode(int nodeIN) {
        Cursor cursor = database.query(SoulissDB.TABLE_NODES, SoulissDB.ALLCOLUMNS_NODES, SoulissDB.COLUMN_NODE_ID
                + " = " + nodeIN, null, null, null, null);
        cursor.moveToFirst();
        SoulissNode ret = SoulissNode.cursorToNode(context, cursor);

        List<SoulissTypical> cod = getNodeTypicals(ret);
        // come sono grezzo, 'sta riga fa schifo
        for (SoulissTypical soulissTypical : cod) {
            // if (soulissTypical.getNodeId() == comment.getNodeId())
            ret.addTypical(soulissTypical);
        }
        cursor.close();
        return ret;
    }

    /**
     * @param parent fake node, id -1
     * @return
     */
    public List<SoulissTypical> getUniqueTypicals(SoulissNode parent) {
        ArrayList<SoulissTypical> comments = new ArrayList<>();
        HashSet<Short> pool = new HashSet<>();
        Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS, null, null, null, null,
                null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissTypical newTyp = SoulissTypicalFactory.getTypical(context, dto.getTypical(), parent, dto, opts);
            newTyp.setParentNode(parent);
            if (parent.getNodeId() == Constants.MASSIVE_NODE_ID) {
                //hack dto ID, could be different if parent is massive
                newTyp.getTypicalDTO().setNodeId(parent.getNodeId());
                newTyp.getTypicalDTO().setSlot(dto.getTypical());
            }
            // if (newTyp.getTypical() !=
            // Constants.Souliss_T_CurrentSensor_slave)
            if (!pool.contains(dto.getTypical())) {
                comments.add(newTyp);
                pool.add(dto.getTypical());
            }
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    /**
     * DB typical factory
     *
     * @param node
     * @param slot
     * @return produced Typical
     */
    public SoulissTypical getTypical(int node, short slot) {
        // query with primary key
        Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
                SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + node + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT + " = "
                        + slot, null, null, null, null);
        cursor.moveToFirst();
        SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
        //TAG? no join, perche 1 a n
        Cursor typTags = database.query(SoulissDB.TABLE_TAGS_TYPICALS, SoulissDB.ALLCOLUMNS_TAGS_TYPICAL,
                SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + dto.getNodeId()
                        + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + dto.getSlot(),
                null, null, null, null);
        typTags.moveToFirst();
        while (!typTags.isAfterLast()) {
            int tagId = typTags.getInt(typTags.getColumnIndex(SoulissDB.COLUMN_TAG_TYP_TAG_ID));
            if (tagId == SoulissDB.FAVOURITES_TAG_ID)
                dto.setFavourite(true);
            else
                dto.setTagged(true);
            typTags.moveToNext();
        }
        typTags.close();
        SoulissTypical ret = SoulissTypicalFactory.getTypical(context, dto.getTypical(), getSoulissNode(node), dto, opts);
        cursor.close();
        return ret;
    }

    /**
     * DB typical factory
     *
     * @return produced Typical
     */
    public List<SoulissTypical> getAllTypicals() {
        List<SoulissTypical> comments = new ArrayList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS, null, null, null, null, SoulissDB.COLUMN_TYPICAL_NODE_ID + " , " + SoulissDB.COLUMN_TYPICAL_SLOT);
        cursor.moveToFirst();

        List<SoulissNode> nodi = getAllNodes();

        while (!cursor.isAfterLast()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissTypical newTyp = SoulissTypicalFactory.getTypical(context, dto.getTypical(), nodi.get(dto.getNodeId()), dto, opts);
            //TAG? no join, perche 1 a n
            Cursor typTags = database.query(SoulissDB.TABLE_TAGS_TYPICALS, SoulissDB.ALLCOLUMNS_TAGS_TYPICAL,
                    SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + dto.getNodeId()
                            + " AND " + SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + dto.getSlot(),
                    null, null, null, null);
            typTags.moveToFirst();
            while (!typTags.isAfterLast()) {
                int tagId = typTags.getInt(typTags.getColumnIndex(SoulissDB.COLUMN_TAG_TYP_TAG_ID));
                if (tagId == SoulissDB.FAVOURITES_TAG_ID)
                    dto.setFavourite(true);
                else
                    dto.setTagged(true);
                typTags.moveToNext();
            }
            typTags.close();
            //hack dto ID, could be different if parent is massive
            newTyp.getTypicalDTO().setNodeId(dto.getNodeId());
            newTyp.setParentNode(nodi.get(dto.getNodeId()));
            // if (newTyp.getTypical() !=
            // Constants.Souliss_T_CurrentSensor_slave)
            comments.add(newTyp);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    public List<SoulissTypical> getNodeTypicals(SoulissNode parent) {


        List<SoulissTypical> comments = new ArrayList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
                SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + parent.getNodeId(), null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissTypical newTyp = SoulissTypicalFactory.getTypical(context, dto.getTypical(), parent, dto, opts);
            //TAG? no join, perche 1 a n
            Cursor typTags = database.query(SoulissDB.TABLE_TAGS_TYPICALS, SoulissDB.ALLCOLUMNS_TAGS_TYPICAL,
                    SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " + dto.getNodeId()
                    + " AND "+SoulissDB.COLUMN_TAG_TYP_SLOT + " = " + dto.getSlot(),
                    null, null, null, null);
            typTags.moveToFirst();
            while (!typTags.isAfterLast()) {
               int tagId = typTags.getInt(typTags.getColumnIndex(SoulissDB.COLUMN_TAG_TYP_TAG_ID));
                if (tagId == SoulissDB.FAVOURITES_TAG_ID)
                    dto.setFavourite(true);
                else
                    dto.setTagged(true);
                typTags.moveToNext();
            }
            typTags.close();
            //hack dto ID, could be different if parent is massive
            newTyp.getTypicalDTO().setNodeId(parent.getNodeId());
            newTyp.setParentNode(parent);
            // if (newTyp.getTypical() !=
            // Constants.Souliss_T_CurrentSensor_slave)
            comments.add(newTyp);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    public ArrayList<SoulissLogDTO> getTypicalLogs(SoulissTypical tgt) {
        ArrayList<SoulissLogDTO> comments = new ArrayList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_LOGS, SoulissDB.ALLCOLUMNS_LOGS, SoulissDB.COLUMN_LOG_NODE_ID
                + " = " + tgt.getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT + " = "
                + tgt.getSlot(), null, null, null, SoulissDB.COLUMN_LOG_DATE + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissLogDTO dto = new SoulissLogDTO(cursor);
            comments.add(dto);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    /**
     * torna la storia di uno slot, raggruppata per giorno
     *
     * @param tgt
     * @return
     */
    public HashMap<Date, SoulissHistoryGraphData> getHistoryTypicalLogs(ISoulissTypical tgt, int range) {
        HashMap<Date, SoulissHistoryGraphData> comments = new HashMap<>();

        Date dff;
        String limitCause = "";
        Calendar now = Calendar.getInstance();
        switch (range) {

            case 0:
                // tutti i dati
                break;
            case 2:
                now.add(Calendar.DATE, -7);
                limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
                break;
            case 1:
                now.add(Calendar.MONTH, -1);
                limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
                break;
            default:
                Log.e("DB", "Unexpected switch ERROR");
                break;
        }
        Cursor cursor = database.query(SoulissDB.TABLE_LOGS, new String[]{
                        "strftime('%Y-%m-%d', datetime((cldlogwhen/1000), 'unixepoch', 'localtime')) AS IDX",
                        "AVG(CAST(flologval AS FLOAT)) AS AVG", "MIN(CAST(flologval AS FLOAT)) AS MIN",
                        "MAX(CAST(flologval AS FLOAT)) AS MAX"}, SoulissDB.COLUMN_LOG_NODE_ID + " = "
                        + tgt.getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT + " = "
                        + tgt.getSlot() + limitCause + " ", null,
                "strftime('%Y-%m-%d', datetime((cldlogwhen/1000), 'unixepoch', 'localtime'))", null, "IDX ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                dff = Constants.yearFormat.parse(cursor.getString(0));
                SoulissHistoryGraphData dto = new SoulissHistoryGraphData(cursor, dff);
                comments.put(dto.key, dto);
            } catch (ParseException e) {
                Log.e(TAG, "getHistoryTypicalLogs", e);
            }

            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    /**
     * TO TEST
     *
     * @param tgt
     * @param range
     * @return
     */
    public int getTypicalOnDurationMsec(SoulissTypical tgt, TimeRangeEnum range) {
        LinkedHashMap<Date, Short> comments = getHistoryTypicalHashMap(tgt, range);
        int accumulator = 0;
        boolean firstGo = true;
        Date accStart = new Date();
        for (Date cur : comments.keySet()) {
            Short val = comments.get(cur);
            if (val != 0) {
                //spento, inizia nuovo per
                accStart = cur;
                firstGo = false;
            } else if (!firstGo) {
                accumulator += cur.getTime() - accStart.getTime();
                firstGo = true;
            }
        }
        return accumulator;
    }

    /**
     * TO TEST
     *
     * @param tgt
     * @param range
     * @return
     */
    public ArrayList<ClockPieHelper> getTypicalOnClockPie(SoulissTypical tgt, TimeRangeEnum range) {
        ArrayList<ClockPieHelper> clockPieHelperArrayList = new ArrayList<ClockPieHelper>();
        LinkedHashMap<Date, Short> comments = getHistoryTypicalHashMap(tgt, range);
        boolean firstGo = true;
        Date accStart = new Date();
        for (Date cur : comments.keySet()) {
            Short val = comments.get(cur);
            if (val != 0) {
                //spento, inizia nuovo per
                accStart = cur;
                firstGo = false;
            } else if (!firstGo) {
                Calendar start = Calendar.getInstance();
                Calendar stop = Calendar.getInstance();
                start.setTime(accStart);
                stop.setTime(cur);
                //aggiungo fetta sse piu di un minuto
                if (!(start.get(Calendar.HOUR_OF_DAY) == stop.get(Calendar.HOUR_OF_DAY) &&
                        (start.get(Calendar.MINUTE) == stop.get(Calendar.MINUTE)))) {
                    Log.d(Constants.TAG, "Aggiungo fetta dalle " + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE)
                            + " alle " + stop.get(Calendar.HOUR_OF_DAY) + ":" + stop.get(Calendar.MINUTE));
                    clockPieHelperArrayList.add(new ClockPieHelper(start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE),
                            stop.get(Calendar.HOUR_OF_DAY), stop.get(Calendar.MINUTE)));
                }
                firstGo = true;
            }
        }

        return clockPieHelperArrayList;
    }


    /**
     * torna la storia di uno slot, raggruppata per giorno
     *
     * @param tgt
     * @return
     */
    public LinkedHashMap<Date, Short> getHistoryTypicalHashMap(SoulissTypical tgt, TimeRangeEnum range) {
        LinkedHashMap<Date, Short> comments = new LinkedHashMap<>();

        Date dff;
        Short how;
        String limitCause = "";
        Calendar now = Calendar.getInstance();
        switch (range) {

            case ALL_DATA:
                // tutti i dati
                break;
            case LAST_WEEK:
                now.add(Calendar.DATE, -7);
                limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
                break;
            case LAST_MONTH:
                now.add(Calendar.MONTH, -1);
                limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
                break;
            case LAST_DAY:
                now.add(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
                limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
                break;
            default:
                Log.e("DB", "Unexpected switch ERROR");
                break;
        }
        Cursor cursor = database.query(SoulissDB.TABLE_LOGS, new String[]{
                SoulissDB.COLUMN_LOG_DATE,
                SoulissDB.COLUMN_LOG_VAL,
                //"strftime('%Y-%m-%d', datetime((cldlogwhen/1000), 'unixepoch', 'localtime')) AS IDX",
                //"AVG(CAST(flologval AS FLOAT)) AS AVG", "MIN(CAST(flologval AS FLOAT)) AS MIN",
                //"MAX(CAST(flologval AS FLOAT)) AS MAX"
        }
                , SoulissDB.COLUMN_LOG_NODE_ID + " = "
                + tgt.getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT + " = "
                + tgt.getSlot() + limitCause + " ", null, null, null, SoulissDB.COLUMN_LOG_DATE + " ASC");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            try {
                dff = new Date(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_DATE)));
                how = (short) cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_VAL));
                //SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
                //comments.put(dto.key, dto);
                comments.put(dff, how);
            } catch (Exception e) {
                Log.e(TAG, "getHistoryTypicalHashMap", e);
            }

            cursor.moveToNext();
        }
        cursor.close();
        return comments;
    }

    /**
     * Raggruppa i risultati di una query sui logs
     *
     * @param tgt     tipico da interrogare
     * @param groupBy mese o giorno
     * @param range   dei log da selezionare
     * @return
     */
    public SparseArray<SoulissGraphData> getGroupedTypicalLogs(ISoulissTypical tgt, String groupBy, int range) {
        SparseArray<SoulissGraphData> comments = new SparseArray<>();
        String limitCause = "";
        Calendar now = Calendar.getInstance();
        switch (range) {
            case 0:
                // tutti i dati
                break;
            case 2:
                now.add(Calendar.DATE, -7);
                limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
                break;
            case 1:
                now.add(Calendar.MONTH, -1);
                limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
                break;
            default:
                Log.e("DB", "Unexpected switch ERROR");
                break;
        }
        int tot;
        if (groupBy.compareTo("%m") == 0)
            tot = 12;
        else if (groupBy.compareTo("%w") == 0)
            tot = 7;
        else
            tot = 24;
        for (int i = 0; i < tot; i++) {
            comments.put(i, new SoulissGraphData());
        }
        Log.d(Constants.TAG, "QUERY GROUPED:");

        Cursor cursor = database.query(SoulissDB.TABLE_LOGS, new String[]{
                        "strftime('" + groupBy +
                                "', datetime((cldlogwhen/1000), 'unixepoch', 'localtime')) AS IDX",
                        "AVG(CAST(flologval AS FLOAT)) AS AVG",
                        "MIN(CAST(flologval AS FLOAT)) AS MIN",
                        "MAX(CAST(flologval AS FLOAT)) AS MAX"},

                SoulissDB.COLUMN_LOG_NODE_ID
                        + " = "// selection
                        + tgt.getNodeId() + " AND " +
                        SoulissDB.COLUMN_LOG_SLOT + " = "
                        + tgt.getSlot() + limitCause + " ",

                null,// String[] selectionArgs


                "strftime('" + groupBy// GROUP BY
                        + "', datetime((cldlogwhen/1000), 'unixepoch', 'localtime'))",

                null, // HAVING

                "IDX ASC");// ORDER BY
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            SoulissGraphData dto = new SoulissGraphData(cursor);
            //assertEquals(true, dto.key >= 0);
            comments.put(Integer.parseInt(dto.key), dto);
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    /**
     * Return antitheft master typical
     *
     * @return produced Typical
     */
    public SoulissTypical41AntiTheft getAntiTheftMasterTypical() {
        // query with primary key
        Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
                SoulissDB.COLUMN_TYPICAL + " = "
                        + Constants.Typicals.Souliss_T41_Antitheft_Main, null, null,
                null, null);
        if (cursor.moveToFirst()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissTypical41AntiTheft ret = (SoulissTypical41AntiTheft) SoulissTypicalFactory.getTypical(context, dto.getTypical(),
                    getSoulissNode(dto.getNodeId()), dto, opts);
            cursor.close();
            return ret;
        } else
            throw new NoSuchElementException();
    }

    public List<SoulissTypical> getAntiTheftSensors() {
        List<SoulissTypical> comments = new ArrayList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
                SoulissDB.COLUMN_TYPICAL + " = "
                        + Constants.Typicals.Souliss_T42_Antitheft_Peer, null, null,
                null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissNode parent = getSoulissNode(dto.getNodeId());
            SoulissTypical42AntiTheftPeer newTyp = (SoulissTypical42AntiTheftPeer) SoulissTypicalFactory.getTypical(context,
                    dto.getTypical(), parent, dto, opts);
            newTyp.setParentNode(parent);
            // if (newTyp.getTypical() !=
            // Constants.Souliss_T_CurrentSensor_slave)
            comments.add(newTyp);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();

        cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS, SoulissDB.COLUMN_TYPICAL
                        + " = " + Constants.Typicals.Souliss_T43_Antitheft_LocalPeer, null,
                null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissNode parent = getSoulissNode(dto.getNodeId());
            SoulissTypical43AntiTheftLocalPeer newTyp = (SoulissTypical43AntiTheftLocalPeer) SoulissTypicalFactory.getTypical(context, dto.getTypical(), parent, dto, opts);
            newTyp.setParentNode(parent);
            // if (newTyp.getTypical() !=
            // Constants.Souliss_T_CurrentSensor_slave)
            comments.add(newTyp);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();

        return comments;
    }


    public SoulissTriggerDTO getSoulissTrigger(long insertId) {
        Cursor cursor = database.query(SoulissDB.TABLE_TRIGGERS, SoulissDB.ALLCOLUMNS_TRIGGERS,
                SoulissDB.COLUMN_TRIGGER_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        SoulissTriggerDTO dto = new SoulissTriggerDTO(cursor);
        // SoulissTypical ret = SoulissTypical.typicalFactory(dto.getTypical(),
        // getSoulissNode(node), dto);
        cursor.close();
        return dto;
    }

    /*
     * public void deleteTypical(int nodeid, SoulissTypical comment) { // long
     * id = comment.getNodeId(); // System.out.println("Comment deleted with id: " +
     * id); database.delete(SoulissDB.TABLE_TYPICALS, SoulissDB.COLUMN_ID +
     * " = " + nodeid, null); }
     */
    public int deleteCommand(SoulissCommand toRename) {
        return database.delete(SoulissDB.TABLE_COMMANDS, SoulissDB.COLUMN_COMMAND_ID + " = "
                + toRename.getCommandId(), null);
    }

    public int truncateImportTables() {
        int ret;
        ret = database.delete(SoulissDB.TABLE_LOGS, null, null);
        ret += database.delete(SoulissDB.TABLE_TYPICALS, null, null);
        ret += database.delete(SoulissDB.TABLE_NODES, null, null);
        return ret;

    }

    public void truncateAll() {
        if (soulissDatabase != null) {
            Log.w(TAG, "DB dropCreate !!!");
            soulissDatabase.dropCreate(database);
        }
    }

    public int deleteScene(SoulissScene toRename) {
        database.delete(SoulissDB.TABLE_COMMANDS, SoulissDB.COLUMN_COMMAND_SCENEID + " = " + toRename.getId(), null);
        return database.delete(SoulissDB.TABLE_SCENES, SoulissDB.COLUMN_SCENE_ID + " = " + toRename.getId(), null);
    }

    public int deleteTag(SoulissTag toRename) {
        //CASCADE sulle associazioni
        database.delete(SoulissDB.TABLE_TAGS_TYPICALS, SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + toRename.getTagId(), null);
        //CASCADE sui figli
        database.delete(SoulissDB.TABLE_TAGS, SoulissDB.COLUMN_TAG_FATHER_ID + " = " + toRename.getTagId(), null);
        return database.delete(SoulissDB.TABLE_TAGS, SoulissDB.COLUMN_TAG_ID + " = " + toRename.getTagId(), null);
    }

    public int deleteTagTypical(long tagId, int nodeid, int slot) {
        //elimino associazione
        return database.delete(SoulissDB.TABLE_TAGS_TYPICALS, SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + tagId
                +" AND "+SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = " +nodeid
                +" AND "+SoulissDB.COLUMN_TAG_TYP_SLOT+ " = " +slot, null);
    }

    public List<SoulissNode> getAllNodes() {
        List<SoulissNode> comments = new ArrayList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_NODES, SoulissDB.ALLCOLUMNS_NODES, null, null, null, null,
                SoulissDB.COLUMN_NODE_ID);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            SoulissNode comment = SoulissNode.cursorToNode(context, cursor);
            List<SoulissTypical> cod = getNodeTypicals(comment);
            // come sono grezzo, 'sta riga fa schifo
            for (SoulissTypical soulissTypical : cod) {
                // if (soulissTypical.getNodeId() == comment.getNodeId())
                comment.addTypical(soulissTypical);
            }
            comments.add(comment);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }

    /**
     * Conta i non vuoti diversi da Souliss_T_related
     *
     * @return
     */
    public int countTypicals() {
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TYPICALS + " where "
                + SoulissDB.COLUMN_TYPICAL + " <> "
                + Constants.Typicals.Souliss_T_related
                +" AND "+ SoulissDB.COLUMN_TYPICAL + " <> "
                + Constants.Typicals.Souliss_T_empty, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countNodes() {
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_NODES, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countFavourites() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TAGS_TYPICALS + " where "
                + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " =  " + SoulissDB.FAVOURITES_TAG_ID, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countTypicalTags() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TAGS_TYPICALS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countTags() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TAGS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countScenes() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_SCENES, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public int countTriggers() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TRIGGERS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public List<SoulissTag> getRootTags(Context context) {
        List<SoulissTag> comments = new ArrayList<>();
        if (!database.isOpen())
            open();
        //solo radici
        Cursor cursor = database.query(SoulissDB.TABLE_TAGS, SoulissDB.ALLCOLUMNS_TAGS,
                SoulissDB.COLUMN_TAG_FATHER_ID + " IS NULL ", null, null, null, SoulissDB.COLUMN_TAG_ORDER + ", " + SoulissDB.COLUMN_TAG_ID);
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
            dto.setChildTags(getTagChild(dto));

            comments.add(dto);
            cursor.moveToNext();
        }
        cursor.close();
        return comments;
    }

    private List<SoulissTag> getTagChild(SoulissTag fatherDto) {
        List<SoulissTag> ret = new ArrayList<>();
        if (!database.isOpen())
            open();
        Cursor cursor = database.query(SoulissDB.TABLE_TAGS, SoulissDB.ALLCOLUMNS_TAGS,
                SoulissDB.COLUMN_TAG_FATHER_ID + " = " + fatherDto.getTagId(), null, null, null, null);
        if (cursor.isLast()) {
            cursor.close();
            return ret;//basta figli
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTag dtoI = new SoulissTag();
            dtoI.setTagOrder(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ORDER)));
            dtoI.setTagId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ID)));
            dtoI.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_NAME)));
            dtoI.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ICONID)));
            dtoI.setImagePath(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_IMGPTH)));
            dtoI.setFatherId(fatherDto.getTagId());
            Log.i(Constants.TAG, "retrieving TAG CHILD OF:" + fatherDto.getTagId() + " CHILD ID: " + dtoI.getTagId());
            fatherDto.setAssignedTypicals(getTagTypicals(dtoI));
            dtoI.setChildTags(getTagChild(dtoI));//recursive
            ret.add(dtoI);
            cursor.moveToNext();
        }
        cursor.close();


        return ret;
    }

    public SoulissTag getTag(long tagId) throws SQLDataException {
        SoulissTag dto = new SoulissTag();
        if (!database.isOpen())
            open();
        Cursor cursor = database.query(SoulissDB.TABLE_TAGS, SoulissDB.ALLCOLUMNS_TAGS,
                SoulissDB.COLUMN_TAG_ID + " = " + tagId, null, null, null, null);
        if (cursor.isLast())
            throw new SQLDataException("Non Existing TagId:" + tagId);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dto.setTagOrder(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ORDER)));
            dto.setTagId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ID)));
            dto.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_NAME)));
            dto.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_ICONID)));
            dto.setImagePath(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_IMGPTH)));
            Long l = null;
            if (!cursor.isNull(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_FATHER_ID)))
                l = cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_TAG_FATHER_ID));
            dto.setFatherId(l);
            Log.i(Constants.TAG, "retrieving TAG:" + dto.getTagId() + " ORDER:" + dto.getTagOrder() + " Father:" + dto.getFatherId());
            dto.setAssignedTypicals(getTagTypicals(dto));
            dto.setChildTags(getTagChild(dto));
            cursor.moveToNext();
        }
        cursor.close();
        return dto;
    }

    public List<SoulissTypical> getTagTypicals(SoulissTag parent) {

        List<SoulissTypical> comments = new ArrayList<>();
        String MY_QUERY = "SELECT * FROM " + SoulissDB.TABLE_TAGS_TYPICALS + " a "
                + " INNER JOIN " + SoulissDB.TABLE_TYPICALS + " b "
                + " ON a." + SoulissDB.COLUMN_TAG_TYP_NODE_ID + " = b." + SoulissDB.COLUMN_TYPICAL_NODE_ID
                + " AND a." + SoulissDB.COLUMN_TAG_TYP_SLOT + " = b." + SoulissDB.COLUMN_TYPICAL_SLOT
                + " WHERE a." + SoulissDB.COLUMN_TAG_TYP_TAG_ID + " = " + parent.getTagId()
                + " ORDER BY a." + SoulissDB.COLUMN_TAG_TYP_PRIORITY;
        Cursor cursor = database.rawQuery(MY_QUERY, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            SoulissNode par = getSoulissNode(dto.getNodeId());
            SoulissTypical newTyp = SoulissTypicalFactory.getTypical(context, dto.getTypical(), par, dto, opts);
            //hack dto ID, could be different if parent is massive
            newTyp.getTypicalDTO().setNodeId(dto.getNodeId());

            //Se e` qui, e` taggato
            if (parent.getTagId() == 0)
                newTyp.getTypicalDTO().setFavourite(true);
            newTyp.getTypicalDTO().setTagged(true);
            comments.add(newTyp);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return comments;
    }
    public List<SoulissTrigger> getAllTriggers(Context context) {
        List<SoulissTrigger> ret = new ArrayList<>();
        // Cursor cursor = database.query(SoulissDB.TABLE_TRIGGERS,
        // SoulissDB.ALLCOLUMNS_TRIGGERS, null, null, null, null,null);
        // String MY_QUERY =
        // "SELECT * FROM "+SoulissDB.TABLE_TRIGGERS+" a INNER JOIN "+SoulissDB.TABLE_COMMANDS
        // +" b ON a.cmdid = b.cmdid WHERE b.property_id=?";
        String MY_QUERY = "SELECT * FROM " + SoulissDB.TABLE_TRIGGERS + " a " + "INNER JOIN "
                + SoulissDB.TABLE_COMMANDS + " b ON a." + SoulissDB.COLUMN_TRIGGER_COMMAND_ID + " = b."
                + SoulissDB.COLUMN_COMMAND_ID;
        Cursor cursor = database.rawQuery(MY_QUERY, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            SoulissCommandDTO cmd = new SoulissCommandDTO(cursor);
            SoulissTypical tgt = getTypical(cmd.getNodeId(), cmd.getSlot());
            SoulissTrigger cols = new SoulissTrigger(cmd, tgt);
            SoulissTriggerDTO comment = new SoulissTriggerDTO(cursor);

            cols.setTriggerDTO(comment);

            ret.add(cols);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return ret;
    }

    public SoulissTriggerDTO getTriggerByCommandId(Context context, long triggerId) {

        String MY_QUERY = "SELECT * FROM " + SoulissDB.TABLE_TRIGGERS + " a " + "INNER JOIN "
                + SoulissDB.TABLE_COMMANDS + " b ON a." + SoulissDB.COLUMN_TRIGGER_COMMAND_ID + " = b."
                + SoulissDB.COLUMN_COMMAND_ID + " WHERE " + SoulissDB.COLUMN_TRIGGER_COMMAND_ID + " = " + triggerId;
        Cursor cursor = database.rawQuery(MY_QUERY, null);
        cursor.moveToFirst();
        if(BuildConfig.DEBUG && !(cursor.getColumnCount() == 1))
            throw new RuntimeException("cursor.getColumnCount() != 1");
        SoulissTriggerDTO comment = new SoulissTriggerDTO(cursor);

        // cols.setTriggerDTO(comment);
        // Make sure to close the cursor
        cursor.close();
        return comment;
    }

    /**
     * Ritorna mappa di tutti i comandi, indicizzati per ID
     *
     * @param ct
     * @return
     */
    public SparseArray<SoulissTriggerDTO> getTriggerMap(Context ct) {
        SparseArray<SoulissTriggerDTO> ret = new SparseArray<>();

        Cursor cursor = database.query(SoulissDB.TABLE_TRIGGERS, SoulissDB.ALLCOLUMNS_TRIGGERS, null, null, null, null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            SoulissTriggerDTO dto = new SoulissTriggerDTO(cursor);
            ret.put((int) dto.getCommandId(), dto);
            cursor.moveToNext();
        }

        cursor.close();
        return ret;

    }

    public LinkedList<SoulissCommand> getUnexecutedCommands(Context context) {
        LinkedList<SoulissCommand> ret = new LinkedList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_COMMANDS, SoulissDB.ALLCOLUMNS_COMMANDS, " ("
                        + SoulissDB.COLUMN_COMMAND_EXECTIME + " is null OR " + SoulissDB.COLUMN_COMMAND_TYPE + " ="
                        + Constants.COMMAND_COMEBACK_CODE + " OR " + SoulissDB.COLUMN_COMMAND_TYPE + " ="
                        + Constants.COMMAND_GOAWAY_CODE + " OR " + SoulissDB.COLUMN_COMMAND_TYPE + " ="
                        + Constants.COMMAND_TRIGGERED + ") AND " + SoulissDB.COLUMN_COMMAND_SCENEID + " IS NULL", null, null,
                null, SoulissDB.COLUMN_COMMAND_SCHEDTIME);
        cursor.moveToFirst();
        Log.d(Constants.TAG, "Found unexecuted commands:" + cursor.getCount());
        while (!cursor.isAfterLast()) {
            SoulissCommandDTO comment = new SoulissCommandDTO(cursor);
            cursor.moveToNext();
            short node = comment.getNodeId();
            short slot = comment.getSlot();
            SoulissCommand adding = null;
            if (node > Constants.MASSIVE_NODE_ID) {
                SoulissTypical tgt = getTypical(node, slot);
                //hack nodeId, Massive
                tgt.getTypicalDTO().setNodeId(node);
                tgt.getTypicalDTO().setSlot(slot);
                adding = new SoulissCommand(context, comment, tgt);
            } else if (node > Constants.COMMAND_FAKE_SCENE) {
                // List massivi = getUniqueTypicals(node);
                SoulissNode minchia = new SoulissNode(context, Constants.MASSIVE_NODE_ID);
                List<SoulissTypical> massivi = getUniqueTypicals(minchia);
                Log.d(Constants.TAG, "Massive command found, Typical:" + slot);
                for (SoulissTypical cazzuto : massivi) {
                    if (slot == cazzuto.getTypicalDTO().getTypical()) {
                        adding = new SoulissCommand(context, comment, cazzuto);
                    }
                }

            } else {
                //scena, ovvero id scena da eseguire = slot
                adding = new SoulissCommand(context, comment);
            }
            assertNotNull(adding);
            ret.add(adding);
        }
        cursor.close();
        return ret;
    }

    public ArrayList<SoulissCommand> getSceneCommands(int sceneId) {
        ArrayList<SoulissCommand> ret = new ArrayList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_COMMANDS, SoulissDB.ALLCOLUMNS_COMMANDS,
                SoulissDB.COLUMN_COMMAND_SCENEID + " =" + sceneId, null, null, null,
                SoulissDB.COLUMN_COMMAND_SCHEDTIME);//se scenario, e` lo step
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            SoulissCommandDTO comment = new SoulissCommandDTO(cursor);
            // comments.add(comment);
            cursor.moveToNext();
            short node = comment.getNodeId();
            short slot = comment.getSlot();
            SoulissCommand adding;
            if (node > Constants.MASSIVE_NODE_ID) {
                SoulissTypical tgt = getTypical(node, slot);
                tgt.getTypicalDTO().setNodeId(node);
                tgt.getTypicalDTO().setSlot(slot);
                adding = new SoulissCommand(context, comment, tgt);
                // comando massivo
            } else {
                SoulissNode fake = new SoulissNode(context, Constants.MASSIVE_NODE_ID);
                SoulissTypical tgt = new SoulissTypical(context, opts);
                tgt.setParentNode(fake);
                tgt.getTypicalDTO().setNodeId(Constants.MASSIVE_NODE_ID);
                // in caso di comando massivo, SLOT = TYPICAL
                tgt.getTypicalDTO().setTypical(slot);
                adding = new SoulissCommand(context, comment, tgt);
            }
            ret.add(adding);
        }
        // Make sure to close the cursor
        cursor.close();
        return ret;
    }

    public LinkedList<SoulissScene> getScenes(Context context) {
        LinkedList<SoulissScene> ret = new LinkedList<>();
        Cursor cursor = database.query(SoulissDB.TABLE_SCENES, SoulissDB.ALLCOLUMNS_SCENES, null, null, null, null,
                SoulissDB.COLUMN_SCENE_ID);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            SoulissScene comment = new SoulissScene(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_ID)));
            comment.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_NAME)));
            comment.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_ICON)));
            cursor.moveToNext();
            ArrayList<SoulissCommand> cmds = getSceneCommands(comment.getId());
            comment.setCommandArray(cmds);
            ret.add(comment);
        }
        // Make sure to close the cursor
        cursor.close();
        return ret;
    }

    public SoulissScene getScene(int sceneId) {
        Cursor cursor = database.query(SoulissDB.TABLE_SCENES, SoulissDB.ALLCOLUMNS_SCENES, SoulissDB.COLUMN_SCENE_ID + " =" + sceneId, null, null, null,
                SoulissDB.COLUMN_SCENE_ID);
        cursor.moveToFirst();

        SoulissScene comment = new SoulissScene(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_ID)));
        comment.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_NAME)));
        comment.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_ICON)));

        ArrayList<SoulissCommand> cmds = getSceneCommands(comment.getId());
        comment.setCommandArray(cmds);
        cursor.close();
        return comment;
    }

    public LinkedList<SoulissCommand> getPositionalPrograms(Context soulissDataService) {
        LinkedList<SoulissCommand> ret = new LinkedList<>();
        Cursor cursor = database
                .query(SoulissDB.TABLE_COMMANDS, SoulissDB.ALLCOLUMNS_COMMANDS, SoulissDB.COLUMN_COMMAND_TYPE + " = "
                        + Constants.COMMAND_COMEBACK_CODE + " OR " + SoulissDB.COLUMN_COMMAND_TYPE + " = "
                        + Constants.COMMAND_GOAWAY_CODE, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            SoulissCommandDTO comment = new SoulissCommandDTO(cursor);
            // comments.add(comment);
            cursor.moveToNext();
            short node = comment.getNodeId();
            short slot = comment.getSlot();
            SoulissCommand adding;
            if (node > MASSIVE_NODE_ID) {
                SoulissTypical parentTypical = getTypical(node, slot);
                adding = new SoulissCommand(context, comment, parentTypical);
            } else {
                adding = new SoulissCommand(context, comment);
            }
            adding.setSceneId(null);
            ret.add(adding);
        }
        cursor.close();
        return ret;
    }


}
