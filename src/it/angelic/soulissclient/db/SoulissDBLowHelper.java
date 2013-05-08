package it.angelic.soulissclient.db;

import it.angelic.soulissclient.Constants;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Classe helper per l'esecuzione di interrogazioni al DB, Inserimenti eccetera
 * 
 * @author Ale
 * 
 */
public class SoulissDBLowHelper extends SoulissDBHelper {

	public SoulissDBLowHelper(Context context) {
		super(context);
	}

	// Database fields

	public static synchronized SQLiteDatabase getDatabase() {
		return database;
	}


	/**
	 * aggiorna l'health dei nodi, pensato per la risposta UDP
	 * della health request. L'array di byte contiene la salute dei nodi
	 * 
	 * @param nodeIN
	 * @return
	 */
	public int refreshNodeHealths(ArrayList<Short> healths, int startOffset) {
		int ret = 0;

		for (int i = 0; i < healths.size(); i++) {
			ContentValues values = new ContentValues();
			values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
			values.put(SoulissDB.COLUMN_NODE_HEALTH, healths.get(i));
			long upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + String.valueOf(i+startOffset), null);
			ret += upd;
		}
		return ret;
	}
	/**
	 * In seguito alla DB structure, viene aggiornata
	 * la struttura dei nodi, senza cancellarla
	 * 
	 * @param numnodes nodi da creare
	 * @param typXnode tipici x nodo
	 */
	public int createOrUpdateStructure(int numnodes, int typXnode){
		int ret = 0;
		for (int i = 0; i < numnodes; i++) {
			ContentValues values = new ContentValues();
			// wrap values from object
			values.put(SoulissDB.COLUMN_NODE_LASTMOD, Calendar.getInstance().getTime().getTime());
			values.put(SoulissDB.COLUMN_NODE_ID, i);
			values.put(SoulissDB.COLUMN_NODE_HEALTH, 0);
			//values.put(SoulissDB.COLUMN_NODE_ICON, nodeIN.getDefaultIconResourceId());
			int upd = database.update(SoulissDB.TABLE_NODES, values, SoulissDB.COLUMN_NODE_ID + " = " + i,
					null);
			if (upd == 0) {
				long insertId = database.insert(SoulissDB.TABLE_NODES, null, values);
				Log.d(Constants.TAG, "Node "+i+" insert returned: "+insertId);
				ret++;
			}else
				ret += upd;
		}
		return ret;
		

	}
	


}
