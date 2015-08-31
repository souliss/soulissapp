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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

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
import it.angelic.soulissclient.helpers.AlertDialogHelper;
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
 * <p/>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class TagDetailActivity extends AbstractStatusedFragmentActivity {


    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    private long tagId;
    private SoulissDBTagHelper db;
    private SoulissTag collected;
    private FloatingActionButton fab;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void faiFigate() {
        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                ImageView mLogoImg = (ImageView) findViewById(R.id.photo);
                mLogoImg.animate().scaleX(1.0f);
                ImageView heroAlpha = (ImageView) findViewById(R.id.infoAlpha);
                heroAlpha.animate().scaleX(1.0f);
                /*ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(), "tint",
                        getResources().getColor(R.color.white), 0);
                color.start();*/
                //findViewById(R.id.fabTag).animate().alpha(1.0f);
                //findViewById(R.id.star).animate().alpha(1.0f);
                getWindow().getEnterTransition().removeListener(this);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissClient.getOpzioni();
        db = new SoulissDBTagHelper(this);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tagswrapper);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.get("TAG") != null)
            tagId = (long) extras.get("TAG");

        collected = db.getTag(SoulissClient.getAppContext(), (int) tagId);
        fab = (FloatingActionButton) findViewById(R.id.fabTag);


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
        bundle.putInt("key", pos);
        List<SoulissTypical> st = collected.getAssignedTypicals();
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
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

        if (NewFrag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                details.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
                details.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));

                // Create new fragment to add (Fragment B)
                NewFrag.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
                NewFrag.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));


                // Our shared element (in Fragment A)
                ImageView mProductImage = (ImageView) details.getView().findViewById(R.id.card_thumbnail_image2);
                TextView mProductText = (TextView)findViewById(R.id.actionbar_title);

                // Add Fragment B
                FragmentTransaction ftt = manager.beginTransaction()
                        .add(R.id.detailPane, NewFrag)
                        .addToBackStack("transaction")
                        .addSharedElement(mProductImage, "MyTransition")
                        .addSharedElement(mProductText, "ToolbarText");
                ftt.commit();
            } else {
                // if (opzioni.isAnimationsEnabled())
                //     ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                ft.add(R.id.detailPane, NewFrag);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tagdetail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ImageView icon = (ImageView) findViewById(R.id.node_icon);
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // nothing to do here...
                } else {
                   /* supportFinishAfterTransition();
                    if (opzioni.isAnimationsEnabled())
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;*/
                    android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                    Fragment details = manager.findFragmentById(R.id.detailPane);
                    FragmentTransaction ftt = manager.beginTransaction()
                            .remove(details);
                    ftt.commit();
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
                AlertDialog.Builder alert = AlertDialogHelper.renameSoulissObjectDialog(this, actionTitle, null, db,
                        collected);
                alert.show();
                return true;
            case R.id.scegliImmagineTag:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, collected.getTagId().intValue());
               /* Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
                Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
                chooserIntent.putExtra
                        (
                                Intent.EXTRA_INITIAL_INTENTS,
                                new Intent[]{takePhotoIntent}
                        );
                //uso come reqId il TagId cosi da riconoscere cosa avevo richiesto
                startActivityForResult(chooserIntent, (int) arrayAdapterPosition);*/
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.i(Constants.TAG, "SAVING IMG RESULT:" + resultCode);

        if (resultCode == RESULT_OK) {
            Uri selectedImage = imageReturnedIntent.getData();
            Log.i(Constants.TAG, "RESULT_OK PATH:" + selectedImage.toString());
            collected.setImagePath(selectedImage.toString());
            //String[] filePathColumn = {MediaStore.Images.Media.DATA};
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
    protected void onStart() {
        super.onStart();
        setActionBarInfo(collected.getNiceName());


    }
}
