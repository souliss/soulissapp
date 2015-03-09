package it.angelic.soulissclient.model.typicals;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * Occupa DUE slot, quindi l'output viene dal suo e dal suo fratello destro (66)
 * 
 * @author Ale
 * 
 */
@Deprecated
public class SoulissTypicalCurrentSensor extends SoulissTypical {

	public SoulissTypicalCurrentSensor(SoulissPreferenceHelper pre) {
		super(pre);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3784476625375361669L;

	public int getOutputFloat() {
		int miofratello = ((SoulissTypical) getParentNode().getTypical((short) (typicalDTO.getSlot() + 1))).getTypicalDTO().getOutput();

		return (int)10 * ( typicalDTO.getOutput() + (int) miofratello / 100);
	}

	public String getOutputAmperes() {
		int miofratello = ((SoulissTypical) getParentNode().getTypical((short) (typicalDTO.getSlot() + 1))).getTypicalDTO().getOutput();

		return "" + typicalDTO.getOutput() + "." + miofratello;
	}

	@Override
	public String getOutputDesc() {
		if (Calendar.getInstance().getTime().getTime() - typicalDTO.getRefreshedAt().getTime().getTime() < prefs.getDataServiceIntervalMsec() * 4)
			return "OK";
		else
			return "STALE";
	}

	@Override
	public void getActionsLayout( Context ctx, LinearLayout cont) {
		cont.removeAllViews();
		final TextView cmd = new TextView(ctx);
		

		cmd.setText("Reading: " + getOutputFloat()+ "W");
		if (prefs.isLightThemeSelected())
			cmd.setTextColor(ctx.getResources().getColor(R.color.black));
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		cmd.setLayoutParams(lp);
		cmd.setGravity(Gravity.TOP);
		cont.addView(cmd);

		ProgressBar par = new ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal);
		// ProgressBar sfumata
		final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(Constants.roundedCorners, null, null));
		final LinearGradient gradient = new LinearGradient(0, 0, cont.getWidth() / 2, 0, ctx.getResources().getColor(
				color.aa_green), ctx.getResources().getColor(color.aa_red), android.graphics.Shader.TileMode.CLAMP);
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
		par.setMax(3000);//20 amperes
		par.setProgress((int) getOutputFloat());

		cont.addView(par);

	}
}
