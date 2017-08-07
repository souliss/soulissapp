package it.angelic.soulissclient.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissDBOpenHelper;

/**
 * Created by shine@angelic.it on 10/10/2015.
 */
public class SoulissUtils {
    private static Criteria criteria;

    /**
     * utility minutes
     *
     * @param ref
     * @return
     */
    public static String getTimeAgo(Calendar ref) {
        Calendar now = Calendar.getInstance();

        long milliseconds1 = ref.getTimeInMillis();
        long milliseconds2 = now.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long diffSeconds = diff / 1000;
        return getScaledTime(diffSeconds) + SoulissApp.getAppContext().getString(R.string.ago);
    }

    public static String getScaledTime(long diffSeconds) {
        if (diffSeconds < 120)
            return "" + diffSeconds + " sec.";
        long diffMinutes = diffSeconds / 60;
        if (diffMinutes < 120)
            return "" + diffMinutes + " min.";
        long diffHours = diffMinutes / (60);
        if (diffHours < 72)
            return "" + diffHours + " hr";

        long diffDays = diffHours / (24);
        return "" + diffDays + SoulissApp.getAppContext().getString(R.string.days);
    }

    public static String getDuration(long typicalOnDurationMsec) {
        long secondi = typicalOnDurationMsec / 1000;
        if (secondi < 60)
            return "" + secondi + " sec.";
        long diffMinutes = secondi / 60;
        secondi = secondi % 60;//resto
        if (diffMinutes < 120)
            return "" + diffMinutes + " minuti e " + secondi + " secondi";
        long diffHours = diffMinutes / (60);
        diffMinutes = diffMinutes % 60;
        return "" + diffHours + " ore e " + diffMinutes + " minuti";
        //return null;
    }

    public static Float celsiusToFahrenheit(float in) {
        return Float.valueOf((9.0f / 5.0f) * in + 32);
    }

    public static Float fahrenheitToCelsius(float fahrenheit) {
        return Float.valueOf((5.0f / 9.0f) * (fahrenheit - 32));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static void fileCopy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getRealPathFromURI(Context ctx, Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = ctx.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;

    }

    public static String getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                return writeToTempImageAndGetPathUri(context, bmp).toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static boolean loadSharedPreferencesFromFile(Context ctx, File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
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
                else if (v instanceof Set)
                    prefEdit.putStringSet(key, (Set) v);

                Log.d(Constants.TAG, "Restored pref:" + key + " Value:" + v);
            }
            prefEdit.commit();
            SoulissApp.getOpzioni().reload();
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

    public static Criteria getGeoCriteria() {

        if (criteria == null)
            criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }


    public static void loadSoulissDbFromFile(Context c, String config, File importDir) throws IOException {

        File bckDb = new File(importDir, config + "_" + SoulissDBOpenHelper.DATABASE_NAME);
        SoulissDBHelper db = new SoulissDBHelper(c);
        SoulissDBHelper.open();
        String DbPath = SoulissDBHelper.getDatabase().getPath();
        db.close();
        File newDb = new File(DbPath);
        SoulissUtils.fileCopy(bckDb, newDb);
        Log.w(Constants.TAG, config + " DB loaded: " + bckDb.getPath());

    }
    /*
         * Esporto tutte le pref utente, non quelle cached
         * */
    public static boolean saveSharedPreferencesToFile(SharedPreferences pref, Context context, File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            Log.w(Constants.TAG, "Persisting preferences, size:" + pref.getAll().size());
            output.writeObject(pref.getAll());

            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
