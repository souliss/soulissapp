package it.angelic.soulissclient.helpers;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

import it.angelic.soulissclient.R;

public class GraphsHelper {

	/**
	 * Renderer per la storia del tipico
	 * 
	 * @param ctx
	 * @param opzioni 
	 * @return
	 */
	public static XYMultipleSeriesRenderer buildHistoryRenderer(Context ctx, SoulissPreferenceHelper opzioni ) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		XYSeriesRenderer lightRenderer = new XYSeriesRenderer();
		
		lightRenderer.setDisplayChartValues(false);
		lightRenderer.setFillPoints(true);
		lightRenderer.setPointStyle(PointStyle.CIRCLE);

		XYSeriesRenderer aRenderer = new XYSeriesRenderer();
		//aRenderer.setChartValuesSpacing(1.0f);
		aRenderer.setDisplayChartValues(false);
		aRenderer.setFillPoints(false);
		aRenderer.setPointStyle(PointStyle.DIAMOND);

		XYSeriesRenderer mRenderer = new XYSeriesRenderer();
		mRenderer.setDisplayChartValues(false);
		mRenderer.setFillPoints(false);
		mRenderer.setPointStyle(PointStyle.DIAMOND);
		if (opzioni.isLightThemeSelected()){
			lightRenderer.setColor(ctx.getResources().getColor(R.color.std_yellow_shadow));
			aRenderer.setColor(ctx.getResources().getColor(R.color.std_blue_shadow));
			mRenderer.setColor(ctx.getResources().getColor(R.color.std_red_shadow));
		}
		else{
		lightRenderer.setColor(ctx.getResources().getColor(R.color.std_yellow));
		aRenderer.setColor(ctx.getResources().getColor(R.color.std_blue));
		mRenderer.setColor(ctx.getResources().getColor(R.color.std_red));
		}
		renderer.addSeriesRenderer(lightRenderer);
		renderer.addSeriesRenderer(aRenderer);
		renderer.addSeriesRenderer(mRenderer);

