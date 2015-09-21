package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
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

public class SetHtmlRootListener implements OnPreferenceClickListener {

	private Activity parent;
	private File mPath;
	private static final int DIALOG_LOAD_FILE = 1000;
	
	private String[] mFileList;
	private String mChosenFile;
	private static final String FTYPE = ".html";


	// EXPORT
	private SoulissPreferenceHelper opzioni;

	public SetHtmlRootListener(Activity parent) {
		super();
		this.parent = parent;
		opzioni = SoulissApp.getOpzioni();
		//datasource = new SoulissDBHelper(parent);
		mPath = new File(Environment.getExternalStorageDirectory() + "//Souliss//");
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(parent);
		mFileList = loadFileList(mPath, FTYPE);
		builder.setTitle(parent.getString(R.string.dialog_choose_html));
		if (mFileList == null) {
			Log.e(Constants.TAG, "Showing file picker before loading the file list");
			dialog = builder.create();
			return true;
		}
		builder.setItems(mFileList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mChosenFile = mFileList[which];
				opzioni.setHtmlRoot("file://"+mPath+"/"+ mChosenFile);
			}
		});
		dialog = builder.show();
		// return dialog;
		return true;
	}
	
	public static String[] loadFileList(File mPath, final String ftype) {
		String[] mFileList;
		try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			Log.e(Constants.TAG, "unable to write on the sd card " + e.toString());
		}
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return filename.contains(ftype) || sel.isDirectory();
				}
			};
			mFileList = mPath.list(filter);
		} else {
			mFileList = new String[0];
		}
		return mFileList;
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
}
