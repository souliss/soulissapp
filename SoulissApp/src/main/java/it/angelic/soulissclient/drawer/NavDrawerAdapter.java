package it.angelic.soulissclient.drawer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.util.FontAwesomeUtil;

import static it.angelic.soulissclient.drawer.DrawerMenuHelper.MANUAL;
import static it.angelic.soulissclient.drawer.DrawerMenuHelper.PROGRAMS;
import static it.angelic.soulissclient.drawer.DrawerMenuHelper.SCENES;
import static it.angelic.soulissclient.drawer.DrawerMenuHelper.TAGS;

public class NavDrawerAdapter extends ArrayAdapter<INavDrawerItem> {

    private final int activeSection;
    private final Activity context;
    private LayoutInflater inflater;

    public NavDrawerAdapter(Activity context, int textViewResourceId, INavDrawerItem[] objects, int mode) {

        super(context, textViewResourceId, objects);
        this.inflater = LayoutInflater.from(context);
        this.activeSection = mode;
        this.context = context;
    }

    public View getItemView(ViewGroup parentView, INavDrawerItem navDrawerItem) {

        NavMenuItem menuItem = (NavMenuItem) navDrawerItem;
        NavMenuItemHolder navMenuItemHolder = null;

        // if (convertView == null) {
        View convertView;
        if (menuItem.getId() < 0)
            convertView = inflater.inflate(R.layout.drawer_list_item, parentView, false);
        else
            convertView = inflater.inflate(R.layout.drawer_list_item_son, parentView, false);


        TextView labelView = (TextView) convertView
                .findViewById(R.id.dtitle);
        TextView iconView = (TextView) convertView
                .findViewById(R.id.dicon);

        navMenuItemHolder = new NavMenuItemHolder();
        navMenuItemHolder.labelView = labelView;
        navMenuItemHolder.iconView = iconView;
        if (menuItem.getId() == activeSection)
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_alpha));

        convertView.setTag(navMenuItemHolder);
        //  }

        //if ( navMenuItemHolder == null ) {
        //   navMenuItemHolder = (NavMenuItemHolder) convertView.getTag();
        //  }

        navMenuItemHolder.labelView.setText(menuItem.getLabel());

        switch (navDrawerItem.getId()) {
            case SCENES:
                navMenuItemHolder.iconView.setTextColor(ContextCompat.getColor(context, R.color.md_yellow_50));
                break;
            case PROGRAMS:
                navMenuItemHolder.iconView.setTextColor(ContextCompat.getColor(context, R.color.md_light_blue_50));
                break;
            case MANUAL:
                navMenuItemHolder.iconView.setTextColor(ContextCompat.getColor(context, R.color.md_green_50));
                break;
            case TAGS:
                navMenuItemHolder.iconView.setTextColor(ContextCompat.getColor(context, R.color.md_purple_50));
                break;
        }
        FontAwesomeUtil.prepareMenuFontAweTextView(context, navMenuItemHolder.iconView, menuItem.getIcon());

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    public View getSectionView(View convertView, ViewGroup parentView,
                               INavDrawerItem navDrawerItem) {

        NavMenuSection menuSection = (NavMenuSection) navDrawerItem;
        NavMenuSectionHolder navMenuItemHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.drawer_section, parentView, false);
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
    public
    @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = null;
        INavDrawerItem menuItem = this.getItem(position);
        if (menuItem.getType() == NavMenuItem.ITEM_TYPE) {
            view = getItemView(parent, menuItem);
        } else {
            view = getSectionView(convertView, parent, menuItem);
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }


    private static class NavMenuItemHolder {
        private TextView iconView;
        private TextView labelView;
    }

    private static class NavMenuSectionHolder {
        private TextView labelView;
    }
}