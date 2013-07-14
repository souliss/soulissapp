package it.angelic.soulissclient.fragments;

/**
 * Antifurto, deve poter leggere lo stato di tutti i sensori. Il pannello e` unico
 */
import static it.angelic.soulissclient.typicals.Constants.*;
import static it.angelic.soulissclient.typicals.Constants.Souliss_T1n_OffCmd;
import static it.angelic.soulissclient.typicals.Constants.Souliss_T1n_OffCoil;
import static it.angelic.soulissclient.typicals.Constants.Souliss_T1n_OffCoil_Auto;
import static it.angelic.soulissclient.typicals.Constants.Souliss_T1n_OnCmd;
import static it.angelic.soulissclient.typicals.Constants.Souliss_T1n_OnCoil;
import static it.angelic.soulissclient.typicals.Constants.Souliss_T1n_OnCoil_Auto;
import static it.angelic.soulissclient.typicals.Constants.Souliss_T1n_Timed;
import static junit.framework.Assert.assertTrue;

import java.util.List;

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
import it.angelic.soulissclient.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.typicals.SoulissTypical42AntiTheftPeer;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.pheelicks.visualizer.VisualizerView;

public class Typical4nFragment extends SherlockFragment {
	private SoulissDBHelper datasource = new SoulissDBHelper(SoulissClient.getAppContext());
	private SoulissPreferenceHelper opzioni;

	private ToggleButton buttPlus;

	private SoulissTypical collected;
	// private SoulissTypical related;
	private Button btSleep;

	// Color change listener.

	private VisualizerView mVisualizerView;
	private TextView alarmInfoTextView;
	private ToggleButton togMassive;
	private CheckBox notifCheckbox;
	private TextView infoTyp;
	private TextView textviewSensors;
	private Button resetButton;
	private SoulissTypical41AntiTheft senseiMaster;
	private List<SoulissTypical42AntiTheftPeer> sensei;

