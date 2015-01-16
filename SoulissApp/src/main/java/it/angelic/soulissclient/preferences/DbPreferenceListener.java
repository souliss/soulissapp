package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.ExportDatabaseCSVTask;
import it.angelic.soulissclient.helpers.ImportDatabaseCSVTask;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

public class DbPreferenceListener implements OnPreferenceClickListener {

	private Activity parent;
	private File mPath;
	private static final String FTYPE = ".csv";
	private static final int DIALOG_LOAD_FILE = 1000;

	// EXPORT
	private String[] mFileList;
	private String mChosenFile;
	private SoulissPreferenceHelper opzioni;
	private SoulissDBHelper datasource;

	public DbPreferenceListener(Activity parent) {
		super();
		this.parent = parent;
		opzioni = SoulissClient.getOpzioni();
		datasource = new SoulissDBHelper(parent);
		mPath = new File(Environment.getExternalStorageDirectory() + "//Souliss//");
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
		} else if ("dbopt".equals(arg0.getKey())) {try {
            datasource.clean();
            Toast.makeText(parent, "Vacuum Complete", Toast.LENGTH_SHORT).show();
            return true;
        }catch (Exception e){
            Toast.makeText(parent, "CLEAN ERROR", Toast.LENGTH_SHORT).show();
        }

		} else if ("dropdb".equals(arg0.getKey())) {
			AlertDialog.Builder alert = AlertDialogHelper.dropSoulissDBDialog(parent, datasource);
			datasource.open();
			alert.show();
			return true;
		}
		return true;

	}

	private boolean exportDb() {
		Toast.makeText(parent, "Exporting Data", Toast.LENGTH_SHORT).show();
		try {
			ExportDatabaseCSVTask t = new ExportDatabaseCSVTask();
			t.loadContext(parent);
			t.execute("");
		} catch (Exception ex) {
			Log.e(Constants.TAG, ex.toString());
			ex.printStackTrace();
		}

		return true;
	}

	private void loadFileList() {
		try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			Log.e(Constants.TAG, "unable to write on the sd card " + e.toString());
		}
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return filename.endsWith(FTYPE) || sel.isDirectory();
				}
			};
			mFileList = mPath.list(filter);
		} else {
			mFileList = new String[0];
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
