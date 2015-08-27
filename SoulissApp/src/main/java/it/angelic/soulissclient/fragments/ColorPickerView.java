package it.angelic.soulissclient.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;

/**
 * Inner class representing the color chooser.
 */
public class ColorPickerView extends View {
    // dimensioni pannello colore
    private static final int CENTER_RADIUS = 50;
    private static final float STROKE_WIDTH = 48;
    private final int[] colors = new int[]{0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000};
    private Paint paint = null;
    private Paint centerPaint = null;
    // private boolean trackingCenter = false;
    private RectF swapRect = new RectF();

    private SoulissTypical16AdvancedRGB collected;
    private RelativeLayout colorSwitchRelativeLayout;
    private T16RGBAdvancedFragment.OnColorChangedListener dialogColorChangedListener;

    /**
     * @param context
     * @param listener
     * @param color
     */
    ColorPickerView(Context context, T16RGBAdvancedFragment.OnColorChangedListener listener, int color, RelativeLayout canv, SoulissTypical16AdvancedRGB typ) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new SweepGradient(0, 0, colors, null));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(color);
        centerPaint.setStrokeWidth(5);

        dialogColorChangedListener = listener;
        colorSwitchRelativeLayout = canv;
        collected = typ;
    }

    public void setCenterColor(int colorIn) {
        centerPaint.setColor(colorIn);
    }

    /**
     * {@inheritDoc}
     */
    protected void onDraw(Canvas canvas) {

        int centerX = colorSwitchRelativeLayout.getWidth() / 2;
        float r = (centerX - paint.getStrokeWidth()) / 2;

        canvas.translate(centerX, r + STROKE_WIDTH);
        swapRect.set(-r, -r, r, r);
        canvas.drawOval(swapRect, paint);
        canvas.drawCircle(0, 0, CENTER_RADIUS, centerPaint);

    }

    @SuppressLint("NewApi")
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Log.d(Constants.TAG, "vis CHANGE");
        setCenterColor(Color.argb(255, Color.red(collected.getColor()), Color.green(collected.getColor()),
                Color.blue(collected.getColor())));
        invalidate();
    }

    /**
     * {@inheritDoc}
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = colorSwitchRelativeLayout.getMeasuredWidth();

        if (width == 0) {
            Log.e(Constants.TAG, "Couldn't measure View");
        }
        int centerX = width / 2;
        float r = (centerX) + STROKE_WIDTH;

        //h = (int) swapRect.height();
        setMeasuredDimension(width, (int)r);
    }

    /**
     * {@inheritDoc}
     */
    public boolean onTouchEvent(MotionEvent event) {
        // remove offset
        int centerX = colorSwitchRelativeLayout.getWidth() / 2;
        float r = (centerX - paint.getStrokeWidth()) / 2;
        float x = event.getX() - centerX;
        float y = event.getY() - (r + STROKE_WIDTH);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float angle = (float) java.lang.Math.atan2(y, x);
                // need to turn angle [-PI ... PI] into unit [0....1]
                float unit = (float) (angle / (2 * Math.PI));

                if (unit < 0) {
                    unit += 1;
                }
                // centerPaint.setColor(interpColor(colors, unit));
                // fa inviare il comando ir
                dialogColorChangedListener.colorChanged(interpColor(colors, unit));
                break;
            case MotionEvent.ACTION_UP:
                collected.issueRefresh();// change center color
                // ColorDialogPreference.this.color = centerPaint.getColor();
                break;
        }
        invalidate();
        return true;
    }

    /**
     * @param colors
     * @param unit
     * @return
     */
    private static int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }

        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    /**
     * @param s
     * @param d
     * @param p
     * @return
     */
    private static int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }
}