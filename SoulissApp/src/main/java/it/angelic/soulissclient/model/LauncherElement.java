package it.angelic.soulissclient.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.util.LauncherElementEnum;

/**
 * Created by shine@angelic.it on 06/10/2015.
 */
public class LauncherElement implements ILauncherTile, Serializable {
    private LauncherElementEnum componentEnum;

    private String title;
    private String desc;
    private Integer id;
    private boolean isFullSpan;
    private ISoulissObject linkedObject;
    private short order;

    public LauncherElement() {
        super();

    }

    public LauncherElement(LauncherElementEnum componentEnum) {
        super();
        this.componentEnum = componentEnum;
    }

    public LauncherElementEnum getComponentEnum() {
        return componentEnum;
    }

    protected void persist(Context cnt) throws SoulissModelException {
        SoulissDBLauncherHelper db = new SoulissDBLauncherHelper(cnt);
        db.updateLauncherElement(this);
    }


    public void setComponentEnum(LauncherElementEnum componentEnum) {
        this.componentEnum = componentEnum;
    }

    public Integer getId() {
        return id;
    }

    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public
    @Nullable
    ISoulissObject getLinkedObject() {
        return linkedObject;
    }

    public void setLinkedObject(ISoulissObject linkedObject) {
        this.linkedObject = linkedObject;
    }

    public short getOrder() {
        return order;
    }

    public void setOrder(short order) {
        this.order = order;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isFullSpan() {
        return isFullSpan;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsFullSpan(boolean isFullSpan) {
        this.isFullSpan = isFullSpan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LauncherElement that = (LauncherElement) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
