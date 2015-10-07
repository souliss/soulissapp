package it.angelic.soulissclient.model;

import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;

/**
 * Created by Ale on 06/10/2015.
 */
public class LauncherElement {
    public boolean isFullSpan;
    private CardView cardView;
    private LauncherElementEnum componentEnum;
    private short id;
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

    public void setId(short id) {
        this.id = id;
    }

    public short getOrder() {
        return order;
    }

    public void setOrder(short order) {
        this.order = order;
    }



}
