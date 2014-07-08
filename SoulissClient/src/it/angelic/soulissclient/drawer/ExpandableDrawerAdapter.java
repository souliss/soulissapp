package it.angelic.soulissclient.drawer;

import it.angelic.soulissclient.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableDrawerAdapter extends BaseExpandableListAdapter {

private static final int POSIZIONE_NODI = 2;
private INavDrawerItem[] items, nodes;
private LayoutInflater mInflater;

public ExpandableDrawerAdapter (Activity context, INavDrawerItem[] iNavDrawerItems, INavDrawerItem[] inode) {
	items = iNavDrawerItems;
	nodes= inode;
    this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
}

public Object getChild(int groupPosition, int childPosition) {

    switch (groupPosition) {
        case POSIZIONE_NODI:
            return nodes[childPosition];
        default:
            return "";
    }
}

public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
}

public View getChildView(final int groupPosition, final int childPosition,
                         boolean isLastChild, View convertView, ViewGroup parent) {

	View view = null ;
	INavDrawerItem menuItem = items[groupPosition];
        view = getItemView(convertView, parent, menuItem );
    return view ;
}

public int getChildrenCount(int groupPosition) {
    switch (groupPosition) {
        case POSIZIONE_NODI:
            return nodes.length;
        default:
            return 0;
    }
}

public Object getGroup(int groupPosition) {
    return items[groupPosition];
}

public int getGroupCount() {
    return items.length;
}

public long getGroupId(int groupPosition) {
    return groupPosition;
}

public View getGroupView(int groupPosition, boolean isExpanded,
                         View convertView, ViewGroup parent) {
	View view = null ;
	INavDrawerItem menuItem = items[groupPosition];
    if ( menuItem.getType() == NavMenuItem.ITEM_TYPE ) {
        view = getItemView(convertView, parent, menuItem );
    }
    else {
        view = getSectionView(convertView, parent, menuItem);
    }
    return view ;
}

public View getItemView( View convertView, ViewGroup parentView, INavDrawerItem navDrawerItem ) {
    
    NavMenuItem menuItem = (NavMenuItem) navDrawerItem ;
    NavMenuItemHolder navMenuItemHolder = null;
   
    if (convertView == null) {
    	if (menuItem.getId() < 0)
            convertView = mInflater.inflate( R.layout.drawer_list_item, parentView, false);
    	else
    		convertView = mInflater.inflate( R.layout.drawer_list_item_son, parentView, false);
    	
    	TextView labelView = (TextView) convertView
                .findViewById( R.id.dtitle );
        ImageView iconView = (ImageView) convertView
                .findViewById( R.id.dicon );

        navMenuItemHolder = new NavMenuItemHolder();
        navMenuItemHolder.labelView = labelView ;
        navMenuItemHolder.iconView = iconView ;

        convertView.setTag(navMenuItemHolder);
    }

    if ( navMenuItemHolder == null ) {
        navMenuItemHolder = (NavMenuItemHolder) convertView.getTag();
    }
               
    navMenuItemHolder.labelView.setText(menuItem.getLabel());
    navMenuItemHolder.iconView.setImageResource(menuItem.getIcon());
   
    return convertView ;
}

public View getSectionView(View convertView, ViewGroup parentView,
        INavDrawerItem navDrawerItem) {
   
    NavMenuSection menuSection = (NavMenuSection) navDrawerItem ;
    NavMenuSectionHolder navMenuItemHolder = null;
   
    if (convertView == null) {
        convertView = mInflater.inflate( R.layout.navdrawer_section, parentView, false);
        TextView labelView = (TextView) convertView
                .findViewById( R.id.navmenusection_label );

        navMenuItemHolder = new NavMenuSectionHolder();
        navMenuItemHolder.labelView = labelView ;
        convertView.setTag(navMenuItemHolder);
    }

    if ( navMenuItemHolder == null ) {
        navMenuItemHolder = (NavMenuSectionHolder) convertView.getTag();
    }
               
    navMenuItemHolder.labelView.setText(menuSection.getLabel());
   
    return convertView ;
}


public boolean hasStableIds() {
    return true;
}

public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
}
private static class NavMenuItemHolder {
    private TextView labelView;
    private ImageView iconView;
}

private class NavMenuSectionHolder {
    private TextView labelView;
}
}