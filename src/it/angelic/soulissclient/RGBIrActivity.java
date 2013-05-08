package it.angelic.soulissclient;

import static junit.framework.Assert.assertTrue;
import static it.angelic.soulissclient.Constants.*;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.typicals.SoulissTypical;
import it.angelic.soulissclient.typicals.SoulissTypical15;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RGBIrActivity extends FragmentActivity {
	private SoulissDBHelper datasource = new SoulissDBHelper(this);
	private SoulissPreferenceHelper opzioni;

	private Button buttPlus;
	private Button buttMinus;

	private Button btOff;
	private Button btOn;
	private SoulissTypical collected;
	// private SoulissTypical related;
	private Button btRed;
	private Button btRed2;
	private Button btRed3;
	private Button btRed4;
	private Button btRed5;
	private Button btGreen;
	private Button btGreen2;
	private Button btGreen3;
	private Button btGreen4;
	private Button btGreen5;
	private Button btBlu;
	private Button btBlu2;
	private Button btBlu3;
	private Button btBlu4;
	private Button btBlu5;
	private Button btWhite;
	private Button btFlash;
	private Button btFade;
	private Button btShoot;
	private Button btStrobo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		opzioni = SoulissClient.getOpzioni();
		// tema
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_irrgb);

		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(this);
		}

		buttPlus = (Button) findViewById(R.id.buttonPlus);
		buttMinus = (Button) findViewById(R.id.buttonMinus);

		btOff = (Button) findViewById(R.id.buttonTurnOff);
		btOn = (Button) findViewById(R.id.buttonTurnOn);

		btWhite = (Button) findViewById(R.id.white);
		btFlash = (Button) findViewById(R.id.flash);
		btFade = (Button) findViewById(R.id.fade);
		btShoot = (Button) findViewById(R.id.smooth);
		btStrobo = (Button) findViewById(R.id.strobe);

		btRed = (Button) findViewById(R.id.red);
		btRed2 = (Button) findViewById(R.id.red2);
		btRed3 = (Button) findViewById(R.id.red3);
		btRed4 = (Button) findViewById(R.id.red4);
		btRed5 = (Button) findViewById(R.id.red5);

		btGreen = (Button) findViewById(R.id.green);
		btGreen2 = (Button) findViewById(R.id.green2);
		btGreen3 = (Button) findViewById(R.id.green3);
		btGreen4 = (Button) findViewById(R.id.green4);
		btGreen5 = (Button) findViewById(R.id.green5);

		btBlu = (Button) findViewById(R.id.blue);
		btBlu2 = (Button) findViewById(R.id.blue2);
		btBlu3 = (Button) findViewById(R.id.blue3);
		btBlu4 = (Button) findViewById(R.id.blue4);
		btBlu5 = (Button) findViewById(R.id.blue5);

		btOff.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_OffCmd);
		btOn.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_OnCmd);

		buttPlus.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T_IrCom_RGB_bright_up);
		buttMinus.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T_IrCom_RGB_bright_down);

		btWhite.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_W);
		btFlash.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T_IrCom_RGB_mode_flash);
		btFade.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T_IrCom_RGB_mode_fade);
		btShoot.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T_IrCom_RGB_mode_smooth);
		btStrobo.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T_IrCom_RGB_mode_strobe);

		btRed.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_R);
		btRed2.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_R2);
		btRed3.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_R3);
		btRed4.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_R4);
		btRed5.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_R5);

		btGreen.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_G);
		btGreen2.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_G2);
		btGreen3.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_G3);
		btGreen4.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_G4);
		btGreen5.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_G5);

		btBlu.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_B);
		btBlu2.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_B2);
		btBlu3.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_B3);
		btBlu4.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_B4);
		btBlu5.setTag(it.angelic.soulissclient.typicals.Constants.Souliss_T1n_RGB_B5);

		datasource = new SoulissDBHelper(this);
		datasource.open();

		Bundle extras = getIntent().getExtras();
		collected = (SoulissTypical15) extras.get("TIPICO");
		assertTrue("TIPICO NULLO", collected instanceof SoulissTypical15);

		collected.setCtx(this);

		// upcast
		// Integer status =
		// Integer.valueOf(collected.getTypicalDTO().getOutput());
	}

	@SuppressLint({ "NewApi", "NewApi" })
	@Override
	protected void onStart() {
		super.onStart();
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = this.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		// final ToggleButton tog = (ToggleButton)
		// findViewById(R.id.toggleButton1);

		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				Short cmd = (Short) v.getTag();
				assertTrue(cmd != null);
				issueIrCommand(cmd);
				return;
			}
		};
		buttPlus.setOnClickListener(plus);
		buttMinus.setOnClickListener(plus);

		btOff.setOnClickListener(plus);
		btOn.setOnClickListener(plus);

		btWhite.setOnClickListener(plus);
		btFlash.setOnClickListener(plus);
		btFade.setOnClickListener(plus);
		btShoot.setOnClickListener(plus);
		btStrobo.setOnClickListener(plus);

		btRed.setOnClickListener(plus);
		btRed2.setOnClickListener(plus);
		btRed3.setOnClickListener(plus);
		btRed4.setOnClickListener(plus);
		btRed5.setOnClickListener(plus);

		btGreen.setOnClickListener(plus);
		btGreen2.setOnClickListener(plus);
		btGreen3.setOnClickListener(plus);
		btGreen4.setOnClickListener(plus);
		btGreen5.setOnClickListener(plus);

		btBlu.setOnClickListener(plus);
		btBlu2.setOnClickListener(plus);
		btBlu3.setOnClickListener(plus);
		btBlu4.setOnClickListener(plus);
		btBlu5.setOnClickListener(plus);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
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
		registerReceiver(datareceiver, filtere);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(datareceiver);
	}

	/**************************************************************************
	 * Souliss RGB light command Souliss OUTPUT Data is:
	 * 
	 * 
	 * INPUT data 'read' from GUI
	 **************************************************************************/
	void issueIrCommand(final short val) {

		Thread t = new Thread() {
			public void run() {
				Looper.prepare();

				UDPHelper.issueSoulissCommand("" + collected.getParentNode().getId(), ""
						+ collected.getTypicalDTO().getSlot(), opzioni, Constants.COMMAND_SINGLE, "" + val);
			}
		};

		t.start();
		return;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//datasource.close();
	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SoulissNode coll = collected.getParentNode();
			Bundle extras = intent.getExtras();
			Bundle vers = (Bundle) extras.get("NODES");
			Log.d(TAG, "Detected data arrival: " + vers.size() + " nodes");
			int howmany = extras.getInt("quantity");

			// SoulissNode[] numversioni = new SoulissNode[(int) howmany];
			int temp = (int) howmany - 1;

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
							Log.i(Constants.TAG, "RGB data refreshed");
							// TODO setta gli spinner
						}
					}// ciclo tipici
				}
			}// ciclo nodi
		}
	};

}
