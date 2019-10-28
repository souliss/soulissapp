package it.angelic.soulissclient.fragments.typicals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pheelicks.visualizer.VisualizerView;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentTransaction;
import im.dacer.androidcharts.ClockPieView;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.fragments.NodeDetailFragment;
import it.angelic.soulissclient.fragments.TimeRangeEnum;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.helpers.TimeHourSpinnerUtils;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical12DigitalOutputAuto;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.util.SoulissUtils;

import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_AutoCmd;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_OffCmd;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_OffCoil;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_OffCoil_Auto;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_OnCmd;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_OnCoil;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_OnCoil_Auto;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T1n_Timed;
import static junit.framework.Assert.assertTrue;

public class T1nGenericLightFragment extends AbstractTypicalFragment implements NumberPicker.OnValueChangeListener {
    private ClockPieView clockPieView;
    private SoulissDBHelper datasource = new SoulissDBHelper(SoulissApp.getAppContext());
	private SoulissPreferenceHelper opzioni;

	private Button buttPlus;
	private NumberPicker warner;
	private CheckBox warnerCheck;
	private SoulissTypical collected;
	// private SoulissTypical related;
	private SeekBar timer;

    // Color change listener.
    private VisualizerView mVisualizerView;
	private TextView timerInfo;
    private TextView autoInfo;
    private TextView infoTyp;
	private SwitchCompat togMassive;
	private TextView infoHistory;

	public static T1nGenericLightFragment newInstance(int index, SoulissTypical content) {
		T1nGenericLightFragment f = new T1nGenericLightFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);

		// Ci metto il nodo dentro
		if (content != null) {
			args.putSerializable("TIPICO", content);
		}
		f.setArguments(args);

