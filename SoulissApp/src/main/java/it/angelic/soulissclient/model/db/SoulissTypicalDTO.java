package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.SoulissTypical;

import static it.angelic.soulissclient.Constants.TAG;
import static junit.framework.Assert.assertTrue;

public class SoulissTypicalDTO implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -7375342157142543740L;
    private String name;
    private short nodeId;
    private short slot = -1;
    private short typical;
    private short output;
    private int iconId;
    private int warnDelayMsec;
    private boolean isFavourite;

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean isTagged) {
        this.isTagged = isTagged;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    private boolean isTagged;
    private short inputCommand;
    private Calendar refreshedAt;

    public SoulissTypicalDTO() {
        // niente di fatto
    }

    public SoulissTypicalDTO(Cursor cursor) {
        // byte typ = (byte) cursor.getShort(1);
        setNodeId(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_NODE_ID)));
        setTypical(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL)));
        setSlot(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_SLOT)));
        setInput((byte) cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_INPUT)));
        setOutput(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_VALUE)));
        setIconId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_ICON)));
        setWarnDelayMsec(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_WARNTIMER)));
        //setFavourite(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_ISFAV)));
        setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_NAME)));
        Calendar now = Calendar.getInstance();
        now.setTime(new Date(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_LASTMOD))));
        setRefreshedAt(now);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SoulissTypicalDTO))
            return false;

        SoulissTypicalDTO tg = (SoulissTypicalDTO) o;
        return tg.getNodeId() == nodeId && tg.getTypical() == typical && tg.getSlot() == slot;

    }

    @Override
    public int hashCode() {
        return getNodeId();
    }

    /**
     * Aggiorna un tipico, pensato per JSON TYP, SLO e VAL
     *
     * @return
     */
    public int persist() {
        SoulissDBHelper.open();
        SQLiteDatabase db = SoulissDBHelper.getDatabase();
        int upd;
        ContentValues values = new ContentValues();
        assertTrue(getSlot() != -1);
        values.put(SoulissDB.COLUMN_TYPICAL_NODE_ID, getNodeId());
        values.put(SoulissDB.COLUMN_TYPICAL, getTypical());
        values.put(SoulissDB.COLUMN_TYPICAL_SLOT, getSlot());
        values.put(SoulissDB.COLUMN_TYPICAL_NAME, getName());
        values.put(SoulissDB.COLUMN_TYPICAL_ICON, getIconId());
        values.put(SoulissDB.COLUMN_TYPICAL_VALUE, getOutput());
        values.put(SoulissDB.COLUMN_TYPICAL_WARNTIMER, getWarnDelayMsec());
        //values.put(SoulissDB.COLUMN_TYPICAL_ISFAV, getFavourite());
        values.put(SoulissDB.COLUMN_TYPICAL_LASTMOD, Calendar.getInstance().getTime().getTime());

        upd = SoulissDBHelper.getDatabase().update(
                SoulissDB.TABLE_TYPICALS,
                values,
                SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + getNodeId() + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT
                        + " = " + getSlot(), null);
        if (upd == 0) {//magari non esiste, prova insert
            try {
                upd = (int) db.insert(SoulissDB.TABLE_TYPICALS, null, values);
            } catch (SQLiteConstraintException sqe) {
                //esiste, magari il primo fail era sfiga. Riproviamo...
                upd = SoulissDBHelper.getDatabase().update(
                        SoulissDB.TABLE_TYPICALS,
                        values,
                        SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + getNodeId() + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT
                                + " = " + getSlot(), null);
            }
        }
        if (upd == 0) {
            //Se ancora 0, qualcosa è storto di sicuro
            Log.w(TAG, "WARNING: UNPERSISTED TYPICAL! ");
        }
        return upd;
    }


    public Date getLastStatusChange() {
        Cursor cursor = SoulissDBHelper.getDatabase().query(
                SoulissDB.TABLE_LOGS,
                new String[]{SoulissDB.COLUMN_LOG_DATE, SoulissDB.COLUMN_LOG_VAL,
                        // "strftime('%Y-%m-%d', datetime((cldlogwhen/1000), 'unixepoch', 'localtime')) AS IDX",
                        // "AVG(CAST(flologval AS FLOAT)) AS AVG",
                        // "MIN(CAST(flologval AS FLOAT)) AS MIN",
                        // "MAX(CAST(flologval AS FLOAT)) AS MAX"
                },
                SoulissDB.COLUMN_LOG_NODE_ID + " = " + this.getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT + " = "
                        + this.getSlot() + " AND " + SoulissDB.COLUMN_LOG_ID + " = ( SELECT MAX("
                        + SoulissDB.COLUMN_LOG_ID + ") FROM " + SoulissDB.TABLE_LOGS + " WHERE "
                        + SoulissDB.COLUMN_LOG_NODE_ID + " = " + this.getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT
                        + " = " + this.getSlot() + ")", null, null, null, SoulissDB.COLUMN_LOG_DATE + " ASC");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            try {
                Date dff = new Date(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_DATE)));
                cursor.close();
                return dff;
            } catch (Exception e) {
                Log.e(TAG, "getHistoryTypicalHashMap", e);
            }

            cursor.moveToNext();
        }
        return null;
    }

    /**
     * Aggiorna un tipico, ma solo il valore eventualmente logga storia
     *
     * @return
     */
    public int refresh(@NonNull SoulissTypical parent) {
        //log di àbasso livelloà sse no sensor ne related
        if (SoulissApp.getOpzioni().isLogHistoryEnabled() && !(parent.isSensor() || parent.isRelated())) {
            // se e` un sensore viene loggato altrove
            Cursor cursor = SoulissDBHelper.getDatabase().query(
                    SoulissDB.TABLE_TYPICALS,
                    SoulissDB.ALLCOLUMNS_TYPICALS,
                    SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + nodeId + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT + " = "
                            + slot, null, null, null, null);
            cursor.moveToFirst();
            SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
            if (dto.getOutput() != getOutput()) {
                parent.logTypical();// logga il nuovo
                Log.i(Constants.TAG, "logging node " + this.getNodeId() + " - slot " + parent.getSlot() + " - " + parent.getNiceName() + " new state from: " + dto.getOutput() + " to " + parent.getOutput());
            }
            cursor.close();
        }
        ContentValues values = new ContentValues();
        assertTrue(getSlot() != -1);
        values.put(SoulissDB.COLUMN_TYPICAL_VALUE, getOutput());
        values.put(SoulissDB.COLUMN_TYPICAL_LASTMOD, Calendar.getInstance().getTime().getTime());
        int upd = SoulissDBHelper.getDatabase().update(
                SoulissDB.TABLE_TYPICALS,
                values,
                SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + getNodeId() + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT
                        + " = " + getSlot(), null);
        // TODO throw exception
        return upd;
    }

    /**
     * INSERT or UPDATE a row, based on primary key
     *
     * @return Inefficient //TODO split
     */

    public SoulissTypical createOrReplaceTypical(SoulissDBHelper database) {
        ContentValues values = new ContentValues();
        assertTrue(getSlot() != -1);
        values.put(SoulissDB.COLUMN_TYPICAL_NODE_ID, getNodeId());
        values.put(SoulissDB.COLUMN_TYPICAL_NAME, getName());
        values.put(SoulissDB.COLUMN_TYPICAL_ICON, getIconId());
        values.put(SoulissDB.COLUMN_TYPICAL, getTypical());
        values.put(SoulissDB.COLUMN_TYPICAL_SLOT, getSlot());
        values.put(SoulissDB.COLUMN_TYPICAL_INPUT, getInput());
        values.put(SoulissDB.COLUMN_TYPICAL_VALUE, getOutput());
        values.put(SoulissDB.COLUMN_TYPICAL_WARNTIMER, getWarnDelayMsec());
        //values.put(SoulissDB.COLUMN_TYPICAL_ISFAV, getFavourite());
        values.put(SoulissDB.COLUMN_TYPICAL_LASTMOD, Calendar.getInstance().getTime().getTime());
        int upd = SoulissDBHelper.getDatabase().update(
                SoulissDB.TABLE_TYPICALS,
                values,
                SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + getNodeId() + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT
                        + " = " + getSlot(), null);
        if (upd == 0) {
            SoulissDBHelper.getDatabase().insert(SoulissDB.TABLE_TYPICALS, null, values);
        }
        return database.getTypical(getNodeId(), getSlot());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getNodeId() {
        return nodeId;
    }

    public void setNodeId(short nodeId) {
        this.nodeId = nodeId;
    }

    public short getOutput() {
        return output;
    }

    public void setOutput(short output) {
        this.output = output;
    }

    public Calendar getRefreshedAt() {
        return refreshedAt;
    }

    public void setRefreshedAt(Calendar refreshedAt) {
        this.refreshedAt = refreshedAt;
    }

    public short getSlot() {
        return slot;
    }

    public void setSlot(short j) {
        slot = j;
    }

    public short getTypical() {
        return typical;
    }

    public void setTypical(short typical) {
        this.typical = typical;
    }

    public short getInput() {
        return inputCommand;
    }

    public void setInput(byte input) {
        this.inputCommand = input;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getTypicalDec() {
        return Integer.toHexString(typical);
    }

    public int getWarnDelayMsec() {
        return warnDelayMsec;
    }

    public void setWarnDelayMsec(int warnDelayMsec) {
        this.warnDelayMsec = warnDelayMsec;
    }
}
