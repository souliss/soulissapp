package it.angelic.soulissclient.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.LauncherElementEnum;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.SoulissTypicalFactory;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 *
 * @author Ale
 */
public class SoulissDBLauncherHelper extends SoulissDBHelper {

    public SoulissDBLauncherHelper(Context context) {
        super(context);
    }

    // Database fields


    public static synchronized SQLiteDatabase getDatabase() {
        return database;
    }


    public List<LauncherElement> getLauncherItems(Context context) {
        List<LauncherElement> comments = new ArrayList<LauncherElement>();
        if (!database.isOpen())
            open();

        //create FAKED Launcher array
        LauncherElement scenari = new LauncherElement(LauncherElementEnum.SCENES);
        comments.add(scenari);

        LauncherElement man = new LauncherElement(LauncherElementEnum.MANUAL);
        comments.add(man);

        LauncherElement pro = new LauncherElement(LauncherElementEnum.PROGRAMS);
        comments.add(pro);

        LauncherElement prop = new LauncherElement(LauncherElementEnum.STATO);
        prop.setIsFullSpan(true);
        comments.add(prop);


        return comments;
    }


    public int countTags() {
        if (!database.isOpen())
            open();
        Cursor mCount = database.rawQuery("select count(*) from " + SoulissDB.TABLE_TAGS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }




}
