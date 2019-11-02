package it.angelic.soulissclient.model;

import android.util.Log;

import androidx.annotation.DrawableRes;

import java.io.Serializable;
import java.util.ArrayList;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;

public class SoulissScene implements Serializable, ISoulissObject, ISoulissExecutable {


    private static final long serialVersionUID = 375896210748961219L;
    private int Id;
    private ArrayList<SoulissCommand> commandArray;
    private int iconId = R.mipmap.ic_launcher_2019;
    //private transient Context ctx;
    private String name = "Name not Set";


    public SoulissScene(int id) {
        super();
        Id = id;
    }

    @Override
    public void execute() {
        for (final SoulissCommand soulissCommand : commandArray) {
            Log.w(Constants.TAG, "EXECUTING SCENE Command:" + soulissCommand.toString() + " DELAY FROM NOW: " + soulissCommand.getInterval());
            try {
                Thread.sleep(soulissCommand.getInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            soulissCommand.execute();
        }
    }

    public ArrayList<SoulissCommand> getCommandArray() {
        return commandArray;
    }

    public int getSceneId() {
        return getId();
    }

    public void setCommandArray(ArrayList<SoulissCommand> commandArray) {
        this.commandArray = commandArray;
    }

    public
    int getIconResourceId() {
        return iconId;
    }

    public void setIconResourceId(@DrawableRes int itemResId) {
        iconId = itemResId;
    }

    public int getId() {
        return Id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNiceName() {
        return name;
    }

    /**
     * Needed for Spinner arrays
     *
     * @return
     */
    @Override
    public String toString() {
        return getNiceName();
    }
}
