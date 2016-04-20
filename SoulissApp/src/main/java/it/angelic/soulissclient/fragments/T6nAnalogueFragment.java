package it.angelic.soulissclient.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.dacer.androidcharts.LineView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.HalfFloatUtils;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissGraphData;
import it.angelic.soulissclient.db.SoulissHistoryGraphData;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical6nAnalogue;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.util.SoulissUtils;
import it.angelic.soulissclient.views.NumberPickerT6;
import it.angelic.tagviewlib.SimpleTagRelativeLayout;

import static it.angelic.soulissclient.Constants.TAG;
import static it.angelic.soulissclient.Constants.yearFormat;
import static junit.framework.Assert.assertTrue;

public class T6nAnalogueFragment extends AbstractTypicalFragment implements NumberPicker.OnValueChangeListener {

    private SoulissDBHelper datasource;
    private Spinner graphtSpinner;
    private ImageView icon;
    private EditText incrementText;
    private LineView lineView;
    private TextView nodeinfo;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(Constants.TAG, "Broadcast received, TODO change Spinners status intent" + intent.toString());
                SoulissDBHelper.open();
                SoulissNode coll = datasource.getSoulissNode(collected.getTypicalDTO().getNodeId());
                collected = coll.getTypical(collected.getTypicalDTO().getSlot());
                nodeinfo.setText(collected.getParentNode().getNiceName() + " - " + getResources().getString(R.string.slot)
                        + " " + collected.getTypicalDTO().getSlot() + " - " + ((SoulissTypical6nAnalogue) collected).getOutputFloat());
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
            }


        }
    };
    private ProgressBar par;
    private Spinner rangeSpinner;
    private NumberPickerT6 tempSlider;
    private TextView upda;

    public static T6nAnalogueFragment newInstance(int index, SoulissTypical content) {
        T6nAnalogueFragment f = new T6nAnalogueFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        // Ci metto il nodo dentro
        if (content != null) {
            args.putSerializable("TIPICO", content);
        }
        f.setArguments(args);

        return f;
    }

    private void drawGroupedGraphAndroChart(SparseArray<SoulissGraphData> logs, ChartTypeEnum bymonth) {
        //must*
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
        //must*
        ArrayList<String> test = new ArrayList<String>();
        ArrayList<Integer> dataList = new ArrayList<Integer>();
        ArrayList<Integer> dataListMin = new ArrayList<Integer>();
        ArrayList<Integer> dataListMax = new ArrayList<Integer>();

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
        Log.d(TAG, "Dataseries size: " + dataList.size());
        lineView.setDataList(dataLists);
        lineView.setDrawDotLine(true);
        lineView.setShowPopup(LineView.SHOW_POPUPS_NONE);

        lineView.setVisibility(View.VISIBLE);
        //layout.addView(lineView);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        if (opzioni.isLightThemeSelected())
            getActivity().setTheme(R.style.LightThemeSelector);
        else
            getActivity().setTheme(R.style.DarkThemeSelector);
        super.onActivityCreated(savedInstanceState);
        // getActivity().setContentView(R.layout.main_typicaldetail);
        setHasOptionsMenu(true);

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.get("TIPICO") != null) {
            collected = (SoulissTypical) extras.get("TIPICO");
        } else if (getArguments() != null) {
            collected = (SoulissTypical) getArguments().get("TIPICO");
        } else {
            Log.e(Constants.TAG, "Error retriving Typical Detail:");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;

        datasource = new SoulissDBHelper(getActivity());
        SoulissDBHelper.open();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.get("TIPICO") != null) {
            collected = (SoulissTypical) extras.get("TIPICO");
        } else if (getArguments() != null) {
            collected = (SoulissTypical) getArguments().get("TIPICO");
        } else {
            Log.e(Constants.TAG, "Error retriving Typical Detail:");

        }
        View ret = inflater.inflate(R.layout.frag_t6n_analogue, container, false);
        nodeinfo = (TextView) ret.findViewById(R.id.TextViewTypNodeInfo);
        graphtSpinner = (Spinner) ret.findViewById(R.id.spinnerGraphType);
        rangeSpinner = (Spinner) ret.findViewById(R.id.spinnerGraphRange);
        incrementText = (EditText) ret.findViewById(R.id.editTextIncrement);
        icon = (ImageView) ret.findViewById(R.id.typ_icon);
        lineView = (LineView) ret.findViewById(R.id.line_view);
        tempSlider = (NumberPickerT6) ret.findViewById(R.id.tempSliderPicker);
        upda = (TextView) ret.findViewById(R.id.TextViewTypUpdate);
        par = (ProgressBar) ret.findViewById(R.id.progressBarTypNodo);
        infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);
        tagView = (SimpleTagRelativeLayout) ret.findViewById(R.id.tag_group);
        assertTrue("TIPICO NULLO", collected != null);

        refreshTagsInfo();

        //Setta STATUS BAR
        super.setCollected(collected);

        refreshStatusIcon();

        nodeinfo.setText(collected.getParentNode().getNiceName() + " - " + getResources().getString(R.string.slot)
                + " " + collected.getTypicalDTO().getSlot() + " - " + getContext().getString(R.string.reading) + " " + ((SoulissTypical6nAnalogue) collected).getOutputFloat());

        par.setMax(Constants.MAX_HEALTH);

        // ProgressBar sfumata
        final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(Constants.roundedCorners, null, null));
        final LinearGradient gradient = new LinearGradient(0, 0, 250, 0, getResources().getColor(color.aa_red),
                getResources().getColor(color.aa_green), android.graphics.Shader.TileMode.CLAMP);
        pgDrawable.getPaint().setStrokeWidth(3);
        pgDrawable.getPaint().setDither(true);
        pgDrawable.getPaint().setShader(gradient);

        ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        par.setBackgroundResource(android.R.drawable.progress_horizontal);
        par.setProgressDrawable(progress);
        par.setMax(50);
        par.setProgress(20);
        par.setProgress(0); // <-- BUG Android
        par.setMax(Constants.MAX_HEALTH);
        par.setProgress(collected.getParentNode().getHealth());
        upda.setText(getResources().getString(R.string.update) + " "
                + SoulissUtils.getTimeAgo(collected.getTypicalDTO().getRefreshedAt()));

        icon.setImageResource(collected.getIconResourceId());

        tempSlider.setModel(collected.getTypical());
        incrementText.setText(String.valueOf(tempSlider.getIncrement()));
        incrementText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    tempSlider.setIncrement(Float.valueOf(s.toString()));
                    int sel = tempSlider.generateDisplayValues(tempSlider.getRealVal());
                    tempSlider.setValue(sel);
                    tempSlider.invalidate();
                } catch (Exception er) {
                    incrementText.setError("Invalid format");
                }
            }
        });
        float vai = ((SoulissTypical6nAnalogue) collected).getOutputFloat();
        //consider NaN as 0, ie upon board start
        if (!Float.isNaN(vai))
            tempSlider.setRealVal(vai);
        else
            tempSlider.setRealVal(0);
        tempSlider.setWrapSelectorWheel(false);

        tempSlider.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        tempSlider.setOnValueChangedListener(this);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // datasource.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(datareceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filtere = new IntentFilter();
        filtere.addAction("it.angelic.soulissclient.GOT_DATA");
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        getActivity().registerReceiver(datareceiver, filtere);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Thread t = new Thread() {
            public void run() {
                // collected.issueCommand(Souliss_T3n_Set, Float.valueOf(tempSlider.getValue()));
                Float work = Float.parseFloat(tempSlider.getDisplayedValues()[tempSlider.getValue()]);
                int re = HalfFloatUtils.fromFloat(work);
                String first, second;
                String pars = Long.toHexString(re);
                Log.i(Constants.TAG, "PARSED SETPOINT TEMP: 0x" + pars);

                try {
                    second = Integer.toString(Integer.parseInt(pars.substring(0, 2), 16));
                } catch (StringIndexOutOfBoundsException sie) {
                    second = "0";
                }
                try {
                    first = Integer.toString(Integer.parseInt(pars.substring(2, 4), 16));
                } catch (StringIndexOutOfBoundsException sie) {
                    first = "0";
                }
                //INVERTITI? Occhio
                String[] cmd = {first, second};
                //verifyCommand(temp, first, second);
                //FIXME
                //XXX
                Log.i(Constants.TAG, "ISSUE COMMAND:" + first + " " + second);
                UDPHelper.issueSoulissCommand("" + collected.getNodeId(), "" + collected.getSlot(),
                        SoulissApp.getOpzioni(), cmd);
            }
        };
        t.start();

    }

    private void redrawGraph(int graphType, int timeFilter) {
        final HorizontalScrollView layout = (HorizontalScrollView) getActivity().findViewById(R.id.horizontalScrollView);
        //final TextView tinfo = (TextView) getActivity().findViewById(R.id.TextViewGraphName);
        // Log.i(TAG, selectedVal);
        try {
            ChartTypeEnum tipoGrafico = ChartTypeEnum.values()[graphType];
            switch (tipoGrafico) {
                case HISTORY:
                    if (collected.isSensor()) {// STORIA
                        //TODO se vuoto skippa
                        HashMap<Date, SoulissHistoryGraphData> logs = datasource.getHistoryTypicalLogs(collected, timeFilter);
                        drawHistoryGraphAndroChart(logs);
                    }
                    break;
                case GROUP_HOUR://fallback
                    if (collected.isSensor()) {// HEUR
                        //TODO se vuoto skippa
                        SparseArray<SoulissGraphData> logs = datasource.getGroupedTypicalLogs(collected, "%H", timeFilter);
                        drawGroupedGraphAndroChart(logs, tipoGrafico);
                    }
                    break;
                case GROUP_MONTH:
                    if (collected.isSensor()) {// HEUR
                        //TODO se vuoto skippa
                        SparseArray<SoulissGraphData> logs = datasource.getGroupedTypicalLogs(collected, "%m", timeFilter);
                        drawGroupedGraphAndroChart(logs, tipoGrafico);
                    }
                    break;
                case GROUP_WEEK:
                    if (collected.isSensor()) {// HEUR
                        //TODO se vuoto skippa
                        SparseArray<SoulissGraphData> logs = datasource.getGroupedTypicalLogs(collected, "%w", timeFilter);
                        drawGroupedGraphAndroChart(logs, tipoGrafico);
                    }
                    break;
            }
        } catch (Exception graphExc) {
            Log.e(Constants.TAG, "Cant draw chart: " + graphExc.getMessage());
        }
    }
}
