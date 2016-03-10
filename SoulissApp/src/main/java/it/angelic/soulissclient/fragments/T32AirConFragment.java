package it.angelic.soulissclient.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import cuneyt.tag.TagView;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical32AirCon;
import it.angelic.soulissclient.net.UDPHelper;

import static junit.framework.Assert.assertTrue;


public class T32AirConFragment extends AbstractTypicalFragment {
	private SoulissDBHelper datasource;

	private SoulissDataService mBoundService;
	private boolean mIsBound;

	Spinner functionSpinner;
	Spinner fanSpiner;

	// time based
	TextView textviewTemperature;

	Spinner spinnerInterval;
	// position based
	TextView textviewPositional;
	SwitchCompat togSwirl;
	SwitchCompat togIon;
	SwitchCompat togEnergySave;
	// data based
	TextView textviewTriggered;

	Button buttPlus;
	Button buttMinus;

	Button btOff;
	Button btOn;
	private SoulissTypical collected;
	private SoulissTypical related;

	public static T32AirConFragment newInstance(int index, SoulissTypical content) {
		T32AirConFragment f = new T32AirConFragment();

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

		// tema
		if (opzioni.isLightThemeSelected())
			getActivity().setTheme(R.style.LightThemeSelector);
		else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);

		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(getActivity());
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		if (container == null)
			return null;
		opzioni = SoulissApp.getOpzioni();
		opzioni.reload();

		View ret = inflater.inflate(R.layout.main_aircon, container, false);
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
		assertTrue("TIPICO NULLO", collected instanceof SoulissTypical32AirCon);

		related = (SoulissTypical) getActivity().getIntent().getExtras().get("RELATO");
		if (related == null)
			related = datasource.getTypical(collected.getNodeId(), (short) (collected.getSlot() + 1));

		collected.setPrefs(opzioni);
		super.setCollected(collected);

		functionSpinner = (Spinner) ret.findViewById(R.id.spinnerFunction);
		fanSpiner = (Spinner) ret.findViewById(R.id.spinnerFan);

		// time based
		textviewTemperature = (TextView) ret.findViewById(R.id.textviewTemp);
		textviewPositional = (TextView) ret.findViewById(R.id.textViewInfoPos);
		togSwirl = (SwitchCompat) ret.findViewById(R.id.toggleButtonSwing);
		togIon = (SwitchCompat) ret.findViewById(R.id.toggleButtonIon);
		togEnergySave = (SwitchCompat) ret.findViewById(R.id.toggleButtonEco);
		textviewTriggered = (TextView) ret.findViewById(R.id.textViewInfoTrig);

		buttPlus = (Button) ret.findViewById(R.id.buttonPlus);
		buttMinus = (Button) ret.findViewById(R.id.buttonMinus);

		btOff = (Button) ret.findViewById(R.id.buttonTurnOff);
		btOn = (Button) ret.findViewById(R.id.buttonTurnOn);

		infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);
        tagView = (TagView) ret.findViewById(R.id.tag_group);

		refreshTagsInfo();

		// upcast
		Integer status = Integer.valueOf(collected.getTypicalDTO().getOutput());
		Integer status2 = Integer.valueOf(related.getTypicalDTO().getOutput());
		Log.d(Constants.TAG, "Power + opts : 0x" + Integer.toHexString(collected.getTypicalDTO().getOutput()));
		Log.d(Constants.TAG, "Function + temp: 0x" + Integer.toHexString(status2));
		int fun = (status2 & 0XF0);
		int temp = (status2 & 0X0F);
		int fan = (status & 0X07);

		textviewTemperature.setText("" + demapTemperature(temp));
		final int[] spinnerArrVal = getResources().getIntArray(R.array.AirConFanValues);

		final int[] spinnerFunVal = getResources().getIntArray(R.array.AirConFunctionValues);

		for (int i = 0; i < spinnerFunVal.length; i++) {
			if (fun == spinnerFunVal[i])
				functionSpinner.setSelection(i);
		}

		/**
		 * LISTENER SPINNER DESTINATARIO, IN TESTATA
		 */
		final OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// /setTypicalSpinner(outputTypicalSpinner, nodiArray[pos]);
				issueIrCommand(buildIrCommand(null));
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};

		final OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// if (pos > 0) {
				// ArrayList<SoulissTypical> re = nodiArray[(int)
				// outputNodeSpinner.getSelectedItemId()].getTypicals();
				issueIrCommand(buildIrCommand(null));
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};
		// avoid auto call upon Creation with runnable
		functionSpinner.post(new Runnable() {
			public void run() {
				functionSpinner.setOnItemSelectedListener(lit);
				fanSpiner.setOnItemSelectedListener(lib);
			}
		});

		return ret;
	}

	@Override
	public void onStart() {
		super.onStart();

		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				int act = Integer.parseInt(textviewTemperature.getText().toString());
				if (act < 30)
					textviewTemperature.setText("" + (act + 1));

				issueIrCommand(buildIrCommand(null));
			}
		};
		buttPlus.setOnClickListener(plus);

		OnClickListener min = new OnClickListener() {
			public void onClick(View v) {
				int act = Integer.parseInt(textviewTemperature.getText().toString());
				if (act > 16)
					textviewTemperature.setText("" + (act - 1));

				issueIrCommand(buildIrCommand(null));
			}
		};
		buttMinus.setOnClickListener(min);

		OnClickListener simpleO = new OnClickListener() {
			public void onClick(View v) {
				issueIrCommand(buildIrCommand(1));
			}
		};
		btOn.setOnClickListener(simpleO);

		// Cancel
		OnClickListener simplecan = new OnClickListener() {
			public void onClick(View v) {
				// T32AirConFragment.this.finish();
				issueIrCommand(buildIrCommand(0));
				return;
			}
		};
		btOff.setOnClickListener(simplecan);

		//setActionBarInfo(collected.getNiceName());
        //this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}




	@Override
	public void onResume() {
		super.onResume();
		SoulissDBHelper.open();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction("it.angelic.soulissclient.GOT_DATA");
		getActivity().registerReceiver(soulissDataReceiver, filtere);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(soulissDataReceiver);
	}

	/**
	 * Collects options from UI and build a Souliss air conditioner command
	 * 
	 * @param optTurnon
	 * @return
	 */
	private long buildIrCommand(Integer optTurnon) {
		final int[] spinnerArrVal = getResources().getIntArray(R.array.AirConFanValues);
		final int[] spinnerFunVal = getResources().getIntArray(R.array.AirConFunctionValues);

		long firstpar = 0;
		// Power ON or OFF
		if (optTurnon != null) {
			if (optTurnon == 1)
				firstpar += 0x8000L;
			else
				firstpar += 0x4000L;
		}

		long mask = togIon.isChecked() ? 1 : 0;
		firstpar += mask << 13;// third output bit = option 1
		mask = togEnergySave.isChecked() ? 1 : 0;
		firstpar += mask << 12;// fourth output bit = option2
		mask = togSwirl.isChecked() ? 1 : 0;
		firstpar += mask << 11;// third output bit = air swing

		int fan = spinnerArrVal[fanSpiner.getSelectedItemPosition()] << 8;
		firstpar += fan;

		int fun = spinnerFunVal[functionSpinner.getSelectedItemPosition()] << 4;
		firstpar += fun;

		int tmp = remapTemperature(Integer.parseInt(textviewTemperature.getText().toString()));
		firstpar += tmp;

		Log.d(Constants.TAG, "Sending Aircon command:" + "\nFan:      " + fan + "\nFunction: " + fun + "\nTemperat: "
				+ tmp + "\nHEX: 0x" + Long.toHexString(firstpar));
		return firstpar;
	}

	/**************************************************************************
	 * Souliss Air Conditioner command Souliss OUTPUT Data is:
	 * 
	 * 0xABCD A = 4 bit mask (XYZT X= powerON, Y PowerOFF, option1 , option2) B
	 * = Fan/Swirl (1bit switch + 3bits for four possible fan values) C =
	 * Function (auto,cool,dry,fan,heat) D = Temperature (from 16 to 30, see .h)
	 * 
	 * INPUT data 'read' from GUI
	 **************************************************************************/
	private long issueIrCommand(final long finalCommand) {

		Thread t = new Thread() {
			public void run() {
				Looper.prepare();

				// comando da due bytes
				// alternativa maschere di bit ma non ho voglia
				String pars = Long.toHexString(finalCommand);
				String par1, par2;
				// i primi 4 bit possono essere tutti a 0
				if (pars.length() == 3)
					pars = "0" + pars;
				par1 = Integer.toString(Integer.parseInt(pars.substring(0, 2), 16));
				par2 = Integer.toString(Integer.parseInt(pars.substring(2, 4), 16));

				UDPHelper.issueSoulissCommand("" + collected.getParentNode().getId(), ""
						+ collected.getTypicalDTO().getSlot(), opzioni,  par1, par2);
			}
		};

		t.start();
		return finalCommand;
	}



	// Aggiorna il feedback
	private BroadcastReceiver soulissDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SoulissNode coll = collected.getParentNode();
			Bundle extras = intent.getExtras();
			Bundle vers = (Bundle) extras.get("NODES");
			Log.d(Constants.TAG, "Detected data arrival: " + vers.size() + " nodes");
			int howmany = extras.getInt("quantity");

			// SoulissNode[] numversioni = new SoulissNode[(int) howmany];
			int temp = howmany - 1;

			//TODO sta qua e` roba vetusta. Controllare
			while (temp >= 0) {
				SoulissNode temrp = (SoulissNode) vers.getSerializable("" + temp);
				temp--;
				if (coll.getId() == temrp.getId()) {
					// rinfresca padre
					coll.setHealth(temrp.getHealth());
					coll.setRefreshedAt(temrp.getRefreshedAt());

					List<SoulissTypical> tips = temrp.getTypicals();

					for (SoulissTypical soulissTypical : tips) {
						if (soulissTypical.getTypicalDTO().getSlot() == collected.getTypicalDTO().getSlot()) {
							collected = soulissTypical;
							// collected.getTypicalDTO().setOutput(soulissTypical.getTypicalDTO().getOutput());
							// collected.getTypicalDTO().setRefreshedAt(soulissTypical.getTypicalDTO().getRefreshedAt());
							Log.i(Constants.TAG, "AirCon data refreshed");
							// TODO setta gli spinner
						}
					}

				}
			}
		}
	};

	/**
	 * Bruttino, ma va ben cosi perche funziona
	 * 
	 * remaps int value to Souliss AirCon protocol
	 * 
	 * @param CelsiusIn
	 * @return
	 */
	static short remapTemperature(int CelsiusIn) {

		switch (CelsiusIn) {
		case 16:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_16C;
		case 17:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_17C;
		case 18:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_18C;
		case 19:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_19C;
		case 20:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_20C;
		case 21:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_21C;
		case 22:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_22C;
		case 23:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_23C;
		case 24:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_24C;
		case 25:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_25C;
		case 26:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_26C;
		case 27:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_27C;
		case 28:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_28C;
		case 29:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_29C;
		case 30:
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_30C;

		default:
			Log.e(Constants.TAG, "error in temp ecode");
			return Constants.Typicals.Souliss_T_IrCom_AirCon_temp_24C;
		}

	}

	/**
	 * Bruttino, ma va ben cosi perche funziona
	 * 
	 * remaps int value to Souliss AirCon protocol
	 * 
	 * @param CelsiusIn
	 * @return
	 */
	static short demapTemperature(int CelsiusIn) {

		switch (CelsiusIn) {
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_16C:
			return 16;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_17C:
			return 17;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_18C:
			return 18;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_19C:
			return 19;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_20C:
			return 20;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_21C:
			return 21;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_22C:
			return 22;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_23C:
			return 23;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_24C:
			return 24;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_25C:
			return 25;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_26C:
			return 26;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_27C:
			return 27;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_28C:
			return 28;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_29C:
			return 29;
		case Constants.Typicals.Souliss_T_IrCom_AirCon_temp_30C:
			return 30;

		default:
			Log.e(Constants.TAG, "Invalid temperature:" + CelsiusIn);
			return 24;
		}

	}
}
