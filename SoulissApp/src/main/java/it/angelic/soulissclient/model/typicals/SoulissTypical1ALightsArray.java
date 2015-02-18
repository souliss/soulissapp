package it.angelic.soulissclient.model.typicals;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * Handle one digital output based on hardware and software commands, output can
 * be timed out.
 * 
 * This logic can be used for lights, wall socket and all the devices that has
 * an ON/OFF behavior.
 * 
 * @author Ale
 * 
 */
public class SoulissTypical1ALightsArray extends SoulissTypical implements ISoulissTypical {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553488985062232592L;

	// Context ctx;

	public SoulissTypical1ALightsArray(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// NO COMMANDS
		ArrayList<SoulissCommand> ret = new ArrayList<>();

		return ret;

	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 */
	@Override
	public void getActionsLayout(Context ctx, final LinearLayout cont) {
		cont.removeAllViews();
		// NO ACTIONS
	}

	/**
	 * Indicates if n-th bit is set
	 * @param bitNum
	 * @return
	 */
	public boolean isnThOn(int bitNum) {
		return ((getTypicalDTO().getOutput() & (1L << bitNum)) != 0);
	}

	public @ColorRes int bitColor(int bitNum) {
		if (isnThOn(bitNum))
			return SoulissClient.getAppContext().getResources().getColor(R.color.std_green);
		else
			return SoulissClient.getAppContext().getResources().getColor(R.color.std_red);
	}

	@Override
	public void setOutputDescView(TextView textStatusVal) {
		textStatusVal.setText(getOutputDesc());

		TextView vw = textStatusVal;
		// ■
		// Set the EditText's text.
		//vw.setText(" ■ ■ ■ ■\n ■ ■ ■ ■");

		// If this were just a TextView, we could do:
		// vw.setText("Italic, highlighted, bold.",
		// TextView.BufferType.SPANNABLE);
		// to force it to use Spannable storage so styles can be attached.
		// Or we could specify that in the XML.

		// Get the EditText's internal text storage
		Spannable str = new SpannableString("\u25A0\u25A0\u25A0\u25A0\u25A0\u25A0\u25A0\u25A0");
		int onCol = R.color.std_green;
		int offCol = R.color.std_red;
		// Create our span sections, and assign a format to each.
		// str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 7,
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// str.setSpan(new BackgroundColorSpan(0xFFFFFF00), 8, 19,
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		for (int i = 0; i < 8; i++) {
			str.setSpan(new ForegroundColorSpan(bitColor(i)), i, i  + 1,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			// cmd.setVisibility(View.GONE);
		}
		// str.setSpan(new ForegroundColorSpan(offCol), 6, 7, 0);
		/*LayoutParams params = textStatusVal.getLayoutParams();
		params.height += 2;
		textStatusVal.setLayoutParams(params);*/
		textStatusVal.setGravity(Gravity.TOP);
		textStatusVal.setBackgroundResource(0);
		textStatusVal.setText(str, BufferType.SPANNABLE);
		textStatusVal.setTextSize(TypedValue.COMPLEX_UNIT_PX,SoulissClient.getAppContext().getResources().getDimensionPixelSize(R.dimen.text_size_vbig) );
		//textStatusVal.setBackgroundResource(R.drawable.borderedbackon);
	}

	@Override
	public String getOutputDesc() {
		if (typicalDTO.getOutput() == Constants.Souliss_T1n_OnCoil
				|| typicalDTO.getOutput() == Constants.Souliss_T1n_OnFeedback)
			return SoulissClient.getAppContext().getString(R.string.ON);
		else if (typicalDTO.getOutput() == Constants.Souliss_T1n_OffCoil
				|| typicalDTO.getOutput() == Constants.Souliss_T1n_OffFeedback)
			return SoulissClient.getAppContext().getString(R.string.OFF);
		else if (typicalDTO.getOutput() >= Constants.Souliss_T1n_Timed)
			return "" + typicalDTO.getOutput();
		// return ctx.getString(R.string.Souliss_TRGB_sleep);
		else
			return "UNKNOWN";
	}

}
