package it.angelic.soulissclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ale on 27/02/2015.
 */
public class SoulissTag implements Serializable, ISoulissObject {
    private long tagId;
    private String name;
    private int iconId;
    private String imagePath;
    private List<SoulissTypical> assignedTypicals = new ArrayList<>();


    public Long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getNiceName() {
        return getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void setIconResourceId(int resId) {
        iconId = resId;
    }

    @Override
    public int getIconResourceId() {
        return iconId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<SoulissTypical> getAssignedTypicals() {
        return assignedTypicals;
    }

    public void setAssignedTypicals(List<SoulissTypical> assignedTypicals) {
        this.assignedTypicals = assignedTypicals;
    }

}
