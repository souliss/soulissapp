package it.angelic.soulissclient.adapters;

import com.poliveira.parallaxrecycleradapter.ParallaxRecyclerAdapter;

import java.util.List;

/**
 * Created by Ale on 08/03/2015.
 */
public class ParallaxExenderAdapter extends ParallaxRecyclerAdapter{
    private int position;
    public ParallaxExenderAdapter(List data) {
        super(data);
    }
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
