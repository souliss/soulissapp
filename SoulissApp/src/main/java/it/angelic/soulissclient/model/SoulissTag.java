package it.angelic.soulissclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ale on 27/02/2015.
 */
public class SoulissTag implements Serializable {
    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    private int tagId;
    private String name;
    private int iconId;
    private String imagePath;
    private List<SoulissTypical> assignedTypicals = new ArrayList<>();

    public List<SoulissTypical> getAssignedTypicals() {
        return assignedTypicals;
    }

    public void setAssignedTypicals(List<SoulissTypical> assignedTypicals) {
        this.assignedTypicals = assignedTypicals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
