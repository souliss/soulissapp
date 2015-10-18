package it.angelic.soulissclient.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;

/**
 * Classe che rappresenta il DB associato a Souliss client
 * <p/>
 * una tabella per i nodi e una per i tipici, collegate da foreign key. Tutte le
 * tabelle salvano l'ultima modifica come un long poi wrappato in Calendar
 * <p/>
 * tabella commands per esecuzione programmi
 * <p/>
 * tabella triggers fa riferimento alla tabella comandi, rappresenta l'input di
 * threshold
 *
 * @author Ale
 */
public class SoulissDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "souliss.db";
    public static final String TABLE_TYPICALS = "typicals";
    public static final String TABLE_NODES = "nodes";
    public static final String TABLE_TRIGGERS = "triggers";
    public static final String TABLE_LOGS = "logs";
    public static final String TABLE_COMMANDS = "commands";
    public static final String TABLE_SCENES = "scenes";
    public static final String TABLE_TAGS = "tags";
    public static final String TABLE_TAGS_TYPICALS = "tags_typicals";
    /*
     * NODES TABLE
     */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NODE_ID = "intnodeid";
    public static final String COLUMN_NODE_HEALTH = "inthlt";
    public static final String COLUMN_NODE_ICON = "intnodeico";
    public static final String COLUMN_NODE_NAME = "strnodename";
    public static final String COLUMN_NODE_LASTMOD = "cldnodemod";
    public static final String[] ALLCOLUMNS_NODES = {COLUMN_ID, COLUMN_NODE_ID, COLUMN_NODE_HEALTH, COLUMN_NODE_ICON,
            COLUMN_NODE_NAME, COLUMN_NODE_LASTMOD};
    /*
     * TYPICALS TABLE
     *
     * create table typicals( _id integer REFERENCES nodes (intnid), FOREIGN
     * KEY( intnid) per garantire esistenza nodo
     */
    public static final String COLUMN_TYPICAL_NODE_ID = "inttypnodeid";
    public static final String COLUMN_TYPICAL = "inttyp";
    public static final String COLUMN_TYPICAL_SLOT = "inttypslo";
    public static final String COLUMN_TYPICAL_VALUE = "inttypval";
    public static final String COLUMN_TYPICAL_WARNTIMER = "inttypwarn";
    public static final String COLUMN_TYPICAL_INPUT = "inttypcmd";
    public static final String COLUMN_TYPICAL_ICON = "inttypico";
    public static final String COLUMN_TYPICAL_NAME = "strtypname";
    public static final String COLUMN_TYPICAL_LASTMOD = "cldtypmod";
    public static final String COLUMN_TYPICAL_ISFAV = "flgtypisfav";
    public static final String[] ALLCOLUMNS_TYPICALS = {COLUMN_TYPICAL_NODE_ID, COLUMN_TYPICAL, COLUMN_TYPICAL_SLOT,
            COLUMN_TYPICAL_INPUT, COLUMN_TYPICAL_VALUE, COLUMN_TYPICAL_VALUE, COLUMN_TYPICAL_ICON, COLUMN_TYPICAL_ISFAV, COLUMN_TYPICAL_NAME,
            COLUMN_TYPICAL_LASTMOD, COLUMN_TYPICAL_WARNTIMER};
    /*
     * TABELLA COMANDI
     */
    public static final String COLUMN_COMMAND_ID = "cmdid";
    public static final String COLUMN_COMMAND_NODE_ID = "intcmdnodeid";
    public static final String COLUMN_COMMAND_SLOT = "intcmdslo";
    public static final String COLUMN_COMMAND_INPUT = "intcmdval";
    public static final String COLUMN_COMMAND_SCHEDTIME = "cldcmdsched";
    public static final String COLUMN_COMMAND_TYPE = "intcmdtype";
    public static final String COLUMN_COMMAND_EXECTIME = "cldcmdexec";
    public static final String COLUMN_COMMAND_SCHEDTIME_INTERVAL = "intcmdinterval";
    public static final String COLUMN_COMMAND_SCENEID = "intcmdsceneid";
    public static final String[] ALLCOLUMNS_COMMANDS = {COLUMN_COMMAND_ID, COLUMN_COMMAND_NODE_ID,
            COLUMN_COMMAND_SLOT, COLUMN_COMMAND_TYPE, COLUMN_COMMAND_INPUT, COLUMN_COMMAND_SCHEDTIME,
            COLUMN_COMMAND_EXECTIME, COLUMN_COMMAND_SCHEDTIME_INTERVAL, COLUMN_COMMAND_SCENEID};
    /*
     * TABELLA TRIGGERS
     */
    public static final String COLUMN_TRIGGER_ID = "trgid";
    public static final String COLUMN_TRIGGER_COMMAND_ID = "inttrgcmdid";
    public static final String COLUMN_TRIGGER_SLOT = "inttrgslo";
    public static final String COLUMN_TRIGGER_NODE_ID = "inttrgnodeid";
    public static final String COLUMN_TRIGGER_OP = "strtrgop";
    public static final String COLUMN_TRIGGER_THRESHVAL = "inttrgthreshold";
    public static final String COLUMN_TRIGGER_ACTIVE = "flgtrgactivated";
    public static final String[] ALLCOLUMNS_TRIGGERS = {COLUMN_TRIGGER_ID, COLUMN_TRIGGER_COMMAND_ID,
            COLUMN_TRIGGER_SLOT, COLUMN_TRIGGER_NODE_ID, COLUMN_TRIGGER_OP, COLUMN_TRIGGER_ACTIVE,
            COLUMN_TRIGGER_THRESHVAL};
    /* TABELLA LOGGING */
    public static final String COLUMN_LOG_ID = "logid";
    public static final String COLUMN_LOG_NODE_ID = "intlognodeid";
    public static final String COLUMN_LOG_SLOT = "intlogslo";
    public static final String COLUMN_LOG_VAL = "flologval";
    public static final String COLUMN_LOG_DATE = "cldlogwhen";
    public static final String[] ALLCOLUMNS_LOGS = {COLUMN_LOG_ID, COLUMN_LOG_NODE_ID, COLUMN_LOG_SLOT,
            COLUMN_LOG_VAL, COLUMN_LOG_DATE};
    /* TABELLA SCENES */
    public static final String COLUMN_SCENE_ID = "sceneid";
    public static final String COLUMN_SCENE_NAME = "strscenename";
    public static final String COLUMN_SCENE_ICON = "intsceneico";
    public static final String[] ALLCOLUMNS_SCENES = {COLUMN_SCENE_ID, COLUMN_SCENE_NAME, COLUMN_SCENE_ICON};
    /*TABELLA TAGS*/
    public static final String COLUMN_TAG_ID = "inttagid";
    public static final String COLUMN_TAG_NAME = "strtagname";
    public static final String COLUMN_TAG_ICONID = "inttagico";
    public static final String COLUMN_TAG_IMGPTH = "strtagpat";
    public static final String COLUMN_TAG_ORDER = "inttagord";
    //   + " FOREIGN KEY( "+ COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT + ") " + " REFERENCES " + TABLE_TYPICALS + " ("
    //   + COLUMN_TYPICAL_NODE_ID + "," + COLUMN_TYPICAL_SLOT + ") " + ");";
    public static final String[] ALLCOLUMNS_TAGS = {COLUMN_TAG_ID, COLUMN_TAG_NAME,
            COLUMN_TAG_ICONID, COLUMN_TAG_IMGPTH, COLUMN_TAG_ORDER};
    /*
     * TABELLA TAGS'TYP
     * tabella di relazione n a m per TAG <-> typical
     */
    public static final String COLUMN_TAG_TYP_SLOT = "inttagtypslo";
    public static final String COLUMN_TAG_TYP_NODE_ID = "inttagtypnodeid";
    public static final String COLUMN_TAG_TYP_TAG_ID = "inttagtagid";
    public static final String COLUMN_TAG_TYP_PRIORITY = "inttagtyppriority";
    public static final String[] ALLCOLUMNS_TAGS_TYPICAL = {COLUMN_TAG_TYP_SLOT,
            COLUMN_TAG_TYP_NODE_ID, COLUMN_TAG_TYP_TAG_ID, COLUMN_TAG_TYP_PRIORITY};
    // Database creation sql statement
    private static final String DATABASE_CREATE_NODES = "create table " + TABLE_NODES
            + "( "
            +
            // COLUMN DEF
            COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NODE_ID + " integer UNIQUE, "
            + COLUMN_NODE_HEALTH + " integer, " + COLUMN_NODE_ICON + " integer, " + COLUMN_NODE_NAME + " textslot, "
            + COLUMN_NODE_LASTMOD + " integer not null" + ");";
    private static final String DATABASE_CREATE_TYPICALS = "create table "
            + TABLE_TYPICALS
            + "( "
            + COLUMN_TYPICAL_NODE_ID + " integer not null, "
            + COLUMN_TYPICAL + " integer not null, "
            + COLUMN_TYPICAL_SLOT + " integer not null, "
            + COLUMN_TYPICAL_INPUT + " integer, "
            + COLUMN_TYPICAL_VALUE + " integer not null, "
            + COLUMN_TYPICAL_ICON + " integer, "
            + COLUMN_TYPICAL_ISFAV + " integer, "
            + COLUMN_TYPICAL_NAME + " textslot, "
            + COLUMN_TYPICAL_LASTMOD + " integer not null,"
            + COLUMN_TYPICAL_WARNTIMER + " integer, "
            + " FOREIGN KEY( " + COLUMN_TYPICAL_NODE_ID
            + ") REFERENCES " + TABLE_NODES + " (" + COLUMN_TYPICAL_NODE_ID + "), "
            + "CONSTRAINT typ_keys PRIMARY KEY(" + COLUMN_TYPICAL_NODE_ID + "," + COLUMN_TYPICAL_SLOT + ")" + ");";
    private static final String DATABASE_CREATE_COMMANDS = "create table "
            + TABLE_COMMANDS
            + "( "
            +
            COLUMN_COMMAND_ID + " integer primary key autoincrement, "
            + COLUMN_COMMAND_NODE_ID + " integer not null, "
            +
            // TIPICO in caso di comando massivo
            COLUMN_COMMAND_SLOT + " integer not null, "
            + COLUMN_COMMAND_TYPE + " integer not null, "
            + COLUMN_COMMAND_INPUT + " integer not null, "
            + COLUMN_COMMAND_SCHEDTIME + " integer, "
            + COLUMN_COMMAND_EXECTIME + " integer, "
            // Se il comando appartiene a scenario, rappresenta l'ordine di
            // esecuzione
            + COLUMN_COMMAND_SCHEDTIME_INTERVAL + " integer, "
            + COLUMN_COMMAND_SCENEID + " integer,"
            + " FOREIGN KEY( " + COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT + ") " + " REFERENCES " + TABLE_TYPICALS + " ("
            + COLUMN_TYPICAL_NODE_ID + "," + COLUMN_TYPICAL_SLOT + ") " + ");";
    private static final String DATABASE_CREATE_TRIGGERS = "create table "
            + TABLE_TRIGGERS
            + "( "
            + COLUMN_TRIGGER_ID + " integer primary key autoincrement, "
            + COLUMN_TRIGGER_COMMAND_ID + " integer not null, "
            + COLUMN_TRIGGER_SLOT + " integer not null, "
            + COLUMN_TRIGGER_NODE_ID + " integer not null, "
            + COLUMN_TRIGGER_OP + " integer not null, "
            + COLUMN_TRIGGER_ACTIVE + " integer not null, "
            + COLUMN_TRIGGER_THRESHVAL + " REAL not null, "
            + " FOREIGN KEY( " + COLUMN_TRIGGER_COMMAND_ID
            + " ) REFERENCES " + TABLE_COMMANDS + " ("
            + COLUMN_COMMAND_ID + ") " + ");";
    private static final String DATABASE_CREATE_LOGS = "create table " + TABLE_LOGS
            + "( "
            // COLUMN DEF
            + COLUMN_LOG_ID + " integer primary key autoincrement, "
            + COLUMN_LOG_NODE_ID + " integer not null, "
            + // command to trig
            COLUMN_LOG_SLOT + " integer not null, "
            + // input slot
            COLUMN_LOG_VAL + " float not null, "
            + COLUMN_LOG_DATE + " integer not null, "
            + " FOREIGN KEY( " + COLUMN_LOG_NODE_ID + "," + COLUMN_LOG_SLOT + ") "
            + " REFERENCES " + TABLE_TYPICALS + " (" + COLUMN_TYPICAL_NODE_ID + "," + COLUMN_TYPICAL_SLOT + ") " + ");";
    private static final String DATABASE_CREATE_SCENES = "create table " + TABLE_SCENES + "( "
            +
            // COLUMN DEF
            COLUMN_SCENE_ID + " integer primary key autoincrement, " + COLUMN_SCENE_ICON + " integer, "
            + COLUMN_SCENE_NAME + " textname " + // command to trig

            ");";
    private static final String DATABASE_CREATE_TAGS = "create table "
            + TABLE_TAGS
            + "( "
            + COLUMN_TAG_ID + " integer primary key autoincrement, "
            + COLUMN_TAG_NAME + " textslot, "
            + COLUMN_TAG_ICONID + " integer not null, "
            + COLUMN_TAG_IMGPTH + " textslot, "
            + COLUMN_TAG_ORDER + " integer "
            + ");";
    private static final String DATABASE_CREATE_TAG_TYPICAL = "create table "
            + TABLE_TAGS_TYPICALS
            + "( "
            + COLUMN_TAG_TYP_SLOT + " integer not null, "
            + COLUMN_TAG_TYP_NODE_ID + " integer not null, "
            + COLUMN_TAG_TYP_TAG_ID + " integer not null, "
            + COLUMN_TAG_TYP_PRIORITY + " integer not null DEFAULT 0 , "
            + " PRIMARY KEY (" + COLUMN_TAG_TYP_NODE_ID + "," + COLUMN_TAG_TYP_SLOT + "," + COLUMN_TAG_TYP_TAG_ID + ") "
            + " FOREIGN KEY ( " + COLUMN_TAG_TYP_TAG_ID + ") "
            + " REFERENCES " + TABLE_TAGS + " (" + COLUMN_TYPICAL_NODE_ID + "), "
            + " FOREIGN KEY ( " + COLUMN_TAG_TYP_NODE_ID + "," + COLUMN_TAG_TYP_SLOT + ") "
            + " REFERENCES " + TABLE_TYPICALS + " (" + COLUMN_TYPICAL_NODE_ID + "," + COLUMN_TYPICAL_SLOT + ") "
            + ");";
    private static final int DATABASE_VERSION = 32;
    public static long FAVOURITES_TAG_ID = 0;
    private Context context;

