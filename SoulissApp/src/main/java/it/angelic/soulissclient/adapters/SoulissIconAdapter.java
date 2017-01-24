package it.angelic.soulissclient.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import it.angelic.soulissclient.R;
import us.feras.ecogallery.EcoGallery;

@Deprecated
public class SoulissIconAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;

    private Integer[] mImageIds = {
            R.drawable.baby1,
            R.drawable.analog1,
            R.drawable.bathtub1,
            R.drawable.bedroom1,
            R.drawable.bell1,
            R.drawable.button1,
            R.drawable.cabinet1,
            R.drawable.cafe1,
            R.drawable.candle1,
            R.drawable.car1,
            R.drawable.chandelier1,
            R.drawable.check1,
            R.drawable.envelope1,
            R.drawable.exit1,
            R.drawable.faucet1,
            R.drawable.favorites2,
            R.drawable.filmmaker1,
            R.drawable.fire1,
            R.drawable.flag1,
            R.drawable.flower,
            R.drawable.fork1,
            R.drawable.frame1,
            R.drawable.gauge1,
            R.drawable.gauge2,
            R.drawable.home1,
            R.drawable.home21,
            R.drawable.home31,
            R.drawable.illumination17,
            R.drawable.knife1,
            R.drawable.lamp,
            R.drawable.light_off,
            R.drawable.light_on,
            R.drawable.lighthouse1,
            R.drawable.lightning1,
            R.drawable.limit1,
            R.drawable.lock1,
            R.drawable.locked1,
            R.drawable.mark1,
            R.drawable.moon,
            R.drawable.pot,
            R.drawable.power,
            R.drawable.robot,
            R.drawable.setpoint,
            R.drawable.shield1,
            R.drawable.souliss_node,
            R.drawable.snow1,
            R.drawable.sos,
            R.drawable.stairs,
            R.drawable.stove1,
            R.drawable.student1,
            R.drawable.sun,
            R.drawable.timer,
            R.drawable.tag1,
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
        imageView.setLayoutParams(new EcoGallery.LayoutParams(200,200));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(mGalleryItemBackground);

        return imageView;
    }
}
