package it.angelic.soulissclient.helpers;

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

import junit.framework.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;

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

			exportDir.mkdirs();
		}

		Date now = new Date();
		File file = new File(exportDir, yearFormat.format(now) + "_SoulissDB.csv");
		Log.d(TAG, "Creating export File: "+ file.getAbsolutePath());
		try {

			file.createNewFile();
			CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

			SoulissDBHelper.open();
			SQLiteDatabase db = SoulissDBHelper.getDatabase();
			// NODI
			Cursor curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_NODES, null);
			String colNames[] = curCSV.getColumnNames();
			csvWrite.writeNext(colNames);
			String arrStr[] = new String[curCSV.getColumnCount()];
			while (curCSV.moveToNext()) {
				for (int t=0; t<arrStr.length;t++ ){
					arrStr[t] = curCSV.getString(curCSV.getColumnIndex(colNames[t]));
				}
				csvWrite.writeNext(arrStr);
				exportedNodes++;
			}
			Log.i(TAG, "exported NODE rows:" + curCSV.getCount());

			curCSV.close();
			// TIPICI
			curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_TYPICALS, null);
			colNames = curCSV.getColumnNames();
			csvWrite.writeNext(colNames);
			arrStr= new String[curCSV.getColumnCount()];
			Assert.assertTrue(colNames.length == arrStr.length);
			while (curCSV.moveToNext()) {

				for (int t=0; t<arrStr.length;t++ ){
					arrStr[t] = curCSV.getString(curCSV.getColumnIndex(colNames[t]));
				}
				csvWrite.writeNext(arrStr);
			}
			Log.i(TAG, "exported TYP rows:" + curCSV.getCount());
			curCSV.close();

			curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_LOGS, null);
			colNames = curCSV.getColumnNames();
			csvWrite.writeNext(colNames);
			arrStr = new String[curCSV.getColumnCount()];
			Assert.assertTrue(colNames.length == arrStr.length);
			while (curCSV.moveToNext()) {
				for (int t=0; t<arrStr.length;t++ ){
					arrStr[t] = curCSV.getString(curCSV.getColumnIndex(colNames[t]));
				}
				csvWrite.writeNext(arrStr);
			}
			Log.i(TAG, "exported LOG rows:" + curCSV.getCount());
			// dialog.setMessage( "exported LOG rows:" + curCSV.getCount());
			csvWrite.close();
			curCSV.close();

			curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_TAGS, null);
			colNames = curCSV.getColumnNames();
			csvWrite.writeNext(colNames);
			arrStr = new String[curCSV.getColumnCount()];
			Assert.assertTrue(colNames.length == arrStr.length);
			while (curCSV.moveToNext()) {
				for (int t=0; t<arrStr.length;t++ ){
					arrStr[t] = curCSV.getString(curCSV.getColumnIndex(colNames[t]));
				}
				csvWrite.writeNext(arrStr);
			}
			Log.i(TAG, "exported TAG rows:" + curCSV.getCount());
			// dialog.setMessage( "exported LOG rows:" + curCSV.getCount());
			csvWrite.close();
			curCSV.close();

			curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_TAGS_TYPICALS, null);
			colNames = curCSV.getColumnNames();
			csvWrite.writeNext(colNames);
			arrStr = new String[curCSV.getColumnCount()];
			Assert.assertTrue(colNames.length == arrStr.length);
			while (curCSV.moveToNext()) {
				for (int t=0; t<arrStr.length;t++ ){
					arrStr[t] = curCSV.getString(curCSV.getColumnIndex(colNames[t]));
				}
				csvWrite.writeNext(arrStr);
			}
			Log.i(TAG, "exported TAG rows:" + curCSV.getCount());
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
