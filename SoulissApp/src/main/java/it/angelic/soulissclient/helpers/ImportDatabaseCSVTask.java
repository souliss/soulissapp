package it.angelic.soulissclient.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Calendar;
import java.util.Date;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.PreferencesActivity;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissModelException;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDB;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.model.db.SoulissDBTagHelper;
import it.angelic.soulissclient.model.db.SoulissLogDTO;
import it.angelic.soulissclient.model.db.SoulissTypicalDTO;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.util.LauncherElementEnum;
import it.angelic.soulissclient.util.SoulissUtils;

public class ImportDatabaseCSVTask extends AsyncTask<String, Void, Boolean>

{
    private static final int PHASE_NODES = 1;
    private static final int PHASE_TYPICALS = 2;
    private static final int PHASE_LOGS = 3;
    private static final int PHASE_TAGS = 4;
    private static final int PHASE_DASHB = 6;
    private static final int PHASE_TAG_TYP = 5;

    // private ProgressDialog dialog;

    private SoulissDBLauncherHelper databaseLauncher;
    private SoulissDBTagHelper database;
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
        //customSharedPreference = activity.getSharedPreferences("SoulissPrefs", Activity.MODE_PRIVATE);
        customSharedPreference = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    /**
     * application context.
     */
    private Activity activity;
    private ProgressDialog mProgressDialog;

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
        int linesNum = 0;
        // File dbFile = null;// getDatabasePath("excerDB.db");
        database = new SoulissDBTagHelper(SoulissApp.getAppContext());
        databaseLauncher = new SoulissDBLauncherHelper(SoulissApp.getAppContext());
        File importDir;
        try {
            Looper.prepare();

            importDir = new File(Environment.getExternalStorageDirectory(), Constants.EXTERNAL_EXP_FOLDER);

            if (!importDir.exists())
                Toast.makeText(SoulissApp.getAppContext(), SoulissApp.getAppContext().getString(R.string.dialog_import_nofolder), Toast.LENGTH_SHORT).show();

            if (!file.exists()) {
                Toast.makeText(SoulissApp.getAppContext(), SoulissApp.getAppContext().getString(R.string.dialog_import_nofile), Toast.LENGTH_SHORT).show();
                Log.e(Constants.TAG, "Import file doesn't exist!" + file.getAbsolutePath());
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
                linesNum++;
            }
            Log.w(Constants.TAG, "Importing " + linesNum + " lines");
            csvReader.close();
            csvReader = new CSVReader(new FileReader(file));

            processCsv(csvReader, editor, linesNum);
            editor.apply();
            csvReader.close();

            Log.i(Constants.TAG, "Import finished");
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getMessage(), e);
            return false;
        } catch (Exception es) {
            Log.e(Constants.TAG, es.getMessage(), es);
            return false;
        } finally {
            database.close();
            mProgressDialog.dismiss();
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog.setMessage("Importing Preferences");
            }
        });
        try {
            //uguale al dump tranne ultima parte
            File filePrefs = new File(importDir, file.getName().substring(0, file.getName().lastIndexOf("_")) + "_SoulissApp.prefs");
            SoulissUtils.loadSharedPreferencesFromFile(SoulissApp.getAppContext(), filePrefs);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Errore import prefs", e);
        }

        return true;

    }

    private void processCsv(CSVReader csvReader, SharedPreferences.Editor editor, final int lines) throws IOException {
        String[] temp;
        int loopMode = -1;

        while ((temp = csvReader.readNext()) != null) {
            // Log.d("Souliss:file import", temp.toString());
            if (temp[1].compareToIgnoreCase(SoulissDB.COLUMN_NODE_ID) == 0) {
                Log.i(Constants.TAG, "Importing nodes...");
                loopMode = PHASE_NODES;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        // lin - 3 head
                        mProgressDialog.setMax(lines - 3);

                    }
                });
                continue;
            } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_TYPICAL_NODE_ID) == 0) {
                Log.i(Constants.TAG, "Imported " + totNodes + " nodes. Importing typicals...");
                editor.putInt("numNodi", totNodes);
                loopMode = PHASE_TYPICALS;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.setMessage(String.format(activity.getString(R.string.importing_be_patient), activity.getString(R.string.typical)));
                    }
                });
                continue;
            } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_LOG_ID) == 0) {
                editor.putInt("numTipici", database.countTypicals());
                Log.i(Constants.TAG, "Imported " + tottyp + " typicals. Importing Logs...");
                loopMode = PHASE_LOGS;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        String melo = String.format(activity.getString(R.string.importing_be_patient), activity.getString(R.string.historyof));
                        mProgressDialog.setMessage(melo);
                    }
                });
                continue;
            } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_TAG_ID) == 0) {
                Log.i(Constants.TAG, "Imported " + tottyp + " typicals. Importing TAGS...");
                loopMode = PHASE_TAGS;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.setMessage(String.format(activity.getString(R.string.importing_be_patient), activity.getString(R.string.tags)));
                    }
                });
                continue;
            } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_TAG_TYP_SLOT) == 0) {
                Log.i(Constants.TAG, "Imported " + tottyp + " typicals. Importing TAG_TYP...");
                loopMode = PHASE_TAG_TYP;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.setMessage(String.format(activity.getString(R.string.importing_be_patient), activity.getString(R.string.tags)));
                    }
                });
                continue;
            } else if (temp[0].compareToIgnoreCase(SoulissDB.COLUMN_LAUNCHER_ID) == 0) {
                Log.i(Constants.TAG, "Imported " + tottyp + " typicals. Importing DASHBOARD...");
                loopMode = PHASE_DASHB;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        mProgressDialog.setMessage("Importing Dashboard Data, please be patient");
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

                case PHASE_NODES:
                    insertNode(temp);
                    totNodes++;
                    break;
                case PHASE_TYPICALS:
                    SoulissTypicalDTO ret = insertTypical(temp);
                    tottyp++;
                    break;
                case PHASE_LOGS:
                    insertLog(temp);
                    break;
                case PHASE_TAGS:
                    insertTag(temp);
                    break;
                case PHASE_TAG_TYP:
                    insertTagTyp(temp);
                    break;
                case PHASE_DASHB:
                    insertDashboard(temp);
                    break;
                default:
                    break;
            }
        }
    }

    private void insertDashboard(String[] temp) {
        try {
            LauncherElement reto = new LauncherElement();
            reto.setId(Long.parseLong(temp[0]));
            reto.setComponentEnum(LauncherElementEnum.valueOf(temp[1]));
            reto.setOrder(Short.parseShort(temp[2]));

            try {
                ISoulissObject assign = null;
                if (temp[1].length() > 0 && temp[2].length() == 0) {//nodeId
                    SoulissNode fat = database.getSoulissNode(Integer.valueOf(temp[1]));
                    assign = fat;
                } else if (temp[2].length() > 0) {//typ
                    SoulissTypical typ = database.getTypical(Integer.valueOf(temp[1]), Short.valueOf(temp[2]));
                    assign = typ;
                } else if (temp[3].length() > 0) {//typ
                    SoulissTag tag = database.getTag(Integer.valueOf(temp[3]));
                    assign = tag;
                } else if (temp[4].length() > 0) {//typ
                    SoulissScene sc = database.getScene(Integer.valueOf(temp[4]));
                    assign = sc;
                } else {
                    throw new SoulissModelException("Caso non contemplato in immportazione");
                }

                reto.setLinkedObject(assign);
            } catch (Exception e) {
                Log.w(Constants.TAG, e.getMessage());
            }

            try {
                if (temp[5].length() > 0)//title
                    reto.setTitle(temp[5]);
            } catch (Exception e) {
                Log.w(Constants.TAG, e.getMessage());
            }

            try {
                if (temp[6].length() > 0)//title
                    reto.setDesc(temp[6]);
            } catch (Exception e) {
                Log.w(Constants.TAG, e.getMessage());
            }

            try {
                if (temp[7].length() > 0)//title
                    reto.setIsFullSpan(!"0".equals(temp[7]));
            } catch (Exception e) {
                Log.w(Constants.TAG, e.getMessage());
            }

            databaseLauncher.addElement(reto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public static final String[] ALLCOLUMNS_TAGS_TYPICAL = {COLUMN_TAG_TYP_SLOT,
                COLUMN_TAG_TYP_NODE_ID, COLUMN_TAG_TYP_TAG_ID, COLUMN_TAG_TYP_PRIORITY};
     */
    private void insertTagTyp(String[] temp) {
        try {
            SoulissTag hero = database.getTag(Long.parseLong(temp[2]));
            SoulissTypical polloTyp = database.getTypical(Short.valueOf(temp[1]),Short.valueOf(temp[0]));

            database.createOrUpdateTagTypicalNode( polloTyp ,hero,Integer.valueOf(temp[3]));
        } catch (SQLDataException e) {
            e.printStackTrace();
        }
    }

    /*
    public static final String[] ALLCOLUMNS_TAGS = {COLUMN_TAG_ID, COLUMN_TAG_NAME,
            COLUMN_TAG_ICONID, COLUMN_TAG_IMGPTH, COLUMN_TAG_ORDER};
     */
    private void insertTag(String[] temp) {
        SoulissTag tIns = new SoulissTag();
        tIns.setTagId(Long.parseLong(temp[0]));

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
        try {
            tIns.setTagOrder(Integer.valueOf(temp[4]));
        } catch (Exception e) {
            Log.w(Constants.TAG, e.getMessage());
        }
        try {
            tIns.setFatherId(Long.valueOf(temp[5]));
        } catch (Exception e) {
            Log.w(Constants.TAG, e.getMessage());
            tIns.setFatherId(null);
        }

        database.createOrUpdateTag(tIns);
    }

    private void insertLog(String[] temp) {
        try {
            SoulissLogDTO log = new SoulissLogDTO();
            log.setLogId(Long.valueOf(temp[0]));
            log.setNodeId(Short.valueOf(temp[1]));
            log.setSlot(Short.valueOf(temp[2]));
            log.setLogValue(Float.valueOf(temp[3]));
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(Long.parseLong(temp[4])));
            log.setLogTime(cal);
            log.persist();
        } catch (Exception e) {
            Log.e("skipped log", e.getMessage());
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
                typo.setFavourite(temp[6].equals("1"));
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

        SoulissNode nit = new SoulissNode(activity, Short.valueOf(temp[1]));
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
            String formatStr = activity.getString(R.string.imported_success);

            Toast.makeText(activity,
                    String.format(formatStr, totNodes, tottyp), Toast.LENGTH_SHORT).show();
            final Intent preferencesActivity = new Intent(activity, PreferencesActivity.class);

            preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
            // preferencesActivity.putExtra
            // (PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
            preferencesActivity.setAction("db_setup");
            preferencesActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            preferencesActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            activity.startActivity(preferencesActivity);

        } else {
            Toast.makeText(SoulissApp.getAppContext(), "Import failed", Toast.LENGTH_SHORT).show();
        }

    }

}
