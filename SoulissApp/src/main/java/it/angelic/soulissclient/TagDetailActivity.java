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

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.model.SoulissTag;

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
    private int tagId;
    private SoulissDBTagHelper db;
    private SoulissTag collected;

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
            tagId = (int) extras.get("TAG");

        collected = db.getTag(SoulissClient.getAppContext(),tagId);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            TagDetailFragment fragment = new TagDetailFragment();
            transaction.replace(R.id.detailPane, fragment);
            transaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(collected.getNiceName());
    }
}
