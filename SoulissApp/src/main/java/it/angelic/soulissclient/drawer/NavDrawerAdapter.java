package it.angelic.soulissclient.drawer;

import it.angelic.soulissclient.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerAdapter extends ArrayAdapter<INavDrawerItem> {

    private final int activeSection;
    private LayoutInflater inflater;

    public NavDrawerAdapter(Context context, int textViewResourceId, INavDrawerItem[] objects, int mode) {

        super(context, textViewResourceId, objects);
        this.inflater = LayoutInflater.from(context);
        this.activeSection = mode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        INavDrawerItem menuItem = this.getItem(position);
        if (menuItem.getType() == NavMenuItem.ITEM_TYPE) {
            view = getItemView(convertView, parent, menuItem);
        } else {
            view = getSectionView(convertView, parent, menuItem);
        }
        return view;
    }

    public View getItemView(View convertView, ViewGroup parentView, INavDrawerItem navDrawerItem) {

        NavMenuItem menuItem = (NavMenuItem) navDrawerItem;
        NavMenuItemHolder navMenuItemHolder = null;

        // if (convertView == null) {
        if (menuItem.getId() < 0)
            convertView = inflater.inflate(R.layout.drawer_list_item, parentView, false);
        else
            convertView = inflater.inflate(R.layout.drawer_list_item_son, parentView, false);


        TextView labelView = (TextView) convertView
                .findViewById(R.id.dtitle);
        ImageView iconView = (ImageView) convertView
                .findViewById(R.id.dicon);

        navMenuItemHolder = new NavMenuItemHolder();
        navMenuItemHolder.labelView = labelView;
        navMenuItemHolder.iconView = iconView;
        if (menuItem.getId() == activeSection)
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.grey_alpha));

        convertView.setTag(navMenuItemHolder);
        //  }

        //if ( navMenuItemHolder == null ) {
        //   navMenuItemHolder = (NavMenuItemHolder) convertView.getTag();
        //  }

        navMenuItemHolder.labelView.setText(menuItem.getLabel());
        navMenuItemHolder.iconView.setImageResource(menuItem.getIcon());

        return convertView;
    }

    public View getSectionView(View convertView, ViewGroup parentView,
                               INavDrawerItem navDrawerItem) {

        NavMenuSection menuSection = (NavMenuSection) navDrawerItem;
        NavMenuSectionHolder navMenuItemHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.navdrawer_section, parentView, false);
            TextView labelView = (TextView) convertView
                    .findViewById(R.id.navmenusection_label);

            navMenuItemHolder = new NavMenuSectionHolder();
            navMenuItemHolder.labelView = labelView;
            convertView.setTag(navMenuItemHolder);
        }

        if (navMenuItemHolder == null) {
            navMenuItemHolder = (NavMenuSectionHolder) convertView.getTag();
        }

        navMenuItemHolder.labelView.setText(menuSection.getLabel());

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }


    private static class NavMenuItemHolder {
        private TextView labelView;
        private ImageView iconView;
    }

    private class NavMenuSectionHolder {
        private TextView labelView;
    }
}