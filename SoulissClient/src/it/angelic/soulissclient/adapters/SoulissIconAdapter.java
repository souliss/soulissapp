package it.angelic.soulissclient.adapters;

import it.angelic.soulissclient.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class SoulissIconAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;

    private Integer[] mImageIds = {
            R.drawable.baby,
            R.drawable.bathtub,
            R.drawable.bedroom,
            R.drawable.bell,
            R.drawable.button,
            R.drawable.cabinet,
            R.drawable.candle,
            R.drawable.car,
            R.drawable.check,
            R.drawable.envelope,
            R.drawable.exit,
            R.drawable.extinguisher,
            R.drawable.faucet,
            R.drawable.filmmaker,
            R.drawable.fire,
            R.drawable.flag,
            R.drawable.flower,
            R.drawable.fork,
            R.drawable.frame,
            R.drawable.gauge,
            R.drawable.gauge2,
            R.drawable.home,
            R.drawable.home2,
            R.drawable.home3,
            R.drawable.knife,
            R.drawable.lamp,
            R.drawable.light_off,
            R.drawable.light_on,
            R.drawable.lighthouse,
            R.drawable.lightning,
            R.drawable.lock,
            R.drawable.locked,
            R.drawable.mark,
            R.drawable.moon,
            R.drawable.pot,
            R.drawable.power,
            R.drawable.robot,
            R.drawable.shield,
            R.drawable.shock,
            R.drawable.sink,
            R.drawable.skull,
            R.drawable.souliss_node,
            R.drawable.snow,
            R.drawable.sos,
            R.drawable.stairs,
            R.drawable.stove,
            R.drawable.student,
            R.drawable.sun,
            R.drawable.timer,
            R.drawable.tv,
            R.drawable.twitter,
            R.drawable.warn,
            R.drawable.window
    };

    public SoulissIconAdapter(Context c) {
        mContext = c;
        TypedArray attr = mContext.obtainStyledAttributes(R.styleable.IconGallery);
        mGalleryItemBackground = attr.getResourceId(
                R.styleable.IconGallery_android_galleryItemBackground, 0);
        attr.recycle();
    }
    

    public int getCount() {
        return mImageIds.length;
    }

    public Object getItem(int position) {
        return position;
    }
    
    public int getItemResId(int position) {
        return mImageIds[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);

        imageView.setImageResource(mImageIds[position]);
        imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(mGalleryItemBackground);

        return imageView;
    }
}
