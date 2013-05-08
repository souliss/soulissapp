package it.angelic.soulissclient.db;

import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.typicals.SoulissTypical;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

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
	private short inputCommand;
	private Calendar refreshedAt;

	public SoulissTypicalDTO() {
		// niente di fatto
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SoulissTypicalDTO))
			return false;

		SoulissTypicalDTO tg = (SoulissTypicalDTO) o;
		if (tg.getNodeId() == nodeId && tg.getTypical() == typical && tg.getSlot() == slot)
			return true;

		return false;
	}
	@Override
	public int hashCode() {
		return getNodeId();
	}

	public SoulissTypicalDTO(Cursor cursor) {
		// byte typ = (byte) cursor.getShort(1);
		setNodeId(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_NODE_ID)));
		setTypical(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL)));
		setSlot(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_SLOT)));
		setInput((byte) cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_INPUT)));
		setOutput(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_VALUE)));
		setIconId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_ICON)));
		setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_NAME)));
		Calendar now = Calendar.getInstance();
		now.setTime(new Date(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_TYPICAL_LASTMOD))));
		setRefreshedAt(now);
	}

	/**
	 * Aggiorna un tipico, pensato per JSON TYP, SLO e VAL
	 * 
	 * @param typicalIN
	 * @return
	 */
	public int persist() {
		ContentValues values = new ContentValues();
		assertTrue(getSlot() != -1);
		values.put(SoulissDB.COLUMN_TYPICAL_NODE_ID, getNodeId());
		values.put(SoulissDB.COLUMN_TYPICAL, getTypical());
		values.put(SoulissDB.COLUMN_TYPICAL_SLOT, getSlot());
		values.put(SoulissDB.COLUMN_TYPICAL_NAME, getName());
		values.put(SoulissDB.COLUMN_TYPICAL_ICON, getIconId());
		values.put(SoulissDB.COLUMN_TYPICAL_VALUE, getOutput());
		values.put(SoulissDB.COLUMN_TYPICAL_LASTMOD, Calendar.getInstance().getTime().getTime());
		int upd = SoulissDBHelper.getDatabase().update(
				SoulissDB.TABLE_TYPICALS,
				values,
				SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + getNodeId() + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT
						+ " = " + getSlot(), null);
		if (upd == 0) {
			upd = (int) SoulissDBHelper.getDatabase().insert(SoulissDB.TABLE_TYPICALS, null, values);
		}
		return upd;
	}

	/**
	 * Aggiorna un tipico, ma solo il valore
	 * 
	 * @param typicalIN
	 * @return
	 */
	public int refresh() {
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
	 * @param typicalIN
	 * @return
	 * 
	 * 
	 *         Inefficient //TODO split
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
		values.put(SoulissDB.COLUMN_TYPICAL_LASTMOD, Calendar.getInstance().getTime().getTime());
		int upd = SoulissDBHelper.getDatabase().update(
				SoulissDB.TABLE_TYPICALS,
				values,
				SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + getNodeId() + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT
						+ " = " + getSlot(), null);
		if (upd == 0) {
			SoulissDBHelper.getDatabase().insert(SoulissDB.TABLE_TYPICALS, null, values);
		}
		return database.getSoulissTypical(getNodeId(), getSlot());
	}

	public String getName() {
		return name;
	}

	public short getNodeId() {
		return nodeId;
	}

	public short getOutput() {
		return output;
	}

	public Calendar getRefreshedAt() {
		return refreshedAt;
	}

	public short getSlot() {
		return slot;
	}

	public short getTypical() {
		return typical;
	}

	public void setInput(byte input) {
		this.inputCommand = input;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNodeId(short nodeId) {
		this.nodeId = nodeId;
	}

	public void setOutput(short output) {
		this.output = output;
	}

	public void setRefreshedAt(Calendar refreshedAt) {
		this.refreshedAt = refreshedAt;
	}

	public void setSlot(short j) {
		slot = j;
	}

	public void setTypical(short typical) {
		this.typical = typical;
	}

	public short getInput() {
		// TODO Auto-generated method stub
		return inputCommand;
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

}
