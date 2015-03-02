/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package it.angelic.soulissclient;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.transition.Transition;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import it.angelic.soulissclient.adapters.TransitionAdapter;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.fragments.T16RGBAdvancedFragment;
import it.angelic.soulissclient.fragments.T19SingleChannelLedFragment;
import it.angelic.soulissclient.fragments.T1nGenericLightFragment;
import it.angelic.soulissclient.fragments.T31HeatingFragment;
import it.angelic.soulissclient.fragments.T4nFragment;
import it.angelic.soulissclient.fragments.T5nSensorFragment;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class TagDetailActivity extends AbstractStatusedFragmentActivity {


    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    private long tagId;
    private SoulissDBTagHelper db;
    private SoulissTag collected;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void faiFigate(){
        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                ImageView hero = (ImageView) findViewById(R.id.photo);
                hero.animate().scaleX(1.0f);
                /*ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(), "tint",
                        getResources().getColor(R.color.white), 0);
                color.start();*/
                findViewById(R.id.fabTag).animate().alpha(1.0f);
                findViewById(R.id.star).animate().alpha(1.0f);
                TextView bro =(TextView) findViewById(R.id.tagTextView);
                bro.setText(collected.getNiceName());
                getWindow().getEnterTransition().removeListener(this);
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissClient.getOpzioni();
        db= new SoulissDBTagHelper(this);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tagswrapper);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.get("TAG") != null)
            tagId = (long) extras.get("TAG");

        collected = db.getTag(SoulissClient.getAppContext(),(int) tagId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            faiFigate();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            TagDetailFragment fragment = new TagDetailFragment();
            transaction.replace(R.id.detailPane, fragment);
            transaction.commit();
        }
    }

    public void showDetails(int pos) {
        Bundle bundle = new Bundle();
        bundle.putInt("key",pos );
        List <SoulissTypical> st = collected.getAssignedTypicals();
        android.support.v4.app.FragmentManager manager=getSupportFragmentManager();
        // Check what fragment is currently shown, replace if needed.
        Fragment details = manager.findFragmentById(R.id.detailPane);
        Fragment NewFrag = null;
        // Istanzia e ci mette l'indice
        if (st.get(pos).isSensor())
            NewFrag = T5nSensorFragment.newInstance(pos, st.get(pos));
        else if (st.get(pos) instanceof SoulissTypical16AdvancedRGB)
            NewFrag = T16RGBAdvancedFragment.newInstance(pos, st.get(pos));
        else if (st.get(pos) instanceof SoulissTypical19AnalogChannel)
            NewFrag = T19SingleChannelLedFragment.newInstance(pos, st.get(pos));
        else if (st.get(pos) instanceof SoulissTypical31Heating)
            NewFrag = T31HeatingFragment.newInstance(pos, st.get(pos));
        else if (st.get(pos) instanceof SoulissTypical11DigitalOutput || st.get(pos) instanceof SoulissTypical12DigitalOutputAuto)
            NewFrag = T1nGenericLightFragment.newInstance(pos, st.get(pos));
        else if (st.get(pos) instanceof SoulissTypical41AntiTheft || st.get(pos) instanceof SoulissTypical42AntiTheftPeer || st.get(pos) instanceof SoulissTypical43AntiTheftLocalPeer)
            NewFrag = T4nFragment.newInstance(pos, st.get(pos));
        FragmentTransaction ft = manager.beginTransaction();
        if (opzioni.isAnimationsEnabled())
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        ft.replace(R.id.detailPane, NewFrag);
        // ft.addToBackStack(null);
        // ft.remove(details);
        //ft.add(NewFrag,"BOH");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(collected.getNiceName());
    }
}
