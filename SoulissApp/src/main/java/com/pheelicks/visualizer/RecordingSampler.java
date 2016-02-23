package com.pheelicks.visualizer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.angelic.soulissclient.Constants;

public class RecordingSampler {
    private static final int RECORDING_SAMPLE_RATE = 8000;
    private AudioTrack audioPlayer;
    private AudioRecord mAudioRecord;
    private int mBufSize;
    private boolean mIsRecording;
    private int mSamplingInterval = 100;
    private Timer mTimer;
    private List<VisualizerView> mVisualizerViews = new ArrayList();
    private RecordingSampler.CalculateVolumeListener mVolumeListener;
    private boolean mulicat;

    public RecordingSampler() {
        this.initAudioRecord();
    }

    private int calculateDecibel(byte[] buf) {
        int sum = 0;

        for (int i = 0; i < this.mBufSize; ++i) {
            sum += Math.abs(buf[i]);
        }

        return sum / this.mBufSize;
    }

    private void initAudioRecord() {
        int bufferSize = AudioRecord.getMinBufferSize(RECORDING_SAMPLE_RATE, 16, AudioFormat.ENCODING_PCM_16BIT);
        this.mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDING_SAMPLE_RATE, 16, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if (this.mAudioRecord.getState() == 1) {
            this.mBufSize = bufferSize;
            Log.i(Constants.TAG, "Buffer size:" + bufferSize);
        }
        this.mTimer = new Timer();

    }

    public boolean isRecording() {
        return this.mIsRecording;
    }

    public void link(VisualizerView visualizerView, final boolean multicast) {
        this.mVisualizerViews.add(visualizerView);
        this.mulicat = multicast;
    }

    public void release() {
        this.stopRecording();
        this.mAudioRecord.release();
        this.mAudioRecord = null;
        this.mTimer = null;
        audioPlayer.release();
    }

    private void runRecording() {

        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDING_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mBufSize, AudioTrack.MODE_STREAM);


        final byte[] buf = new byte[this.mBufSize];
        this.mTimer.schedule(new TimerTask() {
            public void run() {
                if (!RecordingSampler.this.mIsRecording) {
                    RecordingSampler.this.mAudioRecord.stop();
                } else {
                    int readBytes = RecordingSampler.this.mAudioRecord.read(buf, 0, RecordingSampler.this.mBufSize);


                    final int decibel = RecordingSampler.this.calculateDecibel(buf);

                    if (readBytes > 0) {
                        Log.i(Constants.TAG, "decibel " + decibel + "size:" + readBytes);
                        // audioPlayer.write(buf, 0, readBytes);


                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (RecordingSampler.this.mVisualizerViews != null && !RecordingSampler.this.mVisualizerViews.isEmpty()) {
                                    for (int i = 0; i < RecordingSampler.this.mVisualizerViews.size(); ++i) {
                                        RecordingSampler.this.mVisualizerViews.get(i).updateVisualizerFFT(buf);
                                        //  ((VisualizerView) RecordingSampler.this.mVisualizerViews.get(i)).updateVisualizer(buf);
                                        RecordingSampler.this.mVisualizerViews.get(i).sendSoulissPlinio(buf, mulicat);

                                    }
                                }
                            }
                        });
                   /* if (RecordingSampler.this.mVolumeListener != null) {
                        RecordingSampler.this.mVolumeListener.onCalculateVolume(decibel);
                    }*/

                        audioPlayer.play();
                    }
                }
            }
        }, 0L, (long) this.mSamplingInterval);
    }

    public void setSamplingInterval(int samplingInterval) {
        this.mSamplingInterval = samplingInterval;
    }

    public void setVolumeListener(RecordingSampler.CalculateVolumeListener volumeListener) {
        this.mVolumeListener = volumeListener;
    }

    public void startRecording() {
        this.mTimer = new Timer();
        this.mAudioRecord.startRecording();
        this.mIsRecording = true;
        this.runRecording();
    }

    public void stopRecording() {
        this.mIsRecording = false;
        //this.mAudioRecord.stop();
        this.mTimer.cancel();
       /* if (this.mVisualizerViews != null && !this.mVisualizerViews.isEmpty()) {
            for (int i = 0; i < this.mVisualizerViews.size(); ++i) {
                ((VisualizerView) this.mVisualizerViews.get(i)).clearRenderers();
            }
        }*/
        audioPlayer.stop();
    }

    public interface CalculateVolumeListener {
        void onCalculateVolume(int var1);
    }

}
