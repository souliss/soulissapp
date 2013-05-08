package it.angelic.soulissclient.db;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

public class SoulissLogDTO {

	Long logId;
	short nodeId;
	short slot;
	short typical;
	float logValue;
	Calendar logTime;

	public SoulissLogDTO() {
		// niente di fatto
	}

	public SoulissLogDTO(Cursor cursor) {
		setLogId(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_ID)));
		setNodeId(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_NODE_ID)));
		setSlot(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_SLOT)));
		setTypical(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_TYPICAL)));
		Calendar now = Calendar.getInstance();
		now.setTime(new Date(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_DATE))));
		setLogTime(now);
		
		setLogValue(cursor.getFloat(cursor.getColumnIndex(SoulissDB.COLUMN_LOG_VAL)));
	}
	
	
	/**
	 * @param commandDTO
	 * @return
	 */
	public long persist() {
		ContentValues values = new ContentValues();
		// assert (typicalIN.getSlot() != -1);
		// values.put(SoulissDB.COLUMN_COMMAND_ID, typicalIN.getCommandId());
		values.put(SoulissDB.COLUMN_LOG_NODE_ID, nodeId);
		values.put(SoulissDB.COLUMN_LOG_SLOT, slot);
		values.put(SoulissDB.COLUMN_LOG_TYPICAL, typical);
		values.put(SoulissDB.COLUMN_LOG_VAL, logValue);
		values.put(SoulissDB.COLUMN_LOG_DATE, logTime.getTime().getTime());
		
		long upd;
		if (logId != null) {
			upd = SoulissDBHelper.getDatabase().update(SoulissDB.TABLE_LOGS, values,
					
					SoulissDB.COLUMN_LOG_ID + " = " + logId, null);
			if (upd == 0){
				upd = SoulissDBHelper.getDatabase().insert(SoulissDB.TABLE_LOGS, null, values);
				setLogId(upd);
			}
		} else {
			upd = SoulissDBHelper.getDatabase().insert(SoulissDB.TABLE_LOGS, null, values);
			setLogId(upd);
		}
		return upd;
	}

	public Long getLogId() {
		return logId;
	}

	public void setLogId(Long logId) {
		this.logId = logId;
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

	public short getTypical() {
		return typical;
	}

	public void setTypical(short typical) {
		this.typical = typical;
	}

	public float getLogValue() {
		return logValue;
	}

	public void setLogValue(float logValue) {
		this.logValue = logValue;
	}

	public Calendar getLogTime() {
		return logTime;
	}

	public void setLogTime(Calendar logTime) {
		this.logTime = logTime;
	}

	
}
