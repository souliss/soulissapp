package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.HalfFloatUtils;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypicalSensor;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Occupa DUE slot, quindi l'output viene dal suo e dal suo fratello destro
 * 
 * @author Ale
 * 
 */
public class SoulissTypical58PressureSensor extends SoulissTypical implements ISoulissTypicalSensor {

	public SoulissTypical58PressureSensor(SoulissPreferenceHelper pre) {
		super(pre);
	}

	int maxTemp;
	int minTemp;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3784476625112669L;

	@Override
	public String getOutputDesc() {
		if (Calendar.getInstance().getTime().getTime() - typicalDTO.getRefreshedAt().getTime().getTime() < (prefs.getDataServiceIntervalMsec()*3))
			return SoulissClient.getAppContext().getString(R.string.ok);
		else
			return SoulissClient.getAppContext().getString(R.string.stale);
	}

	@Override
	public void getActionsLayout(Context ctx,LinearLayout cont) {
		WindowManager mWinMgr = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
		cont.removeAllViews();
		final TextView cmd = new TextView(ctx);

		cmd.setText(Html.fromHtml("<b>Reading:</b> " + getOutputPascal()));
		if (prefs.isLightThemeSelected())
			cmd.setTextColor(ctx.getResources().getColor(R.color.black));
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		cmd.setLayoutParams(lp);
		lp.setMargins(2, 0, 0, 2);
		//cmd.setGravity(Gravity.TOP);
		cont.addView(cmd);

		ProgressBar par = new ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal);
		// ProgressBar sfumata
		final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(Constants.roundedCorners, null, null));
		final LinearGradient gradient = new LinearGradient(0, 0, displayWidth / 2, 0, ctx.getResources().getColor(
				color.black), ctx.getResources().getColor(color.white), android.graphics.Shader.TileMode.CLAMP);
		pgDrawable.getPaint().setStrokeWidth(3);
		pgDrawable.getPaint().setDither(true);
		pgDrawable.getPaint().setShader(gradient);

		ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		par.setProgressDrawable(progress);
		par.setBackgroundDrawable(ctx.getResources().getDrawable(android.R.drawable.progress_horizontal));
		
		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		par.setLayoutParams(lp2);
		par.setMax(50);
		par.setProgress(20);
		par.setProgress(0);
		par.setMax(255);
		par.setProgress((int) getOutputFloat());

		cont.addView(par);

	}

	private String getOutputPascal() {
		return Constants.twoDecimalFormat.format(getOutputFloat()) + " hPa";
	}

	@Override
	public float getOutputFloat() {
		int miofratello = getParentNode().getTypical((short) (typicalDTO.getSlot() + 1))
				.getTypicalDTO().getOutput();
		// ora ho i due bytes, li converto
		int shifted = miofratello << 8;
		Log.d(Constants.TAG,
				"first:" + Long.toHexString((long) typicalDTO.getOutput()) + " second:"
						+ Long.toHexString((long) miofratello) + " PRESSURE SENSOR Reading:"
						+ Long.toHexString((long) shifted + typicalDTO.getOutput()));

		return HalfFloatUtils.toFloat(shifted + typicalDTO.getOutput());
	}
}
