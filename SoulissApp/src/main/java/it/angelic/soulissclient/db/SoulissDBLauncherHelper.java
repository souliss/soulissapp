package it.angelic.soulissclient.db;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

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
import java.util.Map;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.LauncherElementEnum;
import it.angelic.soulissclient.model.SoulissTypical;

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
    private final Context context;

    public SoulissDBLauncherHelper(Context context) {
        super(context);
        // Database fields
        customCachedPrefs = context.getSharedPreferences("SoulissHome", Activity.MODE_PRIVATE);

        //    file = new File( context.getFilesDir(), "SoulissHome");
        File path = context.getFilesDir();
        file = new File(path, "SoulissHome");

        createFakedMap();
        ///saveMap(myMap);
        this.context = context;
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
        LauncherElement scenari = new LauncherElement(LauncherElementEnum.STATIC_SCENES);
        scenari.setDesc(dbt.countScenes() + " scenari configurati");
        scenari.setId(0);
        myMap.put(scenari.getId(), scenari);

        LauncherElement man = new LauncherElement(LauncherElementEnum.STATIC_MANUAL);
        man.setDesc(dbt.countNodes() + " nodi presenti");
        man.setId(1);
        myMap.put(man.getId(), man);

        LauncherElement pro = new LauncherElement(LauncherElementEnum.STATIC_PROGRAMS);
        pro.setDesc(dbt.countTriggers() + " programmi attivi");
        pro.setId(2);
        myMap.put(pro.getId(), pro);

        LauncherElement prob = new LauncherElement(LauncherElementEnum.STATIC_TAGS);
        prob.setDesc(dbt.countTags() + " tags contenenti " + dbt.countTypicalTags() + " dispositivi");
        prob.setId(5);
        myMap.put(prob.getId(), prob);

        LauncherElement prop = new LauncherElement(LauncherElementEnum.STATIC_STATUS);
        prop.setIsFullSpan(true);
        prop.setId(3);
        myMap.put(prop.getId(), prop);

        LauncherElement prot = new LauncherElement(LauncherElementEnum.TYPICAL);
        SoulissTypical tip = super.getTypical(0, (short) 0);
        Log.i(Constants.TAG, "ricaricato tipico farlocco, value=" + tip.getOutput());
        prot.setLinkedObject(tip);
        prot.setTitle(tip.getNiceName());
        prot.setId(4);
        myMap.put(prot.getId(), prot);

        //TAG example
        LauncherElement tag = new LauncherElement(LauncherElementEnum.TAG);
        try {
            tag.setLinkedObject(dbt.getTag(0L));
        } catch (SQLDataException e) {
            e.printStackTrace();
        }
        tag.setId(6);
        tag.setTitle(tag.getLinkedObject().getNiceName());
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


    //FIXME questo deve morire
    public void refreshMap() {
        createFakedMap();
    }

    public ArrayList<LauncherElement> getLauncherItems(Context context) {
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

    public void remove(LauncherElement launcherElement) {
        myMap.remove(launcherElement);
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
