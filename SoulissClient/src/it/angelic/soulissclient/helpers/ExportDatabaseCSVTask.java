package it.angelic.soulissclient.helpers;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean>

{
	Context context;
	private final String TAG = "SoulissApp:" + getClass().getName();
	private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);

	//private ProgressDialog dialog;
private int exportedNodes;
	// can use UI thread here

	public void loadContext(Context ctx) {
		this.context = ctx;
	}

	@Override
	protected void onPreExecute()

	{
		// dialog = new ProgressDialog(context);
		// dialog.setMessage("Exporting database...");

		// dialog.show();

	}

	// automatically done on worker thread (separate from UI thread)
	protected Boolean doInBackground(final String... args)

	{

		SoulissDBHelper DBob = new SoulissDBHelper(SoulissClient.getAppContext());
		File exportDir = new File(Environment.getExternalStorageDirectory(), "//Souliss");

		if (!exportDir.exists())
		{
			Log.d(TAG, "Creating export DIR");
			exportDir.mkdirs();
		}

		Date now = new Date();
		File file = new File(exportDir, yearFormat.format(now) + "_SoulissDB.csv");

		try {

			file.createNewFile();
			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

			DBob.open();
			SQLiteDatabase db = SoulissDBHelper.getDatabase();
			// NODI
			Cursor curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_NODES, null);
			csvWrite.writeNext(curCSV.getColumnNames());
			while (curCSV.moveToNext()) {
				String arrStr[] = { curCSV.getString(0), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3),
						curCSV.getString(4), curCSV.getString(5) };
				csvWrite.writeNext(arrStr);
				exportedNodes++;
			}
			Log.i(TAG, "exported NODE rows:" + curCSV.getCount());

			curCSV.close();
			// TIPICI
			curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_TYPICALS, null);
			csvWrite.writeNext(curCSV.getColumnNames());
			while (curCSV.moveToNext()) {
				String arrStr[] = { curCSV.getString(0), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3),
						curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7) };
				csvWrite.writeNext(arrStr);
			}
			Log.i(TAG, "exported TYP rows:" + curCSV.getCount());
			curCSV.close();

			curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_LOGS, null);
			csvWrite.writeNext(curCSV.getColumnNames());
			while (curCSV.moveToNext()) {
				String arrStr[] = { curCSV.getString(0), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3),
						curCSV.getString(4), curCSV.getString(5) };
				csvWrite.writeNext(arrStr);
			}
			Log.i(TAG, "exported LOG rows:" + curCSV.getCount());
			// dialog.setMessage( "exported LOG rows:" + curCSV.getCount());
			csvWrite.close();
			curCSV.close();

			//return true;

		}

		catch (SQLException sqlEx){
			Log.e(TAG, "Export error", sqlEx);
			return false;
		}

		catch (IOException e){
			Log.e(TAG, e.getMessage(), e);
			return false;
		}
		
		//ESPORTA PREFS
		File filePrefs = new File(exportDir, yearFormat.format(now) + "_SoulissDB.csv.prefs");
		saveSharedPreferencesToFile(filePrefs);
		return true;

	}

	// can use UI thread here

	@Override
	protected void onPostExecute(final Boolean success)

	{
		String strMeatFormat =context.getString(R.string.export_ok);
		if (success) {
			Toast.makeText(SoulissClient.getAppContext(), String.format(strMeatFormat, exportedNodes), Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(SoulissClient.getAppContext(), "Export failed", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Souliss DB export Failed");
		}

	}
	/*
	 * Esporto tutte le pref utente, non quelle cached
	 * */
	private boolean saveSharedPreferencesToFile(File dst) {
	    boolean res = false;
	    ObjectOutputStream output = null;
	    try {
	        output = new ObjectOutputStream(new FileOutputStream(dst));
	        SharedPreferences pref = 
	        		PreferenceManager.getDefaultSharedPreferences(context);
	        output.writeObject(pref.getAll());

	        res = true;
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally {
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

	

}
