package it.angelic.soulissclient.model;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.model.typicals.SoulissTypical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.res.Resources.NotFoundException;
import android.database.Cursor;

/**
 * POJO used to represent a souliss node with its typicals
 * 
 * @author Ale
 * 
 */
public class SoulissNode implements Serializable, ISoulissObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8673027563853737718L;
	private short health;
	/* Icon resource ID */
	private int iconId = 0;
	private short id;
	private String Name;
	private Calendar refreshedAt;

	private List<SoulissTypical> soulissTypicals;

	public SoulissNode(short id) {
		super();
		this.id = id;
		soulissTypicals = new ArrayList<SoulissTypical>();
	}

	/**
	 * Data transfer method
	 * 
	 * @param cursor
	 *            risultato della select
	 * @return
	 */
	public static SoulissNode cursorToNode(Cursor cursor) {
		SoulissNode comment = new SoulissNode((short) cursor.getShort(1));
		// comment.setName(cursor.getString(3));

		comment.setHealth(cursor.getShort(2));
		comment.setIconResourceId(cursor.getInt(3));
		comment.setName(cursor.getString(4));

		Calendar now = Calendar.getInstance();
		now.setTime(new Date(cursor.getLong(5)));
		comment.setRefreshedAt(now);
		return comment;
	}

	public void add(SoulissTypical rest) {
		soulissTypicals.add(rest);

	}

	public short getHealth() {
		return health;
	}

	public int getDefaultIconResourceId() {
		return iconId;
	}

	public void setIconResourceId(int itemResId) {
		iconId = itemResId;
	}

	public short getId() {
		return id;
	}

	public String getNiceName() {
		if (Name != null && "".compareToIgnoreCase(Name) != 0)
			return Name + " ("+SoulissClient.getAppContext().getString(R.string.node)+" "+ getId() + ")";
		else
			return SoulissClient.getAppContext().getString(R.string.node)+" "+ Constants.int2roman(getId());

	}

	public Calendar getRefreshedAt() {
		return refreshedAt;
	}

	public List<SoulissTypical> getTypicals() {
		return soulissTypicals;
	}

	/**
	 * 
	 * @return solo i tipici MASTER
	 */
	public ArrayList<SoulissTypical> getActiveTypicals() {

		ArrayList<SoulissTypical> copy = new ArrayList<SoulissTypical>();

		for (SoulissTypical soulissTypical : soulissTypicals) {
			if (!soulissTypical.isRelated() && !soulissTypical.isEmpty())
				copy.add(soulissTypical);
		}

		return copy;
	}

	public void setHealth(short health) {
		this.health = health;
	}

	public void setId(short id) {
		this.id = id;
	}

	public void setName(String name) {
		Name = name;
	}

	public void setRefreshedAt(Calendar refreshedAt) {
		this.refreshedAt = refreshedAt;
	}

	public void setTypicals(List<SoulissTypical> soulissTypicals) {
		this.soulissTypicals = soulissTypicals;
	}

	public SoulissTypical getTypical(short slot) throws NotFoundException {
		for (SoulissTypical soulissTypical : soulissTypicals) {
			if (soulissTypical.getTypicalDTO().getSlot() == slot)
				return soulissTypical;
		}
		throw new NotFoundException("Slot " + slot + " not found on node " + getId());

	}

	@Override
	public String toString() {
		return getNiceName();
	}

	public String getName() {
				return Name;
	}
}
