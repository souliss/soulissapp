package it.angelic.soulissclient.model;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.model.db.SoulissDB;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;

/**
 * Souliss Unit, the node
 * <p/>
 * It has a List of @SoulissTypical , and represents an actual arduino-like board
 *
 * @author shine
 */
public class SoulissNode implements Serializable, ISoulissNode {

    private static final long serialVersionUID = 8673027563853737718L;
    private transient Context context;
    private short health;
    /* Icon resource ID > se null torna chipset */
    private Integer iconId;
    private short id;
    private String name;
    private Calendar refreshedAt;

    private List<SoulissTypical> soulissTypicals;

    public SoulissNode(Context c, short id) {
        super();
        this.id = id;
        context = c;
        soulissTypicals = new ArrayList<>();
    }

    /**
     * Data transfer method
     *
     * @param cursor risultato della select
     * @return
     */
    public static SoulissNode cursorToNode(Context c, Cursor cursor) {
        SoulissNode comment = new SoulissNode(c, cursor.getShort(1));

        comment.setHealth(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_NODE_HEALTH)));
        if (!cursor.isNull(cursor.getColumnIndex(SoulissDB.COLUMN_NODE_ICON)))
            comment.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_NODE_ICON)));
        comment.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_NODE_NAME)));

        Calendar now = Calendar.getInstance();
        now.setTime(new Date(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_NODE_LASTMOD))));
        comment.setRefreshedAt(now);
        return comment;
    }

    public void addTypical(SoulissTypical rest) {
        soulissTypicals.add(rest);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoulissNode that = (SoulissNode) o;

        return id == that.id;

    }

    public List<SoulissTypical> getActiveTypicals() {
        ArrayList<SoulissTypical> copy = new ArrayList<>();

        for (SoulissTypical soulissTypical : soulissTypicals) {
            if (!soulissTypical.isRelated() && !soulissTypical.isEmpty())
                copy.add(soulissTypical);
        }

        return copy;
    }

    public short getHealth() {
        return health;
    }

    public void setHealth(short health) {
        this.health = health;
    }

    public String getHealthPercent() {
        return getHealth() * 100 / 255 + "%";
    }

    public int getIconResourceId() {
        return iconId == null ? FontAwesomeUtil.getCodeIndexByFontName(context, FontAwesomeEnum.fa_microchip.getFontName()) : iconId;
    }

    public void setIconResourceId(int itemResId) {
        iconId = itemResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String namer) {
        name = namer;
    }

    public String getNiceName() {
        if (name != null && "".compareToIgnoreCase(name) != 0)
            return name; //+ " ("+SoulissClient.getAppContext().getString(R.string.node)+" "+ getNodeId() + ")";
        else if (id > Constants.MASSIVE_NODE_ID)
            return context.getString(R.string.node) + " " + Constants.int2roman(getNodeId());
        else
            return context.getString(R.string.allnodes);
    }

    public short getNodeId() {
        return id;
    }

    public Calendar getRefreshedAt() {
        return refreshedAt;
    }

    public void setRefreshedAt(Calendar refreshedAt) {
        this.refreshedAt = refreshedAt;
    }

    public SoulissTypical getTypical(short slot) throws NotFoundException {
        for (SoulissTypical soulissTypical : soulissTypicals) {
            if (soulissTypical.getSlot() == slot)
                return soulissTypical;
        }
        throw new NotFoundException("Slot " + slot + " not found on node " + getNodeId());

    }

    public List<SoulissTypical> getTypicals() {
        return soulissTypicals;
    }

    public void setTypicals(List<SoulissTypical> soulissTypicals) {
        this.soulissTypicals = soulissTypicals;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getNiceName();
    }
}
