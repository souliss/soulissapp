package it.angelic.soulissclient.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.HalfFloatUtils;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;
import it.angelic.soulissclient.net.UDPHelper;
import it.angelic.soulissclient.views.NumberPickerT6;
import it.angelic.tagviewlib.SimpleTagRelativeLayout;

import static it.angelic.soulissclient.Constants.Typicals.Souliss_T31;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_AsMeasured;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_Cooling;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_FanAuto;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_FanHigh;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_FanLow;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_FanMed;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_Heating;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_Set;
import static it.angelic.soulissclient.Constants.Typicals.Souliss_T3n_ShutOff;
import static junit.framework.Assert.assertTrue;


public class T31HeatingFragment extends AbstractTypicalFragment implements NumberPicker.OnValueChangeListener {
    private Button asMeasuredButton;
    private Button buttOff;
    private Button buttOn;
    private SoulissTypical31Heating collected;
    private SoulissDBHelper datasource = new SoulissDBHelper(SoulissApp.getAppContext());
    private Spinner fanSpiner;
    private Spinner functionSpinner;
    private FrameLayout hvacChart;
    private ImageView imageFan1;
    private ImageView imageFan2;
    private ImageView imageFan3;
    private EditText incrementText;
    private Handler mHandler;
    private SoulissPreferenceHelper opzioni;
    private NumberPickerT6 tempSlider;
    private TextView textViewTagDescgroup;
    private TextView textviewStatus;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(Constants.TAG, "Broadcast received, TODO change Spinners status intent" + intent.toString());
                SoulissDBHelper.open();
                SoulissNode coll = datasource.getSoulissNode(collected.getTypicalDTO().getNodeId());
                collected = (SoulissTypical31Heating) coll.getTypical(collected.getTypicalDTO().getSlot());
                refreshStatusIcon();
                //refreshTagsInfo();
                textviewStatus.setText(collected.getTypedOutputValue());
                Log.e(Constants.TAG, "Setting Temp Slider:" + collected.getTemperatureSetpointVal());

