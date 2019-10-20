package it.angelic.soulissclient.drawer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.util.FontAwesomeUtil;

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


        TextView labelView = convertView
                .findViewById(R.id.dtitle);
        TextView iconView = convertView
                .findViewById(R.id.dicon);

        navMenuItemHolder = new NavMenuItemHolder();
        navMenuItemHolder.labelView = labelView;
        navMenuItemHolder.iconView = iconView;
        if (menuItem.getId() == activeSection)
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_alpha));

        convertView.setTag(navMenuItemHolder);

        navMenuItemHolder.labelView.setText(menuItem.getLabel());
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
            TextView labelView = convertView
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