package it.angelic.soulissclient.preferences;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ExportDatabaseCSVTask;
import it.angelic.soulissclient.helpers.ImportDatabaseCSVTask;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.db.SoulissDBHelper;

public class DbPreferenceListener implements OnPreferenceClickListener {

    private Activity parent;
    private File mPath;
    private static final String DB_BACKUP_FORMAT = ".csv";
    private static final int DIALOG_LOAD_FILE = 1000;

    // EXPORT
    private String[] mFileList;
    private String mChosenFile;
    private SoulissPreferenceHelper opzioni;
    private SoulissDBHelper datasource;

    public DbPreferenceListener(Activity parent) {
        super();
        this.parent = parent;
        opzioni = SoulissApp.getOpzioni();
        datasource = new SoulissDBHelper(parent);
        mPath = new File(Environment.getExternalStorageDirectory() + Constants.EXTERNAL_EXP_FOLDER);
    }

    @Override
    public boolean onPreferenceClick(Preference arg0) {
        if ("dbexp".equals(arg0.getKey()))
            return exportDb();
        else if ("dbimp".equals(arg0.getKey())) {
            loadFileList();
            onCreateDialog(DIALOG_LOAD_FILE);
            return true;
        } else if ("createdb".equals(arg0.getKey())) {
            return createDbRequest();
        } else if ("settingshare".equals(arg0.getKey())) {
            return shareSettings();
        } else if ("dbopt".equals(arg0.getKey())) {
            try {
                datasource.clean();
                Toast.makeText(parent, parent.getString(R.string.opt_vacuum_complete) + datasource.getSize(), Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Toast.makeText(parent, "CLEAN ERROR: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else if ("dropdb".equals(arg0.getKey())) {
            AlertDialog.Builder alert = AlertDialogHelper.dropSoulissDBDialog(parent, datasource);
            SoulissDBHelper.open();
            alert.show();
            return true;
        }
        return true;

    }


    protected boolean exportDb() {
        if ((ContextCompat.checkSelfPermission(parent, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(parent, "Exporting Data", Toast.LENGTH_SHORT).show();
            try {
                ExportDatabaseCSVTask t = new ExportDatabaseCSVTask();
                t.loadContext(parent);
                t.execute("");
            } catch (Exception ex) {
                Log.e(Constants.TAG, ex.toString());
                ex.printStackTrace();
                return false;
            }
            return true;
        } else {

            ActivityCompat.requestPermissions(parent, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            //Toast.makeText(parent, "Descarc noi actualizari!", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private void loadFileList() {
        if ((ContextCompat.checkSelfPermission(parent, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            mPath.mkdirs();
            //TODO filtrare anche per config
            if (mPath.exists()) {
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        File sel = new File(dir, filename);
                        return filename.endsWith(DB_BACKUP_FORMAT);
                    }
                };
                mFileList = mPath.list(filter);
            } else {
                mFileList = new String[0];
            }
        } else {
            ActivityCompat.requestPermissions(parent, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }

    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new Builder(parent);

        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                // builder.setMessage("The DB will be replaced with chosen file's contents");
                if (mFileList == null) {
                    Log.e(Constants.TAG, "NULL in Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                if (opzioni.isDbConfigured()) {
                    Log.w(Constants.TAG, "DB not empty, can't import");
                    dialog = builder.create();
                    Toast.makeText(parent, parent.getString(R.string.db_notempty), Toast.LENGTH_SHORT).show();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        Log.d(Constants.TAG, "Import from " + mChosenFile);
                        try {
                            ImportDatabaseCSVTask t = new ImportDatabaseCSVTask(parent);
                            t.setImportFile(new File(mPath + File.separator + mChosenFile));
                            t.execute("");

                        } catch (Exception ex) {
                            Log.e(Constants.TAG, "Import ERROR", ex);
                        }
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }

    private boolean shareSettings() {
        // File sharedDir = parent.getApplicationContext().getCacheDir();
        File exportDir = new File(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_EXP_FOLDER + "/export");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File sharedP;
        try {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            sharedP = File.createTempFile("Souliss", ".preferences", exportDir);
            // File sharedP = new File(sharedDir, "exportSettings.tmp");
            saveSharedPreferencesToFile(sharedP);
            Uri uriToZip = Uri.fromFile(sharedP);

            Log.w(Constants.TAG, "Exported preferences to: " + uriToZip.toString());
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriToZip);
            shareIntent.setType("*/*");
            parent.startActivity(Intent.createChooser(shareIntent, parent.getString(R.string.command_send)));
            Toast.makeText(parent, "Export complete", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(parent, "Sharing Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "Share ERR:", e);
        }


        return true;
    }

    /*
     * DA TOGLIRE DOPO LA 1.7.0
	 * */
    private boolean saveSharedPreferencesToFile(File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref =
                    PreferenceManager.getDefaultSharedPreferences(parent);
            output.writeObject(pref.getAll());

            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    private boolean createDbRequest() {
        if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
            // mostro anche con IP privato non configurato MA souliss
            // raggiungibile
            AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(parent);
            alert.show();
            return true;
        }
        // mostra dialogo creazione DB
        AlertDialog.Builder alert = AlertDialogHelper
                .updateSoulissDBDialog(parent, opzioni.getCachedAddress(), opzioni);
        alert.show();
        return true;
    }
}
