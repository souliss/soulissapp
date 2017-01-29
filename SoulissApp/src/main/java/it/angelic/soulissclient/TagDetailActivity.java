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

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLDataException;
import java.util.List;

import it.angelic.soulissclient.fragments.T16RGBAdvancedFragment;
import it.angelic.soulissclient.fragments.T19SingleChannelLedFragment;
import it.angelic.soulissclient.fragments.T1nGenericLightFragment;
import it.angelic.soulissclient.fragments.T31HeatingFragment;
import it.angelic.soulissclient.fragments.T4nFragment;
import it.angelic.soulissclient.fragments.T5nSensorFragment;
import it.angelic.soulissclient.fragments.T6nAnalogueFragment;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical6nAnalogue;


public class TagDetailActivity extends AbstractStatusedFragmentActivity {

    private long tagId;
    private SoulissDBTagHelper db;
    private SoulissTag collected;
    private TagDetailFragment fragment;

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.i(Constants.TAG, "SAVING IMG RESULT:" + resultCode);

        if (resultCode == RESULT_OK) {
            Uri selectedImage = imageReturnedIntent.getData();
            Log.i(Constants.TAG, "RESULT_OK PATH:" + selectedImage.toString());
            collected.setImagePath(selectedImage.toString());
            //father nullo che' siamo sulla lista grid
            db.createOrUpdateTag(collected);
            //Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
            Log.i(Constants.TAG, "SAVED IMG PATH:" + collected.getImagePath());
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TagDetailFragment fragment = new TagDetailFragment();
        transaction.replace(R.id.detailPane, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {

        Fragment details = getSupportFragmentManager().findFragmentById(R.id.detailPane);
        Log.w(Constants.TAG, "instanceof: " + details.getClass());
        if (details instanceof TagDetailFragment) {
            supportFinishAfterTransition();
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
            setActionBarInfo(collected.getNiceName());
            //don't call super here
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();

        db = new SoulissDBTagHelper(this);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tag_detail);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.get("TAG") != null)
            tagId = (long) extras.get("TAG");


        try {
            collected = db.getTag((int) tagId);
        } catch (SQLDataException sql) {
            Log.i(Constants.TAG, "TAGID NOT FOUND: " + tagId);
        }


        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new TagDetailFragment();
            transaction.replace(R.id.detailPane, fragment);
            transaction.commit();
        }
       /* try {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    Log.i(Constants.TAG, "EnterSharedElement.onMapSharedElements:" + sharedElements.size());
                    //manual override perche il fragment ancora non c'e
                    //sharedElements.put("photo_hero", fragment.getView().findViewById(R.id.photo));
                    //  sharedElements.put("shadow_hero", fragment.getView().findViewById(R.id.infoAlpha));
                    // sharedElements.put("tag_icon", fragment.getView().findViewById(R.id.imageTagIcon));
                    super.onMapSharedElements(names, sharedElements);
                }

                @Override
                public void onRejectSharedElements(List<View> rejectedSharedElements) {
                    Log.i(Constants.TAG, "EnterSharedElement.onMapSharedElements:" + rejectedSharedElements.size());
                    super.onRejectSharedElements(rejectedSharedElements);
                }

            });
        } catch (Exception uie) {
            Log.e(Constants.TAG, "UIE:" + uie.getMessage());
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tagdetail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView icon = (TextView) findViewById(R.id.imageTagIconFAwe);
        switch (item.getItemId()) {
            case android.R.id.home:

                Fragment details = getSupportFragmentManager().findFragmentById(R.id.detailPane);
                Log.w(Constants.TAG, "instanceof: " + details.getClass());
                if (details instanceof TagDetailFragment)
                    supportFinishAfterTransition();
                else {
                    getSupportFragmentManager().popBackStack();
                    setActionBarInfo(collected.getNiceName());
                }
                return true;


            case R.id.Opzioni:
                Intent settingsActivity = new Intent(this, PreferencesActivity.class);
                startActivity(settingsActivity);
                final Intent preferencesActivity = new Intent(this.getBaseContext(), PreferencesActivity.class);
                // evita doppie aperture per via delle sotto-schermate
                preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(preferencesActivity);
                return true;
            case R.id.scegliconaTag:
                AlertDialog.Builder alert2 = AlertDialogHelper.chooseIconDialog(this, icon, null, db, collected);
                alert2.show();
                return true;
            case R.id.rinominaTag:
                AlertDialog.Builder alert = AlertDialogHelper.renameSoulissObjectDialog(this, actionTitleTextView, null, db,
                        collected);
                alert.show();
                return true;
            case R.id.scegliImmagineTag:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, collected.getTagId().intValue());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActionBarInfo(collected.getNiceName());
    }

    public void showDetails(int pos) {
        Bundle bundle = new Bundle();
        bundle.putInt("key", pos);
        List<SoulissTypical> typicalList = collected.getAssignedTypicals();
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        // Check what fragment is currently shown, replace if needed.
        Fragment details = manager.findFragmentById(R.id.detailPane);
        Fragment NewFrag = null;
        // Istanzia e ci mette l'indice
        if (typicalList.get(pos).isSensor())
            NewFrag = T5nSensorFragment.newInstance(pos, typicalList.get(pos));
        else if (typicalList.get(pos) instanceof SoulissTypical16AdvancedRGB)
            NewFrag = T16RGBAdvancedFragment.newInstance(pos, typicalList.get(pos));
        else if (typicalList.get(pos) instanceof SoulissTypical19AnalogChannel)
            NewFrag = T19SingleChannelLedFragment.newInstance(pos, typicalList.get(pos));
        else if (typicalList.get(pos) instanceof SoulissTypical31Heating)
            NewFrag = T31HeatingFragment.newInstance(pos, typicalList.get(pos));
        else if (typicalList.get(pos) instanceof SoulissTypical11DigitalOutput || typicalList.get(pos) instanceof SoulissTypical12DigitalOutputAuto)
            NewFrag = T1nGenericLightFragment.newInstance(pos, typicalList.get(pos));
        else if (typicalList.get(pos) instanceof SoulissTypical41AntiTheft || typicalList.get(pos) instanceof SoulissTypical42AntiTheftPeer || typicalList.get(pos) instanceof SoulissTypical43AntiTheftLocalPeer)
            NewFrag = T4nFragment.newInstance(pos, typicalList.get(pos));
        else if (typicalList.get(pos) instanceof SoulissTypical6nAnalogue)
            NewFrag = T6nAnalogueFragment.newInstance(pos, typicalList.get(pos));
        FragmentTransaction ft = manager.beginTransaction();

        if (NewFrag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //details.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
                //details.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));

                // Create new fragment to add (Fragment B)
                // NewFrag.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
                NewFrag.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));

                // Our shared element (in Fragment A)
                TextView mProductImage = (TextView) details.getView().findViewById(R.id.card_thumbnail_image2);
                TextView mProductText = (TextView) findViewById(R.id.TextViewTypicalsTitle);

                AppBarLayout.Behavior beh = new AppBarLayout.Behavior();

                // Add Fragment B
                manager.beginTransaction()
                        .replace(R.id.detailPane, NewFrag)
                        .addToBackStack("transaction")
                        .addSharedElement(mProductText, "hero_title").commit();//NOT WORK
                //.addSharedElement(mProductText, "ToolbarText");
            } else {
                // if (opzioni.isAnimationsEnabled())
                //     ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                ft.replace(R.id.detailPane, NewFrag);
                ft.addToBackStack(null);
                // ft.remove(details);
                //ft.add(NewFrag,"BOH");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                ft.commit();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No detail to show", Toast.LENGTH_SHORT).show();
        }

    }
}
