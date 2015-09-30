package it.angelic.soulissclient.db;

import android.database.Cursor;
import android.util.Log;

import java.math.BigDecimal;
import java.util.Date;

public class SoulissHistoryGraphData {


    public SoulissHistoryGraphData() {
        super();
    }

    public SoulissHistoryGraphData(Cursor cursor, Date keyss) {
        //MONTH index zero based
        key = keyss;
        //average = cursor.getFloat(1);
        //max = cursor.getFloat(3);
        //min = cursor.getFloat(2);
        int decimalPlaces = 1;
        BigDecimal bd = new BigDecimal(cursor.getFloat(2));

        // setScale is immutable
        bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
        min = bd.doubleValue();
        bd = new BigDecimal(cursor.getFloat(3));
        bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
        max = bd.doubleValue();
        bd = new BigDecimal(cursor.getFloat(1));
        bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
        average = bd.doubleValue();
        Log.d("GRAPH", "key=" + key + " AVG=" + average);
    }

    public SoulissHistoryGraphData(Date t, Cursor cursor) {
        // TODO Auto-generated constructor stub
    }

    public double average;
    public double max;
    public double min;

    Date key;


}
