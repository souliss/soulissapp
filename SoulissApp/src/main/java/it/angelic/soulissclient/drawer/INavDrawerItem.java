package it.angelic.soulissclient.drawer;
public interface INavDrawerItem {
    int getId();
    String getLabel();
    int getType();
    boolean isEnabled();
    boolean updateActionBarTitle();
}