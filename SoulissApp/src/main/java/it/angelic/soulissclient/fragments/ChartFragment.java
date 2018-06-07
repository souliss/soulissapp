package it.angelic.soulissclient.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import im.dacer.androidcharts.LineView;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.model.ISoulissTypicalSensor;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissGraphData;
import it.angelic.soulissclient.model.db.SoulissHistoryGraphData;

import static it.angelic.soulissclient.Constants.TAG;
import static it.angelic.soulissclient.Constants.yearFormat;
import static junit.framework.Assert.assertTrue;

public class ChartFragment extends Fragment {

    private static ISoulissTypicalSensor collected;
    private SoulissDBHelper datasource;
    private Spinner graphtSpinner;
    private LineView lineView;
    private Spinner rangeSpinner;

    public static ChartFragment newInstance(ISoulissTypicalSensor content) {
        ChartFragment f = new ChartFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        collected = content;
        // Ci metto il nodo dentro
        if (content != null) {
            args.putSerializable("TIPICO", content);
        }
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;

        datasource = new SoulissDBHelper(getActivity());
        SoulissDBHelper.open();

        Bundle extras = getActivity().getIntent().getExtras();

        View ret = inflater.inflate(R.layout.frag_chart, container, false);
        TextView nodeinfo = ret.findViewById(R.id.TextViewTypNodeInfo);
        graphtSpinner = ret.findViewById(R.id.spinnerGraphType);
        rangeSpinner = ret.findViewById(R.id.spinnerGraphRange);
        rangeSpinner.setSelection(2);
        lineView = ret.findViewById(R.id.line_view);
        //TextView upda = ret.findViewById(R.id.TextViewTypUpdate);

        assertTrue("TIPICO NULLO", collected != null);

        /**
         * LISTENER TIPO GRAFICO
         */
        OnItemSelectedListener lit = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Filtro tempo
                String bid = (String) rangeSpinner.getSelectedItem();
                int len = 0;
                final String[] tempArray = getResources().getStringArray(R.array.graphRange);
                for (int i = 0; i < tempArray.length; i++) {
                    if (tempArray[i].compareTo(bid) == 0)
                        len = i;
                }
                redrawGraph(pos, len);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        graphtSpinner.setOnItemSelectedListener(lit);
        /**
         * LISTENER RANGE
         */
        OnItemSelectedListener lite = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Filtro tempo
                String bid = (String) graphtSpinner.getSelectedItem();
                int len = 0;
                final String[] tempArray = getResources().getStringArray(R.array.graphType);
                for (int i = 0; i < tempArray.length; i++) {
                    if (tempArray[i].compareTo(bid) == 0)
                        len = i;
                }
                redrawGraph(len, pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        rangeSpinner.setOnItemSelectedListener(lite);

        return ret;

    }

    private void redrawGraph(int graphType, int timeFilter) {

        ChartTypeEnum tipoGrafico = ChartTypeEnum.values()[graphType];
        switch (tipoGrafico) {
            case HISTORY:
                HashMap<Date, SoulissHistoryGraphData> logs = datasource.getHistoryTypicalLogs(collected, timeFilter);
                drawHistoryGraphAndroChart(logs);
                break;
            case GROUP_HOUR://fallback
                SparseArray<SoulissGraphData> logss = datasource.getGroupedTypicalLogs(collected, "%H", timeFilter);
                drawGroupedGraphAndroChart(logss, tipoGrafico);
                break;
            case GROUP_MONTH:
                SparseArray<SoulissGraphData> logsd = datasource.getGroupedTypicalLogs(collected, "%m", timeFilter);
                drawGroupedGraphAndroChart(logsd, tipoGrafico);
                break;
            case GROUP_WEEK:
                SparseArray<SoulissGraphData> logsf = datasource.getGroupedTypicalLogs(collected, "%w", timeFilter);
                drawGroupedGraphAndroChart(logsf, tipoGrafico);
                break;
        }
    }


    private void drawGroupedGraphAndroChart(SparseArray<SoulissGraphData> logs, ChartTypeEnum bymonth) {
        if (logs == null || logs.size() < 1)
            return;
        ArrayList<String> test = new ArrayList<>();
        ArrayList<Integer> dataList = new ArrayList<>();
        ArrayList<Integer> dataListMin = new ArrayList<>();
        ArrayList<Integer> dataListMax = new ArrayList<>();

        if (bymonth == ChartTypeEnum.GROUP_MONTH) {
            Calendar cal = Calendar.getInstance();
            for (int k = 0; k < logs.size(); k++) {
                cal.set(Calendar.MONTH, k);
                test.add(String.format(Locale.getDefault(), "%tb", cal));
            }
        } else if (bymonth == ChartTypeEnum.GROUP_WEEK) {//DAY OF W
            Calendar cal = Calendar.getInstance();
            for (int k = 0; k < logs.size(); k++) {
                cal.set(Calendar.DAY_OF_WEEK, k + 1);//+1 perche Calendar.DAY_OF_WEEK parte da 1, domenica
                test.add(String.format(Locale.getDefault(), "%ta", cal));
            }
        } else if (bymonth == ChartTypeEnum.GROUP_HOUR) {
            for (int k = 0; k < logs.size(); k++) {
                test.add(k + ":00");
            }
        }
        int lengt = logs.size();
        for (int k = 0; k < lengt; k++) {
            dataList.add((int) logs.get(k).average);
            dataListMin.add((int) logs.get(k).min);
            dataListMax.add((int) logs.get(k).max);

            Log.d(TAG, "Adding serie " + (k) + ": min=" + logs.get(k).min + " max="
                    + logs.get(k).max
                    + " AVG=" + logs.get(k).average);
        }

        lineView.setBottomTextList(test);
        ArrayList<ArrayList<Integer>> dataLists = new ArrayList<>();

        dataLists.add(dataListMax);
        dataLists.add(dataListMin);
        dataLists.add(dataList);

        assertTrue(test.size() == dataList.size());
        lineView.setDataList(dataLists);
        lineView.setDrawDotLine(true);
        lineView.setShowPopup(LineView.SHOW_POPUPS_NONE);
        lineView.setVisibility(View.VISIBLE);

    }

    /**
     * @param logs
     */
    private void drawHistoryGraphAndroChart(HashMap<Date, SoulissHistoryGraphData> logs) {

        if (logs == null || logs.size() < 1)
            return;

        //must*
        ArrayList<String> test = new ArrayList<>();
        ArrayList<Integer> dataList = new ArrayList<>();
        ArrayList<Integer> dataListMin = new ArrayList<>();
        ArrayList<Integer> dataListMax = new ArrayList<>();

        Set<Date> dates = logs.keySet();
        for (Date date2 : dates) {
            test.add(yearFormat.format(date2));
            dataList.add((int) logs.get(date2).average);
            dataListMin.add((int) logs.get(date2).min);
            dataListMax.add((int) logs.get(date2).max);
            Log.d(TAG, "Adding serie " + date2 + ": min=" + logs.get(date2).min
                    + " max=" + logs.get(date2).max);
        }
        lineView.setBottomTextList(test);
        ArrayList<ArrayList<Integer>> dataLists = new ArrayList<>();

        dataLists.add(dataListMax);
        dataLists.add(dataListMin);
        dataLists.add(dataList);

        assertTrue(test.size() == dataList.size());
        lineView.setDataList(dataLists);
        lineView.setDrawDotLine(true);
        lineView.setShowPopup(LineView.SHOW_POPUPS_NONE);

        lineView.setVisibility(View.VISIBLE);
        //layout.addView(lineView);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // datasource.close();
    }


}
