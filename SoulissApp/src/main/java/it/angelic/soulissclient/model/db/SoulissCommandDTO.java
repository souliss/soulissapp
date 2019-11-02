package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.Serializable;

import it.angelic.soulissclient.Constants;

public class SoulissCommandDTO implements Serializable {

    private static final long serialVersionUID = 1621944596010487586L;
    //see constants COMMAND_*
    int type;
    private long command;
    private Long commandId;
    private Long executedTime;
    private int interval;
    private short nodeId;
    private Integer sceneId;
    private Long scheduledTime;
    private short slot;

    public SoulissCommandDTO(Cursor cursor) {
        setCommandId(cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_ID)));
        setNodeId(cursor.getShort(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_NODE_ID)));
        setSlot(cursor.getShort(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SLOT)));
        setCommand(cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_INPUT)));
        setScheduledTime(cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SCHEDTIME)));
        setExecutedTime(cursor.getLong(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_EXECTIME)));
        setInterval(cursor.getInt(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SCHEDTIME_INTERVAL)));
        setSceneId(cursor.getInt(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_SCENEID)));
        setType(cursor.getInt(cursor.getColumnIndex(SoulissDBOpenHelper.COLUMN_COMMAND_TYPE)));
    }

    public SoulissCommandDTO() {
        super();
    }

    public long getCommand() {
        return command;
    }

    public void setCommand(long command) {
        this.command = command;
    }

    public long getCommandId() {
        return commandId;
    }

    public void setCommandId(long commandId) {
        this.commandId = commandId;
    }

    public Long getExecutedTime() {
        return executedTime;
    }

    public void setExecutedTime(Long executedTime) {
        this.executedTime = executedTime;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public short getNodeId() {
        return nodeId;
    }

    public void setNodeId(short nodeId) {
        this.nodeId = nodeId;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(@Nullable Integer sceneId) {
        this.sceneId = sceneId;
    }

    public Long getScheduledTime() {
        return scheduledTime;
    }

    /*
     * public boolean isExecuted() { return isExecuted; }
     */

    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public short getSlot() {
        return slot;
    }

    public void setSlot(short slot) {
        this.slot = slot;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return
     */
    public long persistCommand() {
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
        if (interval != 0)
            values.put(SoulissDBOpenHelper.COLUMN_COMMAND_SCHEDTIME_INTERVAL, interval);

        long upd;
        if (commandId != null) {
            Log.d(Constants.TAG, "Updating command: " + commandId + " excTime: " + executedTime);
            upd = SoulissDBHelper.getDatabase().update(SoulissDBOpenHelper.TABLE_COMMANDS, values,
                    SoulissDBOpenHelper.COLUMN_COMMAND_ID + " = " + commandId, null);
        } else {
            upd = SoulissDBHelper.getDatabase().insert(SoulissDBOpenHelper.TABLE_COMMANDS, null, values);
            setCommandId(upd);
        }
        return upd;
    }

}
