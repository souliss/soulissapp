package it.angelic.soulissclient.fragments;

/**
 * Antifurto, deve poter leggere lo stato di tutti i sensori. Il pannello e` unico
 */

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import com.pheelicks.visualizer.VisualizerView;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical41AntiTheft;
import it.angelic.soulissclient.net.UDPHelper;

import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T42_Antitheft_Group;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T4n_Alarm;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T4n_Antitheft;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T4n_Armed;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T4n_NoAntitheft;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T4n_NotArmed;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T4n_ReArm;
import static junit.framework.Assert.assertTrue;

public class T4nFragment extends Fragment {
    private SoulissDBHelper datasource = new SoulissDBHelper(SoulissApp.getAppContext());
    private SoulissPreferenceHelper opzioni;

    private SwitchCompat toggleButton;

    private SoulissTypical collected;
    // private SoulissTypical related;
    //private Button btSleep;

    // Color change listener.

    private VisualizerView mVisualizerView;
    private TextView alarmInfoTextView;
    //private ToggleButton togMassive;
    private CheckBox notifCheckbox;
    private TextView infoTyp;
    private TextView textviewSensors;
    private Button resetButton;
    private SoulissTypical41AntiTheft senseiMaster;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(Constants.TAG, "Broadcast received, intent" + intent.toString());
                SoulissDBHelper.open();
                senseiMaster = datasource.getAntiTheftMasterTypical();
                alarmInfoTextView.setText(Html.fromHtml("<b>" + getString(R.string.antitheft_status) + "</b> "
                        + senseiMaster.getOutputDesc()));

                setSensorsView();
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
            }
        }
    };
    private List<SoulissTypical> sensei;

    public static T4nFragment newInstance(int index, SoulissTypical content) {
        T4nFragment f = new T4nFragment();

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
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;
        opzioni = SoulissApp.getOpzioni();
        View ret = inflater.inflate(R.layout.frag_t4n, container, false);
        datasource = new SoulissDBHelper(getActivity());
        SoulissDBHelper.open();
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
        assertTrue("TIPICO NULLO", collected!=null);
        collected.setPrefs(opzioni);


        toggleButton = (SwitchCompat) ret.findViewById(R.id.buttonPlus);
        resetButton = (Button) ret.findViewById(R.id.resetButton);

        alarmInfoTextView = (TextView) ret.findViewById(R.id.textviewAlarmInfo);
        notifCheckbox = (CheckBox) ret.findViewById(R.id.checkBoxnotifAndroid);
        infoTyp = (TextView) ret.findViewById(R.id.textView4nInfo);
        mVisualizerView = (VisualizerView) ret.findViewById(R.id.visualizerView);

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
        toggleButton.setOnClickListener(plus);

        // Listener generico
        OnClickListener resett = new OnClickListener() {
            public void onClick(View v) {
                // if (senseiMaster.getTypicalDTO().getOutput() ==
                // Souliss_T4n_InAlarm) {
                reset();
                // }
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
         * if (collected instanceof SoulissTypical12DigitalOutputAuto) {
		 * btSleep.setVisibility(View.GONE);
		 * alarmInfoTextView.setVisibility(View.GONE); // Check AUTO mode if
		 * (collected.getOutputLongDesc().contains("AUTO"))
		 * autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " ON"); else
		 * autoInfo.setText(getString(R.string.Souliss_Auto_mode) + " OFF"); }
		 * else if (collected instanceof SoulissTypical11DigitalOutput) {
		 * buttAuto.setVisibility(View.GONE); autoInfo.setVisibility(View.GONE);
		 * }
		 */
        setToggleButtonDrawable();
        // toggleButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.round_button));
        return ret;
    }

    private void setToggleButtonDrawable() {
        // sfondo bottone
        if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_Antitheft) {
            toggleButton.setChecked(true);
            toggleButton.setBackgroundDrawable(null);
        }
        // toggleButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.round_button));
        else if (senseiMaster.getTypicalDTO().getOutput() == Souliss_T4n_NoAntitheft) {
            toggleButton.setChecked(false);
            toggleButton.setBackgroundDrawable(null);
        } else if (senseiMaster.getTypicalDTO().getOutput() >= Souliss_T4n_Alarm) {
            toggleButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.alert_theft));
            toggleButton.setTextOn("ALARM");
            toggleButton.setTextOff("ALARM");
            // alarmInfoTextView.setText("Cycles to shutoff: " +
            // collected.getTypicalDTO().getOutput());
        } else {
            Log.w(Constants.TAG, "Unknown toggleButton Alarm status");
        }
    }


    /**
     * comandi sempre inviati al master
     */
    private void shutoff() {
        Thread t = new Thread() {
            public void run() {
                Looper.prepare();
                UDPHelper.issueSoulissCommand("" + senseiMaster.getParentNode().getId(), ""
                        + senseiMaster.getTypicalDTO().getSlot(), opzioni, ""
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
                        + senseiMaster.getTypicalDTO().getSlot(), opzioni, ""
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
                    ft.replace(R.id.detailPane, details);
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
        SoulissDBHelper.open();
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

    private void setSensorsView() {
        StringBuilder tmp = new StringBuilder();

        sensei = datasource.getAntiTheftSensors();
        for (SoulissTypical soulissTypical42AntiTheftPeer : sensei) {
            tmp.append(soulissTypical42AntiTheftPeer.getParentNode().getNiceName())
                    .append(" - ")
                    .append(soulissTypical42AntiTheftPeer.getNiceName())
                    .append(" - ")
                    .append(soulissTypical42AntiTheftPeer.getOutputDesc()).append("\n");
        }
        textviewSensors.setText(Html.fromHtml(tmp.toString()));
    }

    /**
     * Interface describing a color change listener.
     */
    public interface OnColorChangedListener {
        /**
         * Method colorChanged is called when a new color is selected.
         *
         * @param color new color.
         */
        void colorChanged(int color);
    }
}
