package it.angelic.soulissclient.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by shine@angelic.it on 06/10/2015.
 */
public class LauncherElement implements ILauncherTile {
    private LauncherElementEnum componentEnum;
    private short id;
    private boolean isFullSpan;
    private ISoulissObject linkedObject;
    private short order;

    public LauncherElement(LauncherElementEnum componentEnum) {
        super();
        this.componentEnum = componentEnum;
    }

    public LauncherElementEnum getComponentEnum() {
        return componentEnum;
    }

    public void setComponentEnum(LauncherElementEnum componentEnum) {
        this.componentEnum = componentEnum;
    }

    public short getId() {
        return id;
    }

    public void setId(@NonNull short id) {
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

    public boolean isFullSpan() {
        return isFullSpan;
    }

    public void setIsFullSpan(boolean isFullSpan) {
        this.isFullSpan = isFullSpan;
    }


}
