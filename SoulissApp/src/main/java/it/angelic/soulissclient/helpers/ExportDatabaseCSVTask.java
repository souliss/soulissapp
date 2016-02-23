package it.angelic.soulissclient.helpers;

import android.content.Context;
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
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;

public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean>

{
    private final String TAG = "SoulissApp:" + getClass().getName();
    private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
    Context context;
    //private ProgressDialog dialog;
    private int exportedNodes;
    // can use UI thread here

    // automatically done on worker thread (separate from UI thread)
    protected Boolean doInBackground(final String... args)

    {

        //SoulissDBHelper DBob = new SoulissDBHelper(SoulissApp.getAppContext());
        File exportDir = new File(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_EXP_FOLDER);

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        Date now = new Date();
        File file = new File(exportDir, yearFormat.format(now) + "_" + SoulissApp.getCurrentConfig() + "_SoulissDB.csv");
        Log.d(TAG, "Creating export File: " + file.getAbsolutePath());
        try {
            saveToFile(file);
        } catch (SQLException sqlEx) {
            Log.e(TAG, "Export error", sqlEx);
            return false;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }

        //ESPORTA PREFS
        File filePrefs = new File(exportDir, yearFormat.format(now) + "_" + SoulissApp.getCurrentConfig() + "_SoulissApp.prefs");
        Utils.saveSharedPreferencesToFile(PreferenceManager.getDefaultSharedPreferences(context), context, filePrefs);
        return true;

    }

    public void loadContext(Context ctx) {
        this.context = ctx;
    }

    @Override
    protected void onPostExecute(final Boolean success)

    {
        String strMeatFormat = context.getString(R.string.export_ok);
        if (success) {
            Toast.makeText(SoulissApp.getAppContext(), String.format(strMeatFormat, exportedNodes), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SoulissApp.getAppContext(), R.string.export_failed, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Souliss DB export Failed");
        }

    }

    @Override
    protected void onPreExecute()

    {
        // dialog = new ProgressDialog(context);
        // dialog.setMessage("Exporting database...");
        // dialog.show();
    }

    // can use UI thread here

    private void saveToFile(File file) throws IOException {

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
            for (int t = 0; t < arrStr.length; t++) {
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
        arrStr = new String[curCSV.getColumnCount()];
        Assert.assertTrue(colNames.length == arrStr.length);
        while (curCSV.moveToNext()) {

            for (int t = 0; t < arrStr.length; t++) {
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
            for (int t = 0; t < arrStr.length; t++) {
                arrStr[t] = curCSV.getString(curCSV.getColumnIndex(colNames[t]));
            }
            csvWrite.writeNext(arrStr);
        }
        Log.i(TAG, "exported LOG rows:" + curCSV.getCount());
        curCSV.close();

        curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_TAGS, null);
        colNames = curCSV.getColumnNames();
        csvWrite.writeNext(colNames);
        arrStr = new String[curCSV.getColumnCount()];
        Assert.assertTrue(colNames.length == arrStr.length);
        while (curCSV.moveToNext()) {
            for (int t = 0; t < arrStr.length; t++) {
                arrStr[t] = curCSV.getString(curCSV.getColumnIndex(colNames[t]));
            }
            csvWrite.writeNext(arrStr);
        }
        Log.i(TAG, "exported TAG rows:" + curCSV.getCount());


        curCSV.close();

        curCSV = db.rawQuery("SELECT * FROM " + SoulissDB.TABLE_TAGS_TYPICALS, null);
        colNames = curCSV.getColumnNames();
        csvWrite.writeNext(colNames);
        arrStr = new String[curCSV.getColumnCount()];
        Assert.assertTrue(colNames.length == arrStr.length);
        while (curCSV.moveToNext()) {
            for (int t = 0; t < arrStr.length; t++) {
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


}
