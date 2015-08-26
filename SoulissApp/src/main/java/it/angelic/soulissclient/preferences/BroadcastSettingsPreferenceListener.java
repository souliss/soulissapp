package it.angelic.soulissclient.preferences;

import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class BroadcastSettingsPreferenceListener implements OnPreferenceClickListener {

	private Activity parent;
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
        File mPath = new File(Environment.getExternalStorageDirectory() + "//Souliss//");
	}
	private void fireBCastSubScreen(){
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
	public boolean onPreferenceClick(Preference arg0) {
		//if ("dbexp".equals(arg0.getKey()))
		//	return exportDb();
		fireBCastSubScreen();
		return true;

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
