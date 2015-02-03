package it.angelic.soulissclient.model.typicals;

import it.angelic.soulissclient.R;
/**
 * Not used anymore, showed an input slider to control analogue Input
 */
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.helpers.ListButton;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissTypical;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
@Deprecated
public class SoulissTypical5n extends SoulissTypical implements ISoulissTypical {

	/**
	 * Typical 51 : Analog input, unsigned byte Typical 52 : Analog input,
	 * unsigned byte square root mean value Typical 53 : Analog input, unsigned
	 * byte max iterative mean value
	 */
	private static final long serialVersionUID = 4553488325062232092L;
	private transient ListButton turnOnButton;
	private int transientVal;
	private transient SeekBar bar;

	// Context ctx;

	public SoulissTypical5n(SoulissPreferenceHelper fg) {
		super(fg);
	}

	@Override
	public ArrayList<SoulissCommand> getCommands(Context ctx) {
		// ritorna le bozze dei comandi, da riempire con la schermata addProgram
		ArrayList<SoulissCommand> ret = new ArrayList<SoulissCommand>();

		SoulissCommand t = new SoulissCommand(this);
		t.getCommandDTO().setCommand(Constants.Souliss_T1n_OnCmd);
		t.getCommandDTO().setSlot(getTypicalDTO().getSlot());
		t.getCommandDTO().setNodeId(getTypicalDTO().getNodeId());
		ret.add(t);

		return ret;
	}

	/**
	 * Ottiene il layout del pannello comandi
	 * 
	 * @param ble
	 * @param ctx
	 * @param parentIntent
	 * @param convertView
	 * @param parent
	 */
	@Override
	public void getActionsLayout(final TypicalsListAdapter ble, Context ctx, final Intent parentIntent,
			View convertView, final ViewGroup parent) {
		LinearLayout cont = (LinearLayout) convertView.findViewById(R.id.linearLayoutButtons);
		cont.removeAllViews();

		LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		p1.weight = 0.1f;
		// cmd.setGravity(Gravity.TOP);

		LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		p2.weight = 0.9f;

		// cont.addView(getQuickActionTitle());

		bar = new SeekBar(getCtx());
		bar.setMax(255);
		cont.addView(bar);
		bar.setLayoutParams(p2);
		bar.setProgress(transientVal);

		turnOnButton = new ListButton(ctx);
		// turnOnButton.setText(ctx.getString(R.string.open));
		turnOnButton.setLayoutParams(p1);
		cont.addView(turnOnButton);
		bar.setOnSeekBarChangeListener(new analogInputListener());
		turnOnButton.setText("" + transientVal);
		turnOnButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				turnOnButton.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
						UDPHelper.issueSoulissCommand("" + getTypicalDTO().getNodeId(), "" + typicalDTO.getSlot(),
								prefs, it.angelic.soulissclient.Constants.COMMAND_SINGLE, "" + transientVal);
					}
				};
				t.start();
			}

		});
	}

	@Override
	public String getOutputDesc() {
		return String.valueOf(getTypicalDTO().getOutput());
	}

	private class analogInputListener implements SeekBar.OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			// set textView's text
			turnOnButton.setText("" + progress);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			transientVal = bar.getProgress();
		}

	}

	public int getTransientVal() {
		return transientVal;
	}

	public void setTransientVal(int transientVal) {
		this.transientVal = transientVal;
	}
}
