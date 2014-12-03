package it.angelic.soulissclient.db;

import android.database.Cursor;
import android.util.Log;

public class SoulissGraphData {
	
	public float average;
	public float max;
	public float min;
	
	String key;
	
	public SoulissGraphData() {
		super();
	}
	
	public SoulissGraphData(Cursor cursor) {
		//MONTH index zero based
		key = cursor.getString(0);
		//average = cursor.getFloat(1);
		//max = cursor.getFloat(3);
		//min = cursor.getFloat(2);
		int decimalPlaces = 1;

		min = cursor.getFloat(2);
		max = cursor.getFloat(3);
		average =cursor.getFloat(1);
		
		Log.d("GRAPHDATA", "key="+key+" AVG="+average+" MAX="+max+" MIN="+min);
	}


	


}
