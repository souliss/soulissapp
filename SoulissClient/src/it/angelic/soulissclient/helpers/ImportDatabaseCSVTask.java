package it.angelic.soulissclient.helpers;

import it.angelic.soulissclient.PreferencesActivity;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissLogDTO;
import it.angelic.soulissclient.db.SoulissTypicalDTO;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.preferences.DbSettingsFragment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class ImportDatabaseCSVTask extends AsyncTask<String, Void, Boolean>

{
	private final String TAG = "SoulissApp:" + getClass().getName();
	// private ProgressDialog dialog;

	private SoulissDBHelper DBob;
	private SharedPreferences customSharedPreference;
	private File file;
	private int tottyp = 0;
	private int totNodes = 0;

	// can use UI thread here

	public File getFile() {
		return file;
	}

	public void setImportFile(File file) {
		this.file = file;
	}

	public ImportDatabaseCSVTask(Activity activity) {
		this.activity = activity;
		customSharedPreference = activity.getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
	}

	/** application context. */
	private Activity activity;
	ProgressDialog mProgressDialog;

	@Override
	protected void onPreExecute()

	{
		// dialog = new ProgressDialog(context);
		// dialog.setMessage("Exporting database...");
		// dialog.show();
		activity.runOnUiThread(new Runnable() {
			public void run() {
				mProgressDialog = new ProgressDialog(activity);
				mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
				// mProgressDialog.setTitle(SoulissClient.getAppContext().getString(R.string.));
				mProgressDialog.setTitle("Importing DB");
			}
		});

	}

	// automatically done on worker thread (separate from UI thread)
	protected Boolean doInBackground(final String... args)

	{
		int lin = 0;
		int loopMode = 0;
		// File dbFile = null;// getDatabasePath("excerDB.db");
		DBob = new SoulissDBHelper(SoulissClient.getAppContext());

		try {
			Looper.prepare();
			
			File importDir = new File(Environment.getExternalStorageDirectory(), "//Souliss");

			if (!importDir.exists())
				Toast.makeText(SoulissClient.getAppContext(), SoulissClient.getAppContext().getString(R.string.dialog_import_nofolder), Toast.LENGTH_SHORT).show();

			if (!file.exists()) {
				Toast.makeText(SoulissClient.getAppContext(),SoulissClient.getAppContext().getString(R.string.dialog_import_nofile) , Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Import file doesn't exist!" + file.getAbsolutePath());
				return false;
			}

			activity.runOnUiThread(new Runnable() {
				public void run() {

					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

					mProgressDialog.setMessage("Preparing import");
					mProgressDialog.show();
				}
			});

			file.createNewFile();
			CSVReader csvReader = new CSVReader(new FileReader(file));
			String[] temp;
			DBob.open();
			// SQLiteDatabase db = DBob.getDatabase();
			DBob.truncateImportTables();
			SharedPreferences.Editor editor = customSharedPreference.edit();
			// sistema configurato
			if (customSharedPreference.contains("numNodi"))
				editor.remove("numNodi");
			if (customSharedPreference.contains("numTipici"))
				editor.remove("numTipici");

			activity.runOnUiThread(new Runnable() {
				public void run() {
					mProgressDialog.setMessage("Importing Nodes");
				}
			});
			Thread.sleep(200);
			while ((temp = csvReader.readNext()) != null) {
				lin++;
			}
			final int lines = lin;
			Log.e(TAG, "Importing " + lin + " lines");
			csvReader.close();
			csvReader = new CSVReader(new FileReader(file));

			while ((temp = csvReader.readNext()) != null) {
				// Log.d("Souliss:file import", temp.toString());
				if (temp[1].compareToIgnoreCase(SoulissDB.COLUMN_NODE_ID) == 0) {
					loopMode = 1;
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// lin - 3 head
							mProgressDialog.setMax(lines - 3);

						}
					});
					continue;
				} else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_TYPICAL_NODE_ID) == 0) {
					editor.putInt("numNodi", totNodes);
					loopMode = 2;
					activity.runOnUiThread(new Runnable() {
						public void run() {
							mProgressDialog.setMessage("Importing Typicals");
						}
					});
					continue;
				} else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_LOG_ID) == 0) {
					editor.putInt("numTipici", tottyp);
					loopMode = 3;
					activity.runOnUiThread(new Runnable() {
						public void run() {
							mProgressDialog.setMessage("Importing Log Data, please be patient");
						}
					});
					continue;
				}
				activity.runOnUiThread(new Runnable() {
					public void run() {
						mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
					}
				});
				switch (loopMode) {

				case 1:
					insertNode(temp);
					totNodes++;
					break;
				case 2:
					insertTypical(temp);
					tottyp++;
					break;
				case 3:
					insertLog(temp);
					break;
				default:
					break;
				}

			}
			editor.commit();
			csvReader.close();
			DBob.close();
			Log.i(TAG, "Import finished");
		} catch (SQLException sqlEx) {
			Log.e(TAG, sqlEx.getMessage(), sqlEx);
			return false;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		} catch (Exception es) {
			Log.e(TAG, es.getMessage(), es);
			return false;
		} finally {
			mProgressDialog.dismiss();
		}
		return true;

	}

	private void insertLog(String[] temp) {
		try {
			SoulissLogDTO log = new SoulissLogDTO();
			log.setLogId(Long.valueOf(temp[0]));
			log.setNodeId(Short.valueOf(temp[1]));
			log.setSlot(Short.valueOf(temp[2]));
			log.setTypical(Short.valueOf(temp[3]));
			log.setLogValue(Float.valueOf(temp[4]));

			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(Long.valueOf(temp[5])));
			log.setLogTime(cal);
			log.persist();
		} catch (Exception e) {
			Log.w("skipped log", e.getMessage());
		}

	}

	private void insertTypical(String[] temp) {
		SoulissTypicalDTO typo = new SoulissTypicalDTO();
		typo.setNodeId(Short.valueOf(temp[0]));
		typo.setTypical(Short.valueOf(temp[1]));
		typo.setSlot(Short.valueOf(temp[2]));

		try {
			typo.setInput(Byte.valueOf(temp[3]));
		} catch (Exception e) {
			Log.w("typical W/o input", e.getMessage());
		}
		typo.setOutput(Short.valueOf(temp[4]));
		try {
			typo.setIconId(Integer.valueOf(temp[5]));
		} catch (Exception e) {
			// NO icon here
		}
		try {
			if (temp[6].length() > 0)
				typo.setName(temp[6]);
		} catch (Exception e) {
			Log.w("Unnamed typical", e.getMessage());
		}
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(Long.valueOf(temp[7])));
			typo.setRefreshedAt(cal);
		} catch (Exception e) {
			Log.w("Untimed typ", e.getMessage());
		}
		typo.persist();

	}

	private void insertNode(String[] temp) {

		SoulissNode nit = new SoulissNode(Short.valueOf(temp[1]));
		nit.setHealth(Short.valueOf(temp[2]));
		try {
			nit.setIconResourceId(Integer.valueOf(temp[3]));
		} catch (Exception e) {
			// null icon
		}
		try {
			nit.setName(temp[4]);
		} catch (Exception e) {
			// null name
		}

		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(Long.valueOf(temp[5])));
			nit.setRefreshedAt(cal);
		} catch (Exception e) {
			Log.w("Untimed node", e.getMessage());
		}
		DBob.createOrUpdateNode(nit);

	}

	// can use UI thread here

	@Override
	protected void onPostExecute(final Boolean success)

	{
		if (success) {
			Toast.makeText(SoulissClient.getAppContext(),
					"Imported successfully " + totNodes + " and " + tottyp + " typicals", Toast.LENGTH_SHORT).show();
			final Intent preferencesActivity = new Intent(activity, PreferencesActivity.class);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				AlertDialogHelper.setExtra(preferencesActivity, DbSettingsFragment.class.getName()); //
			// preferencesActivity.putExtra
			// (PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
			preferencesActivity.setAction("db_setup");
			preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			activity.startActivity(preferencesActivity);

		}

		else {
			Toast.makeText(SoulissClient.getAppContext(), "Import failed", Toast.LENGTH_SHORT).show();
		}

	}

}
