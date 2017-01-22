package it.angelic.soulissclient.model;

import it.angelic.soulissclient.util.LauncherElementEnum;

/**
 * Created by Ale on 10/02/2015.
 * <p/>
 * Common methods to all commands
 */
public interface ILauncherTile {

    LauncherElementEnum getComponentEnum();

    ISoulissObject getLinkedObject();


    //CardView getCardView();
}