	public static Typical4nFragment newInstance(int index, SoulissTypical content) {
		Typical4nFragment f = new Typical4nFragment();

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
			getActivity().setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		else
			getActivity().setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
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
		View ret = inflater.inflate(R.layout.frag_t4n, container, false);
		datasource = new SoulissDBHelper(getActivity());
		datasource.open();
		// Il master sara` sempre lo stesso, anche se collected e` un peer
		if (opzioni.isAntitheftPresent()) {
			senseiMaster = datasource.getAntiTheftMasterTypical();
		}

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

		buttPlus = (ToggleButton) ret.findViewById(R.id.buttonPlus);
		alarmInfoTextView = (TextView) ret.findViewById(R.id.textviewAlarmInfo);
		notifCheckbox = (CheckBox) ret.findViewById(R.id.checkBoxnotifAndroid);
		infoTyp = (TextView) ret.findViewById(R.id.textView4nInfo);
		mVisualizerView = (VisualizerView) ret.findViewById(R.id.visualizerView);
		resetButton = (Button) ret.findViewById(R.id.resetButton);
		textviewSensors = (TextView) ret.findViewById(R.id.textviewSensors);

		infoTyp.setText(collected.getParentNode().getNiceName() + ", slot " + collected.getTypicalDTO().getSlot());
		alarmInfoTextView.setText(Html.fromHtml("<b>" + getString(R.string.antitheft_status) + "</b> "
				+ senseiMaster.getOutputDesc()));
		setSensorsView();
		// Listener generico
		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_Antitheft) {
					shutoff();
				} else {
					turnOn(0);
				}
				return;
			}

		};
		buttPlus.setOnClickListener(plus);

		// Listener generico
		OnClickListener resett = new OnClickListener() {
			public void onClick(View v) {
				//if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_InAlarm) {
					reset();
				//}
				return;
			}

		};
		resetButton.setOnClickListener(resett);

		notifCheckbox.setChecked(opzioni.isAntitheftNotify());
		notifCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				opzioni.setAntitheftNotify(isChecked);
			}
		});
		/*
		 * if (collected instanceof SoulissTypical12) {
		 * btSleep.setVisibility(View.GONE);
		 * alarmInfoTextView.setVisibility(View.GONE); // Check AUTO mode if
		 * (collected.getOutputDesc().contains("AUTO"))
		 * autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " ON"); else
		 * autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " OFF"); }
		 * else if (collected instanceof SoulissTypical11) {
		 * buttAuto.setVisibility(View.GONE); autoInfo.setVisibility(View.GONE);
		 * }
		 */
		// sfondo bottone
		if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_Antitheft)
			buttPlus.setChecked(true);
		// buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.round_button));
		else if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_NoAntitheft)
			buttPlus.setChecked(false);
		else if (senseiMaster.getTypicalDTO().getOutput() >= Souliss_T4n_Alarm) {
			buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.alert_theft));
			buttPlus.setTextOn("ALARM");
			buttPlus.setTextOff("ALARM");
			// alarmInfoTextView.setText("Cycles to shutoff: " +
			// collected.getTypicalDTO().getOutput());
		}
		// buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.round_button));
		return ret;
	}

	/**
	 * comandi sempre inviati al master
	 */
	private void shutoff() {
		Thread t = new Thread() {
			public void run() {
				Looper.prepare();
				UDPHelper.issueSoulissCommand("" + senseiMaster.getParentNode().getId(), ""
						+ senseiMaster.getTypicalDTO().getSlot(), opzioni, Constants.COMMAND_SINGLE, ""
						+ (Souliss_T4n_NotArmed));
			}
		};
		t.start();
		Toast.makeText(getActivity(),
				getActivity().getString(R.string.TurnOFF) + " " + getActivity().getString(R.string.command_sent),
				Toast.LENGTH_SHORT).show();
		return;

	}

	/**
	 * comandi massivi ai peers
	 */
	private void reset() {
		Thread t = new Thread() {
			public void run() {
				Looper.prepare();
				UDPHelper.issueMassiveCommand("" + Souliss_T42_Antitheft_Group, opzioni, "" + Souliss_T4n_ReArm);
			}
		};
		t.start();
		Toast.makeText(getActivity(),
				getActivity().getString(R.string.reset) + " " + getActivity().getString(R.string.command_sent),
				Toast.LENGTH_SHORT).show();
		return;

	}

	private void turnOn(final int i) {
		Thread t = new Thread() {
			public void run() {
				Looper.prepare();
				UDPHelper.issueSoulissCommand("" + senseiMaster.getParentNode().getId(), ""
						+ senseiMaster.getTypicalDTO().getSlot(), opzioni, Constants.COMMAND_SINGLE, ""
						+ Souliss_T4n_Armed);
			}
		};

		t.start();
		Toast.makeText(getActivity(),
				getActivity().getString(R.string.TurnON) + " " + getActivity().getString(R.string.command_sent),
				Toast.LENGTH_SHORT).show();
		return;

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
				senseiMaster = datasource.getAntiTheftMasterTypical();
				alarmInfoTextView.setText(Html.fromHtml("<b>" + getString(R.string.antitheft_status) + "</b> "
						+ senseiMaster.getOutputDesc()));
				if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_Antitheft) {
					buttPlus.setChecked(true);
					// buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.round_button));
				} else if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_NoAntitheft) {
					buttPlus.setChecked(false);
				} else if (senseiMaster.getTypicalDTO().getOutput() >= Souliss_T4n_Alarm) {
					buttPlus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.alert_theft));
					buttPlus.setTextOn("ALARM");
					buttPlus.setTextOff("ALARM");
					// alarmInfoTextView.setText("Cycles to shutoff: " +
					// collected.getTypicalDTO().getOutput());
				} else {
					Log.w(Constants.TAG, "Unknown status");
				}
				setSensorsView();
			} catch (Exception e) {
				Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
			}
		}
	};
	private void setSensorsView() {
		StringBuilder tmp = new StringBuilder();

		sensei = datasource.getAntiTheftSensors();
		for (SoulissTypical42AntiTheftPeer soulissTypical42AntiTheftPeer : sensei) {
			tmp.append(soulissTypical42AntiTheftPeer.getParentNode().getNiceName() + " - "
					+ soulissTypical42AntiTheftPeer.getNiceName() + " - "
					+ soulissTypical42AntiTheftPeer.getOutputDesc() + "\n");
		}
		textviewSensors.setText(Html.fromHtml(tmp.toString()));
	}
}
