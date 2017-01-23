package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.HalfFloatUtils;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypicalSensor;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Occupa DUE slot, quindi l'output viene dal suo e dal suo fratello destro (66)
 *
 * @author Ale
 */
public class SoulissTypical6nAnalogue extends SoulissTypical implements ISoulissTypicalSensor {

    public SoulissTypical6nAnalogue(Context c, SoulissPreferenceHelper pre) {
        super(c, pre);
    }

    /**
     *
     */
    private static final long serialVersionUID = 3784476625375361669L;

    @Override
    public String getOutputDesc() {
        if (Calendar.getInstance().getTime().getTime() - typicalDTO.getRefreshedAt().getTime().getTime() < (prefs.getDataServiceIntervalMsec() * 3))
            return "" + String.format("%.2f", getOutputFloat());
        else
            return context.getString(R.string.stale);

    }

    /**
     * La conversione del half fp si basa su HalfFloatUtils.toFloat
     */
    @Override
    public float getOutputFloat() {
        int miofratello = getParentNode().getTypical((short) (typicalDTO.getSlot() + 1)).getTypicalDTO().getOutput();
        //ora ho i due bytes, li converto
        int shifted = miofratello << 8;
        Log.i(Constants.TAG, "first:" + Long.toHexString((long) typicalDTO.getOutput()) + " second:" + Long.toHexString((long) miofratello) + "T6n Reading:" + Long.toHexString((long) shifted + typicalDTO.getOutput()));

        //return HalfFloatUtils.toFloat(shifted + typicalDTO.getOutput());
        //non voglio usare DecimalFormat
        //float temp1 = (float) (HalfFloatUtils.toFloat(shifted + typicalDTO.getOutput())*100.0);
        //float temp2 = Math.round(temp1*100.0);//(round to nearest value)
        //return (float) (Math.round(temp2*100.0)/100.0);
        return HalfFloatUtils.toFloat(shifted + typicalDTO.getOutput());

    }

    @Override
    public void getActionsLayout(Context ctx, final LinearLayout contLinear) {
        WindowManager mWinMgr = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
        contLinear.removeAllViews();
        final TextView cmd = new TextView(ctx);
        cmd.setText(Html.fromHtml(ctx.getString(R.string.reading) + ": " + getOutputFloat()));
        if (prefs.isLightThemeSelected())
            cmd.setTextColor(ctx.getResources().getColor(color.black));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        cmd.setLayoutParams(lp);
        lp.setMargins(2, 0, 0, 2);
        //cmd.setGravity(Gravity.TOP);
        contLinear.addView(cmd);

    }
}
