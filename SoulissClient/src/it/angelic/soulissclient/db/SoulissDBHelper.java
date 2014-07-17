package it.angelic.soulissclient.db;

import static it.angelic.soulissclient.Constants.TAG;
import static junit.framework.Assert.assertEquals;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTrigger;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.model.typicals.SoulissTypical42AntiTheftPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical43AntiTheftLocalPeer;
import it.angelic.soulissclient.model.typicals.SoulissTypical51AnalogueSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical52TemperatureSensor;
import it.angelic.soulissclient.model.typicals.SoulissTypical53HumiditySensor;
import it.angelic.soulissclient.model.typicals.SoulissTypicalTemperatureSensor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 * 
 * @author Ale
 * 
 */
public class SoulissDBHelper {

	// Database fields
	protected static SQLiteDatabase database;
	protected SoulissDB soulissDatabase;
	protected SoulissPreferenceHelper opts;

	public static synchronized SQLiteDatabase getDatabase() {
		return database;
	}

	public SoulissDBHelper(Context context) {
		soulissDatabase = new SoulissDB(context);
		opts = SoulissClient.getOpzioni();
	}

	public synchronized void open() throws SQLException {
		if (database == null || !database.isOpen())
			database = soulissDatabase.getWritableDatabase();
	}

	public void close() {
		soulissDatabase.close();
		if (database != null && database.isOpen())
			database.close();
		else
			Log.w(TAG, "DB already closed");
	}

	/**
	 * presuppone che il nodo esista, asserError altrimenti Light update, solo
	 * data ed health, pensato per JSON
	 * 
	 * @param nodeIN
	 * @return
	 */
	public int refreshNode(SoulissNode nodeIN) {
		ContentValues values = new ContentValues();
		// wrap values from object
		values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
		values.put(SoulissDB.COLUMN_NODE_HEALTH, nodeIN.getHealth());
		long upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + nodeIN.getId(),
				null);

