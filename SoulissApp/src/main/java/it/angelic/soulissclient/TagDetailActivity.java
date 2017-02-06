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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLDataException;

import it.angelic.soulissclient.fragments.T16RGBAdvancedFragment;
import it.angelic.soulissclient.fragments.T19SingleChannelLedFragment;
import it.angelic.soulissclient.fragments.T1nGenericLightFragment;
import it.angelic.soulissclient.fragments.T31HeatingFragment;
import it.angelic.soulissclient.fragments.T4nFragment;
import it.angelic.soulissclient.fragments.T5nSensorFragment;
import it.angelic.soulissclient.fragments.T6nAnalogueFragment;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
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
import it.angelic.soulissclient.util.LauncherElementEnum;


public class TagDetailActivity extends AbstractStatusedFragmentActivity {

    private SoulissTag collected;
    private SoulissDBTagHelper db;
    private TagDetailFragment fragment;
    private long tagId;

    /**
     * Don't forget to call setResult(Activity.RESULT_OK) in the returning
     * activity or else this method won't be called!
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        // Postpone the shared element return transition.
        postponeEnterTransition();
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
            setResult(Activity.RESULT_OK);
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
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        db = new SoulissDBTagHelper(this);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tag_detail);

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }*/

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
        /*
        try {
            setEnterSharedElementCallback(new SharedElementCallback() {


                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    Log.i(Constants.TAG, "EnterSharedElement.onMapSharedElements:" + sharedElements.size() + collected);
                    //manual override perche il fragment ancora non c'e
                    //sharedElements.put("photo_hero", fragment.getView().findViewById(R.id.photo));
                    //  sharedElements.put("shadow_hero", fragment.getView().findViewById(R.id.infoAlpha));
                    // sharedElements.put("tag_icon", fragment.getView().findViewById(R.id.imageTagIcon));
                    super.onMapSharedElements(names, sharedElements);
                }

                @Override
                public void onRejectSharedElements(List<View> rejectedSharedElements) {
                    Log.i(Constants.TAG, "EnterSharedElement.onRejectSharedElements:" + rejectedSharedElements.size() + collected);
                    super.onRejectSharedElements(rejectedSharedElements);
                }

            });
        } catch (Exception uie) {
            Log.e(Constants.TAG, "UIE:" + uie.getMessage());
        }
*/
/*
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Log.d(Constants.TAG, "ExitSharedElementCallback.onMapSharedElements:"
                        + sharedElements.size() + collected);
                super.onMapSharedElements(names, sharedElements);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onRejectSharedElements(List<View> rejectedSharedElements) {
                Log.d(Constants.TAG, "ExitSharedElementCallback.onRejectSharedElements:"
                        + rejectedSharedElements.size() + collected);

                super.onRejectSharedElements(rejectedSharedElements);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                Log.i(Constants.TAG, "ExitSharedElementCallback.onSharedElementEnd");
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                Log.d(Constants.TAG, "ExitSharedElementCallback.onSharedElementStart:" + sharedElementNames.size() + collected);
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }
        });
*/

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
                if (details instanceof TagDetailFragment) {
                    setResult(Activity.RESULT_OK);
                    supportFinishAfterTransition();
                    return true;
                } else {
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
            case R.id.AddToDashboard:
                SoulissDBLauncherHelper dbl = new SoulissDBLauncherHelper(TagDetailActivity.this);
                LauncherElement nodeLauncher = new LauncherElement();
                nodeLauncher.setComponentEnum(LauncherElementEnum.TAG);
                nodeLauncher.setLinkedObject(collected);
                dbl.addElement(nodeLauncher);
                Toast.makeText(TagDetailActivity.this, collected.getNiceName() + " " + getString(R.string.added_to_dashboard), Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void showTypical(int pos, SoulissTypical soulissTypical) {
        Bundle bundle = new Bundle();
        bundle.putInt("key", pos);
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        // Check what fragment is currently shown, replace if needed.
        Fragment oldFrag = manager.findFragmentById(R.id.detailPane);
        Fragment NewFrag = null;
        // Istanzia e ci mette l'indice
        if (soulissTypical.isSensor())
            NewFrag = T5nSensorFragment.newInstance(pos, soulissTypical);
        else if (soulissTypical instanceof SoulissTypical16AdvancedRGB)
            NewFrag = T16RGBAdvancedFragment.newInstance(pos, soulissTypical);
        else if (soulissTypical instanceof SoulissTypical19AnalogChannel)
            NewFrag = T19SingleChannelLedFragment.newInstance(pos, soulissTypical);
        else if (soulissTypical instanceof SoulissTypical31Heating)
            NewFrag = T31HeatingFragment.newInstance(pos, soulissTypical);
        else if (soulissTypical instanceof SoulissTypical11DigitalOutput || soulissTypical instanceof SoulissTypical12DigitalOutputAuto)
            NewFrag = T1nGenericLightFragment.newInstance(pos, soulissTypical);
        else if (soulissTypical instanceof SoulissTypical41AntiTheft || soulissTypical instanceof SoulissTypical42AntiTheftPeer || soulissTypical instanceof SoulissTypical43AntiTheftLocalPeer)
            NewFrag = T4nFragment.newInstance(pos, soulissTypical);
        else if (soulissTypical instanceof SoulissTypical6nAnalogue)
            NewFrag = T6nAnalogueFragment.newInstance(pos, soulissTypical);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (NewFrag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //details.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));
                oldFrag.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));
                NewFrag.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));

                // Add Fragment B
                ft
                        .replace(R.id.detailPane, NewFrag)
                        .addToBackStack("transaction")
                        // .addSharedElement(mProductText, "hero_title")
                        .commit();
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
