package it.angelic.soulissclient;

import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical32AirCon;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;


public class T32AirConActivity extends AbstractStatusedFragmentActivity {
	private SoulissDBHelper datasource = new SoulissDBHelper(this);

	private SoulissDataService mBoundService;
	private boolean mIsBound;

	Spinner functionSpinner;
	Spinner fanSpiner;

	// time based
	TextView textviewTemperature;

	Spinner spinnerInterval;
	// position based
	TextView textviewPositional;
	ToggleButton togSwirl;
	ToggleButton togIon;
	ToggleButton togEnergySave;
	// data based
	TextView textviewTriggered;

	Button buttPlus;
	Button buttMinus;

	Button btOff;
	Button btOn;
	private SoulissTypical collected;
	private SoulissTypical related;

	/* SOULISS DATA SERVICE BINDING */
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((SoulissDataService.LocalBinder) service).getService();
			// Tell the user about this for our demo.
			Log.i(Constants.TAG, "Dataservice connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
			// if (ta != null)
			Log.i(Constants.TAG, "Dataservice disconnected");
		}

	};

	void doBindService() {
		if (!mIsBound) {
			bindService(new Intent(T32AirConActivity.this, SoulissDataService.class), mConnection,
					Context.BIND_AUTO_CREATE);
			mIsBound = true;
		}
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// tema
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_aircon);

		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(this);
		}

		functionSpinner = (Spinner) findViewById(R.id.spinnerFunction);
		fanSpiner = (Spinner) findViewById(R.id.spinnerFan);

		// time based
		textviewTemperature = (TextView) findViewById(R.id.textviewTemp);
		textviewPositional = (TextView) findViewById(R.id.textViewInfoPos);
		togSwirl = (ToggleButton) findViewById(R.id.toggleButtonSwing);
		togIon = (ToggleButton) findViewById(R.id.toggleButtonIon);
		togEnergySave = (ToggleButton) findViewById(R.id.toggleButtonEco);
		textviewTriggered = (TextView) findViewById(R.id.textViewInfoTrig);

		buttPlus = (Button) findViewById(R.id.buttonPlus);
		buttMinus = (Button) findViewById(R.id.buttonMinus);

		btOff = (Button) findViewById(R.id.buttonTurnOff);
		btOn = (Button) findViewById(R.id.buttonTurnOn);

		datasource = new SoulissDBHelper(this);
		datasource.open();

		Bundle extras = getIntent().getExtras();
		collected = (SoulissTypical32AirCon) extras.get("TIPICO");
		assertTrue("TIPICO NULLO", collected instanceof SoulissTypical32AirCon);
		
		related = (SoulissTypical) extras.get("RELATO");
		collected.setCtx(this);

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

	}

	@Override
	protected void onStart() {
		super.onStart();
		setActionBarInfo(collected.getNiceName());
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
				// T32AirConActivity.this.finish();
				issueIrCommand(buildIrCommand(0));
				return;
			}
		};
		btOff.setOnClickListener(simplecan);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			if (opzioni.isAnimationsEnabled())
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		datasource.open();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction("it.angelic.soulissclient.GOT_DATA");
		registerReceiver(soulissDataReceiver, filtere);
		doBindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(soulissDataReceiver);
		doUnbindService();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		datasource.close();
		doUnbindService();
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
			int temp = (int) howmany - 1;

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
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_16C;
		case 17:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_17C;
		case 18:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_18C;
		case 19:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_19C;
		case 20:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_20C;
		case 21:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_21C;
		case 22:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_22C;
		case 23:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_23C;
		case 24:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_24C;
		case 25:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_25C;
		case 26:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_26C;
		case 27:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_27C;
		case 28:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_28C;
		case 29:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_29C;
		case 30:
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_30C;

		default:
			Log.e(Constants.TAG, "error in temp ecode");
			return it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_24C;
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
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_16C:
			return 16;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_17C:
			return 17;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_18C:
			return 18;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_19C:
			return 19;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_20C:
			return 20;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_21C:
			return 21;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_22C:
			return 22;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_23C:
			return 23;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_24C:
			return 24;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_25C:
			return 25;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_26C:
			return 26;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_27C:
			return 27;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_28C:
			return 28;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_29C:
			return 29;
		case it.angelic.soulissclient.model.typicals.Constants.Souliss_T_IrCom_AirCon_temp_30C:
			return 30;

		default:
			Log.e(Constants.TAG, "Invalid temperature:" + CelsiusIn);
			return 24;
		}

	}
}