		assertEquals(upd, 1);
		return (int) upd;
	}

	public int refreshAllTypicalsTime() {
		ContentValues values = new ContentValues();
		// wrap values from object
		values.put(SoulissDB.COLUMN_TYPICAL_LASTMOD, Calendar.getInstance().getTime().getTime());
		return database.update(SoulissDB.TABLE_TYPICALS, values, null, null);
	}

	/**
	 * presuppone che il nodo esista, asserError altrimenti Light update, solo
	 * data ed health, pensato per JSON. Skippa refresh se tipico vuoto
	 * 
	 * @param nodeIN
	 * @return
	 */
	public int refreshNodeAndTypicals(SoulissNode nodeIN) {
		ContentValues values = new ContentValues();
		// wrap values from object
		values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
		values.put(SoulissDB.COLUMN_NODE_HEALTH, nodeIN.getHealth());
		long upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + nodeIN.getId(),
				null);

		assertEquals(upd, 1);

		List<SoulissTypical> tips = nodeIN.getTypicals();
		for (SoulissTypical x : tips) {
			if (!x.isEmpty())
				x.getTypicalDTO().refresh();
		}

		return (int) upd;
	}

	/**
	 * campi singoli altrimenti side effects
	 * 
	 * @param nodeIN
	 * @return
	 */
	public int createOrUpdateNode(SoulissNode nodeIN) {
		ContentValues values = new ContentValues();
		// wrap values from object
		values.put(SoulissDB.COLUMN_NODE_NAME, nodeIN.getName());
		values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
		values.put(SoulissDB.COLUMN_NODE_ID, nodeIN.getId());
		values.put(SoulissDB.COLUMN_NODE_HEALTH, nodeIN.getHealth());
		values.put(SoulissDB.COLUMN_NODE_ICON, nodeIN.getDefaultIconResourceId());
		int upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + nodeIN.getId(),
				null);
		if (upd == 0) {
			long insertId = database.insert(SoulissDB.TABLE_NODES, null, values);
		}

		return upd;
	}

	/**
	 * Crea un nuovo scenario vuoto
	 * 
	 * @param nodeIN
	 * @return
	 */
	public int createOrUpdateScene(SoulissScene nodeIN) {
		ContentValues values = new ContentValues();

		if (nodeIN != null) {
			// wrap values from object
			values.put(SoulissDB.COLUMN_SCENE_ID, nodeIN.getId());
			values.put(SoulissDB.COLUMN_SCENE_ICON, nodeIN.getDefaultIconResourceId());
			values.put(SoulissDB.COLUMN_SCENE_NAME, nodeIN.toString());
			return database.update(SoulissDB.TABLE_SCENES, values, SoulissDB.COLUMN_SCENE_ID + " = " + nodeIN.getId(),
					null);
		} else {
			values.put(SoulissDB.COLUMN_SCENE_ICON, R.drawable.lamp);
			// Inserisco e risetto il nome
			int ret = (int) database.insert(SoulissDB.TABLE_SCENES, null, values);
			values.put(SoulissDB.COLUMN_SCENE_NAME,
					SoulissClient.getAppContext().getResources().getString(R.string.scene) + " " + ret);
			database.update(SoulissDB.TABLE_SCENES, values, SoulissDB.COLUMN_SCENE_ID + " = " + ret, null);
			return ret;
		}

	}

	/**
	 * Decide come interpretare gli out e logga
	 * @param soulissTypical
	 */
	public void logTypical(SoulissTypical soulissTypical) {
		ContentValues values = new ContentValues();
		// wrap values from object
		values.put(SoulissDB.COLUMN_LOG_NODE_ID, soulissTypical.getTypicalDTO().getNodeId());
		values.put(SoulissDB.COLUMN_LOG_DATE, Calendar.getInstance().getTime().getTime());
		values.put(SoulissDB.COLUMN_LOG_SLOT, soulissTypical.getTypicalDTO().getSlot());
		values.put(SoulissDB.COLUMN_LOG_TYPICAL, soulissTypical.getTypicalDTO().getTypical());
		if (soulissTypical instanceof SoulissTypicalTemperatureSensor) {
			values.put(SoulissDB.COLUMN_LOG_VAL, ((SoulissTypicalTemperatureSensor) soulissTypical).getOutputCelsius());
		} else if (soulissTypical instanceof SoulissTypical51AnalogueSensor) {
			values.put(SoulissDB.COLUMN_LOG_VAL, ((SoulissTypical51AnalogueSensor) soulissTypical).getOutputFloat());
		} else if (soulissTypical instanceof SoulissTypical52TemperatureSensor) {
			values.put(SoulissDB.COLUMN_LOG_VAL, ((SoulissTypical52TemperatureSensor) soulissTypical).getOutputFloat());
		} else if (soulissTypical instanceof SoulissTypical53HumiditySensor) {
			values.put(SoulissDB.COLUMN_LOG_VAL, ((SoulissTypical53HumiditySensor) soulissTypical).getOutputFloat());
		} else {
			values.put(SoulissDB.COLUMN_LOG_VAL, soulissTypical.getTypicalDTO().getOutput());
		}
		try {
			database.insert(SoulissDB.TABLE_LOGS, null, values);
		} catch (SQLiteConstraintException e) {
			// sensori NaN violano il constraint
			Log.e(Constants.TAG, "error saving log:"+e);
		}
		
	}

	public SoulissNode getSoulissNode(int nodeIN) {
		Cursor cursor = database.query(SoulissDB.TABLE_NODES, SoulissDB.ALLCOLUMNS_NODES, SoulissDB.COLUMN_NODE_ID
				+ " = " + nodeIN, null, null, null, null);
		cursor.moveToFirst();
		SoulissNode ret = SoulissNode.cursorToNode(cursor);

		List<SoulissTypical> cod = getNodeTypicals(ret);
		// come sono grezzo, 'sta riga fa schifo
		for (SoulissTypical soulissTypical : cod) {
			// if (soulissTypical.getNodeId() == comment.getId())
			ret.add(soulissTypical);
		}
		cursor.close();
		return ret;
	}

	/**
	 * 
	 * @param parent
	 *            fake node, id -1
	 * @return
	 */
	public List<SoulissTypical> getUniqueTypicals(SoulissNode parent) {
		ArrayList<SoulissTypical> comments = new ArrayList<SoulissTypical>();
		HashSet<Short> pool = new HashSet<Short>();
		Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS, null, null, null, null,
				null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
			SoulissTypical newTyp = SoulissTypical.typicalFactory(dto.getTypical(), parent, dto, opts);
			newTyp.setParentNode(parent);
			// if (newTyp.getTypical() !=
			// Constants.Souliss_T_CurrentSensor_slave)
			if (!pool.contains(dto.getTypical())) {
				comments.add(newTyp);
				pool.add(dto.getTypical());
			}
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return comments;
	}

	public List<SoulissTypical> getNodeTypicals(SoulissNode parent) {

		List<SoulissTypical> comments = new ArrayList<SoulissTypical>();
		Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
				SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + parent.getId(), null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
			SoulissTypical newTyp = SoulissTypical.typicalFactory(dto.getTypical(), parent, dto, opts);

			newTyp.setParentNode(parent);
			// if (newTyp.getTypical() !=
			// Constants.Souliss_T_CurrentSensor_slave)
			comments.add(newTyp);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return comments;
	}

	public ArrayList<SoulissLogDTO> getTypicalLogs(SoulissTypical tgt) {
		ArrayList<SoulissLogDTO> comments = new ArrayList<SoulissLogDTO>();
		Cursor cursor = database.query(SoulissDB.TABLE_LOGS, SoulissDB.ALLCOLUMNS_LOGS, SoulissDB.COLUMN_LOG_NODE_ID
				+ " = " + tgt.getTypicalDTO().getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT + " = "
				+ tgt.getTypicalDTO().getSlot(), null, null, null, SoulissDB.COLUMN_LOG_DATE + " ASC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SoulissLogDTO dto = new SoulissLogDTO(cursor);
			comments.add(dto);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return comments;
	}

	/**
	 * torna la storia di uno slot, raggruppata per giorno
	 * 
	 * @param tgt
	 * @return
	 */
	public HashMap<Date, SoulissHistoryGraphData> getHistoryTypicalLogs(SoulissTypical tgt, int range) {
		HashMap<Date, SoulissHistoryGraphData> comments = new HashMap<Date, SoulissHistoryGraphData>();

		Date dff;
		String limitCause = "";
		Calendar now = Calendar.getInstance();
		switch (range) {

		case 0:
			// tutti i dati
			break;
		case 2:
			now.add(Calendar.DATE, -7);
			limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
			break;
		case 1:
			now.add(Calendar.MONTH, -1);
			limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
			break;
		default:
			Log.e("DB", "Unexpected switch ERROR");
			break;
		}
		Cursor cursor = database.query(SoulissDB.TABLE_LOGS, new String[] {
				"strftime('%Y-%m-%d', datetime((cldlogwhen/1000), 'unixepoch', 'localtime')) AS IDX",
				"AVG(CAST(flologval AS FLOAT)) AS AVG", "MIN(CAST(flologval AS FLOAT)) AS MIN",
				"MAX(CAST(flologval AS FLOAT)) AS MAX" }, SoulissDB.COLUMN_LOG_NODE_ID + " = "
				+ tgt.getTypicalDTO().getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT + " = "
				+ tgt.getTypicalDTO().getSlot() + limitCause + " ", null,
				"strftime('%Y-%m-%d', datetime((cldlogwhen/1000), 'unixepoch', 'localtime'))", null, "IDX ASC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			try {
				dff = Constants.yearFormat.parse(cursor.getString(0));
				SoulissHistoryGraphData dto = new SoulissHistoryGraphData(cursor, dff);
				comments.put(dto.key, dto);
			} catch (ParseException e) {
				Log.e(TAG, "getHistoryTypicalLogs", e);
			}

			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return comments;
	}

	/**
	 * Raggruppa i risultati di una query sui logs
	 * 
	 * @param tgt
	 *            tipico da interrogare
	 * @param groupBy
	 *            mese o giorno
	 * @param range
	 *            dei log da selezionare
	 * @return
	 */
	public SparseArray<SoulissGraphData> getGroupedTypicalLogs(SoulissTypical tgt, String groupBy, int range) {
		SparseArray<SoulissGraphData> comments = new SparseArray<SoulissGraphData>();
		String limitCause = "";
		Calendar now = Calendar.getInstance();
		switch (range) {

		case 0:
			// tutti i dati
			break;
		case 2:
			now.add(Calendar.DATE, -7);
			limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
			break;
		case 1:
			now.add(Calendar.MONTH, -1);
			limitCause = " AND " + SoulissDB.COLUMN_LOG_DATE + "  > " + now.getTime().getTime();
			break;
		default:
			Log.e("DB", "Unexpected switch ERROR");
			break;
		}
		int tot;
		if (groupBy.compareTo("%m") == 0)
			tot = 12;
		else
			tot = 24;
		for (int i = 0; i < tot; i++) {
			comments.put(i, new SoulissGraphData());
		}

		Cursor cursor = database.query(SoulissDB.TABLE_LOGS, new String[] {
				"strftime('" + groupBy + "', datetime((cldlogwhen/1000), 'unixepoch', 'localtime')) AS IDX",
				"AVG(CAST(flologval AS FLOAT)) AS AVG", "MIN(CAST(flologval AS FLOAT)) AS MIN",
				"MAX(CAST(flologval AS FLOAT)) AS MAX" }, SoulissDB.COLUMN_LOG_NODE_ID + " = "
				+ tgt.getTypicalDTO().getNodeId() + " AND " + SoulissDB.COLUMN_LOG_SLOT + " = "
				+ tgt.getTypicalDTO().getSlot() + limitCause + " ", null, "strftime('" + groupBy
				+ "', datetime((cldlogwhen/1000), 'unixepoch', 'localtime'))", null, "IDX ASC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SoulissGraphData dto = new SoulissGraphData(cursor);
			assertEquals(true, dto.key >= 0);
			comments.put(dto.key, dto);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		return comments;
	}

	/**
	 * Return antitheft master typical
	 * 
	 * @param node
	 * @param slot
	 * @return produced Typical
	 */
	public SoulissTypical41AntiTheft getAntiTheftMasterTypical() {
		// query with primary key
		Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
				SoulissDB.COLUMN_TYPICAL + " = "
						+ it.angelic.soulissclient.model.typicals.Constants.Souliss_T41_Antitheft_Main, null, null, null,
				null);
		if (cursor.moveToFirst()) {
			SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
			SoulissTypical41AntiTheft ret = (SoulissTypical41AntiTheft) SoulissTypical.typicalFactory(dto.getTypical(), getSoulissNode(dto.getNodeId()), dto, opts);
			cursor.close();
			return ret;
		} else
			throw new NoSuchElementException();
	}
	
	public List<SoulissTypical> getAntiTheftSensors() {
		List<SoulissTypical> comments = new ArrayList<SoulissTypical>();
		Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
				SoulissDB.COLUMN_TYPICAL + " = " + it.angelic.soulissclient.model.typicals.Constants.Souliss_T42_Antitheft_Peer, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
			SoulissNode parent = getSoulissNode(dto.getNodeId());
			SoulissTypical42AntiTheftPeer newTyp = (SoulissTypical42AntiTheftPeer) SoulissTypical.typicalFactory(dto.getTypical(), parent, dto, opts);
			newTyp.setParentNode(parent);
			// if (newTyp.getTypical() !=
			// Constants.Souliss_T_CurrentSensor_slave)
			comments.add(newTyp);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
				SoulissDB.COLUMN_TYPICAL + " = " + it.angelic.soulissclient.model.typicals.Constants.Souliss_T43_Antitheft_LocalPeer, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
			SoulissNode parent = getSoulissNode(dto.getNodeId());
			SoulissTypical43AntiTheftLocalPeer newTyp = (SoulissTypical43AntiTheftLocalPeer) SoulissTypical.typicalFactory(dto.getTypical(), parent, dto, opts);
			newTyp.setParentNode(parent);
			// if (newTyp.getTypical() !=
			// Constants.Souliss_T_CurrentSensor_slave)
			comments.add(newTyp);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		return comments;
	}

	/**
	 * DB typical factory
	 * 
	 * @param node
	 * @param slot
	 * @return produced Typical
	 */
	public SoulissTypical getSoulissTypical(int node, short slot) {
		// query with primary key
		Cursor cursor = database.query(SoulissDB.TABLE_TYPICALS, SoulissDB.ALLCOLUMNS_TYPICALS,
				SoulissDB.COLUMN_TYPICAL_NODE_ID + " = " + node + " AND " + SoulissDB.COLUMN_TYPICAL_SLOT + " = "
						+ slot, null, null, null, null);
		cursor.moveToFirst();
		SoulissTypicalDTO dto = new SoulissTypicalDTO(cursor);
		SoulissTypical ret = SoulissTypical.typicalFactory(dto.getTypical(), getSoulissNode(node), dto, opts);
		cursor.close();
		return ret;
	}
	


	public SoulissTriggerDTO getSoulissTrigger(long insertId) {
		Cursor cursor = database.query(SoulissDB.TABLE_TRIGGERS, SoulissDB.ALLCOLUMNS_TRIGGERS,
				SoulissDB.COLUMN_TRIGGER_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		SoulissTriggerDTO dto = new SoulissTriggerDTO(cursor);
		// SoulissTypical ret = SoulissTypical.typicalFactory(dto.getTypical(),
		// getSoulissNode(node), dto);
		cursor.close();
		return dto;
	}

	/*
	 * public void deleteTypical(int nodeid, SoulissTypical comment) { // long
	 * id = comment.getId(); // System.out.println("Comment deleted with id: " +
	 * id); database.delete(SoulissDB.TABLE_TYPICALS, SoulissDB.COLUMN_ID +
	 * " = " + nodeid, null); }
	 */
	public int deleteCommand(SoulissCommand toRename) {
		return database.delete(SoulissDB.TABLE_COMMANDS, SoulissDB.COLUMN_COMMAND_ID + " = "
				+ toRename.getCommandDTO().getProgramId(), null);
	}

	public int truncateImportTables() {
		int ret;

		ret = database.delete(SoulissDB.TABLE_LOGS, null, null);
		ret += database.delete(SoulissDB.TABLE_TYPICALS, null, null);
		ret += database.delete(SoulissDB.TABLE_NODES, null, null);
		return ret;

	}

	public int deleteScene(SoulissScene toRename) {
		database.delete(SoulissDB.TABLE_COMMANDS, SoulissDB.COLUMN_COMMAND_SCENEID + " = " + toRename.getId(), null);
		return database.delete(SoulissDB.TABLE_SCENES, SoulissDB.COLUMN_SCENE_ID + " = " + toRename.getId(), null);
	}

	public List<SoulissNode> getAllNodes() {
		List<SoulissNode> comments = new ArrayList<SoulissNode>();
		Cursor cursor = database.query(SoulissDB.TABLE_NODES, SoulissDB.ALLCOLUMNS_NODES, null, null, null, null,
				SoulissDB.COLUMN_NODE_ID);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			SoulissNode comment = SoulissNode.cursorToNode(cursor);
			List<SoulissTypical> cod = getNodeTypicals(comment);
			// come sono grezzo, 'sta riga fa schifo
			for (SoulissTypical soulissTypical : cod) {
				// if (soulissTypical.getNodeId() == comment.getId())
				comment.add(soulissTypical);
			}
			comments.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return comments;
	}
	
	public int countTypicals() {
		Cursor mCount= database.rawQuery("select count(*) from "+SoulissDB.TABLE_TYPICALS+" where "+SoulissDB.COLUMN_TYPICAL+" <> "+it.angelic.soulissclient.model.typicals.Constants.Souliss_T_related, null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		return count;
	}
	

	public List<SoulissTrigger> getAllTriggers(Context context) {
		List<SoulissTrigger> ret = new ArrayList<SoulissTrigger>();
		// Cursor cursor = database.query(SoulissDB.TABLE_TRIGGERS,
		// SoulissDB.ALLCOLUMNS_TRIGGERS, null, null, null, null,null);
		// String MY_QUERY =
		// "SELECT * FROM "+SoulissDB.TABLE_TRIGGERS+" a INNER JOIN "+SoulissDB.TABLE_COMMANDS
		// +" b ON a.cmdid = b.cmdid WHERE b.property_id=?";
		String MY_QUERY = "SELECT * FROM " + SoulissDB.TABLE_TRIGGERS + " a " + "INNER JOIN "
				+ SoulissDB.TABLE_COMMANDS + " b ON a." + SoulissDB.COLUMN_TRIGGER_COMMAND_ID + " = b."
				+ SoulissDB.COLUMN_COMMAND_ID;
		Cursor cursor = database.rawQuery(MY_QUERY, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			SoulissCommandDTO cmd = new SoulissCommandDTO(cursor);
			SoulissTypical tgt = getSoulissTypical(cmd.getNodeId(), cmd.getSlot());
			SoulissTrigger cols = new SoulissTrigger(context, cmd, tgt);
			SoulissTriggerDTO comment = new SoulissTriggerDTO(cursor);

			cols.setTriggerDto(comment);

			ret.add(cols);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return ret;
	}

	/**
	 * Ritorna mappa di tutti i comandi, indicizzati per ID
	 * 
	 * @param ct
	 * @return
	 */
	public SparseArray<SoulissTriggerDTO> getTriggerMap(Context ct) {
		SparseArray<SoulissTriggerDTO> ret = new SparseArray<SoulissTriggerDTO>();

		Cursor cursor = database.query(SoulissDB.TABLE_TRIGGERS, SoulissDB.ALLCOLUMNS_TRIGGERS, null, null, null, null,
				null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			SoulissTriggerDTO dto = new SoulissTriggerDTO(cursor);
			ret.put((int) dto.getCommandId(), dto);
			cursor.moveToNext();
		}

		cursor.close();
		return ret;

	}

	public LinkedList<SoulissCommand> getUnexecutedCommands(Context context) {
		LinkedList<SoulissCommand> ret = new LinkedList<SoulissCommand>();
		Cursor cursor = database.query(SoulissDB.TABLE_COMMANDS, SoulissDB.ALLCOLUMNS_COMMANDS, " ("
				+ SoulissDB.COLUMN_COMMAND_EXECTIME + " is null OR " + SoulissDB.COLUMN_COMMAND_TYPE + " ="
				+ Constants.COMMAND_COMEBACK_CODE + " OR " + SoulissDB.COLUMN_COMMAND_TYPE + " ="
				+ Constants.COMMAND_GOAWAY_CODE + " OR " + SoulissDB.COLUMN_COMMAND_TYPE + " ="
				+ Constants.COMMAND_TRIGGERED + ") AND " + SoulissDB.COLUMN_COMMAND_SCENEID + " IS NULL", null, null,
				null, SoulissDB.COLUMN_COMMAND_SCHEDTIME);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {

			SoulissCommandDTO comment = new SoulissCommandDTO(cursor);
			cursor.moveToNext();
			short node = comment.getNodeId();
			short slot = comment.getSlot();
			SoulissTypical tgt = getSoulissTypical(node, slot);
			tgt.setCtx(context);
			tgt.getTypicalDTO().setNodeId(node);
			tgt.getTypicalDTO().setSlot(slot);
			SoulissCommand adding = new SoulissCommand(context, comment, tgt);
			ret.add(adding);
		}
		cursor.close();
		return ret;
	}

	public ArrayList<SoulissCommand> getSceneCommands(Context context, int sceneId) {
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();
		Cursor cursor = database.query(SoulissDB.TABLE_COMMANDS, SoulissDB.ALLCOLUMNS_COMMANDS,
				SoulissDB.COLUMN_COMMAND_SCENEID + " =" + sceneId, null, null, null,
				SoulissDB.COLUMN_COMMAND_SCHEDTIME_INTERVAL);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {

			SoulissCommandDTO comment = new SoulissCommandDTO(cursor);
			// comments.add(comment);
			cursor.moveToNext();
			short node = comment.getNodeId();
			short slot = comment.getSlot();
			SoulissCommand adding;
			if (node > Constants.MASSIVE_NODE_ID) {
				SoulissTypical tgt = getSoulissTypical(node, slot);
				tgt.setCtx(context);
				tgt.getTypicalDTO().setNodeId(node);
				tgt.getTypicalDTO().setSlot(slot);
				adding = new SoulissCommand(context, comment, tgt);
				// comando massivo
			} else {
				SoulissTypical tgt = new SoulissTypical(opts);
				assertEquals(true, (node == Constants.MASSIVE_NODE_ID));
				// in caso di comando massivo, SLOT = TYPICAL
				tgt.getTypicalDTO().setTypical(slot);
				adding = new SoulissCommand(context, comment, tgt);
			}
			ret.add(adding);
		}
		// Make sure to close the cursor
		cursor.close();
		return ret;
	}

	public LinkedList<SoulissScene> getScenes(Context context) {
		LinkedList<SoulissScene> ret = new LinkedList<SoulissScene>();
		Cursor cursor = database.query(SoulissDB.TABLE_SCENES, SoulissDB.ALLCOLUMNS_SCENES, null, null, null, null,
				SoulissDB.COLUMN_SCENE_ID);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			SoulissScene comment = new SoulissScene(cursor.getInt(cursor
					.getColumnIndex(SoulissDB.COLUMN_SCENE_ID)));
			comment.setName(cursor.getString(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_NAME)));
			comment.setIconResourceId(cursor.getInt(cursor.getColumnIndex(SoulissDB.COLUMN_SCENE_ICON)));
			cursor.moveToNext();
			ArrayList<SoulissCommand> cmds = getSceneCommands(context, comment.getId());
			comment.setCommandArray(cmds);
			ret.add(comment);
		}
		// Make sure to close the cursor
		cursor.close();
		return ret;
	}

	public LinkedList<SoulissCommand> getPositionalPrograms(Context soulissDataService) {
		LinkedList<SoulissCommand> ret = new LinkedList<SoulissCommand>();
		Cursor cursor = database
				.query(SoulissDB.TABLE_COMMANDS, SoulissDB.ALLCOLUMNS_COMMANDS, SoulissDB.COLUMN_COMMAND_TYPE + " = "
						+ Constants.COMMAND_COMEBACK_CODE + " OR " + SoulissDB.COLUMN_COMMAND_TYPE + " = "
						+ Constants.COMMAND_GOAWAY_CODE, null, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {

			SoulissCommandDTO comment = new SoulissCommandDTO(cursor);
			// comments.add(comment);
			cursor.moveToNext();
			short node = comment.getNodeId();
			short slot = comment.getSlot();
			SoulissTypical parentTypical = getSoulissTypical(node, slot);
			SoulissCommand adding = new SoulissCommand(soulissDataService, comment, parentTypical);
			// adding.setParentTypical( getSoulissTypical(tgt) );
			ret.add(adding);
		}
		cursor.close();
		return ret;
	}

	

}
