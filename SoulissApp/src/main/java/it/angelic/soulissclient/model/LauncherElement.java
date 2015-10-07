package it.angelic.soulissclient.model;

/**
 * Created by Ale on 06/10/2015.
 */
public class LauncherElement {
    private boolean isFullSpan;
    private LauncherElementEnum componentEnum;
    private short id;
    private short order;

    public ISoulissObject getLinkedObject() {
        return linkedObject;
    }

    public void setLinkedObject(ISoulissObject linkedObject) {
        this.linkedObject = linkedObject;
    }

    private ISoulissObject linkedObject;

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

    public void setId(short id) {
        this.id = id;
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