//AUTOF

    /**
     * super wrapper createDB
     *
     * @param context
     */
    public SoulissDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    private void dropCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIGGERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMANDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCENES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS_TYPICALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TYPICALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_NODES);
        database.execSQL(DATABASE_CREATE_TYPICALS);
        database.execSQL(DATABASE_CREATE_COMMANDS);
        database.execSQL(DATABASE_CREATE_TRIGGERS);
        database.execSQL(DATABASE_CREATE_LOGS);
        database.execSQL(DATABASE_CREATE_SCENES);
        database.execSQL(DATABASE_CREATE_TAGS);
        database.execSQL(DATABASE_CREATE_TAG_TYPICAL);
        /* DEFAULT TAG , Order=0 */
        database.execSQL("INSERT INTO " + TABLE_TAGS + " (" + COLUMN_TAG_ID + "," + COLUMN_TAG_NAME + ","+ COLUMN_TAG_ORDER + "," + COLUMN_TAG_ICONID
                + ") VALUES (" + FAVOURITES_TAG_ID + ",'" + context.getResources().getString(R.string.favourites) + "'," + 0 + ","
                + R.drawable.favorites2 + ")");
        /* DEFAULT SCENES */
        database.execSQL("INSERT INTO " + TABLE_SCENES + " (" + COLUMN_SCENE_NAME + "," + COLUMN_SCENE_ICON
                + ") VALUES ('" + context.getResources().getString(R.string.scene_turnoff_lights) + "',"
                + R.drawable.light_off + ")");
        database.execSQL("INSERT INTO " + TABLE_SCENES + " (" + COLUMN_SCENE_NAME + "," + COLUMN_SCENE_ICON
                + ") VALUES ('" + context.getResources().getString(R.string.scene_turnon_lights) + "',"
                + R.drawable.light_on + ")");
        // Comandi massivi di default OFF, NODEID = -1
        database.execSQL("INSERT INTO " + TABLE_COMMANDS + " (" + COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT
                + "," + COLUMN_COMMAND_INPUT + "," + COLUMN_COMMAND_SCENEID + "," + COLUMN_COMMAND_TYPE + ","
                + COLUMN_COMMAND_SCHEDTIME_INTERVAL + ")"
                + " VALUES " + "(" + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ", " + Constants.Typicals.Souliss_T11 + "," + Constants.Typicals.Souliss_T1n_OffCmd
                + ",1," + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ",200)");
        database.execSQL("INSERT INTO " + TABLE_COMMANDS + " (" + COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT
                + "," + COLUMN_COMMAND_INPUT + "," + COLUMN_COMMAND_SCENEID + "," + COLUMN_COMMAND_TYPE + ","
                + COLUMN_COMMAND_SCHEDTIME_INTERVAL + ")"
                + " VALUES " + "(" + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ", " + Constants.Typicals.Souliss_T12 + "," + Constants.Typicals.Souliss_T1n_OffCmd
                + ",1," + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ",400)");
        database.execSQL("INSERT INTO " + TABLE_COMMANDS + " (" + COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT//RGB
                + "," + COLUMN_COMMAND_INPUT + "," + COLUMN_COMMAND_SCENEID + "," + COLUMN_COMMAND_TYPE + ","
                + COLUMN_COMMAND_SCHEDTIME_INTERVAL + ")"
                + " VALUES " + "(" + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ", " + Constants.Typicals.Souliss_T16 + "," + Constants.Typicals.Souliss_T1n_OffCmd
                + ",1," + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ",600)");
        // Comandi massivi di default ON
        database.execSQL("INSERT INTO " + TABLE_COMMANDS + " (" + COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT
                + "," + COLUMN_COMMAND_INPUT + "," + COLUMN_COMMAND_SCENEID + "," + COLUMN_COMMAND_TYPE + ","
                + COLUMN_COMMAND_SCHEDTIME_INTERVAL + ")"
                + " VALUES " + "(" + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ", " + Constants.Typicals.Souliss_T11 + "," + Constants.Typicals.Souliss_T1n_OnCmd
                + ",2," + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ",200)");
        database.execSQL("INSERT INTO " + TABLE_COMMANDS + " (" + COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT
                + "," + COLUMN_COMMAND_INPUT + "," + COLUMN_COMMAND_SCENEID + "," + COLUMN_COMMAND_TYPE + ","
                + COLUMN_COMMAND_SCHEDTIME_INTERVAL + ")"
                + " VALUES " + "(" + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ", " + Constants.Typicals.Souliss_T12 + "," + Constants.Typicals.Souliss_T1n_OnCmd
                + ",2," + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ",400)");
        database.execSQL("INSERT INTO " + TABLE_COMMANDS + " (" + COLUMN_COMMAND_NODE_ID + "," + COLUMN_COMMAND_SLOT
                + "," + COLUMN_COMMAND_INPUT + "," + COLUMN_COMMAND_SCENEID + "," + COLUMN_COMMAND_TYPE + ","
                + COLUMN_COMMAND_SCHEDTIME_INTERVAL + ")"
                + " VALUES " + "(" + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ", " + Constants.Typicals.Souliss_T16 + "," + Constants.Typicals.Souliss_T1n_OnCmd
                + ",2," + it.angelic.soulissclient.Constants.MASSIVE_NODE_ID + ",600)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SoulissDB.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        boolean dropNeeded = true;
        if (oldVersion <= 30 && newVersion == DATABASE_VERSION) {
            //added warn TIMER
            try {
                String upgradeQuery = "ALTER TABLE " + TABLE_TYPICALS + " ADD COLUMN " + COLUMN_TYPICAL_WARNTIMER + " INTEGER";
                db.execSQL(upgradeQuery);
                dropNeeded = false;
            } catch (Exception cazzo) {
                //somehow already existing, just log
                Log.e(SoulissDB.class.getName(), "Upgrading database ERROR:" + cazzo.getMessage());
            }
        }
        if (oldVersion <= 31 && newVersion == DATABASE_VERSION) {
            //added tag Order
            try {
                String upgradeQuery = "ALTER TABLE " + TABLE_TAGS + " ADD COLUMN " + COLUMN_TAG_ORDER + " INTEGER";
                db.execSQL(upgradeQuery);
                dropNeeded = false;
            } catch (Exception cazzo) {
                //somehow already existing, just log
                Log.e(SoulissDB.class.getName(), "Upgrading database ERROR:" + cazzo.getMessage());
            }
        }


        /*
        CREATE TABLE typBck AS select * from typicals;

DROP TABLE typicals;

CREATE TABLE typicals(inttypid integer PRIMARY KEY, inttypnodeid integer not null, inttyp integer not null, inttypslo integer not null, inttypcmd integer, inttypval integer not null, inttypico integer, flgtypisfav integer, strtypname textslot, cldtypmod integer not null,inttypwarn integer,  FOREIGN KEY( inttypnodeid) REFERENCES nodes (inttypnodeid), UNIQUE(inttypnodeid,inttypslo) )

INSERT INTO typicals (inttypnodeid, inttyp, inttypslo, inttypcmd, inttypval, inttypico, flgtypisfav, strtypname, cldtypmod, inttypwarn)
SELECT inttypnodeid, inttyp, inttypslo, inttypcmd, inttypval, inttypico, flgtypisfav, strtypname, cldtypmod, inttypwarn
FROM typBck
         */

        if (dropNeeded) {
            Log.e(SoulissDB.class.getName(), "Upgrading database went wrong, DROPPI&RE-CREATE");
            dropCreate(db);
        }


    }


}