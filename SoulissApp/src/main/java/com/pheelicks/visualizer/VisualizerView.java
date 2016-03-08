/**
 * Copyright 2011, Felix Palmer
 * <p/>
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.pheelicks.visualizer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaRecorder;
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
public class
        VisualizerView extends View {

    float absMax_low = 300;
    float absMax_med = 300;
    float absMax_high = 300;
    int k_low = 30;
    int k_med = 1;
    int k_high = 1;
    Bitmap mCanvasBitmap;
    Canvas mCanvas;
    Matrix neo = new Matrix();

    // private Paint transPainter;
    private byte[] mBytes;
    AudioData audioData = new AudioData(mBytes);
    private byte[] mFFTBytes;
    FFTData fftData = new FFTData(mFFTBytes);
    //private Paint mFlashPaint = new Paint();
    private Paint mFadePaint = new Paint();
    private Rect mRect = new Rect();

    // non devono essere azzerate ad ogni iterazione!!!
    private Set<Renderer> mRenderers;
    private Visualizer mVisualizer;
    private SoulissPreferenceHelper opz;
    private AbstractMusicVisualizerFragment parent;
    private RecordingSampler recordingSampler;

    public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public VisualizerView(Context context) {
        this(context, null, 0);
    }

    public void addRenderer(Renderer renderer) {
        if (renderer != null) {
            mRenderers.add(renderer);
        }
    }

    public void clearRenderers() {
        mRenderers.clear();
    }

    public SoulissPreferenceHelper getOpz() {
        return opz;
    }

    public void setOpz(SoulissPreferenceHelper opz) {
        this.opz = opz;
    }

    private void init() {
        mBytes = null;
        mFFTBytes = null;

        //mFlashPaint.setColor(Color.argb(122, 255, 255, 255));
        mFadePaint.setColor(Color.argb(100, 255, 255, 255)); // Adjust alpha to

        mRenderers = new HashSet<>();


        mVisualizer = new Visualizer(0);
        Log.w(Constants.TAG, "SetCapture Size (FFT MINRANGE):" + mVisualizer.getCaptureSize());
        recordingSampler = new RecordingSampler();

        // transPainter = new Paint();
        // transPainter.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
    }

    /**
     * Links the visualizer to a player
     * <p/>
     * - MediaPlayer instance to link to
     */

    public void link(final boolean multicast) {
        // Create the Visualizer object and attach it to our media player.
        if (opz.getAudioInputChannel() == MediaRecorder.AudioSource.MIC) {
            Log.w(Constants.TAG, "MIC input selected");
            // recordingSampler.setVolumeListener(this);  // for custom implements
            recordingSampler.setSamplingInterval(100); // voice sampling interval
            recordingSampler.link(this, multicast);// link to visualizer


            // mVisualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        } else {
            Log.w(Constants.TAG, "default audio input selected");
            // Pass through Visualizer data to VisualizerView
            Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    byte[] copy = new byte[bytes.length / 2];
                    System.arraycopy(bytes, 0, copy, 0, copy.length);
                    updateVisualizerFFT(copy);
                    sendSoulissPlinio(copy, multicast);
                }

                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    updateVisualizer(bytes);
                    Log.w(Constants.TAG, "should not run this");
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
    }

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
    }

    /**
     * Call to release the resources used by VisualizerView. Like with the
     * MediaPlayer it is good practice to call this method
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void release() {
        // XXX sposta il disable
        mVisualizer.release();
        recordingSampler.release();
    }

    /**
     * Magie di Dario.
     * Dalla trasformata di fourier a tre colori
     *
     * @param data      FFT
     * @param multicast se inviare a tutti
     */
    public void sendSoulissPlinio(byte[] data, boolean multicast) {

        float dbValue_low = 1;
        float dbValue_medium = 1;
        float dbValue_high = 1;
        Log.v(Constants.TAG, "data.length:" + data.length);
        float low_freq_slider = opz.getEqLowRange() * data.length / 2;
        float med_freq_slider = opz.getEqMedRange() * data.length / 2;
        float high_freq_slider = opz.getEqHighRange() * data.length / 2;
        //Log.v(Constants.TAG, "low_freq_slider:" + low_freq_slider+"med_freq_slider:" + med_freq_slider+"hig_freq_slider:" + high_freq_slider);
        for (int i = 0; i < data.length / 2 - 1; i++) {// half part is imaginary
            byte rfk, ifk;
            if ((i > low_freq_slider / 2) && (i < low_freq_slider)) {
                rfk = data[2 * i];
                ifk = data[2 * (i + 1)];
                float magnitude_low = (rfk * rfk + ifk * ifk);
                dbValue_low += magnitude_low;
            }
            if ((i > med_freq_slider / 2) && (i < med_freq_slider)) {
                // MEDI
                rfk = data[2 * i];
                ifk = data[2 * (i + 1)];
                float magnitude_med = (rfk * rfk + ifk * ifk);
                dbValue_medium += magnitude_med;
            }
            if ((i > high_freq_slider / 2) && (i < high_freq_slider)) {
                // ALTI
                rfk = data[2 * i];
                ifk = data[2 * (i + 1)];
                float magnitude_high = (rfk * rfk + ifk * ifk);
                dbValue_high += magnitude_high;
            }
        }
        Log.v(Constants.TAG, "RAWLOW:" + dbValue_low + " MED:" + dbValue_medium + " HI:" + dbValue_high);

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
        dbValue_medium *= opz.getEqMed();
        dbValue_high *= opz.getEqHigh();
        Log.v(Constants.TAG, "LOW:" + dbValue_low + " MED:" + dbValue_medium + " HI:" + dbValue_high);

        parent.issueIrCommand(Constants.Typicals.Souliss_T1n_Set, (int) dbValue_low,

                (int) dbValue_medium, (int) dbValue_high, multicast);

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void setEnabled(boolean in) {
        try {
            mVisualizer.setEnabled(in);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Errore setEnabled:" + e.getMessage());
        }
        //gestione microfono
        if (recordingSampler != null && opz.getAudioInputChannel() == MediaRecorder.AudioSource.MIC) {
            if (!in && recordingSampler.isRecording())
                recordingSampler.stopRecording();
            else if (!recordingSampler.isRecording())
                recordingSampler.startRecording();
        }

    }

    public void setFrag(AbstractMusicVisualizerFragment par) {
        parent = par;
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
}