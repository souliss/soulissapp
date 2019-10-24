package it.angelic.soulissclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.ISoulissCommand;
import it.angelic.soulissclient.model.ISoulissExecutable;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissCommandDTO;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.db.SoulissTriggerDTO;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;


public class AddProgramActivity extends AbstractStatusedFragmentActivity {

    //arrays per spinner
    private SoulissNode[] nodiArray;
    private SoulissNode[] nodiArrayWithExtra;

    private SoulissDBHelper datasource = new SoulissDBHelper(this);
    private TextView tvcommand;
    private SoulissCommand collected;
    private Spinner outputNodeSpinner;
    private Spinner outputTypicalSpinner;
    private RadioButton radioTimed;
    private RadioButton radioPositional;
    private RadioButton radioTrigger;
    private SwitchCompat togglehomeaway;
    private LinkedList<SoulissScene> scenes;
    private Spinner outputCommandSpinner;
    private TimePicker commandTimePicker;
    private CheckBox checkboxRecursive;
    private int[] spinnerArrVal;
    private Spinner commandSpinnerInterval;
    private Spinner triggeredNodeSpinner;
    private Button threshButton;
    private Spinner triggeredTypicalSpinner;
    private EditText threshValEditText;
    private SoulissTriggerDTO inputTrigger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(this.getApplicationContext());
        // tema
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_addprogram);
        if (!opzioni.isDbConfigured()) {
            AlertDialogHelper.dbNotInitedDialog(this);
        }

        datasource = new SoulissDBHelper(this);
        SoulissDBHelper.open();
        spinnerArrVal = getResources().getIntArray(R.array.scheduleIntervalValues);
        TextView nodeic = findViewById(R.id.timed_icon);
        tvcommand = findViewById(R.id.textViewCommand);
        /* Icona timed */
        nodeic.setTextColor(ContextCompat.getColor(AddProgramActivity.this, R.color.md_light_blue_200));
        FontAwesomeUtil.prepareFontAweTextView(AddProgramActivity.this, nodeic, FontAwesomeEnum.fa_clock_o.getFontName());
/* Icona positional */
        TextView nodeic2 = findViewById(R.id.position_icon);
        nodeic2.setTextColor(ContextCompat.getColor(AddProgramActivity.this, R.color.md_light_blue_400));
        FontAwesomeUtil.prepareFontAweTextView(AddProgramActivity.this, nodeic2, FontAwesomeEnum.fa_sign_out.getFontName());
