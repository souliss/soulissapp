package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.db.SoulissTriggerDTO;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AddProgramActivity extends SherlockActivity {
	private SoulissNode[] nodiArray;
	private SoulissDBHelper datasource = new SoulissDBHelper(this);
	private SoulissPreferenceHelper opzioni;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		opzioni = new SoulissPreferenceHelper(this.getApplicationContext());
		// tema
		if (opzioni.isLightThemeSelected())
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		else
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_addprogram);

		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(this);
		}

		datasource = new SoulissDBHelper(this);
		datasource.open();

		ImageView nodeic = (ImageView) findViewById(R.id.timed_icon);
		nodeic.setColorFilter(getResources().getColor(R.color.aa_violet), android.graphics.PorterDuff.Mode.SRC_ATOP);

		ImageView nodeic2 = (ImageView) findViewById(R.id.position_icon);
		nodeic2.setColorFilter(getResources().getColor(R.color.aa_blue), android.graphics.PorterDuff.Mode.SRC_ATOP);

		ImageView nodeic3 = (ImageView) findViewById(R.id.triggered_icon);
		nodeic3.setColorFilter(getResources().getColor(R.color.aa_red), android.graphics.PorterDuff.Mode.SRC_ATOP);

		// prendo tipici dal DB
		List<SoulissNode> goer = datasource.getAllNodes();
		nodiArray = new SoulissNode[goer.size()];
		nodiArray = goer.toArray(nodiArray);
		SoulissClient.setBackground((ScrollView) findViewById(R.id.ScrollView01), getWindowManager());

	}

	@SuppressLint({ "NewApi", "NewApi" })
	@Override
	protected void onStart() {
		super.onStart();
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = this.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		final Spinner outputNodeSpinner = (Spinner) findViewById(R.id.spinner2);
		fillNodeSpinner(outputNodeSpinner);
		final Spinner outputTypicalSpinner = (Spinner) findViewById(R.id.spinner3);
		final Spinner outputCommandSpinner = (Spinner) findViewById(R.id.spinnerCommand);

		final RadioButton radioTimed = (RadioButton) findViewById(R.id.RadioButtonTime);
		final RadioButton radioPositional = (RadioButton) findViewById(R.id.RadioButtonPosition);
		final RadioButton radioSensorial = (RadioButton) findViewById(R.id.RadioButtonTriggered);
		// time based
		final TextView textviewTimed = (TextView) findViewById(R.id.textviewtimed);
		final CheckBox checkboxRecursive = (CheckBox) findViewById(R.id.checkBoxRecursive);
		final TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
		final Spinner spinnerInterval = (Spinner) findViewById(R.id.spinnerRepeatEvery);
		// position based
		final TextView textviewPositional = (TextView) findViewById(R.id.textViewInfoPos);
		final ToggleButton togglehomeaway = (ToggleButton) findViewById(R.id.toggleButtonPosition);
		// data based
		final TextView textviewTriggered = (TextView) findViewById(R.id.textViewInfoTrig);
		final Spinner triggeredNodeSpinner = (Spinner) findViewById(R.id.Spinner05);
		final Spinner triggeredTypicalSpinner = (Spinner) findViewById(R.id.Spinner06);
		fillNodeSpinner(triggeredNodeSpinner);
		final Button buttComparator = (Button) findViewById(R.id.buttonComparator);
		final EditText et = (EditText) findViewById(R.id.editTextThreshold);
		final Button btCancel = (Button) findViewById(R.id.buttonInsertProgram);
		final Button btSave = (Button) findViewById(R.id.buttonCancelProgram);

		/**
		 * LISTENER SPINNER DESTINATARIO, IN TESTATA
		 */
		OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				setTypicalSpinner(outputTypicalSpinner, nodiArray[pos]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};
		outputNodeSpinner.setOnItemSelectedListener(lit);

		OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// if (pos > 0) {
				ArrayList<SoulissTypical> re = nodiArray[(int) outputNodeSpinner.getSelectedItemId()]
						.getActiveTypicals();

				fillCommandSpinner(outputCommandSpinner, re.get(pos));
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};
		outputTypicalSpinner.setOnItemSelectedListener(lib);
		/**
		 * INTERLOCK della GUI
		 */
		OnClickListener first_radio_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				radioPositional.setChecked(false);
				radioSensorial.setChecked(false);
				checkboxRecursive.setEnabled(true);
				tp.setEnabled(true);
				spinnerInterval.setEnabled(checkboxRecursive.isChecked());
				togglehomeaway.setEnabled(false);
				triggeredTypicalSpinner.setEnabled(false);
				triggeredNodeSpinner.setEnabled(false);
				buttComparator.setEnabled(false);
				et.setEnabled(false);
				textviewPositional.setEnabled(false);
				textviewTimed.setEnabled(true);
				textviewTriggered.setEnabled(false);
			}
		};
		radioTimed.setOnClickListener(first_radio_listener);

		OnClickListener se_radio_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				radioTimed.setChecked(false);
				radioSensorial.setChecked(false);
				checkboxRecursive.setEnabled(false);
				tp.setEnabled(false);
				spinnerInterval.setEnabled(false);
				togglehomeaway.setEnabled(true);
				triggeredTypicalSpinner.setEnabled(false);
				triggeredNodeSpinner.setEnabled(false);
				buttComparator.setEnabled(false);
				et.setEnabled(false);
				textviewPositional.setEnabled(true);
				textviewTimed.setEnabled(false);
				textviewTriggered.setEnabled(false);
			}
		};
		radioPositional.setOnClickListener(se_radio_listener);

		OnClickListener rw_radio_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				radioTimed.setChecked(false);
				radioPositional.setChecked(false);
				checkboxRecursive.setEnabled(false);
				tp.setEnabled(false);
				spinnerInterval.setEnabled(false);
				togglehomeaway.setEnabled(false);
				triggeredTypicalSpinner.setEnabled(true);
				triggeredNodeSpinner.setEnabled(true);
				buttComparator.setEnabled(true);
				et.setEnabled(true);
				textviewPositional.setEnabled(false);
				textviewTimed.setEnabled(false);
				textviewTriggered.setEnabled(true);
			}
		};
		radioSensorial.setOnClickListener(rw_radio_listener);
		// Check box ricorsivo, Interlock
		checkboxRecursive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				spinnerInterval.setEnabled(isChecked);
			}
		});
		/**
		 * LISTENER SPINNER INPUT
		 */
		OnItemSelectedListener lityp = new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				setTypicalSpinner(triggeredTypicalSpinner, nodiArray[pos]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};
		triggeredNodeSpinner.setOnItemSelectedListener(lityp);

		OnClickListener simplebttttt = new OnClickListener() {
			public void onClick(View v) {
				String old = buttComparator.getText().toString();
				if (">".compareTo(old) == 0) {
					buttComparator.setText("=");
				} else if ("<".compareTo(old) == 0) {
					buttComparator.setText(">");
				} else {
					buttComparator.setText("<");
				}
			}
		};
		buttComparator.setOnClickListener(simplebttttt);

		OnClickListener saveProgramButtonListener = new OnClickListener() {
			public void onClick(View v) {

				SoulissCommand programToSave = (SoulissCommand) outputCommandSpinner.getSelectedItem();

				if (programToSave == null) {
					Toast.makeText(AddProgramActivity.this, "Command not selected", Toast.LENGTH_SHORT);
					return;
				}
				programToSave.getCommandDTO().setSceneId(null);
				Intent intent = AddProgramActivity.this.getIntent();
				datasource.open();
				if (radioTimed.isChecked()) {// temporal schedule
					Calendar base = Calendar.getInstance();
					base.set(Calendar.HOUR_OF_DAY, tp.getCurrentHour());
					base.set(Calendar.MINUTE, tp.getCurrentMinute());
					//FIXME se scelgo un'ora precedente a quella attuale, aggiungere un giorno
					programToSave.getCommandDTO().setType(Constants.COMMAND_TIMED);
					programToSave.getCommandDTO().setScheduledTime(base);
					if (checkboxRecursive.isChecked()) {
						final int[] spinnerArrVal = getResources().getIntArray(R.array.scheduleIntervalValues);
						programToSave.getCommandDTO().setInterval(spinnerArrVal[spinnerInterval.getSelectedItemPosition()]);
					}
					// inserimento nuovo
					programToSave.getCommandDTO().persistCommand(datasource);
					intent.putExtra("returnedData", Constants.COMMAND_TIMED);
				} else if (radioPositional.isChecked()) {//POSIZIONALE
					if (togglehomeaway.isChecked()) {
						programToSave.getCommandDTO().setType(Constants.COMMAND_COMEBACK_CODE);
					} else {
						programToSave.getCommandDTO().setType(Constants.COMMAND_GOAWAY_CODE);
					}
					// inserimento nuovo
					programToSave.getCommandDTO().persistCommand(datasource);
					intent.putExtra("returnedData", Constants.COMMAND_COMEBACK_CODE);
				} else if (radioSensorial.isChecked()) {// TRIGGER
					SoulissTypical trig = ((SoulissTypical) triggeredTypicalSpinner.getSelectedItem());
					if (trig == null || trig.getTypicalDTO() == null || et.getText() == null
							|| "".compareTo(et.getText().toString()) == 0) {
						Toast.makeText(AddProgramActivity.this, "You must select an input trigger and threshold value",
								Toast.LENGTH_SHORT).show();
						return;
					}
					programToSave.getCommandDTO().setType(Constants.COMMAND_TRIGGERED);
					// inserimento nuovo
					programToSave.getCommandDTO().persistCommand(datasource);
					// campi trigger
					SoulissTriggerDTO trigger = new SoulissTriggerDTO();
					trigger.setInputNodeId(trig.getTypicalDTO().getNodeId());

					trigger.setInputSlot(trig.getTypicalDTO().getSlot());
					trigger.setOp(buttComparator.getText().toString());
					trigger.setCommandId(programToSave.getCommandDTO().getProgramId());
					trigger.setThreshVal(Integer.parseInt(et.getText().toString()));

					trigger.persist(datasource);
					intent.putExtra("returnedData", Constants.COMMAND_TRIGGERED);
				}
				//datasource.close();
				AddProgramActivity.this.setResult(RESULT_OK, intent);
				AddProgramActivity.this.finish();
				return;
			}
		};
		btSave.setOnClickListener(saveProgramButtonListener);
		// Cancel
		OnClickListener simplecan = new OnClickListener() {
			public void onClick(View v) {
				AddProgramActivity.this.finish();
				return;
			}
		};
		btCancel.setOnClickListener(simplecan);
		// start with timed schedule
		radioTimed.performClick();
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

	/**
	 * Fills a spinner with nodes
	 * 
	 * @param tgt
	 */
	private void fillNodeSpinner(Spinner tgt) {
		// spinner popolato in base nodeArray
		ArrayAdapter<SoulissNode> adapter = new ArrayAdapter<SoulissNode>(this, android.R.layout.simple_spinner_item,
				nodiArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tgt.setAdapter(adapter);
	}

	/**
	 * popola spinner tipici in base al nodo
	 * 
	 * @param tgt
	 * @param dec
	 */
	private void setTypicalSpinner(Spinner tgt, SoulissNode ref) {
		try {
			SoulissTypical[] strArray = new SoulissTypical[ref.getActiveTypicals().size()];
			ref.getActiveTypicals().toArray(strArray);
			// set del contesto
			for (SoulissTypical soulissTypical : strArray) {
				soulissTypical.setCtx(this);
			}

			ArrayAdapter<SoulissTypical> adapter = new ArrayAdapter<SoulissTypical>(this,
					android.R.layout.simple_spinner_item, strArray);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			tgt.setAdapter(adapter);

		} catch (Exception e) {
			Log.e(Constants.TAG, "Errore in setTypicalSpinner:" + e.getMessage(), e);
		}
	}

	/**
	 * popola spinner comandi in base al tipico. Mette nell'adapter i comandi
	 * ottenuti da getCommands
	 * 
	 * @param tgt
	 *            Spinner da riempire
	 * @param ref
	 *            tipico da cui ottenere i comandi
	 * 
	 */
	private void fillCommandSpinner(Spinner tgt, SoulissTypical ref) {
		SoulissCommand[] strArray = new SoulissCommand[ref.getCommands(this).size()];
		ref.getCommands(this).toArray(strArray);

		ArrayAdapter<SoulissCommand> adapter = new ArrayAdapter<SoulissCommand>(this,
				android.R.layout.simple_spinner_item, strArray);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tgt.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//datasource.close();
	}

}
