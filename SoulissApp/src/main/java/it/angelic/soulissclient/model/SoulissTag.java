package it.angelic.soulissclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Un tag è un insieme di tipici definiti dall'utente
 * <p/>
 * Created by Ale on 27/02/2015.
 */
public class SoulissTag implements Serializable, ISoulissObject {
    private long tagId;
    private String name;
    private int iconId;
    private String imagePath;
    private Integer tagOrder;
    private List<SoulissTypical> assignedTypicals = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof SoulissTag) {
            return (((SoulissTag) o).getTagId().equals(getTagId()));
        }
        return false;
    }

    public List<SoulissTypical> getAssignedTypicals() {
        return assignedTypicals;
    }

    public void setAssignedTypicals(List<SoulissTypical> assignedTypicals) {
        this.assignedTypicals = assignedTypicals;
    }

    @Override
    public int getIconResourceId() {
        return iconId;
    }

    @Override
    public void setIconResourceId(int resId) {
        iconId = resId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getNiceName() {
        return getName();
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public Integer getTagOrder() {
        return tagOrder;
    }

    public void setTagOrder(Integer tagOrder) {
        this.tagOrder = tagOrder;
    }

    @Override
    public int hashCode() {
        return (int) (getTagId() % Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return getName();
    }
}
