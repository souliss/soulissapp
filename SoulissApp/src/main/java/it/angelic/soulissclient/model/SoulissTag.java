package it.angelic.soulissclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Un tag è un insieme di tipici definiti dall'utente
 * <p/>
 * Created by Ale on 27/02/2015.
 */
public class SoulissTag implements Serializable, ISoulissSortableObject {
    private List<SoulissTypical> assignedTypicals = new ArrayList<>();
    private List<SoulissTag> childTags = new ArrayList<>();
    private Long fatherId = null;
    private int iconId;
    private String imagePath;
    private String name;
    private long tagId;
    private Integer tagOrder;

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

    public List<SoulissTag> getChildTags() {
        return childTags;
    }

    public void setChildTags(List<SoulissTag> childTags) {
        this.childTags = childTags;
    }

    public Long getFatherId() {
        return fatherId;
    }

    public void setFatherId(Long fatherId) {
        this.fatherId = fatherId;
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

    @Override
    public Integer getOrder() {
        return getTagOrder();
    }

    @Override
    public void setOrder(Integer order) {
        setTagOrder(order);
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
