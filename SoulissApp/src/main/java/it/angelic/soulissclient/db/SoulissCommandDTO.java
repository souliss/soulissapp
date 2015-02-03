package it.angelic.soulissclient.db;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

public class SoulissCommandDTO implements Serializable {

	private static final long serialVersionUID = 1621944596010487586L;
    private Long commandId;
    private short nodeId;
    private short slot;
    private Calendar scheduledTime;
    private Calendar executedTime;
    private long command;
    private Integer sceneId;
    private int interval;
	//see constants COMMAND_*
	int type;

	public SoulissCommandDTO() {
		// niente di fatto
	}

	public SoulissCommandDTO(Cursor cursor) {
		setCommandId(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_ID)));
		setNodeId(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_NODE_ID)));
		setSlot(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_SLOT)));
		setCommand(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_INPUT)));
		Calendar now = Calendar.getInstance();
		now.setTime(new Date(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_SCHEDTIME))));
		setScheduledTime(now);
		long exd = cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_EXECTIME));
		if (exd != 0) {
			Calendar bis = Calendar.getInstance();
			bis.setTime(new Date(exd));
			setExecutedTime(bis);
		}
		setInterval(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_SCHEDTIME_INTERVAL)));
		setSceneId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_SCENEID)));
		setType(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_COMMAND_TYPE)));
	}

	/**
	 * @return
	 * 
	 * FIXME: non deve prendere parametri
	 * 
	 * 
	 */
	public long persistCommand(SoulissDBHelper dbh) {
		ContentValues values = new ContentValues();
		// assert (typicalIN.getSlot() != -1);
		// values.put(SoulissDB.COLUMN_COMMAND_ID, typicalIN.getCommandId());
		values.put(SoulissDB.COLUMN_COMMAND_NODE_ID, nodeId);
		values.put(SoulissDB.COLUMN_COMMAND_SLOT, slot);
		values.put(SoulissDB.COLUMN_COMMAND_INPUT, command);
		values.put(SoulissDB.COLUMN_COMMAND_TYPE, type);
        values.put(SoulissDB.COLUMN_COMMAND_SCENEID, sceneId);
		if (scheduledTime != null)
		values.put(SoulissDB.COLUMN_COMMAND_SCHEDTIME, scheduledTime.getTime().getTime());
		if (executedTime != null)
			values.put(SoulissDB.COLUMN_COMMAND_EXECTIME, executedTime.getTime().getTime());
		// else
		// values.put(SoulissDB.COLUMN_EXECTIME, null);
		if (interval != 0)
			values.put(SoulissDB.COLUMN_COMMAND_SCHEDTIME_INTERVAL, interval);

		long upd;
		if (commandId != null) {
			upd = SoulissDBHelper.getDatabase().update(SoulissDB.TABLE_COMMANDS, values,
					SoulissDB.COLUMN_COMMAND_ID + " = " + commandId, null);
		} else {
			upd = SoulissDBHelper.getDatabase().insert(SoulissDB.TABLE_COMMANDS, null, values);
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

	public Calendar getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Calendar scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public Calendar getExecutedTime() {
		return executedTime;
	}

	public void setExecutedTime(Calendar executedTime) {
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

	public void setSceneId(Integer sceneId) {
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
