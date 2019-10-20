package it.angelic.soulissclient.preferences;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceActivity;

import androidx.preference.Preference;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.db.SoulissDBHelper;

public class BroadcastSettingsPreferenceListener implements Preference.OnPreferenceClickListener {

    private static final String FTYPE = ".csv";
    private static final int DIALOG_LOAD_FILE = 1000;
    private String mChosenFile;
    // EXPORT
    private String[] mFileList;
    private Activity parent;

    public BroadcastSettingsPreferenceListener(Activity parent) {
        super();
        this.parent = parent;
        SoulissPreferenceHelper opzioni = SoulissApp.getOpzioni();
        SoulissDBHelper datasource = new SoulissDBHelper(parent);
        //File mPath = new File(Environment.getExternalStorageDirectory() + Constants.EXTERNAL_EXP_FOLDER);
    }

    private void fireBCastSubScreen() {
        Intent inten = parent.getIntent();
        inten.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        parent.finish();
        parent.overridePendingTransition(0, 0);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        inten.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, BroadcastSettingsFragment.class.getName());
        inten.setAction("bcast_setup");
        parent.startActivity(inten);
        return;

    }

    @Override
    public boolean onPreferenceClick(androidx.preference.Preference arg0) {
        //if ("dbexp".equals(arg0.getKey()))
        //	return exportDb();
        fireBCastSubScreen();
        return true;

    }

}
