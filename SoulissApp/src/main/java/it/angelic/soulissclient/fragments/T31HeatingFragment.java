package it.angelic.soulissclient.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;

import static it.angelic.soulissclient.model.typicals.Constants.*;

import static junit.framework.Assert.assertTrue;


public class T31HeatingFragment extends AbstractTypicalFragment {
	private SoulissDBHelper datasource = new SoulissDBHelper(SoulissClient.getAppContext());
	private SoulissPreferenceHelper opzioni;

	private Button buttPlus;

	private SoulissTypical31Heating collected;

	private Button buttMinus;
	private Spinner functionSpinner;
	private Spinner fanSpiner;
	private TextView textviewTemperature;
	private TextView textviewStatus;
	private Button buttOn;
	private Button buttOff;
    private TableRow infoFavs;
    private TableRow infoTags;


    public static T31HeatingFragment newInstance(int index, SoulissTypical content) {
		T31HeatingFragment f = new T31HeatingFragment();

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
		View ret = inflater.inflate(R.layout.frag_t31, container, false);
		datasource = new SoulissDBHelper(getActivity());
		datasource.open();

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && extras.get("TIPICO") != null) {
			collected = (SoulissTypical31Heating) extras.get("TIPICO");
		} else if (getArguments() != null) {
			collected = (SoulissTypical31Heating) getArguments().get("TIPICO");
		} else {
			Log.e(Constants.TAG, "Error retriving node:");
			return ret;
		}
		assertTrue("TIPICO NULLO", collected instanceof SoulissTypical);
		collected.setPrefs(opzioni);

		super.setCollected(collected);
		refreshStatusIcon();
		
		buttPlus = (Button) ret.findViewById(R.id.buttonPlus);
		buttMinus = (Button) ret.findViewById(R.id.buttonMinus);
		
		buttOn = (Button) ret.findViewById(R.id.buttonTurnOn);
		buttOff = (Button) ret.findViewById(R.id.buttonTurnOff);
		textviewTemperature = (TextView) ret.findViewById(R.id.textviewTemp);
		textviewStatus = (TextView) ret.findViewById(R.id.textviewStatus);
		
		functionSpinner = (Spinner) ret.findViewById(R.id.spinnerFunction);
		fanSpiner = (Spinner) ret.findViewById(R.id.spinnerFan);
        infoFavs = (TableRow) ret.findViewById(R.id.tableRowFavInfo);
        infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);
		
		final int[] spinnerFunVal = getResources().getIntArray(R.array.AirConFunctionValues);

		/**
		 * LISTENER SPINNER DESTINATARIO, IN TESTATA
		 */
		final OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (pos == 0){//fa cagare
					collected.issueCommand(Souliss_T3n_Heating, null);
				}else{
					collected.issueCommand(Souliss_T3n_Cooling, null);
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};

		final OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (pos == 0){//fa cagare
					collected.issueCommand(Souliss_T3n_FanAuto, null);
				}else if (pos == 1){
					collected.issueCommand(Souliss_T3n_FanLow, null);
				}else if (pos == 2){
					collected.issueCommand(Souliss_T3n_FanMed, null);
				}else if (pos == 3){
					collected.issueCommand(Souliss_T3n_FanHigh, null);
				}
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
		
		// Listener generico
		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				int act = Integer.parseInt(textviewTemperature.getText().toString());
				if (act < 35)
					textviewTemperature.setText("" + (act + 1));
				collected.issueCommand(Souliss_T3n_Set, Float.valueOf(textviewTemperature.getText().toString()));
				return;
			}
		
		};
		buttPlus.setOnClickListener(plus);

		buttMinus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int act = Integer.parseInt(textviewTemperature.getText().toString());
				if (act > 16)
					textviewTemperature.setText("" + (act - 1));
				collected.issueCommand(Souliss_T3n_Set, Float.valueOf(textviewTemperature.getText().toString()));
			}
		});
		
		buttOn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//int act = Integer.parseInt(textviewTemperature.getText().toString());
			
				if (functionSpinner.getSelectedItemPosition() == 0)
					collected.issueCommand(Souliss_T3n_Heating, Float.valueOf(textviewTemperature.getText().toString()));
				else
					collected.issueCommand(Souliss_T3n_Cooling, Float.valueOf(textviewTemperature.getText().toString()));
			}
		});
		
		buttOff.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				collected.issueCommand(Souliss_T3n_ShutOff, null);
			}
		});


		return ret;
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
        if (collected.getTypicalDTO().isFavourite()) {
            infoFavs.setVisibility(View.VISIBLE);
        }else if (collected.getTypicalDTO().isTagged()){
            infoTags.setVisibility(View.VISIBLE);
        }
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
	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Log.i(Constants.TAG, "Broadcast received, TODO change Spinners status intent" + intent.toString());
				datasource.open();
				SoulissNode coll = datasource.getSoulissNode(collected.getTypicalDTO().getNodeId());
				collected = (SoulissTypical31Heating) coll.getTypical(collected.getTypicalDTO().getSlot());
				refreshStatusIcon();
                if (collected.getTypicalDTO().isFavourite()) {
                    infoFavs.setVisibility(View.VISIBLE);
                }else if (collected.getTypicalDTO().isTagged()){
                    infoTags.setVisibility(View.VISIBLE);
                }
				textviewStatus.setText(collected.getOutputDesc());
				// datasource.close();
			} catch (Exception e) {
				Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
			}
		}
	};

}