                if (collected.isFannTurnedOn(1))
                    imageFan1.setVisibility(View.VISIBLE);
                else
                    imageFan1.setVisibility(View.INVISIBLE);
                if (collected.isFannTurnedOn(2))
                    imageFan2.setVisibility(View.VISIBLE);
                else
                    imageFan2.setVisibility(View.INVISIBLE);
                if (collected.isFannTurnedOn(3))
                    imageFan3.setVisibility(View.VISIBLE);
                else
                    imageFan3.setVisibility(View.INVISIBLE);


            } catch (Exception e) {
                Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
            }


        }
    };
    private View viewTagDescgroup;

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

        opzioni = SoulissApp.getOpzioni();
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
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflater.inflate(R.menu.queue_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;
        opzioni = SoulissApp.getOpzioni();
        View ret = inflater.inflate(R.layout.frag_t31, container, false);
        datasource = new SoulissDBHelper(getActivity());
        SoulissDBHelper.open();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.get("TIPICO") != null) {
            collected = (SoulissTypical31Heating) extras.get("TIPICO");
        } else if (getArguments() != null) {
            collected = (SoulissTypical31Heating) getArguments().get("TIPICO");
        } else {
            Log.e(Constants.TAG, "Error retriving node:");
            return ret;
        }
        //refresh forzato
        final SoulissNode coll = datasource.getSoulissNode(collected.getTypicalDTO().getNodeId());
        collected = (SoulissTypical31Heating) coll.getTypical(collected.getTypicalDTO().getSlot());

        assertTrue("TIPICO NULLO", collected instanceof SoulissTypical31Heating);
        collected.setPrefs(opzioni);

        super.setCollected(collected);
        refreshStatusIcon();

        buttOn = (Button) ret.findViewById(R.id.buttonTurnOn);
        buttOff = (Button) ret.findViewById(R.id.buttonTurnOff);
        textviewStatus = (TextView) ret.findViewById(R.id.textviewStatus);
        textViewTagDescgroup = (TextView) ret.findViewById(R.id.TextViewTagDescgroup);
        viewTagDescgroup = ret.findViewById(R.id.TagDiv);
        tempSlider = (NumberPickerT6) ret.findViewById(R.id.tempSlider);
        functionSpinner = (Spinner) ret.findViewById(R.id.spinnerFunction);
        fanSpiner = (Spinner) ret.findViewById(R.id.spinnerFan);
        asMeasuredButton = (Button) ret.findViewById(R.id.asMeasuredButton);
        infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);
        imageFan1 = (ImageView) ret.findViewById(R.id.ImageFan1);
        imageFan2 = (ImageView) ret.findViewById(R.id.ImageFan2);
        imageFan3 = (ImageView) ret.findViewById(R.id.ImageFan3);
        incrementText = (EditText) ret.findViewById(R.id.editTextIncrement);
        tagView = (SimpleTagRelativeLayout) ret.findViewById(R.id.tag_group);
        //hvacChart = (FrameLayout) ret.findViewById(R.id.hvacChart);

        final android.support.v4.app.FragmentManager manager = getActivity().getSupportFragmentManager();
        //Fragment details = manager.findFragmentById(R.id.hvacChart);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ChartFragment NewFrag = ChartFragment.newInstance(collected);
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.hvacChart, NewFrag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        }, 1000);


        if (!collected.getTypicalDTO().isTagged()) {
            textViewTagDescgroup.setVisibility(View.GONE);
            viewTagDescgroup.setVisibility(View.GONE);
        }
        refreshTagsInfo();


        /**
         * LISTENER SPINNER DESTINATARIO, IN TESTATA
         */
        final OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {//fa cagare
                    collected.issueCommand(Souliss_T3n_Cooling, null);
                } else if (pos == 1) {
                    collected.issueCommand(Souliss_T3n_Heating, null);
                } else {
                    collected.issueCommand(Souliss_T3n_ShutOff, null);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        final OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {//fa cagare
                    collected.issueCommand(Souliss_T3n_FanAuto, null);
                } else if (pos == 1) {
                    collected.issueCommand(Souliss_T3n_FanHigh, null);
                } else if (pos == 2) {
                    collected.issueCommand(Souliss_T3n_FanMed, null);
                } else if (pos == 3) {
                    collected.issueCommand(Souliss_T3n_FanLow, null);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        // avoid auto call upon Creation with runnable
        functionSpinner.post(new Runnable() {
            public void run() {
                if (!collected.isTurnedOn())
                    functionSpinner.setSelection(2);
                else
                    functionSpinner.setSelection(collected.isCoolMode() ? 0 : 1, false);
                functionSpinner.setOnItemSelectedListener(lit);
                fanSpiner.setOnItemSelectedListener(lib);
            }
        });
        tempSlider.setModel(Souliss_T31);
        incrementText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                tempSlider.invalidate();
                // visto che non fa lui, faccio io
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UDPHelper.pollRequest(opzioni, 1, coll.getNodeId());
                    }
                }).start();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (Float.valueOf(s.toString()) < 0.1f || Float.valueOf(s.toString()) > 10f)
                        throw new Exception();
                    tempSlider.setIncrement(Float.valueOf(s.toString()));
                    int sel = tempSlider.generateDisplayValues(tempSlider.getRealVal());
                    tempSlider.setValue(sel);
                    tempSlider.invalidate();
                } catch (Exception er) {
                    incrementText.setError(getContext().getString(R.string.increment_input_err));
                }
            }
        });
        // int sel = tempSlider.generateDisplayValues(collected.getTemperatureSetpointVal());
        tempSlider.setRealVal(collected.getTemperatureSetpointVal());
        // tempSlider.setValue(( collected.getTemperatureSetpointVal()));
        tempSlider.setWrapSelectorWheel(false);
        //tempSlider.setDisplayedValues(nums);
        tempSlider.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        tempSlider.setOnValueChangedListener(this);

        // Listener generico
        OnClickListener asMeasuredListener = new OnClickListener() {
            public void onClick(View v) {
                collected.issueCommand(Souliss_T3n_AsMeasured, null);
                return;
            }

        };
        asMeasuredButton.setOnClickListener(asMeasuredListener);

        buttOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //int act = Integer.parseInt(textviewTemperature.getText().toString());
                if (functionSpinner.getSelectedItemPosition() == 0)
                    collected.issueCommand(Souliss_T3n_Cooling, Float.valueOf(tempSlider.getValue()));
                else if (functionSpinner.getSelectedItemPosition() == 1)
                    collected.issueCommand(Souliss_T3n_Heating, Float.valueOf(tempSlider.getValue()));
                //else OFF, non fare nulla
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
                    ft.replace(R.id.detailPane, details);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    ft.commit();
                } else {
                    Log.i(Constants.TAG, "Close fragment");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(getFragmentManager().findFragmentById(R.id.detailPane)).commit();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(datareceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        SoulissDBHelper.open();
        IntentFilter filtere = new IntentFilter();
        filtere.addAction("it.angelic.soulissclient.GOT_DATA");
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        getActivity().registerReceiver(datareceiver, filtere);
        refreshTagsInfo();
        //tempSlider.setValue(((int) collected.getTemperatureSetpointVal()));
        //Ask first refresh
        collected.issueRefresh();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Thread t = new Thread() {
            public void run() {
                // collected.issueCommand(Souliss_T3n_Set, Float.valueOf(tempSlider.getValue()));
                Float work = Float.parseFloat(tempSlider.getDisplayedValues()[tempSlider.getValue()]);
                int re = HalfFloatUtils.fromFloat(work);
                String first, second;
                String pars = Long.toHexString(re);
                Log.i(Constants.TAG, "PARSED SETPOINT TEMP: 0x" + pars);

                try {
                    second = Integer.toString(Integer.parseInt(pars.substring(0, 2), 16));
                } catch (StringIndexOutOfBoundsException sie) {
                    second = "0";
                }
                try {
                    first = Integer.toString(Integer.parseInt(pars.substring(2, 4), 16));
                } catch (StringIndexOutOfBoundsException sie) {
                    first = "0";
                }
                //INVERTITI? Occhio
                final String[] cmd = {"" + Souliss_T3n_Set, "0", "0", first, second};
                //verifyCommand(temp, first, second);

                UDPHelper.issueSoulissCommand("" + collected.getNodeId(), "" + collected.getSlot(),
                        SoulissApp.getOpzioni(), cmd);
            }
        };
        t.start();

    }
}
