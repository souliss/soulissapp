package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;

public class SoulissCommandDTO implements Serializable {

    private static final long serialVersionUID = 1621944596010487586L;
    private Long commandId;
    private short nodeId;
    private short slot;
    private Long scheduledTime;
    private Long executedTime;
    private long command;
    private Integer sceneId;
    private int interval;
    //see constants COMMAND_*
    int type;

    public SoulissCommandDTO(Cursor cursor) {
        setCommandId(cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_ID)));
        setNodeId(cursor.getShort(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_NODE_ID)));
        setSlot(cursor.getShort(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SLOT)));
        setCommand(cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_INPUT)));
        Calendar now = Calendar.getInstance();
        setScheduledTime(cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SCHEDTIME)));
        long exd = cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_EXECTIME));
        if (exd != 0) {
            Calendar bis = Calendar.getInstance();
            bis.setTime(new Date(exd));
            setExecutedTime(exd);
        }
        setInterval(cursor.getInt(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SCHEDTIME_INTERVAL)));
        setSceneId(cursor.getInt(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SCENEID)));
        setType(cursor.getInt(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_TYPE)));
    }

    public SoulissCommandDTO() {

    }

    /**
     * @return
     */
    public long persistCommand( ) {
        ContentValues values = new ContentValues();
        // assert (typicalIN.getSlot() != -1);
        if (commandId != null)
            values.put(SoulissDBOpenHelper.COLUMN_COMMAND_ID, commandId);
        values.put(SoulissDBOpenHelper.COLUMN_COMMAND_NODE_ID, nodeId);
        values.put(SoulissDBOpenHelper.COLUMN_COMMAND_SLOT, slot);
        values.put(SoulissDBOpenHelper.COLUMN_COMMAND_INPUT, command);
        values.put(SoulissDBOpenHelper.COLUMN_COMMAND_TYPE, type);
        values.put(SoulissDBOpenHelper.COLUMN_COMMAND_SCENEID, sceneId);
        if (scheduledTime != null)
            values.put(SoulissDBOpenHelper.COLUMN_COMMAND_SCHEDTIME, scheduledTime);
        if (executedTime != null)
            values.put(SoulissDBOpenHelper.COLUMN_COMMAND_EXECTIME, executedTime);
        // else
        // values.put(SoulissDB.COLUMN_EXECTIME, null);
        if (interval != 0)
            values.put(SoulissDBOpenHelper.COLUMN_COMMAND_SCHEDTIME_INTERVAL, interval);

        long upd;
        if (commandId != null) {
            upd = SoulissDBHelper.getDatabase().update(SoulissDBOpenHelper.TABLE_COMMANDS, values,
                    SoulissDBOpenHelper.COLUMN_COMMAND_ID + " = " + commandId, null);
        } else {
            upd = SoulissDBHelper.getDatabase().insert(SoulissDBOpenHelper.TABLE_COMMANDS, null, values);
            setCommandId(upd);
        }
        return upd;
    }

    public long getCommandId() {
        return commandId;
    }

    public void setCommandId(long commandId) {
        this.commandId = commandId;
    }

    public short getNodeId() {
        return nodeId;
    }

    public void setNodeId(short nodeId) {
        this.nodeId = nodeId;
    }

    public short getSlot() {
        return slot;
    }

    public void setSlot(short slot) {
        this.slot = slot;
    }

    public Long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Long getExecutedTime() {
        return executedTime;
    }

    public void setExecutedTime(Long executedTime) {
        this.executedTime = executedTime;
    }

    public long getCommand() {
        return command;
    }

    public void setCommand(long command) {
        this.command = command;
    }

	/*
     * public boolean isExecuted() { return isExecuted; }
	 */

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(@Nullable Integer sceneId) {
        this.sceneId = sceneId;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
