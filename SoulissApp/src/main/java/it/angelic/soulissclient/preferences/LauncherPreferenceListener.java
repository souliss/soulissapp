package it.angelic.soulissclient.preferences;

import android.app.Activity;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissModelException;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;

public class LauncherPreferenceListener implements OnPreferenceClickListener {

    private static final String DB_BACKUP_FORMAT = ".csv";
    private static final int DIALOG_LOAD_FILE = 1000;
    private SoulissDBHelper datasource;
    private String mChosenFile;
    // EXPORT
    private String[] mFileList;
    private File mPath;
    private SoulissPreferenceHelper opzioni;
    private Activity parent;

    public LauncherPreferenceListener(Activity parent) {
        super();
        this.parent = parent;
        opzioni = SoulissApp.getOpzioni();
        datasource = new SoulissDBHelper(parent);
        mPath = new File(Environment.getExternalStorageDirectory() + Constants.EXTERNAL_EXP_FOLDER);
    }

    @Override
    public boolean onPreferenceClick(Preference arg0) {
        if ("rstlauncher".equals(arg0.getKey()))
            resetDefaultPref();

        return true;

    }


    protected void resetDefaultPref() {
        SoulissDBLauncherHelper database = new SoulissDBLauncherHelper(parent);
        List<LauncherElement> po = database.getLauncherItems(parent);
        List<LauncherElement> def = database.getDefaultStaticDBLauncherElements(parent);

        for (LauncherElement la : def
                ) {
            try {
                database.addElement(la);
            } catch (SoulissModelException e) {
                Toast.makeText(parent, "Errore salvataggio tile", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }


}