		// renderer.setBarSpacing(0.5);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
		renderer.setMargins(new int[] { 10, 30, 20, 0 });
		renderer.setYLabelsAlign(Align.RIGHT);
		// Background fix
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));

		//renderer.setChartTitle("Sensor History");
		
		renderer.setXLabelsAngle(45);
		renderer.setXLabelsAlign(Align.LEFT);

		if (opzioni.isLightThemeSelected()){
			renderer.setAxesColor(Color.DKGRAY);
			renderer.setXLabelsColor(Color.BLACK);
			// renderer.setYLabelsColor(1, Color.BLACK);
			renderer.setLabelsColor(Color.BLACK);
		}
		else{
		renderer.setAxesColor(Color.GRAY);
		renderer.setLabelsColor(Color.LTGRAY);
		}
		
		renderer.setPanEnabled(true, true);
		renderer.setZoomEnabled(true, false);
		
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);

		renderer.setAxisTitleTextSize(12);
		renderer.setChartTitleTextSize(16);
		renderer.setLabelsTextSize(12);
		renderer.setLegendTextSize(12);
		//renderer.setLegendHeight(20);
		
		return renderer;
	}

	public static XYMultipleSeriesRenderer buildHourRenderer(Context ctx) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		SimpleSeriesRenderer rangeRenderer = new SimpleSeriesRenderer();
		rangeRenderer.setColor(ctx.getResources().getColor(R.color.std_green));
		rangeRenderer.setDisplayChartValues(false);
		// r.setChartValuesSpacing(3);
		rangeRenderer.setGradientEnabled(true);
		rangeRenderer.setGradientStart(-10, ctx.getResources().getColor(R.color.std_blue));
		rangeRenderer.setGradientStop(40, ctx.getResources().getColor(R.color.std_red));
		
		XYSeriesRenderer lightRenderer = new XYSeriesRenderer();
		lightRenderer.setColor(ctx.getResources().getColor(R.color.std_yellow));
		lightRenderer.setDisplayChartValues(true);
		lightRenderer.setChartValuesFormat(new DecimalFormat("#.##"));
		// lightRenderer.setGradientEnabled(true);
		lightRenderer.setFillPoints(true);
		lightRenderer.setPointStyle(PointStyle.DIAMOND);

		renderer.addSeriesRenderer(rangeRenderer);
		renderer.addSeriesRenderer(1, lightRenderer);
		
		renderer.setShowLegend(false);
		renderer.setBarSpacing(1.0);
		renderer.setXLabels(0);
		renderer.setYLabels(10);
		renderer.addXTextLabel(1, "0:00");
		renderer.addXTextLabel(7, "6:00");
		renderer.addXTextLabel(13, "12:00");
		renderer.addXTextLabel(19, "18:00");
		renderer.addXTextLabel(24, "23:00");
		renderer.setPanEnabled(true, false);
		renderer.setShowGrid(true);
		renderer.setMargins(new int[] { 10, 30, 5, 0 });
		renderer.setYLabelsAlign(Align.RIGHT);
		// Background fix
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));

		setChartSettings(ctx, renderer, "Hour of Day", "Celsius degrees", -2, 30, Color.GRAY, Color.LTGRAY);
		return renderer;
	}

	public static XYMultipleSeriesRenderer buildMonthRenderer(Context ctx) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		SimpleSeriesRenderer rangeRenderer = new SimpleSeriesRenderer();
		rangeRenderer.setColor(ctx.getResources().getColor(R.color.std_green));
		rangeRenderer.setDisplayChartValues(true);
		rangeRenderer.setChartValuesFormat(new DecimalFormat("#.##"));
		// r.setChartValuesSpacing(3);
		rangeRenderer.setGradientEnabled(true);
		rangeRenderer.setGradientStart(-10, ctx.getResources().getColor(R.color.std_blue));
		rangeRenderer.setGradientStop(50, ctx.getResources().getColor(R.color.std_red));

		renderer.addSeriesRenderer(rangeRenderer);
		
		XYSeriesRenderer lightRenderer = new XYSeriesRenderer();
		lightRenderer.setColor(ctx.getResources().getColor(R.color.std_yellow));
		lightRenderer.setDisplayChartValues(true);
		lightRenderer.setChartValuesFormat(new DecimalFormat("#.##"));
		// lightRenderer.setGradientEnabled(true);

		lightRenderer.setFillPoints(true);
		lightRenderer.setPointStyle(PointStyle.DIAMOND);
		renderer.addSeriesRenderer(lightRenderer);
		
		renderer.setShowLegend(false);
		renderer.setBarSpacing(1.5);
		renderer.setXLabels(0);
		renderer.setYLabels(10);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, 0);
		//renderer.addXTextLabel(2, cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
		renderer.addXTextLabel(2,String.format( Locale.getDefault(),"%tB",cal));
		// cal.set(Calendar.MONTH, 1);
		// renderer.addXTextLabel(2, cal.getDisplayName(Calendar.MONTH,
		// Calendar.SHORT, Locale.getDefault()));
		cal.set(Calendar.MONTH, 2);
		//renderer.addXTextLabel(4, cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
		renderer.addXTextLabel(4,String.format( Locale.getDefault(),"%tB",cal));
		// cal.set(Calendar.MONTH, 3);
		// renderer.addXTextLabel(4, cal.getDisplayName(Calendar.MONTH,
		// Calendar.SHORT, Locale.getDefault()));
		cal.set(Calendar.MONTH, 4);
		//renderer.addXTextLabel(6, cal.getDisplayName(Calendar.MONTH,  java.util.Calendar.SHORT, Locale.getDefault()));
		renderer.addXTextLabel(6,String.format( Locale.getDefault(),"%tB",cal));
		// cal.set(Calendar.MONTH, 5);
		// renderer.addXTextLabel(6, cal.getDisplayName(Calendar.MONTH,
		// Calendar.SHORT, Locale.getDefault()));
		cal.set(Calendar.MONTH, 6);
		//renderer.addXTextLabel(8, cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
		renderer.addXTextLabel(8,String.format( Locale.getDefault(),"%tB",cal));
		// cal.set(Calendar.MONTH, 7);
		// renderer.addXTextLabel(8, cal.getDisplayName(Calendar.MONTH,
		// Calendar.SHORT, Locale.getDefault()));
		cal.set(Calendar.MONTH, 8);
	//	renderer.addXTextLabel(10, cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
		renderer.addXTextLabel(10,String.format( Locale.getDefault(),"%tB",cal));
		// cal.set(Calendar.MONTH, 9);
		// renderer.addXTextLabel(10, cal.getDisplayName(Calendar.MONTH,
		// Calendar.SHORT, Locale.getDefault()));
		// cal.set(Calendar.MONTH, 10);
		// renderer.addXTextLabel(11, cal.getDisplayName(Calendar.MONTH,
		// Calendar.SHORT, Locale.getDefault()));
		cal.set(Calendar.MONTH, 11);
		//renderer.addXTextLabel(13, cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
		renderer.addXTextLabel(13,String.format( Locale.getDefault(),"%tB",cal));
		renderer.setPanEnabled(true, false);
		renderer.setShowGrid(true);
		renderer.setAntialiasing(true);
		renderer.setMargins(new int[] { 10, 30, 5, 0 });
		renderer.setYLabelsAlign(Align.RIGHT);
		// Background fix
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));

		setChartSettings(ctx, renderer, "Month", "Celsius degrees", 0, 13, Color.GRAY, Color.LTGRAY);
		return renderer;
	}

	/**
	 * Builds a bar multiple series renderer to use the provided colors.
	 * 
	 * @param colors
	 *            the series renderers colors
	 * @return the bar multiple series renderer
	 */
	@Deprecated
	protected static XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(16);
		renderer.setLegendTextSize(16);

		int length = colors.length;
        for (int color : colors) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(color);
            renderer.addSeriesRenderer(r);
        }
		return renderer;
	}

	
	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer
	 *            the renderer to set the properties to
	 * @param title
	 *            the chart title

	 * @param yTitle
	 *            the title for the Y axis
	 * @param xMin
	 *            the minimum value on the X axis
	 * @param xMax
	 *            the maximum value on the X axis
	 * @param axesColor
	 *            the axes color
	 * @param labelsColor
	 *            the labels color
	 */
	public static void setChartSettings(Context ctx,XYMultipleSeriesRenderer renderer, String title, String yTitle, double xMin,
			double xMax, int axesColor, int labelsColor) {

		renderer.setChartTitle(title);
		// renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);

		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, metrics);
		
		renderer.setAxisTitleTextSize(val);
		renderer.setChartTitleTextSize(val);
		renderer.setLabelsTextSize(val);
		renderer.setLegendTextSize(val);
		renderer.setChartValuesTextSize(val-2);
	}

}