/* Icona Trigger */
        TextView nodeic3 = findViewById(R.id.triggered_icon);
        nodeic3.setTextColor(ContextCompat.getColor(AddProgramActivity.this, R.color.md_light_blue_900));
        FontAwesomeUtil.prepareFontAweTextView(AddProgramActivity.this, nodeic3, FontAwesomeEnum.fa_puzzle_piece.getFontName());

        // prendo tipici dal DB
        List<SoulissNode> goer = datasource.getAllNodes();
        nodiArray = new SoulissNode[goer.size()];
        nodiArray = goer.toArray(nodiArray);
        //Aggiungo massivo
        SoulissNode massive = new SoulissNode(this, Constants.MASSIVE_NODE_ID);// MASSIVO
        massive.setName(getString(R.string.allnodes));
        massive.setTypicals(datasource.getUniqueTypicals(massive));
        goer.add(massive);
        //AGGIUNGO scene
        SoulissNode fake = new SoulissNode(this, Constants.COMMAND_FAKE_SCENE);// MASSIVO
        fake.setName(getString(R.string.scenes_title));
        goer.add(fake);

        nodiArrayWithExtra = new SoulissNode[goer.size()];
        nodiArrayWithExtra = goer.toArray(nodiArrayWithExtra);
        SoulissApp.setBackground(findViewById(R.id.ScrollView01), getWindowManager());

    }


    /**
     * Carica gli spinner ed i widget ai valori salvati in precedenza
     */
    private void setFields() {
        if (collected == null)
            return;
        else if (collected.getNodeId() == Constants.COMMAND_FAKE_SCENE) {
            //get last
            outputNodeSpinner.setSelection(outputNodeSpinner.getAdapter().getCount() - 1);
            //tipici sono scene
            int sceneid = collected.getSlot();
            Log.w(Constants.TAG, "Restoring scene ID" + sceneid);
            setTypicalSpinner(outputTypicalSpinner, nodiArrayWithExtra[nodiArrayWithExtra.length - 1]);
            int tot = outputTypicalSpinner.getCount();
            for (int i = 0; i < tot; i++) {
                SoulissScene now = (SoulissScene) outputTypicalSpinner.getItemAtPosition(i);
                if (now.getId() == sceneid) {
                    //seleziono la scena da eseguire
                    outputTypicalSpinner.setSelection(i);
                    Log.i(Constants.TAG, "SELECTED:" + i);
                }
            }
            tvcommand.setVisibility(View.INVISIBLE);
        } else {//reload nodo, tipico e comando
            int nodeId = collected.getNodeId();
            Log.i(Constants.TAG, "SELECTED NODEID:" + nodeId);
            int toti = outputNodeSpinner.getCount();
            int selIdx = 0;
            for (selIdx = 0; selIdx < toti; selIdx++) {
                SoulissNode now = (SoulissNode) outputNodeSpinner.getItemAtPosition(selIdx);

                if (nodeId == now.getNodeId()) {
                    Log.i(Constants.TAG, "SELECTED NODEID:" + nodeId);
                    outputNodeSpinner.setSelection(selIdx);
                    break;//selIdx ok
                }
            }
            setTypicalSpinner(outputTypicalSpinner, nodiArrayWithExtra[selIdx]);
            int tot = outputTypicalSpinner.getCount();
            for (int i = 0; i < tot; i++) {
                SoulissTypical now = (SoulissTypical) outputTypicalSpinner.getItemAtPosition(i);
                if (now.getSlot() == collected.getSlot()) {
                    //seleziono la scena da eseguire
                    outputTypicalSpinner.setSelection(i);
                    Log.i(Constants.TAG, "SELECTED TYP:" + i);
                    fillCommandSpinner(outputCommandSpinner, now);
                    break;
                }
            }
            tot = outputCommandSpinner.getCount();
            for (int t = 0; t < tot; t++) {
                SoulissCommand now = (SoulissCommand) outputCommandSpinner.getItemAtPosition(t);
                if (now.getCommand() == collected.getCommand()) {
                    //seleziono la scena da eseguire
                    outputCommandSpinner.setSelection(t);
                    Log.i(Constants.TAG, "SELECTED COMMAND POS:" + t);
                    break;
                }
            }
        }
        //SETTA TYPE
        if (collected.getType() == Constants.COMMAND_TIMED) {
            Calendar sched = collected.getScheduledTime();
            commandTimePicker.setCurrentHour(sched.get(Calendar.HOUR_OF_DAY));
            commandTimePicker.setCurrentMinute(sched.get(Calendar.MINUTE));

            if (collected.getInterval() > 0) {
                int j = 0;
                for (int spi : spinnerArrVal) {
                    if (collected.getInterval() == spi)
                        commandSpinnerInterval.setSelection(j);
                    j++;
                }
                checkboxRecursive.setChecked(true);
            }
        } else if (collected.getType() == Constants.COMMAND_COMEBACK_CODE || collected.getType() == Constants.COMMAND_GOAWAY_CODE) {
            radioPositional.performClick();
            togglehomeaway.setChecked(collected.getType() == Constants.COMMAND_COMEBACK_CODE);
        } else if (collected.getType() == Constants.COMMAND_TRIGGERED) {
            radioTrigger.performClick();

            inputTrigger = datasource.getTriggerByCommandId(collected.getCommandId());

            triggeredNodeSpinner.setSelection(inputTrigger.getInputNodeId());
            Log.d(Constants.TAG, "Setting trigger nodeid:" + inputTrigger.getInputNodeId() + " slot:" + inputTrigger.getInputSlot());
            setTypicalSpinner(triggeredTypicalSpinner, nodiArray[inputTrigger.getInputNodeId()]);
            for (int u = 0;u<triggeredTypicalSpinner.getCount();u++){
                if( inputTrigger.getInputSlot() == ((SoulissTypical)triggeredTypicalSpinner.getItemAtPosition(u)).getSlot())
                    triggeredTypicalSpinner.setSelection(u);
            }
            threshValEditText.setText(String.valueOf(inputTrigger.getThreshVal()));
            threshButton.setText(inputTrigger.getOp());
        }
    }

    @SuppressLint({"NewApi", "NewApi"})
    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(getString(R.string.app_addprog));

        scenes = datasource.getScenes();
        outputNodeSpinner = findViewById(R.id.spinner2);
        fillNodeSpinnerWithExtra(outputNodeSpinner);
        outputTypicalSpinner = findViewById(R.id.spinner3);
        outputCommandSpinner = findViewById(R.id.spinnerCommand);

        radioTimed = findViewById(R.id.RadioButtonTime);
        radioPositional = findViewById(R.id.RadioButtonPosition);
        radioTrigger = findViewById(R.id.RadioButtonTriggered);
        // time based
        final TextView textviewTimed = findViewById(R.id.textviewtimed);
        checkboxRecursive = findViewById(R.id.checkBoxRecursive);
        commandTimePicker = findViewById(R.id.timePicker);
        commandSpinnerInterval = findViewById(R.id.spinnerRepeatEvery);
        // position based
        final TextView textviewPositional = findViewById(R.id.textViewInfoPos);
        togglehomeaway = findViewById(R.id.toggleButtonPosition);
        // data based
        final TextView textviewTriggered = findViewById(R.id.textViewInfoTrig);
        triggeredNodeSpinner = findViewById(R.id.Spinner05);
        triggeredTypicalSpinner = findViewById(R.id.Spinner06);
        fillNodeSpinner(triggeredNodeSpinner);
        threshButton = findViewById(R.id.buttonComparator);
        threshValEditText = findViewById(R.id.editTextThreshold);
        final Button btCancel = findViewById(R.id.buttonInsertProgram);
        final Button btSave = findViewById(R.id.buttonCancelProgram);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final OnItemSelectedListener nodeSelectionListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                setTypicalSpinner(outputTypicalSpinner, nodiArrayWithExtra[pos]);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };


        final OnItemSelectedListener typSelectedListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    // if (pos > 0) {
                    SoulissNode mynode = nodiArrayWithExtra[(int) outputNodeSpinner.getSelectedItemId()];
                Log.d(Constants.TAG, "mynode nodeId:" + mynode.getNodeId());
                if (mynode.getNodeId() > Constants.COMMAND_FAKE_SCENE) {
                        //le scene non hanno comandi
                    /*List<SoulissTypical> re = mynode
                            .getActiveTypicals(AddProgramActivity.this);
                    Log.d(Constants.TAG, "hack nodeId:" + re.get(pos).getNodeId());*/
                        fillCommandSpinner(outputCommandSpinner, (SoulissTypical) outputTypicalSpinner.getSelectedItem());
                    } else {
                        fillSceneCommandSpinner(outputCommandSpinner, (SoulissScene) outputTypicalSpinner.getSelectedItem());
                    }

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        final OnItemSelectedListener triggeredNodeSpinnerListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                setTypicalSpinner(triggeredTypicalSpinner, nodiArray[pos]);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        togglehomeaway.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /*
                    android:textOff="@string/addprogram_positional_goout"
                    android:textOn="@string/addprogram_positional_comeback"*/
                togglehomeaway.setText(togglehomeaway.isChecked()?getString(R.string.addprogram_positional_comeback):getString(R.string.addprogram_positional_goout));
                return false;
            }
        });


        OnClickListener firstRadioListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                radioPositional.setChecked(false);
                radioTrigger.setChecked(false);
                checkboxRecursive.setEnabled(true);
                commandTimePicker.setEnabled(true);
                commandSpinnerInterval.setEnabled(checkboxRecursive.isChecked());
                togglehomeaway.setEnabled(false);
                triggeredTypicalSpinner.setEnabled(false);
                triggeredNodeSpinner.setEnabled(false);
                threshButton.setEnabled(false);
                threshValEditText.setEnabled(false);
                textviewPositional.setEnabled(false);
                textviewTimed.setEnabled(true);
                textviewTriggered.setEnabled(false);
            }
        };
        radioTimed.setOnClickListener(firstRadioListener);

        OnClickListener posListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                radioTimed.setChecked(false);
                radioTrigger.setChecked(false);
                checkboxRecursive.setEnabled(false);
                commandTimePicker.setEnabled(false);
                commandSpinnerInterval.setEnabled(false);
                togglehomeaway.setEnabled(true);
                triggeredTypicalSpinner.setEnabled(false);
                triggeredNodeSpinner.setEnabled(false);
                threshButton.setEnabled(false);
                threshValEditText.setEnabled(false);
                textviewPositional.setEnabled(true);
                textviewTimed.setEnabled(false);
                textviewTriggered.setEnabled(false);
            }
        };
        radioPositional.setOnClickListener(posListener);

        OnClickListener triggeredListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                radioTimed.setChecked(false);
                radioPositional.setChecked(false);
                checkboxRecursive.setEnabled(false);
                commandTimePicker.setEnabled(false);
                commandSpinnerInterval.setEnabled(false);
                togglehomeaway.setEnabled(false);
                triggeredTypicalSpinner.setEnabled(true);
                triggeredNodeSpinner.setEnabled(true);
                threshButton.setEnabled(true);
                threshValEditText.setEnabled(true);
                textviewPositional.setEnabled(false);
                textviewTimed.setEnabled(false);
                textviewTriggered.setEnabled(true);
            }
        };
        radioTrigger.setOnClickListener(triggeredListener);
        // Check box ricorsivo, Interlock
        checkboxRecursive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                commandSpinnerInterval.setEnabled(isChecked);
            }
        });

        OnClickListener simplebttttt = new OnClickListener() {
            public void onClick(View v) {
                String old = threshButton.getText().toString();
                if (">".compareTo(old) == 0) {
                    threshButton.setText("=");
                } else if ("<".compareTo(old) == 0) {
                    threshButton.setText(">");
                } else {
                    threshButton.setText("<");
                }
            }
        };
        threshButton.setOnClickListener(simplebttttt);

        OnClickListener saveProgramButtonListener = new SaveProgramListener();
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

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.get("PROG") != null) {
            collected = (SoulissCommand) extras.get("PROG");
            Log.i(Constants.TAG, "Found a SAVED PROGRAM");

            // avoid auto call upon Creation with runnable
            outputTypicalSpinner.post(new Runnable() {
                public void run() {
                    outputTypicalSpinner.setOnItemSelectedListener(typSelectedListener);
                    outputNodeSpinner.setOnItemSelectedListener(nodeSelectionListener);
                }
            });
            triggeredNodeSpinner.post(new Runnable() {
                        public void run() {
                            triggeredNodeSpinner.setOnItemSelectedListener(triggeredNodeSpinnerListener);
                        }
                    }
            );
            setFields();
        } else {
            //explicitly riempi gli spinner
            outputTypicalSpinner.setOnItemSelectedListener(typSelectedListener);
            outputNodeSpinner.setOnItemSelectedListener(nodeSelectionListener);
            triggeredNodeSpinner.setOnItemSelectedListener(triggeredNodeSpinnerListener);
        }
    }//onStart


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
    private void fillNodeSpinnerWithExtra(Spinner tgt) {
        // spinner popolato in base nodeArray
        ArrayAdapter<SoulissNode> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                nodiArrayWithExtra);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tgt.setAdapter(adapter);
    }

    /**
     * Fills a spinner with nodes
     *
     * @param tgt
     */
    private void fillNodeSpinner(Spinner tgt) {
        // spinner popolato in base nodeArray
        ArrayAdapter<SoulissNode> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                nodiArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tgt.setAdapter(adapter);
    }

    /**
     * popola spinner tipici in base al nodo
     */
    private void setTypicalSpinner(Spinner tgt, SoulissNode ref) {
        if (ref.getNodeId() > Constants.COMMAND_FAKE_SCENE) {
            try {
                SoulissTypical[] strArray = new SoulissTypical[ref.getActiveTypicals().size()];
                ref.getActiveTypicals().toArray(strArray);

                ArrayAdapter<SoulissTypical> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, strArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tgt.setAdapter(adapter);

            } catch (Exception e) {
                Log.e(Constants.TAG, "Errore in setTypicalSpinner:" + e.getMessage(), e);
            }
        } else if (ref.getNodeId() == Constants.COMMAND_FAKE_SCENE) {

            SoulissScene[] strArray = new SoulissScene[scenes.size()];
            strArray = scenes.toArray(strArray);
            ArrayAdapter<SoulissScene> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, strArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            tgt.setAdapter(adapter);
        } else {
            Log.e(Constants.TAG, "UNPREDICTED");
        }
    }

    /**
     * popola spinner comandi in base al tipico. Mette nell'adapter i comandi
     * ottenuti da getCommands
     *
     * @param tgt Spinner da riempire
     * @param ref tipico da cui ottenere i comandi
     */
    private void fillCommandSpinner(@NonNull Spinner tgt, SoulissTypical ref) {
        List<ISoulissCommand> cmds = ref.getCommands(this);
        //maledetto bastardo
        if (cmds!=null&&cmds.size() > 0) {
            tvcommand.setVisibility(View.VISIBLE);
            tgt.setVisibility(View.VISIBLE);
            SoulissCommand[] strArray = new SoulissCommand[cmds.size()];
            strArray = cmds.toArray(strArray);
            Log.d(Constants.TAG, "Filled commandspinner, with commands :" + strArray.length);
            ArrayAdapter<SoulissCommand> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, strArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            tgt.setAdapter(adapter);
        }else{
            tgt.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Mette un comando fake, ovvero la scena che verra\ eseguita
     * NODEID = -2
     *
     * @param tgt Spinner da riempire
     * @param ref tipico da cui ottenere i comandi
     */
    private void fillSceneCommandSpinner(Spinner tgt, SoulissScene ref) {
        tgt.setVisibility(View.INVISIBLE);
        tvcommand.setVisibility(View.INVISIBLE);
        ISoulissExecutable[] strArray = new SoulissScene[1];
        strArray[0] = ref;

        ArrayAdapter<ISoulissExecutable> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, strArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tgt.setAdapter(adapter);
    }


    private class SaveProgramListener implements OnClickListener {

        public void onClick(View v) {
            ISoulissExecutable IToSave = (ISoulissExecutable) outputCommandSpinner.getSelectedItem();
            SoulissCommand programToSave = null;
            if (IToSave instanceof SoulissScene) {
                SoulissScene toExec = (SoulissScene) IToSave;
                SoulissCommandDTO dto = new SoulissCommandDTO();
                dto.setNodeId(Constants.COMMAND_FAKE_SCENE);
                dto.setSlot((short) toExec.getId());
                programToSave = new SoulissCommand(AddProgramActivity.this, dto);
            } else if (IToSave instanceof SoulissCommand) {
                programToSave = (SoulissCommand) IToSave;
                Log.i(Constants.TAG, "PERSISTING COMMAND NODEID:" + programToSave.getNodeId());
                //programToSave.setCommandId(collected.getCommandId());
                //programToSave.setNodeId((short) Constants.MASSIVE_NODE_ID);
                //programToSave.setSlot(((SoulissTypical)outputTypicalSpinner.getSelectedItem()).getTypicalDTO().getTypical());
            }
            if (programToSave == null) {
                Toast.makeText(AddProgramActivity.this, "Command not selected", Toast.LENGTH_SHORT).show();
                return;
            }

            //sceneId solo per i comandi che appartengono a una scena
            programToSave.setSceneId(null);


            Intent intent = AddProgramActivity.this.getIntent();
            SoulissDBHelper.open();
            if (radioTimed.isChecked()) {// temporal schedule
                Calendar baseNow = Calendar.getInstance();

                // se l'ora e` gia passata, fai domani
                if (commandTimePicker.getCurrentHour().compareTo(baseNow.get(Calendar.HOUR_OF_DAY)) < 0
                        || (commandTimePicker.getCurrentHour().compareTo(baseNow.get(Calendar.HOUR_OF_DAY)) == 0 && commandTimePicker
                        .getCurrentMinute().compareTo(baseNow.get(Calendar.MINUTE)) < 0)) {
                    baseNow.add(Calendar.DAY_OF_YEAR, 1);
                    Log.i(Constants.TAG, "Timed program delayed by one day");
                }
                baseNow.set(Calendar.HOUR_OF_DAY, commandTimePicker.getCurrentHour());
                baseNow.set(Calendar.MINUTE, commandTimePicker.getCurrentMinute());
                programToSave.setType(Constants.COMMAND_TIMED);
                programToSave.setScheduledTime(baseNow);
                if (checkboxRecursive.isChecked()) {

                    programToSave.setInterval(
                            spinnerArrVal[commandSpinnerInterval.getSelectedItemPosition()]);
                }
                // inserimento nuovo
                intent.putExtra("returnedData", Constants.COMMAND_TIMED);
            } else if (radioPositional.isChecked()) {// POSIZIONALE
                if (opzioni.getHomeLatitude() == 0) {
                    Toast.makeText(AddProgramActivity.this, getString(R.string.programs_warn_home_notset),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (togglehomeaway.isChecked()) {
                    programToSave.setType(Constants.COMMAND_GOAWAY_CODE);
                    intent.putExtra("returnedData", Constants.COMMAND_GOAWAY_CODE);
                } else {
                    programToSave.setType(Constants.COMMAND_COMEBACK_CODE);
                    intent.putExtra("returnedData", Constants.COMMAND_COMEBACK_CODE);
                }
            } else if (radioTrigger.isChecked()) {// TRIGGER
                SoulissTypical trig = ((SoulissTypical) triggeredTypicalSpinner.getSelectedItem());
                if (trig == null || trig.getTypicalDTO() == null || threshValEditText.getText() == null
                        || "".compareTo(threshValEditText.getText().toString()) == 0) {
                    Toast.makeText(AddProgramActivity.this, getString(R.string.programs_warn_trigger_notset),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("returnedData", Constants.COMMAND_TRIGGERED);
                programToSave.setType(Constants.COMMAND_TRIGGERED);
            }
            // MERGE
            if (collected != null)
                programToSave.setCommandId(collected.getCommandId());
            programToSave.persistCommand();

            if (radioTrigger.isChecked()) {// TRIGGER
                SoulissTypical trig = ((SoulissTypical) triggeredTypicalSpinner.getSelectedItem());
                SoulissTriggerDTO trigger = new SoulissTriggerDTO();
                //MERGE
                if (inputTrigger != null)
                    trigger.setTriggerId(inputTrigger.getTriggerId());
                trigger.setInputNodeId(trig.getNodeId());

                trigger.setInputSlot(trig.getSlot());
                trigger.setOp(threshButton.getText().toString());
                trigger.setCommandId(programToSave.getCommandId());
                try {
                    trigger.setThreshVal(Float.parseFloat(threshValEditText.getText().toString()));
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Can't parse threshold " + e.getMessage());
                }

                trigger.persist(datasource);
            }
            // datasource.close();
            AddProgramActivity.this.setResult(RESULT_OK, intent);
            AddProgramActivity.this.finish();
        }

    }
}
