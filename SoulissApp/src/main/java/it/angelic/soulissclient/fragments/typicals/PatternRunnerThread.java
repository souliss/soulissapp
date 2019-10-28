package it.angelic.soulissclient.fragments.typicals;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Button;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.util.CCFAnimator;

/**
 * Created by shine@angelic.it on 13/05/2017.
 */
public class PatternRunnerThread extends Thread {
    private int color = 0;
    private int colorFrom = 0;
    private Activity context;

    public int getSliderSpeed() {
        return sliderSpeed;
    }

    public void setSliderSpeed(int sliderSpeed) {
        this.sliderSpeed = sliderSpeed;
    }

    private int sliderSpeed = 0;
    private int colorTo = 0;

    public boolean isScatterMode() {
        return scatterMode;
    }

    public void setScatterMode(boolean scatterMode) {
        this.scatterMode = scatterMode;
    }

    public boolean isMulticastMode() {
        return multicastMode;
    }

    public void setMulticastMode(boolean multicastMode) {
        this.multicastMode = multicastMode;
    }

    boolean scatterMode = false;
    boolean multicastMode = false;
    boolean patternRunning = false;

    private Button btFeedBackPatern;
    private SoulissTypical16AdvancedRGB collected;
    private CCFAnimator patternAnimator;

    PatternRunnerThread() {
        super();
    }

    PatternRunnerThread(Activity ctx, SoulissTypical16AdvancedRGB coll, Button feedback) {
        super();
        collected = coll;
        patternAnimator = CCFAnimator.rgb(colorFrom, colorTo);
        btFeedBackPatern = feedback;
        context = ctx;
    }

    public int getColorFrom() {
        return colorFrom;
    }

    public void setColorFrom(int colorFrom) {
        this.colorFrom = colorFrom;
        patternAnimator = CCFAnimator.rgb(colorFrom, colorTo);
    }

    public int getColorTo() {
        return colorTo;
    }

    public void setColorTo(int colorTo) {
        this.colorTo = colorTo;
        patternAnimator = CCFAnimator.rgb(colorFrom, colorTo);
    }

    public boolean isPatternRunning() {
        return patternRunning;
    }

    public void setPatternRunning(boolean patternRunning) {
        this.patternRunning = patternRunning;
    }

    @Override
    public void run() {
        int iterCount = 0;
        float cnt = 0f;
        float step = 0.01f;
        boolean goinUp = true;
        while (patternRunning) {
            iterCount++;
            try {
                if (scatterMode)
                    cnt = Constants.random.nextFloat();
                //digital filter
                if (cnt > 1.0f)
                    cnt = 1;
                if (cnt < 0)
                    cnt = 0;
                color = patternAnimator.getColor(cnt);
                //varia il colore solo ogni tot
                if (iterCount % (11 - sliderSpeed / 10) == 0) {
                    if (goinUp)
                        cnt += step;
                    else
                        cnt -= step;

                    collected.issueRGBCommand(Constants.Typicals.Souliss_T1n_Set,
                            Color.red(color), Color.green(color), Color.blue(color), multicastMode);
                    Log.d(Constants.TAG, "dialogColorChangedListener, pattern color asc: " + goinUp
                            + " cnt=" + cnt);
                    if (btFeedBackPatern != null) {//could be detached
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                btFeedBackPatern
                                        .setBackgroundColor(color);
                            }
                        });
                    }
                    if (cnt >= 1)
                        goinUp = false;
                    else if (cnt <= 0)
                        goinUp = true;
                    //slider max = 255
                }
                //do respiro almeno 50msec
                Thread.sleep(150 - sliderSpeed);
            } catch (InterruptedException e) {
                Log.e(Constants.TAG, "Error Thread.sleep:");
            }
        }
    }
}
