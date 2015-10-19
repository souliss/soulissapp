package it.angelic.soulissclient.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.LauncherElementEnum;

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

        SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissApp.getAppContext());

        //create FAKED Launcher array
        LauncherElement scenari = new LauncherElement(LauncherElementEnum.SCENE);
        comments.add(scenari);

        LauncherElement man = new LauncherElement(LauncherElementEnum.MANUAL);
        comments.add(man);

        LauncherElement pro = new LauncherElement(LauncherElementEnum.PROGRAMS);
        comments.add(pro);

        LauncherElement prop = new LauncherElement(LauncherElementEnum.STATO);
        prop.setIsFullSpan(true);
        comments.add(prop);

        LauncherElement prot = new LauncherElement(LauncherElementEnum.TYPICAL);
        prot.setLinkedObject(super.getTypical(0, (short) 0));
        comments.add(prot);

        //TAG example
        LauncherElement tag = new LauncherElement(LauncherElementEnum.TAG);
        try {
            tag.setLinkedObject(dbt.getTag(SoulissApp.getAppContext(),0L ));
        } catch (SQLDataException e) {
            e.printStackTrace();
        }
        comments.add(tag);


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
