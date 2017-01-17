package it.angelic.soulissclient.db;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.LauncherElementEnum;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 *
 * @author Ale
 */
public class SoulissDBLauncherHelper extends SoulissDBHelper {
    private static final String MAP = "map";
    private static final Type MAP_TYPE = new TypeToken<Map<Integer, LauncherElement>>() {
    }.getType();

    // private static SharedPreferences prefs = MyApplication.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

    private static Map<Integer, LauncherElement> myMap;
    private final SharedPreferences customCachedPrefs;
    private final File file;

    public SoulissDBLauncherHelper(Context context) {
        super(context);
        // Database fields
        customCachedPrefs = context.getSharedPreferences("SoulissHome", Activity.MODE_PRIVATE);

        //    file = new File( context.getFilesDir(), "SoulissHome");
        File path = context.getFilesDir();
        file = new File(path, "SoulissHome");

        createFakedMap();
        ///saveMap(myMap);
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

    private void createFakedMap() {
        myMap = new HashMap<Integer, LauncherElement>();
        //Map<> comments = new ArrayList<LauncherElement>();
        open();

        SoulissDBTagHelper dbt = new SoulissDBTagHelper(SoulissApp.getAppContext());

        //create FAKED Launcher array
        LauncherElement scenari = new LauncherElement(LauncherElementEnum.SCENE);
        scenari.setId(0);
        myMap.put(scenari.getId(), scenari);

        LauncherElement man = new LauncherElement(LauncherElementEnum.MANUAL);
        man.setId(1);
        myMap.put(man.getId(), man);

        LauncherElement pro = new LauncherElement(LauncherElementEnum.PROGRAMS);
        pro.setId(2);
        myMap.put(pro.getId(), pro);

        LauncherElement prop = new LauncherElement(LauncherElementEnum.STATO);
        prop.setIsFullSpan(true);
        prop.setId(3);
        myMap.put(prop.getId(), prop);

        LauncherElement prot = new LauncherElement(LauncherElementEnum.TYPICAL);
        prot.setLinkedObject(super.getTypical(0, (short) 0));
        prot.setId(4);
        myMap.put(prot.getId(), prot);

        //TAG example
        LauncherElement tag = new LauncherElement(LauncherElementEnum.TAG);
        try {
            tag.setLinkedObject(dbt.getTag(0L));
        } catch (SQLDataException e) {
            e.printStackTrace();
        }
        tag.setId(5);
        myMap.put(tag.getId(), tag);

    }
   /* public void saveLauncherItems( List<LauncherElement> in){

        SharedPreferences.Editor editor = customCachedPrefs.edit();
        JSONArray array = new JSONArray();
        if (customCachedPrefs.contains("launcherItems"))
            editor.remove("launcherItems");
            // Add your objects to the array

        try {
            array.put(in);
            editor.putString("launcherItems", array.toString());
        } catch ( Exception e) {
            e.printStackTrace();
        }



        editor.apply();
    }

    public List<LauncherElement>  loadLauncherItems(){
        String deMarshall = customCachedPrefs.getString("launcherItems","");

        JSONArray array = new JSONArray();
    }
*/

    public List<LauncherElement> getLauncherItems(Context context) {
        //Set chiavi= myMap.keySet();

        //TODO sorting by order
        return new ArrayList<LauncherElement>(myMap.values());
    }

    public Map<Integer, LauncherElement> loadMap() {
        int length = (int) file.length();

        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(file);

            in.read(bytes);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String contents = new String(bytes);
        if (myMap == null) {
            myMap = new Gson().fromJson(contents, MAP_TYPE);
        }
        return myMap;
    }

    public void saveMap(Map<Integer, LauncherElement> map) {

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write((new Gson().toJson(map)).getBytes());
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        myMap = map;
    }


}
