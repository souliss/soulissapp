package it.angelic.soulissclient.fragments;

import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.RangeBarChart;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissGraphData;
import it.angelic.soulissclient.db.SoulissHistoryGraphData;
import it.angelic.soulissclient.helpers.GraphsHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;

import static it.angelic.soulissclient.Constants.TAG;
import static junit.framework.Assert.assertTrue;

public class T5nSensorFragment extends AbstractTypicalFragment {

	private GraphicalView BarChartView;
	private SoulissTypical collected;
	private SoulissDBHelper datasource;

	private TextView upda;
	private SoulissPreferenceHelper opzioni;

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
		else {
			Bundle extras = getActivity().getIntent().getExtras();
			if (extras != null && extras.get("TIPICO") != null) {
				collected = (SoulissTypical) extras.get("TIPICO");
			} else if (getArguments() != null) {
				collected = (SoulissTypical) getArguments().get("TIPICO");
			} else {
				Log.e(Constants.TAG, "Error retriving Typical Detail:");

			}
			View ret = inflater.inflate(R.layout.frag_typicaldetail, container, false);
			TextView nodeinfo = (TextView) ret.findViewById(R.id.TextViewTypNodeInfo);
			upda = (TextView) ret.findViewById(R.id.TextViewTypUpdate);
			assertTrue("TIPICO NULLO", collected != null);

			//Setta STATUS BAR
			super.setCollected(collected);
			/*super.actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
			super.actionBar.setCustomView(R.layout.custom_actionbar); // load
			super.actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM); // show
			super.actionBar.setDisplayHomeAsUpEnabled(true);*/
			refreshStatusIcon();
			
			datasource = new SoulissDBHelper(getActivity());
			SoulissDBHelper.open();

			/* SFONDO */
			// SoulissClient.setBackground((RelativeLayout)
			// getActivity().findViewById(R.id.containerlista),
			// getActivity().getWindowManager());
			nodeinfo.setText(collected.getParentNode().getNiceName() + " - " + getResources().getString(R.string.slot)
					+ " " + collected.getTypicalDTO().getSlot());

			LinearLayout layout = (LinearLayout) ret.findViewById(R.id.trendchart);
			if (collected.isSensor()) {
				TextView tinfo = (TextView) ret.findViewById(R.id.TextViewGraphName);
				SparseArray<SoulissGraphData> logs = new SparseArray<>();
				logs = datasource.getGroupedTypicalLogs(collected, "%H", 0);
				tinfo.setText("Daily temperature range");

				drawGroupedGraph(layout, logs, 1, collected.getTypicalDTO().getTypical());
			}
			return ret;
		}
	}

	public static T5nSensorFragment newInstance(int index, SoulissTypical content) {
		T5nSensorFragment f = new T5nSensorFragment();
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

	@Override
	public void onStart() {
		super.onStart();
		opzioni = SoulissApp.getOpzioni();
		if (opzioni.isDbConfigured()) {
			SoulissDBHelper.open();
			// per il refresh dal dettaglio
			collected = datasource.getTypical(collected.getTypicalDTO().getNodeId(), collected.getTypicalDTO()
					.getSlot());
		}
		final Spinner graphtSpinner = (Spinner) getActivity().findViewById(R.id.spinnerGraphType);
		final Spinner rangeSpinner = (Spinner) getActivity().findViewById(R.id.spinnerGraphRange);
		ImageView icon = (ImageView) getActivity().findViewById(R.id.typ_icon);

		super.refreshStatusIcon();

		ProgressBar par = (ProgressBar) getActivity().findViewById(R.id.progressBarTypNodo);
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
				+ Constants.getTimeAgo(collected.getTypicalDTO().getRefreshedAt()));

		icon.setImageResource(collected.getIconResourceId());
		/**
		 * LISTENER TIPO GRAFICO
		 */
		OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
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
		OnItemSelectedListener lite = new AdapterView.OnItemSelectedListener() {
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

	}

	private void redrawGraph(int graphType, int timeFilter) {
		final LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.trendchart);
		final TextView tinfo = (TextView) getActivity().findViewById(R.id.TextViewGraphName);
		// Log.i(TAG, selectedVal);
		if (graphType == 0) {
			if (collected.isSensor()) {// STORIA
				HashMap<Date, SoulissHistoryGraphData> logs = datasource.getHistoryTypicalLogs(collected, timeFilter);
				tinfo.setText(getString(R.string.historyof) + " " + collected.getNiceName());
				drawHistoryGraph(layout, logs, collected.getTypicalDTO().getTypical());
			}
		} else if (graphType == 2) {
			if (collected.isSensor()) {// GIORNALIERO
				SparseArray<SoulissGraphData> logs = datasource.getGroupedTypicalLogs(collected, "%H", timeFilter);
				tinfo.setText(getString(R.string.daily));
				drawGroupedGraph(layout, logs, 1, collected.getTypicalDTO().getTypical());
			}
		} else {
			if (collected.isSensor()) {
				SparseArray<SoulissGraphData> logs = datasource.getGroupedTypicalLogs(collected, "%m", timeFilter);
				tinfo.setText(getString(R.string.monthly));
				drawGroupedGraph(layout, logs, 0, collected.getTypicalDTO().getTypical());
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// datasource.close();
	}

	/**
	 * 
	 * @param layout
	 * @param logs
	 * @param type
	 *            temp. hum, ecc.
	 */
	private void drawHistoryGraph(LinearLayout layout, HashMap<Date, SoulissHistoryGraphData> logs, int type) {

		XYMultipleSeriesRenderer renderer;

		renderer = GraphsHelper.buildHistoryRenderer(getActivity(), opzioni);

		switch (type) {
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_TemperatureSensor:
			renderer.setYAxisMin(-15);
			renderer.setYAxisMax(50);
			renderer.setYTitle("Celsius degrees");
			break;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_HumiditySensor:
			renderer.setYAxisMin(0);
			renderer.setYAxisMax(100);
			renderer.setYTitle("Humidity %");
			break;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T51:// generic
																			// analog
			renderer.setYAxisMin(0);
			renderer.setYAxisMax(100);
			break;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T54_LuxSensor:
			renderer.setYAxisMin(0);
			renderer.setYAxisMax(1024);
			renderer.setYTitle("Lux");
			break;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T58_PressureSensor:
			renderer.setYAxisMin(0);
			renderer.setYAxisMax(1024);
			renderer.setYTitle("hPa");
			break;
		default:
			break;
		}
		
		renderer.setFitLegend(true);
		renderer.setLegendTextSize(12);

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		TimeSeries avgSeries = new TimeSeries("Average");
		TimeSeries minSeries = new TimeSeries("Min");
		TimeSeries maxSeries = new TimeSeries("Max");

		Set<Date> dates = logs.keySet();
		for (Date date2 : dates) {
			if (logs.get(date2).min != 0) {
				avgSeries.add(date2, logs.get(date2).average);
				minSeries.add(date2, logs.get(date2).min);
				maxSeries.add(date2, logs.get(date2).max);
				Log.d(TAG, "Adding serie " + date2 + ": min=" + logs.get(date2).min
						+ " max=" + logs.get(date2).max);
			} else {
				avgSeries.add(date2, 0);
				minSeries.add(date2, 0);
				maxSeries.add(date2, 0);
			}
		}

		renderer.setXAxisMin(avgSeries.getMinX());

		dataset.addSeries(0, avgSeries);
		dataset.addSeries(1, minSeries);
		dataset.addSeries(2, maxSeries);

		BarChartView = ChartFactory.getTimeChartView(getActivity(), dataset, renderer, "Test");
		// BarChartView = ChartFactory.getCombinedXYChartView(this, dataset,
		// renderer, types);
		layout.removeAllViews();
		layout.addView(BarChartView);

	}

	private void drawGroupedGraph(LinearLayout layout, SparseArray<SoulissGraphData> logs, int bymonth, int type) {

		XYMultipleSeriesRenderer renderer;
		// sceglie tra ora del giorno e anno
		if (bymonth == 1)
			renderer = GraphsHelper.buildHourRenderer(getActivity());
		else
			renderer = GraphsHelper.buildMonthRenderer(getActivity());

		switch (type) {
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_TemperatureSensor:
			renderer.setYAxisMin(-15);
			renderer.setYAxisMax(50);
			break;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_HumiditySensor:
			renderer.setYAxisMin(0);
			renderer.setYAxisMax(100);
			break;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T54_LuxSensor:
			renderer.setYAxisMin(0);
			renderer.setYAxisMax(1024);
			break;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T58_PressureSensor:
			renderer.setYAxisMin(0);
			renderer.setYAxisMax(1024);
			break;
		default:
			break;
		}
		
		//renderer.setAxisTitleTextSize(14);
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		RangeCategorySeries minmaxserie = new RangeCategorySeries("Temperature");
		XYSeries sunSeries = new XYSeries("Average");

		int lengt = logs.size();
		for (int k = 0; k < lengt; k++) {
			Log.d(TAG, "Adding serie " + (k ) + ": min=" + logs.get(k).min + " max="
					+ logs.get(k).max
					 + " AVG=" + logs.get(k).average);
			
			if (logs.get(k ).min != 0) {
				minmaxserie.add(logs.get(k ).min,
						logs.get(k ).max);
			} else {
				minmaxserie.add(0, 0);
			}
			sunSeries.add(k+1, logs.get(k).average);
			Log.d(TAG, "Adding serie " + (k) + " AVG=" + logs.get(k).average);
		}

		dataset.addSeries(0, minmaxserie.toXYSeries());
		dataset.addSeries(1, sunSeries);

		//String[] types = new String[] { RangeBarChart.TYPE, LineChart.TYPE };
		CombinedXYChart.XYCombinedChartDef[] types = new CombinedXYChart.XYCombinedChartDef[] {
				new CombinedXYChart.XYCombinedChartDef(RangeBarChart.TYPE, 0), new CombinedXYChart.XYCombinedChartDef(LineChart.TYPE, 1) };
		BarChartView = ChartFactory.getCombinedXYChartView(getActivity(), dataset, renderer, types);
		layout.removeAllViews();
		layout.addView(BarChartView);
	}

}
