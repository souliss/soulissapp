/**
 * Copyright 2011, Felix Palmer
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.pheelicks.visualizer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.pheelicks.visualizer.renderer.Renderer;

import java.util.HashSet;
import java.util.Set;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.fragments.AbstractMusicVisualizerFragment;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;

/**
 * A class that draws visualizations of data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture } and
 * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
 */
public class VisualizerView extends View {

	private byte[] mBytes;
	private byte[] mFFTBytes;
	private Rect mRect = new Rect();
	private Visualizer mVisualizer;
	private SoulissPreferenceHelper opz;

	private Set<Renderer> mRenderers;

	//private Paint mFlashPaint = new Paint();
	private Paint mFadePaint = new Paint();

	private AbstractMusicVisualizerFragment parent;

	// private Paint transPainter;

	public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		init();
	}

	public VisualizerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VisualizerView(Context context) {
		this(context, null, 0);
	}

	public void setFrag(AbstractMusicVisualizerFragment par) {
		parent = par;
	}

	@SuppressLint("NewApi")
	private void init() {
		mBytes = null;
		mFFTBytes = null;

		//mFlashPaint.setColor(Color.argb(122, 255, 255, 255));
		mFadePaint.setColor(Color.argb(100, 255, 255, 255)); // Adjust alpha to
																// change how
																// quickly the
																// image
																// fades
		// mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));

		mRenderers = new HashSet<>();
		mVisualizer = new Visualizer(0);
		mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		// mVisualizer.
		// transPainter = new Paint();
		// transPainter.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
	}

	/**
	 * Links the visualizer to a player
	 *
	 *            - MediaPlayer instance to link to
	 */

	@SuppressLint("NewApi")
	public void link(final boolean multicast) {
		// Create the Visualizer object and attach it to our media player.

		// mVisualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);

		// Pass through Visualizer data to VisualizerView
		Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
			@Override
			public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
				updateVisualizer(bytes);
				Log.w(Constants.TAG, "should not run this");
			}

			@Override
			public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
				byte[] copy = new byte[bytes.length / 2];
                System.arraycopy(bytes, 0, copy, 0, copy.length);
				updateVisualizerFFT(copy);
				sendSoulissDario(copy,multicast);
			}
		};
		int dcRate = Visualizer.getMaxCaptureRate();
		if (dcRate < 30000) {
			Log.w(Constants.TAG, "MAXDCRATE invalid, defaulting to:" + dcRate);
		} else {
			dcRate = 30000; // 15kHz
		}
		mVisualizer.setDataCaptureListener(captureListener, dcRate, false, true);
		// Enabled Visualizer and disable when we're done with the stream
		mVisualizer.setEnabled(true);

	}

	// non devono essere azzerate ad ogni iterazione!!!

	float absMax_low = 300;
	float absMax_med = 300;
	float absMax_high = 300;

	int k_low = 30;
	int k_med = 1;
	int k_high = 1;
	/**
	 * Magie di Dario.
	 * Dalla trasformata di fourier a tre colori
	 * 
	 * @param data FFT
	 * @param multicast se inviare a tutti
	 */
	private void sendSoulissDario(byte[] data, boolean multicast) {

		int mDivisions = 3;
		float dbValue_low = 1;
		float dbValue_medium = 1;
		float dbValue_high = 1;
		int divisions = data.length / mDivisions - 1;

		for (int i = 0; i < divisions / 2; i++) {// half part is imaginary

			byte rfk = data[2 * i];
			byte ifk = data[2 * (i + 1)];
			float magnitude_low = (rfk * rfk + ifk * ifk);
			dbValue_low += magnitude_low;

			// MEDI
			rfk = data[divisions + 2 * i];
			ifk = data[divisions + 2 * (i + 1)];
			float magnitude_med = (rfk * rfk + ifk * ifk);
			dbValue_medium += magnitude_med;

			// ALTI
			rfk = data[2 * divisions + 2 * i];
			ifk = data[2 * divisions + 2 * (i + 1)];
			float magnitude_high = (rfk * rfk + ifk * ifk);
			dbValue_high += magnitude_high;

		}

		// scaling

		dbValue_low /= k_low;
		dbValue_medium /= k_med;
		dbValue_high /= k_high;

		if (dbValue_high > absMax_high)
			absMax_high = dbValue_high;

		if (dbValue_medium > absMax_med)
			absMax_med = dbValue_medium;

		if (dbValue_low > absMax_low)
			absMax_low = dbValue_low;

		try {
			dbValue_low = 255 * (dbValue_low / absMax_low);
			dbValue_medium = 255 * (dbValue_medium / absMax_med);
			dbValue_high = 255 * (dbValue_high / absMax_high);
		} catch (ArithmeticException e) {

			dbValue_low = 0;
			dbValue_medium = 0;
			dbValue_high = 0;

		}
		dbValue_low *= opz.getEqLow();
		dbValue_medium*= opz.getEqMed();
		dbValue_high*= opz.getEqHigh();
		Log.v(Constants.TAG, "LOW:" + dbValue_low + " MED:" + dbValue_medium + " HI:" + dbValue_high);

		parent.issueIrCommand(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Set, (int) dbValue_low,

		(int) dbValue_medium, (int) dbValue_high, multicast);

	}

	
	public void addRenderer(Renderer renderer) {
		if (renderer != null) {
			mRenderers.add(renderer);
		}
	}

	public void clearRenderers() {
		mRenderers.clear();
	}

	/**
	 * Call to release the resources used by VisualizerView. Like with the
	 * MediaPlayer it is good practice to call this method
	 */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void release() {
		// XXX sposta il disable
		mVisualizer.release();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void setEnabled(boolean in) {
		mVisualizer.setEnabled(in);
	}

	/**
	 * Pass data to the visualizer. Typically this will be obtained from the
	 * Android Visualizer.OnDataCaptureListener call back. See
	 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
	 * 
	 * @param bytes
	 */
	public void updateVisualizer(byte[] bytes) {
		mBytes = bytes;
		invalidate();
	}

	/**
	 * Pass FFT data to the visualizer. Typically this will be obtained from the
	 * Android Visualizer.OnDataCaptureListener call back. See
	 * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
	 * 
	 * @param bytes
	 */
	public void updateVisualizerFFT(byte[] bytes) {
		mFFTBytes = bytes;
		invalidate();
	}

	//boolean mFlash = false;

	/**
	 * Call this to make the visualizer flash. Useful for flashing at the start
	 * of a song/loop etc...
	 */
	/*public void flash() {
		mFlash = true;
		invalidate();
	}*/

	Bitmap mCanvasBitmap;
	Canvas mCanvas;
	FFTData fftData = new FFTData(mFFTBytes);
	AudioData audioData = new AudioData(mBytes);
	Matrix neo = new Matrix();
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Create canvas once we're ready to draw
		mRect.set(0, 0, getWidth(), getHeight());
		if (mCanvasBitmap == null) {
			mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_4444);
		}
		if (mCanvas == null) {
			mCanvas = new Canvas(mCanvasBitmap);
		}

		// mCanvas.drawRect(0, 0,mCanvasBitmap.getWidth(),
		// mCanvasBitmap.getHeight(), transPainter);
		canvas.drawBitmap(mCanvasBitmap, neo, null);

		// Fade out old contents
		mCanvas.drawPaint(mFadePaint);

		if (mBytes != null) {
			// Render all audio renderers
			//AudioData audioData = new AudioData(mBytes);
			audioData.bytes = mBytes;
			for (Renderer r : mRenderers) {
				r.render(mCanvas, audioData, mRect);
			}
		}

		if (mFFTBytes != null) {
			// Render all FFT renderers
			//FFTData fftData = new FFTData(mFFTBytes);
			fftData.bytes = mFFTBytes;
			for (Renderer r : mRenderers) {
				r.render(mCanvas, fftData, mRect);
			}
		}

		/*if (mFlash) {
			mFlash = false;
			mCanvas.drawPaint(mFlashPaint);
		}*/

	}

	public SoulissPreferenceHelper getOpz() {
		return opz;
	}

	public void setOpz(SoulissPreferenceHelper opz) {
		this.opz = opz;
	}
}