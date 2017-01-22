package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.model.ISoulissObject;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.SoulissModelException;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.LauncherElementEnum;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 *
 * @author Ale
 */
public class SoulissDBLauncherHelper extends SoulissDBHelper {
    private static final String MAP = "map";


    // private static SharedPreferences prefs = MyApplication.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

    private static List<LauncherElement> launcherElementList;

    private final Context context;
    private final SharedPreferences preferences;

    public SoulissDBLauncherHelper(Context context) {
        super(context);
        open();
        // Database fields
        launcherElementList = getDBLauncherElements(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> visibili = preferences.getStringSet("launcher_elems", new HashSet<String>());
        ///init
        this.context = context;
        if (launcherElementList.isEmpty()) {
            List<LauncherElement> launcherElementtemp = getDefaultStaticDBLauncherElements(context);

            for (LauncherElement lau : launcherElementtemp) {
                try {
                    Log.w(Constants.TAG, "Launcher empty, adding def: " + lau.getTitle());
                    addElement(lau);
                } catch (SoulissModelException e) {
                    e.printStackTrace();
                }
            }
            // preferences.edit().putStringSet("launcher_elems", visibili).apply();
        }

        //tolgo i nascosti
        Set<LauncherElement> removeSet = new HashSet<>();
        for (int i = 0; i < launcherElementList.size(); i++) {
            if (!visibili.contains("" + launcherElementList.get(i).getId())) {
                removeSet.add(launcherElementList.get(i));
            }
        }
        launcherElementList.removeAll(removeSet);


    }


    /**
     * campi singoli altrimenti side effects
     *
     * @param nodeIN
     * @return
     */
    public int createOrUpdateLauncherElement(LauncherElement nodeIN) throws SoulissModelException {
        ContentValues values = new ContentValues();
        // wrap values from object
        values.put(SoulissDB.COLUMN_LAUNCHER_ID, nodeIN.getId());
        values.put(SoulissDB.COLUMN_LAUNCHER_TITLE, nodeIN.getTitle());
        values.put(SoulissDB.COLUMN_LAUNCHER_DESC, nodeIN.getDesc());
        values.put(SoulissDB.COLUMN_LAUNCHER_TYPE, nodeIN.getComponentEnum().ordinal());
        values.put(SoulissDB.COLUMN_LAUNCHER_ORDER, nodeIN.getOrder());
        values.put(SoulissDB.COLUMN_LAUNCHER_FULL_SPAN, nodeIN.isFullSpan() ? 1 : 0);
        if (nodeIN.getLinkedObject() != null) {
            if (nodeIN.getLinkedObject() instanceof SoulissNode)
                values.put(SoulissDB.COLUMN_LAUNCHER_NODE_ID, ((SoulissNode) nodeIN.getLinkedObject()).getNodeId());
            else if (nodeIN.getLinkedObject() instanceof SoulissTypical) {
                values.put(SoulissDB.COLUMN_LAUNCHER_NODE_ID, ((SoulissTypical) nodeIN.getLinkedObject()).getNodeId());
                values.put(SoulissDB.COLUMN_LAUNCHER_SLOT_ID, ((SoulissTypical) nodeIN.getLinkedObject()).getSlot());
            } else if (nodeIN.getLinkedObject() instanceof SoulissScene)
                values.put(SoulissDB.COLUMN_LAUNCHER_SCENE_ID, ((SoulissScene) nodeIN.getLinkedObject()).getId());
            else if (nodeIN.getLinkedObject() instanceof SoulissTag)
                values.put(SoulissDB.COLUMN_LAUNCHER_TAG_ID, ((SoulissTag) nodeIN.getLinkedObject()).getTagId());
            else
                throw new SoulissModelException("Missing ISoulissObject cast");
        }
        int upd = database.update(SoulissDB.TABLE_LAUNCHER, values, SoulissDB.COLUMN_LAUNCHER_ID + " = " + nodeIN.getId(),
                null);
        if (upd == 0) {
            long insertId = database.insert(SoulissDB.TABLE_LAUNCHER, null, values);
        }

        return upd;
    }

    private int deleteLauncher(LauncherElement toRename) {
        return database.delete(SoulissDB.TABLE_LAUNCHER, SoulissDB.COLUMN_LAUNCHER_ID + " = " + toRename.getId(), null);
    }

    public List<LauncherElement> getDBLauncherElements(Context context) {
        List<LauncherElement> comments = new ArrayList<>();
        if (!database.isOpen())
            open();
        Cursor cursor = database.query(SoulissDB.TABLE_LAUNCHER, SoulissDB.ALLCOLUMNS_LAUNCHER,
                null, null, null, null, SoulissDB.COLUMN_LAUNCHER_ORDER + ", " + SoulissDB.COLUMN_LAUNCHER_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LauncherElement dto = new LauncherElement();
            dto.setId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_ID)));
            dto.setTitle(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_TITLE)));
            dto.setDesc(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_DESC)));
            dto.setOrder(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_ORDER)));
            dto.setIsFullSpan(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_FULL_SPAN)) == 1);
            dto.setComponentEnum(LauncherElementEnum.values()[cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_TYPE))]);
            ISoulissObject isoulissObj = null;
            switch (dto.getComponentEnum()) {
                case STATIC_MANUAL:
                    dto.setDesc(countNodes() + " nodi presenti");
                    break;
                case NODE:
                    isoulissObj = getSoulissNode(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_NODE_ID)));
                    break;
                case TYPICAL:
                    isoulissObj = getTypical(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_NODE_ID)),
                            cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_SLOT_ID)));
                    break;
                case TAG:
                    try {
                        isoulissObj = getTag(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_TAG_ID)));
                    } catch (SQLDataException e) {
                        e.printStackTrace();
                    }
                    break;
                case SCENE:
                    isoulissObj = getScene(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_SCENE_ID)));
                    break;
                default:
                    break;
            }
            dto.setLinkedObject(isoulissObj);
            Log.i(Constants.TAG, "retrieving LAUNCHER item:" + dto.getTitle() + " ID:" + dto.getId() + " ORDER:" + dto.getOrder());

            comments.add(dto);
            cursor.moveToNext();
        }
        cursor.close();
        return comments;
    }

    public List<LauncherElement> getDefaultStaticDBLauncherElements(Context context) {
        List<LauncherElement> ret = new ArrayList<>();
        LauncherElement scenari = new LauncherElement(LauncherElementEnum.STATIC_SCENES);
        scenari.setTitle(context.getString(R.string.scenes_title));
        scenari.setDesc(countScenes() + " scenari configurati");
        scenari.setId(0);
        ret.add(scenari);

        LauncherElement man = new LauncherElement(LauncherElementEnum.STATIC_MANUAL);
        man.setTitle(context.getString(R.string.manual_title));
        man.setDesc(countNodes() + " nodi presenti");
        man.setId(1);
        ret.add(man);

        LauncherElement pro = new LauncherElement(LauncherElementEnum.STATIC_PROGRAMS);
        pro.setTitle(context.getString(R.string.programs_title));
        pro.setDesc(countTriggers() + " programmi attivi");
        pro.setId(2);
        ret.add(pro);

        LauncherElement prop = new LauncherElement(LauncherElementEnum.STATIC_STATUS);
        prop.setTitle(context.getString(R.string.status_souliss));
        prop.setIsFullSpan(true);
        prop.setId(3);
        ret.add(prop);


        LauncherElement prob = new LauncherElement(LauncherElementEnum.STATIC_TAGS);
        prob.setDesc(countTags() + " tags contenenti " + countTypicalTags() + " dispositivi");
        prob.setTitle(context.getString(R.string.tags));
        prob.setId(4);
        ret.add(prob);

        return ret;
    }

    public List<LauncherElement> getLauncherItems(Context context) {
        //Set chiavi= launcherElementList.keySet();

        //TODO sorting by order
        return launcherElementList;
    }

    /*public List<LauncherElement> loadLauncherItems(){
        String deMarshall = customCachedPrefs.getString("launcherItems","");

        JSONArray array = new JSONArray();
    }*/

    /*public List<Integer, LauncherElement> loadMap() {
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
        if (launcherElementList == null) {
            launcherElementList = new Gson().fromJson(contents, MAP_TYPE);
        }
        return launcherElementList;
    }*/

    public void refreshMapFromDB() {
        launcherElementList = getDBLauncherElements(context);
    }

    public void remove(LauncherElement launcherElement) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> visibili = preferences.getStringSet("launcher_elems", new HashSet<String>());
        launcherElementList.remove(launcherElement);
        deleteLauncher(launcherElement);
        visibili.remove("" + launcherElement.getId());
        preferences.edit().putStringSet("launcher_elems", visibili).apply();
    }

    public void addElement(LauncherElement lau) throws SoulissModelException {
        launcherElementList.add(lau);
        lau.setOrder((short) (launcherElementList.size()));
        createOrUpdateLauncherElement(lau);
        Set<String> visibili = preferences.getStringSet("launcher_elems", new HashSet<String>());
        visibili.add("" + lau.getId());

        preferences.edit().putStringSet("launcher_elems", visibili).apply();
    }

   /* public void synch() {
        short order = 0;
        for (LauncherElement lau : launcherElementList) {
            try {
                lau.setOrder(order++);
                createOrUpdateLauncherElement(lau);
            } catch (SoulissModelException e) {
                Log.e(Constants.TAG, "Errore synch laucher:" + e.getMessage());
            }
        }
    }*/


   /* public void saveMap(Map<Integer, LauncherElement> map) {

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

        launcherElementList = map;
    }*/


}
