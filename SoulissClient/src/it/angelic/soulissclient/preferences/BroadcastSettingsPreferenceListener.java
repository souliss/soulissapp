package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.PreferencesActivity;
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
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

public class BroadcastSettingsPreferenceListener implements OnPreferenceClickListener {

	private Activity parent;
	private File mPath;
	private static final String FTYPE = ".csv";
	private static final int DIALOG_LOAD_FILE = 1000;

	// EXPORT
	private String[] mFileList;
	private String mChosenFile;
	private SoulissPreferenceHelper opzioni;
	private SoulissDBHelper datasource;

	public BroadcastSettingsPreferenceListener(Activity parent) {
		super();
		this.parent = parent;
		opzioni = SoulissClient.getOpzioni();
		datasource = new SoulissDBHelper(parent);
		mPath = new File(Environment.getExternalStorageDirectory() + "//Souliss//");
	}
	private void fireBCastSubScreen(){
		Intent inten = parent.getIntent();
		inten.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		parent.finish();
		parent.overridePendingTransition(0, 0);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			inten.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, BroadcastSettingsFragment.class.getName());
		inten.setAction("bcast_setup");
		Toast.makeText(parent,
				parent.getResources().getString(R.string.dbstruct_req),
				Toast.LENGTH_SHORT).show();
		parent.startActivity(inten);
		
		return;
	}
	@Override
	public boolean onPreferenceClick(Preference arg0) {
		//if ("dbexp".equals(arg0.getKey()))
		//	return exportDb();
		fireBCastSubScreen();
		return true;

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
			if (opzioni.isDbConfigured()){
				Log.w(Constants.TAG, "DB not empty, can't import");
				dialog = builder.create();
				Toast.makeText(parent, "DB not empty. Drop DB first", Toast.LENGTH_SHORT).show();
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
			//mostro anche con IP privato non configurato MA souliss raggiungibile
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
