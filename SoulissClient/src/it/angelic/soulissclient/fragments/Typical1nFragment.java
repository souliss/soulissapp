package it.angelic.soulissclient.fragments;

import static it.angelic.soulissclient.Constants.TAG;
import static it.angelic.soulissclient.typicals.Constants.*;
import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.typicals.SoulissTypical;
import it.angelic.soulissclient.typicals.SoulissTypical11;
import it.angelic.soulissclient.typicals.SoulissTypical12;
import it.angelic.soulissclient.typicals.SoulissTypical16AdvancedRGB;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.pheelicks.visualizer.VisualizerView;

public class Typical1nFragment extends Fragment {
	private SoulissDBHelper datasource = new SoulissDBHelper(SoulissClient.getAppContext());
	private SoulissPreferenceHelper opzioni;

	private Button buttPlus;

	private SoulissTypical collected;
	// private SoulissTypical related;
	private SeekBar timer;
	private Button btSleep;

	// Color change listener.

	private VisualizerView mVisualizerView;
	private TextView timerInfo;
	private Button buttAuto;
	private TextView autoInfo;
	private TextView infoTyp;
	private ToggleButton togMassive;

	/**
	 * Interface describing a color change listener.
	 */
	public interface OnColorChangedListener {
		/**
		 * Method colorChanged is called when a new color is selected.
		 * 
		 * @param color
		 *            new color.
		 */
		void colorChanged(int color);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		opzioni = SoulissClient.getOpzioni();
		// tema
		if (opzioni.isLightThemeSelected())
			getActivity().setTheme(R.style.LightThemeSelector);
		else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(getActivity());
		}

	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null)
			return null;
		opzioni = SoulissClient.getOpzioni();
		View ret = inflater.inflate(R.layout.frag_t1n, container, false);
		datasource = new SoulissDBHelper(getActivity());
		datasource.open();

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && extras.get("TIPICO") != null) {
			collected = (SoulissTypical) extras.get("TIPICO");
		} else if (getArguments() != null) {
			collected = (SoulissTypical) getArguments().get("TIPICO");
		} else {
			Log.e(Constants.TAG, "Error retriving node:");
			return ret;
		}
		assertTrue("TIPICO NULLO", collected instanceof SoulissTypical);
		collected.setPrefs(opzioni);
		collected.setCtx(getActivity());
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(collected.getNiceName());
		}

		buttPlus = (Button) ret.findViewById(R.id.buttonPlus);
		buttAuto = (Button) ret.findViewById(R.id.buttonAuto);
		timer = (SeekBar) ret.findViewById(R.id.sleepBar);
		timerInfo = (TextView) ret.findViewById(R.id.textviewTimerInfo);
		autoInfo = (TextView) ret.findViewById(R.id.textviewAutoInfo);
		btSleep = (Button) ret.findViewById(R.id.sleep);
		infoTyp = (TextView) ret.findViewById(R.id.textView1nInfo);
		togMassive = (ToggleButton) ret.findViewById(R.id.buttonMassive);
		mVisualizerView = (VisualizerView) ret.findViewById(R.id.visualizerView);
		buttPlus.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_BrightUp);
		infoTyp.setText(collected.getParentNode().getNiceName() + ", slot " + collected.getTypicalDTO().getSlot());
		btSleep.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T_related);
		// Listener generico
		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				Short cmd = (Short) v.getTag();
				if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OnCoil) {
					shutoff();
				} else if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OffCoil) {
					turnOn(0);
				} else {
					Log.e(Constants.TAG, "OUTPUT Error");
				}
				assertTrue(cmd != null);
				return;
			}

		};
		buttPlus.setOnClickListener(plus);

		buttAuto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						if (togMassive.isChecked())
							UDPHelper.issueMassiveCommand(""+collected.getTypicalDTO().getTypical(), opzioni,
									String.valueOf(Souliss_T1n_AutoCmd));
						else
						UDPHelper.issueSoulissCommand("" + collected.getTypicalDTO().getNodeId(), ""
								+ collected.getTypicalDTO().getSlot(), opzioni,
								it.angelic.soulissclient.Constants.COMMAND_SINGLE, String.valueOf(Souliss_T1n_AutoCmd));
					}
				};

				t.start();
				Toast.makeText(
						getActivity(),
						getActivity().getString(R.string.Souliss_AutoCmd_desc) + " "
								+ getActivity().getString(R.string.command_sent), Toast.LENGTH_SHORT).show();
			}
		});

		OnClickListener plusSlip = new OnClickListener() {
			public void onClick(View v) {
				turnOn(timer.getProgress());
				return;
			}
		};
		btSleep.setOnClickListener(plusSlip);

		if (collected instanceof SoulissTypical12) {
			btSleep.setVisibility(View.GONE);
			timer.setVisibility(View.GONE);
			timerInfo.setVisibility(View.GONE);
			// Check AUTO mode
			if (collected.getOutputDesc().contains("auto"))
				autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " ON");
			else
				autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " OFF");
		} else if (collected instanceof SoulissTypical11) {
			buttAuto.setVisibility(View.GONE);
			autoInfo.setVisibility(View.GONE);
		}
		// sfondo bottone
		if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OnCoil
				|| collected.getTypicalDTO().getOutput() == Souliss_T1n_OnCoil_Auto)
			buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.bulb_on));
		else if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OffCoil
				|| collected.getTypicalDTO().getOutput() == Souliss_T1n_OffCoil_Auto)
			buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.bulb_off));
		return ret;
	}

	private void shutoff() {
		Thread t = new Thread() {
			public void run() {
				Looper.prepare();
				if (togMassive.isChecked())
					UDPHelper.issueMassiveCommand(""+collected.getTypicalDTO().getTypical(), opzioni,
							""+(Souliss_T1n_OffCmd));
				else
				UDPHelper.issueSoulissCommand("" + collected.getParentNode().getId(), ""
						+ collected.getTypicalDTO().getSlot(), opzioni, Constants.COMMAND_SINGLE, ""
						+ (Souliss_T1n_OffCmd));

			}
		};
		t.start();
		Toast.makeText(getActivity(),
				getActivity().getString(R.string.TurnOFF) + " " + getActivity().getString(R.string.command_sent),
				Toast.LENGTH_SHORT).show();
		return;

	}

	private void turnOn(final int i) {
		Thread t = new Thread() {
			public void run() {
				Looper.prepare();

				if (togMassive.isChecked())
					UDPHelper.issueMassiveCommand(""+collected.getTypicalDTO().getTypical(), opzioni,
							""+(Souliss_T1n_OnCmd + i));
				else
					UDPHelper.issueSoulissCommand("" + collected.getParentNode().getId(), ""
							+ collected.getTypicalDTO().getSlot(), opzioni, Constants.COMMAND_SINGLE, ""
							+ (Souliss_T1n_OnCmd + i));

			}
		};

		t.start();
		Toast.makeText(getActivity(),
				getActivity().getString(R.string.TurnON) + " " + getActivity().getString(R.string.command_sent),
				Toast.LENGTH_SHORT).show();
		return;

	}

	public static Typical1nFragment newInstance(int index, SoulissTypical content) {
		Typical1nFragment f = new Typical1nFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		// Ci metto il nodo dentro
		if (content != null) {
			args.putSerializable("TIPICO", (SoulissTypical) content);
		}
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// inflater.inflate(R.menu.queue_options, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				NodeDetailFragment details = NodeDetailFragment.newInstance(collected.getTypicalDTO().getNodeId(),
						collected.getParentNode());
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				if (opzioni.isAnimationsEnabled())
					ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
				ft.replace(R.id.details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
				ft.commit();
			} else {
				getActivity().finish();
				if (opzioni.isAnimationsEnabled())
					getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		datasource.open();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction("it.angelic.soulissclient.GOT_DATA");
		filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		getActivity().registerReceiver(datareceiver, filtere);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(datareceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// datasource.close();
		if (mVisualizerView != null)
			mVisualizerView.release();
	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Log.i(Constants.TAG, "Broadcast received, intent" + intent.toString());
				datasource.open();
				SoulissNode coll = datasource.getSoulissNode(collected.getTypicalDTO().getNodeId());
				collected = coll.getTypical(collected.getTypicalDTO().getSlot());
				if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OnCoil)
					buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.bulb_on));
				else if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OffCoil)
					buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.bulb_off));
				else if (collected.getTypicalDTO().getOutput() >= Souliss_T1n_Timed) {
					timer.setProgress(collected.getTypicalDTO().getOutput());
					buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.bulb_on));
					timerInfo.setText("Cycles to shutoff: " + collected.getTypicalDTO().getOutput());
				} else {
					Log.w(Constants.TAG, "Unknown status");
				}
				// datasource.close();
			} catch (Exception e) {
				Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
			}
		}
	};

}
