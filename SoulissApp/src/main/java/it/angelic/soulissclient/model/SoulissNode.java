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

/**
 * Souliss Unit, the node
 * <p/>
 * It has a List of @SoulissTypical , and represents an actual arduino-like board
 *
 * @author shine
 */
public class SoulissNode implements Serializable, ISoulissNode {

    private static final long serialVersionUID = 8673027563853737718L;
    private Context context;
    private short health;
    /* Icon resource ID */
    private int iconId = R.drawable.square;
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

        comment.setHealth(cursor.getShort(2));
        comment.setIconResourceId(cursor.getInt(3));
        comment.setName(cursor.getString(4));

        Calendar now = Calendar.getInstance();
        now.setTime(new Date(cursor.getLong(5)));
        comment.setRefreshedAt(now);
        return comment;
    }

    public void addTypical(SoulissTypical rest) {
        soulissTypicals.add(rest);
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

    public String getHealthPercent() {
        return getHealth() * 100 / 255 + "%";
    }

    public void setHealth(short health) {
        this.health = health;
    }

    public int getIconResourceId() {
        return iconId;
    }

    public void setIconResourceId(int itemResId) {
        iconId = itemResId;
    }

    public short getNodeId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
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
    public String toString() {
        return getNiceName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoulissNode that = (SoulissNode) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) id;
    }
}
