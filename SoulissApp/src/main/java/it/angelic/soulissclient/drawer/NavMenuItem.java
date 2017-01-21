package it.angelic.soulissclient.drawer;

import android.content.Context;

public class NavMenuItem implements INavDrawerItem {

    public static final int ITEM_TYPE = 1 ;

    private int id; 
    private String label ;
    private String icon;
    private boolean updateActionBarTitle ;

	public NavMenuItem() {
    }

    public NavMenuItem(int id, String label, String fontAweId, boolean updateActionBarTitle, Context context) {
        setId(id);
        setLabel(label);
        setIcon(fontAweId);
        setUpdateActionBarTitle(updateActionBarTitle);
    }
   
    @Override
    public int getType() {
        return ITEM_TYPE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean updateActionBarTitle() {
        return this.updateActionBarTitle;
    }

    public void setUpdateActionBarTitle(boolean updateActionBarTitle) {
        this.updateActionBarTitle = updateActionBarTitle;
    }
}