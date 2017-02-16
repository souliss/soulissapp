package it.angelic.soulissclient.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public SoulissDBLauncherHelper(Context context) {
        super(context);
        this.context = context;
        open();
        // Database fields
        launcherElementList = getDBLauncherElements(context);

        ///init sse vuoto
        if (launcherElementList.isEmpty()) {
            List<LauncherElement> launcherElementtemp = getDefaultStaticDBLauncherElements();

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

        recomputeOrder();

    }


    public LauncherElement addElement(LauncherElement lau) throws SoulissModelException {
        launcherElementList.add(lau);
        recomputeOrder();

        long id = createLauncherElement(lau);
        lau.setId(id);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Log.i(Constants.TAG, "tile creato: " + lau.getId() + " = " + lau.getTitle());
        return lau;
    }

    /**
     * campi singoli altrimenti side effects
     *
     * @param nodeIN
     * @return
     */
    private long createLauncherElement(LauncherElement nodeIN) throws SoulissModelException {
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
                values.put(SoulissDB.COLUMN_LAUNCHER_SCENE_ID, ((SoulissScene) nodeIN.getLinkedObject()).getSceneId());
            else if (nodeIN.getLinkedObject() instanceof SoulissTag)
                values.put(SoulissDB.COLUMN_LAUNCHER_TAG_ID, ((SoulissTag) nodeIN.getLinkedObject()).getTagId());
            else
                throw new SoulissModelException("Missing ISoulissObject cast");
        }
        // int upd = database.update(SoulissDB.TABLE_LAUNCHER, values, SoulissDB.COLUMN_LAUNCHER_ID + " = " + nodeIN.getSceneId(),
        //         null);
        // if (upd == 0) {
        long insertId = database.insert(SoulissDB.TABLE_LAUNCHER, null, values);
        //  }

        return insertId;
    }

    private int deleteLauncher(LauncherElement toRename) {
        return database.delete(SoulissDB.TABLE_LAUNCHER, SoulissDB.COLUMN_LAUNCHER_ID + " = " + toRename.getId(), null);
    }

    private List<LauncherElement> getDBLauncherElements(Context context) {
        List<LauncherElement> comments = new ArrayList<>();
        if (!database.isOpen())
            open();
        Cursor cursor = database.query(SoulissDB.TABLE_LAUNCHER, SoulissDB.ALLCOLUMNS_LAUNCHER,
                null, null, null, null, SoulissDB.COLUMN_LAUNCHER_ORDER + ", " + SoulissDB.COLUMN_LAUNCHER_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LauncherElement dto = new LauncherElement();
            dto.setId(cursor.getLong(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_ID)));
            dto.setTitle(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_TITLE)));
            dto.setDesc(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_DESC)));
            dto.setOrder(cursor.getShort(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_ORDER)));
            dto.setIsFullSpan(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_FULL_SPAN)) == 1);
            dto.setComponentEnum(LauncherElementEnum.values()[cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_LAUNCHER_TYPE))]);
            ISoulissObject isoulissObj = null;
            switch (dto.getComponentEnum()) {
                case STATIC_LOCATION:
                    //dto.setDesc(context.getString(R.string.opt_homepos_err));
                    break;
                case STATIC_MANUAL:
                    int cntTyp = countTypicals();
                    int cntN = countNodes();
                    dto.setDesc(context.getResources().getQuantityString(R.plurals.Nodes,
                            cntN, cntN)
                            + "\n"
                            + context.getResources().getQuantityString(R.plurals.Devices,
                            cntTyp, cntTyp));
                    break;
                case STATIC_TAGS:
                    dto.setDesc(context.getResources().getQuantityString(R.plurals.tags_plur,
                            countTags(), countTags()));
                    break;
                case STATIC_SCENES:
                    dto.setDesc(context.getResources().getQuantityString(R.plurals.scenes_plur,
                            countScenes(), countScenes()) + " " + context.getString(R.string.string_configured));
                    break;
                case STATIC_PROGRAMS:
                    dto.setDesc(context.getResources().getQuantityString(R.plurals.programs_plur,
                            countTriggers(), countTriggers()));//FIXME count progs
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
            Log.d(Constants.TAG, "retrieving LAUNCHER item:" + dto.getTitle() + " ID:" + dto.getId() + " ORDER:" + dto.getOrder());

            comments.add(dto);
            cursor.moveToNext();
        }
        cursor.close();
        return comments;
    }

    public List<LauncherElement> getDefaultStaticDBLauncherElements() {
        List<LauncherElement> ret = new ArrayList<>();
        LauncherElement scenari = new LauncherElement(LauncherElementEnum.STATIC_SCENES);

        scenari.setTitle(context.getString(R.string.scenes_title));
        scenari.setOrder((short) 0);
        scenari.setDesc(context.getResources().getQuantityString(R.plurals.scenes_plur,
                countScenes(), countScenes()) + " " + context.getString(R.string.string_configured));
        ret.add(scenari);

        LauncherElement prob = new LauncherElement(LauncherElementEnum.STATIC_TAGS);
        prob.setDesc(context.getResources().getQuantityString(R.plurals.tags_plur,
                countTags(), countTags()));
        prob.setTitle(context.getString(R.string.tags));
        prob.setOrder((short) 1);
        ret.add(prob);

        LauncherElement prop = new LauncherElement(LauncherElementEnum.STATIC_STATUS);
        prop.setTitle(context.getString(R.string.status_souliss));
        prop.setIsFullSpan(true);
        prop.setOrder((short) 2);
        ret.add(prop);

        LauncherElement man = new LauncherElement(LauncherElementEnum.STATIC_MANUAL);
        man.setTitle(context.getString(R.string.manual_typicals));
        man.setOrder((short) 3);
        man.setDesc(countNodes() + " nodi presenti");
        ret.add(man);

        LauncherElement pro = new LauncherElement(LauncherElementEnum.STATIC_PROGRAMS);
        pro.setTitle(context.getString(R.string.programs_title));
        pro.setOrder((short) 4);
        pro.setDesc(context.getResources().getQuantityString(R.plurals.programs_plur,
                countTriggers(), countTriggers()));//FIXME count progs
        ret.add(pro);

        LauncherElement loc = new LauncherElement(LauncherElementEnum.STATIC_LOCATION);
        loc.setTitle(context.getString(R.string.position));
        loc.setOrder((short) 5);
        loc.setDesc("");//dopo
        ret.add(loc);

        return ret;
    }

    public List<LauncherElement> getLauncherItems(Context context) {
        Collections.sort(launcherElementList, new Comparator<LauncherElement>() {
            @Override
            public int compare(final LauncherElement lhs, LauncherElement rhs) {
                return lhs.getOrder() - rhs.getOrder();
            }
        });
        // sorted by order from DB
        return launcherElementList;
    }

    private void recomputeOrder() {
        for (int t = 0; t < launcherElementList.size(); t++) {
            launcherElementList.get(t).setOrder((short) t);
        }
    }


    public void refreshMapFromDB() {
        Log.d(Constants.TAG, "refresh launcher from DB");
        launcherElementList = getDBLauncherElements(context);
        recomputeOrder();
    }

    public void remove(LauncherElement launcherElement) {
        launcherElementList.remove(launcherElement);
        deleteLauncher(launcherElement);
        //non serve togliero, la lista e` in sottrazione
        //visibili.remove("" + launcherElement.getId());
        //preferences.edit().putStringSet("launcher_elems", visibili).apply();
        recomputeOrder();
    }

    public void updateLauncherElement(LauncherElement nodeIN) {
        try {
            if (launcherElementList.contains(nodeIN)) {

                updateLauncherElementImpl(nodeIN);
                launcherElementList.set(launcherElementList.indexOf(nodeIN), nodeIN);

                recomputeOrder();
            } else {
                throw new SoulissModelException("Nodo non trovato: " + nodeIN.getTitle());
            }
        } catch (SoulissModelException e) {
            Log.e(Constants.TAG, e.getMessage());
        }
    }

    /**
     * campi singoli altrimenti side effects
     *
     * @param nodeIN
     * @return
     */
    private long updateLauncherElementImpl(LauncherElement nodeIN) throws SoulissModelException {
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
                values.put(SoulissDB.COLUMN_LAUNCHER_SCENE_ID, ((SoulissScene) nodeIN.getLinkedObject()).getSceneId());
            else if (nodeIN.getLinkedObject() instanceof SoulissTag)
                values.put(SoulissDB.COLUMN_LAUNCHER_TAG_ID, ((SoulissTag) nodeIN.getLinkedObject()).getTagId());
            else
                throw new SoulissModelException("Missing ISoulissObject cast");
        }
        int upd = database.update(SoulissDB.TABLE_LAUNCHER, values, SoulissDB.COLUMN_LAUNCHER_ID + " = " + nodeIN.getId(),
                null);
        if (upd == 0) {
            Log.e(Constants.TAG, "updateLauncherElement() NO ROW UPDATED!!");
        }

        return upd;
    }


}
