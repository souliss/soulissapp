package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.PreferencesActivity;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissLogDTO;
import it.angelic.soulissclient.db.SoulissTypicalDTO;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.preferences.DbSettingsFragment;

public class ImportDatabaseCSVTask extends AsyncTask<String, Void, Boolean>

{
    private final String TAG = "SoulissApp:" + getClass().getName();
    // private ProgressDialog dialog;

    private SoulissDBHelper database;
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

    /**
     * application context.
     */
    private Activity activity;
    ProgressDialog mProgressDialog;

    private File importDir;

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
        database = new SoulissDBHelper(SoulissClient.getAppContext());

        try {
            Looper.prepare();

            importDir = new File(Environment.getExternalStorageDirectory(), "//Souliss");

            if (!importDir.exists())
                Toast.makeText(SoulissClient.getAppContext(), SoulissClient.getAppContext().getString(R.string.dialog_import_nofolder), Toast.LENGTH_SHORT).show();

            if (!file.exists()) {
                Toast.makeText(SoulissClient.getAppContext(), SoulissClient.getAppContext().getString(R.string.dialog_import_nofile), Toast.LENGTH_SHORT).show();
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
            SoulissDBHelper.open();
            // SQLiteDatabase db = database.getDatabase();
            database.truncateImportTables();
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
                    Log.i(TAG, "Importing nodes...");
                    loopMode = 1;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            // lin - 3 head
                            mProgressDialog.setMax(lines - 3);

                        }
                    });
                    continue;
                } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_TYPICAL_NODE_ID) == 0) {
                    Log.i(TAG, "Imported " + totNodes + " nodes. Importing typicals...");
                    editor.putInt("numNodi", totNodes);
                    loopMode = 2;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            mProgressDialog.setMessage("Importing Typicals");
                        }
                    });
                    continue;
                } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_LOG_ID) == 0) {
                    editor.putInt("numTipici", database.countTypicals());
                    Log.i(TAG, "Imported " + tottyp + " typicals. Importing Logs...");
                    loopMode = 3;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            mProgressDialog.setMessage("Importing Log Data, please be patient");
                        }
                    });
                    continue;
                } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_TAG_ID) == 0) {
                    editor.putInt("numTipici", database.countTypicals());
                    Log.i(TAG, "Imported " + tottyp + " typicals. Importing Logs...");
                    loopMode = 4;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            mProgressDialog.setMessage("Importing Tag Data, please be patient");
                        }
                    });
                    continue;
                } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_TAG_TYP_TAG_ID) == 0) {
                    editor.putInt("numTipici", database.countTypicals());
                    Log.i(TAG, "Imported " + tottyp + " typicals. Importing Logs...");
                    loopMode = 5;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            mProgressDialog.setMessage("Importing Tag Data, please be patient");
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
                        SoulissTypicalDTO ret = insertTypical(temp);
                        tottyp++;
                        break;
                    case 3:
                        insertLog(temp);
                        break;
                    case 4:
                        insertTag(temp);
                        break;
                    case 5:
                        insertTagTyp(temp);
                        break;
                    default:
                        break;
                }

            }
            editor.commit();
            csvReader.close();
            database.close();
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
        activity.runOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog.setMessage("Importing Preferences");
            }
        });
        try {
            File filePrefs = new File(importDir, file.getName() + ".prefs");
            loadSharedPreferencesFromFile(filePrefs);
        } catch (Exception e) {
            Log.e(TAG, "Errore import prefs", e);
        }

        return true;

    }

    private void insertTagTyp(String[] temp) {

    }

    /*
    public static final String[] ALLCOLUMNS_TAGS = {COLUMN_TAG_ID, COLUMN_TAG_NAME,
            COLUMN_TAG_ICONID, COLUMN_TAG_IMGPTH, COLUMN_TAG_ORDER};
     */
    private void insertTag(String[] temp) {
        SoulissTag tIns = new SoulissTag();
        tIns.setTagId(Long.valueOf(temp[0]));

        try {
            if (temp[1].length() > 0)
                tIns.setName(temp[1]);
        } catch (Exception e) {
            Log.w(Constants.TAG, e.getMessage());
        }

        try {
            tIns.setIconResourceId(Integer.valueOf(temp[2]));
        } catch (Exception e) {
            // NO icon here
        }

        try {
            if (temp[3].length() > 0)
                tIns.setImagePath(temp[3]);
        } catch (Exception e) {
            Log.w(Constants.TAG, e.getMessage());
        }
        tIns.setTagOrder(Integer.valueOf(temp[4]));
         
    }

    private void insertLog(String[] temp) {
        try {
            SoulissLogDTO log = new SoulissLogDTO();
            log.setLogId(Long.valueOf(temp[0]));
            log.setNodeId(Short.valueOf(temp[1]));
            log.setSlot(Short.valueOf(temp[2]));
            log.setLogValue(Float.valueOf(temp[3]));
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(Long.valueOf(temp[4])));
            log.setLogTime(cal);
            log.persist();
        } catch (Exception e) {
            Log.w("skipped log", e.getMessage());
        }
    }

    private SoulissTypicalDTO insertTypical(String[] temp) {
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
                typo.setFavourite(temp[6] == "1" ? true : false);
        } catch (Exception e) {
            Log.w("NOT Favourite", e.getMessage());
        }
        try {
            if (temp[7].length() > 0)
                typo.setName(temp[7]);
        } catch (Exception e) {
            Log.w("Unnamed typical", e.getMessage());
        }
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(Long.valueOf(temp[8])));
            typo.setRefreshedAt(cal);
        } catch (Exception e) {
            Log.w("Untimed typ", e.getMessage());
        }
        try {
            typo.setWarnDelayMsec(Integer.valueOf(temp[9]));
        } catch (Exception e) {
            // NO icon here
        }
        typo.persist();
        return typo;
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
        database.createOrUpdateNode(nit);
    }

    // can use UI thread here

    @Override
    protected void onPostExecute(final Boolean success)

    {
        if (success) {
            Toast.makeText(SoulissClient.getAppContext(),
                    "Imported successfully " + totNodes + " and " + tottyp + " typicals", Toast.LENGTH_SHORT).show();
            final Intent preferencesActivity = new Intent(activity, PreferencesActivity.class);

            preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
            // preferencesActivity.putExtra
            // (PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
            preferencesActivity.setAction("db_setup");
            preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            activity.startActivity(preferencesActivity);

        } else {
            Toast.makeText(SoulissClient.getAppContext(), "Import failed", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(activity).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));

                Log.d(Constants.TAG, "Restored pref:" + key + " Value:" + v);
            }
            prefEdit.commit();
            SoulissClient.getOpzioni().reload();
            res = true;
        } catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

}
