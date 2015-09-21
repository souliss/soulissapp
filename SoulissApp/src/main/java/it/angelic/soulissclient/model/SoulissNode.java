package it.angelic.soulissclient.model;

import android.content.res.Resources.NotFoundException;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;

/**
 * Souliss Unit, the node
 *
 * It has a List of @SoulissTypical , and represents an actual arduino-like board
 *
 * @author Ale
 */
public class SoulissNode implements Serializable, ISoulissObject {

    /**
     *
     */
    private static final long serialVersionUID = 8673027563853737718L;
    private short health;
    /* Icon resource ID */
    private int iconId = R.drawable.square;
    private short id;
    private String name;
    private Calendar refreshedAt;

    private List<SoulissTypical> soulissTypicals;

    public SoulissNode(short id) {
        super();
        this.id = id;
        soulissTypicals = new ArrayList<>();
    }

    @Override
    public String toString() {
        return getNiceName();
    }

    /**
     * Data transfer method
     *
     * @param cursor risultato della select
     * @return
     */
    public static SoulissNode cursorToNode(Cursor cursor) {
        SoulissNode comment = new SoulissNode(cursor.getShort(1));
        // comment.setName(cursor.getString(3));

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

    public short getHealth() {
        return health;
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

    public  short  getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public String getNiceName() {
        if (name != null && "".compareToIgnoreCase(name) != 0)
            return name; //+ " ("+SoulissClient.getAppContext().getString(R.string.node)+" "+ getId() + ")";
        else if (id > Constants.MASSIVE_NODE_ID)
            return SoulissApp.getAppContext().getString(R.string.node) + " " + Constants.int2roman(getId());
        else
            return SoulissApp.getAppContext().getString(R.string.allnodes);
    }

    public Calendar getRefreshedAt() {
        return refreshedAt;
    }

    public void setRefreshedAt(Calendar refreshedAt) {
        this.refreshedAt = refreshedAt;
    }

    public List<SoulissTypical> getTypicals() {
        return soulissTypicals;
    }

    public void setTypicals(List<SoulissTypical> soulissTypicals) {
        this.soulissTypicals = soulissTypicals;
    }

    public List<SoulissTypical> getActiveTypicals() {
        ArrayList<SoulissTypical> copy = new ArrayList<>();

        for (SoulissTypical soulissTypical : soulissTypicals) {
            if (!soulissTypical.isRelated() && !soulissTypical.isEmpty())
                copy.add(soulissTypical);
        }

        return copy;
    }


    public SoulissTypical getTypical(short slot) throws NotFoundException {
        for (SoulissTypical soulissTypical : soulissTypicals) {
            if (soulissTypical.getSlot() == slot)
                return soulissTypical;
        }
        throw new NotFoundException("Slot " + slot + " not found on node " + getId());

    }

    public String getName() {
        return name;
    }

    public void setName(String namer) {
        name = namer;
    }


}