		return f;
	}



	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		opzioni = SoulissApp.getOpzioni();
		opzioni.reload();
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
	}

	@Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null)
			return null;
		opzioni = SoulissApp.getOpzioni();
		opzioni.reload();

		View ret = inflater.inflate(R.layout.frag_t1n, container, false);

        datasource = new SoulissDBHelper(getActivity());
		SoulissDBHelper.open();

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && extras.get("TIPICO") != null) {
			collected = (SoulissTypical) extras.get("TIPICO");
		} else if (getArguments() != null) {
			collected = (SoulissTypical) getArguments().get("TIPICO");
		} else {
			Log.e(Constants.TAG, "Error retriving node:");
			return ret;
		}
        assertTrue("ID TIPICO NULLO", collected instanceof SoulissTypical);
        //ora che ho ID, ricarico
        SoulissNode coll = datasource.getSoulissNode(collected.getNodeId());
        collected = coll.getTypical(collected.getSlot());

		collected.setPrefs(opzioni);

		super.setCollected(collected);

        warnerCheck = ret.findViewById(R.id.checkBoxWarn);
        warner = ret.findViewById(R.id.warnTimer);
        buttPlus = ret.findViewById(R.id.buttonPlus);
        Button buttAuto = ret.findViewById(R.id.buttonAuto);
        timer = ret.findViewById(R.id.sleepBar);
        timerInfo = ret.findViewById(R.id.textviewTimerInfo);
        autoInfo = ret.findViewById(R.id.textviewAutoInfo);
        Button btSleep = ret.findViewById(R.id.sleep);
        infoTyp = ret.findViewById(R.id.textView1nInfo);
        infoTags = ret.findViewById(R.id.tableRowTagInfo);
        infoHistory = ret.findViewById(R.id.textviewHistoryInfo);
        togMassive = ret.findViewById(R.id.buttonMassive);
        mVisualizerView = ret.findViewById(R.id.visualizerView);
        tagView = ret.findViewById(R.id.tag_group);
        clockPieView = ret.findViewById(R.id.pie_view);

		buttPlus.setTag(Constants.Typicals.Souliss_T1n_BrightUp);
		infoTyp.setText(collected.getParentNode().getNiceName() + " - Slot " + collected.getSlot() + " - Out: 0x" + Integer.toHexString(collected.getOutput().intValue()));

        refreshTagsInfo();
        refreshHistoryInfo();

		String[] nums = {getString(R.string.hours), "1/4", "1/2", "3/4", "1", "2", "3", "4", "5", "6", "8", "12", "24"};


		warner.setMaxValue(nums.length-1);
		warner.setMinValue(0);
		warner.setWrapSelectorWheel(false);
		warner.setDisplayedValues(nums);
		warner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		warner.setOnValueChangedListener(this);
		warnerCheck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(warnerCheck.isChecked()){
					if (!opzioni.isDataServiceEnabled()) {
						AlertDialogHelper.serviceNotActiveDialog(getActivity());
					}
                    Log.d(Constants.TAG, "Spinner VAL: " + TimeHourSpinnerUtils.getTimeArrayValMsec(warner.getValue()));
					collected.getTypicalDTO().setWarnDelayMsec(TimeHourSpinnerUtils.getTimeArrayValMsec(warner.getValue()));
					collected.getTypicalDTO().persist();
				}else{
					System.out.println("Un-Checked");
					collected.getTypicalDTO().setWarnDelayMsec(0);
					collected.getTypicalDTO().persist();
                    // salvo il warn

				}
                Log.d(Constants.TAG, "SAVED warn timer: " + collected.getTypicalDTO().getWarnDelayMsec());
			}
		});

		btSleep.setTag(Constants.Typicals.Souliss_T_related);
		// Listener generico
		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				Short cmd = (Short) v.getTag();
				if (collected.getOutput() == Souliss_T1n_OnCoil || collected.getOutput() == Souliss_T1n_OnCoil_Auto) {
					shutoff();
				} else if (collected.getOutput() == Souliss_T1n_OffCoil || collected.getOutput() == Souliss_T1n_OffCoil_Auto) {
					turnOn(0);
				} else {
					Log.e(Constants.TAG, "OUTPUT Error");
				}
				assertTrue(cmd != null);
			}

		};
		buttPlus.setOnClickListener(plus);

		buttAuto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Thread t = new Thread() {
					public void run() {
						if (togMassive.isChecked())
							UDPHelper.issueMassiveCommand("" + collected.getTypical(), opzioni,
									String.valueOf(Souliss_T1n_AutoCmd));
						else
							UDPHelper.issueSoulissCommand("" + collected.getNodeId(), ""
									+ collected.getSlot(), opzioni,
									String.valueOf(Souliss_T1n_AutoCmd));
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
				// Il timer parte da 10...
				turnOn(timer.getProgress() + 0x30);
				return;
			}
		};
		btSleep.setOnClickListener(plusSlip);

		if (collected instanceof SoulissTypical12DigitalOutputAuto) {
			btSleep.setVisibility(View.GONE);
			timer.setVisibility(View.GONE);
			timerInfo.setVisibility(View.GONE);
			// Check AUTO mode
			if (collected.getOutput() == Souliss_T1n_OnCoil || collected.getOutput() == Souliss_T1n_OnCoil_Auto)
				autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " ON");
			else
				autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " OFF");
		} else if (collected instanceof SoulissTypical11DigitalOutput) {
			buttAuto.setVisibility(View.GONE);
			autoInfo.setVisibility(View.GONE);
		}
		// sfondo bottone
		if (collected.getOutput() == Souliss_T1n_OnCoil
				|| collected.getOutput() == Souliss_T1n_OnCoil_Auto)
			buttPlus.setBackgroundResource(R.drawable.bulb_on);
		else if (collected.getOutput() == Souliss_T1n_OffCoil
				|| collected.getOutput() == Souliss_T1n_OffCoil_Auto)
			buttPlus.setBackgroundResource(R.drawable.bulb_off);

		return ret;
	}


    private void refreshHistoryInfo() {
        if (!opzioni.isLogHistoryEnabled()) {
            //nascondi roba
            infoHistory.setVisibility(View.GONE);
            clockPieView.setVisibility(View.GONE);
            return;
        }
        try {
            StringBuilder str = new StringBuilder();
			int msecOn = datasource.getTypicalOnDurationMsec(collected, TimeRangeEnum.LAST_WEEK);
			if (collected.getOutput() != 0) {
                Date when = collected.getTypicalDTO().getLastStatusChange();
                long swap = new Date().getTime() - when.getTime();
                msecOn += swap;
                String strMeatFormat = getResources().getString(R.string.manual_litfrom);
				String strMeatMsg = String.format(strMeatFormat, SoulissUtils.getDuration(swap));
				str.append(strMeatMsg);
            }
            str.append("\n");
            String strMeatFormat = getResources().getString(R.string.manual_tyinf);
			String strMeatMsg = String.format(strMeatFormat, SoulissUtils.getDuration(msecOn));
			str.append(strMeatMsg);
            infoHistory.setText(str.toString());

            //bella questa eh
            clockPieView.setDate(datasource.getTypicalOnClockPie(collected, TimeRangeEnum.LAST_WEEK));

        }catch (Exception ie){
            Log.e(Constants.TAG,"cant refresh history:"+ie.getMessage());
        }
    }

    private void shutoff() {
		Thread t = new Thread() {
			public void run() {
				Looper.prepare();
				if (togMassive.isChecked())
					UDPHelper.issueMassiveCommand("" + collected.getTypical(), opzioni, ""
							+ (Souliss_T1n_OffCmd));
				else
					UDPHelper.issueSoulissCommand("" + collected.getParentNode().getNodeId(), ""
							+ collected.getSlot(), opzioni,""
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
					UDPHelper.issueMassiveCommand("" + collected.getTypical(), opzioni, ""
							+ (Souliss_T1n_OnCmd + i));
				else
					UDPHelper.issueSoulissCommand("" + collected.getParentNode().getNodeId(), ""
							+ collected.getSlot(), opzioni, ""
							+ (Souliss_T1n_OnCmd + i));

			}
		};

		t.start();
		if (i > 0)
			Toast.makeText(
					getActivity(),
					getActivity().getString(R.string.Souliss_TRGB_sleep) + " "
							+ getActivity().getString(R.string.command_sent), Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getActivity(),
					getActivity().getString(R.string.TurnON) + " " + getActivity().getString(R.string.command_sent),
					Toast.LENGTH_SHORT).show();
		return;

	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				NodeDetailFragment details = NodeDetailFragment.newInstance(collected.getNodeId(),
						collected.getParentNode());
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				if (opzioni.isAnimationsEnabled())
					ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
				ft.replace(R.id.detailPane, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
				ft.commit();
			} else {
				Log.i(Constants.TAG, "Close fragment");
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.remove(getFragmentManager().findFragmentById(R.id.detailPane)).commit();
				/*getActivity().supportFinishAfterTransition();
				if (opzioni.isAnimationsEnabled())
					getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);*/

			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //disabilita la checkbox cosi da forzare il refresh
        warnerCheck.setChecked(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(Constants.TAG, "Injecting warn timer: " + collected.getTypicalDTO().getWarnDelayMsec());
        warner.setValue(TimeHourSpinnerUtils.getTimeArrayPos(collected.getTypicalDTO().getWarnDelayMsec()));
        if (collected.getTypicalDTO().getWarnDelayMsec() > 0){
            warnerCheck.setChecked(true);
            Log.d(Constants.TAG, "Injecting warn timer: "+ collected.getTypicalDTO().getWarnDelayMsec());
        }

    }



    @Override
	public void onResume() {
		super.onResume();
		SoulissDBHelper.open();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction("it.angelic.soulissclient.GOT_DATA");
		filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
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
				SoulissDBHelper.open();
				SoulissNode coll = datasource.getSoulissNode(collected.getNodeId());
				collected = coll.getTypical(collected.getSlot());
                if (collected.getOutput() == Souliss_T1n_OnCoil || collected.getOutput() == Souliss_T1n_OnCoil_Auto)
                    buttPlus.setBackgroundResource(R.drawable.bulb_on);
                else if (collected.getOutput() == Souliss_T1n_OffCoil || collected.getOutput() == Souliss_T1n_OffCoil_Auto)
                    buttPlus.setBackgroundResource(R.drawable.bulb_off);
				else if (collected.getOutput() >= Souliss_T1n_Timed) {
					timer.setProgress(collected.getOutput().intValue());
					buttPlus.setBackgroundResource(R.drawable.bulb_on);
					timerInfo.setText("Cycles to shutoff: " + collected.getOutput());
				} else {
					Log.w(Constants.TAG, "Unknown status");
				}
                //refresh "acceso da" info
                if (opzioni.isLogHistoryEnabled()) {
                    refreshHistoryInfo();
                }
				refreshStatusIcon();
                if (collected.getOutput() == Souliss_T1n_OffCoil_Auto || collected.getOutput() == Souliss_T1n_OnCoil_Auto)
                    autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " ON");
                else
                    autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " OFF");
                // datasource.close();
                infoTyp.setText(collected.getParentNode().getNiceName() + ", slot " + collected.getSlot() + "Val: 0x" + Integer.toHexString(collected.getOutput().intValue()));

			} catch (Exception e) {
				Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
			}
		}
	};

}
