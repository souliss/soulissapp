package it.angelic.soulissclient.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.tagviewlib.SimpleTagViewUtils;

public class SoulissFontAwesomeAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;

    public SoulissFontAwesomeAdapter(Context c) {
        mContext = c;
        TypedArray attr = mContext.obtainStyledAttributes(R.styleable.IconGallery);
        mGalleryItemBackground = attr.getResourceId(
                R.styleable.IconGallery_android_galleryItemBackground, 0);
        attr.recycle();
    }


    public int getCount() {
        return SimpleTagViewUtils.getAwesomeNames(mContext).size();
    }

    public Object getItem(int position) {
        return SimpleTagViewUtils.getAwesomeNames(mContext).get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView txtAwesome = new TextView(mContext);
        txtAwesome.setTypeface(FontAwesomeUtil.getAwesomeTypeface(mContext));
        String code = FontAwesomeUtil.translateAwesomeCode(mContext, (String) getItem(position));
        //content.setFontAwesomeCode(code);
        txtAwesome.setText(code);
        txtAwesome.setTextSize(96);
        txtAwesome.setPadding(20, 20, 10, 0);
        return txtAwesome;
    }
}
