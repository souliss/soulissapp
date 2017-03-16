package it.angelic.soulissclient.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.pheelicks.visualizer.VisualizerView;
import com.pheelicks.visualizer.renderer.BarGraphRenderer;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import it.angelic.tagviewlib.SimpleTagRelativeLayout;

import static junit.framework.Assert.assertTrue;

public class T19SingleChannelLedFragment extends AbstractMusicVisualizerFragment {


    private SoulissDBHelper datasource;

    private SoulissTypical19AnalogChannel collected;
    private TextView eqText;

    private int intensity = 0;
    // Color change listener.
    private VisualizerView mVisualizerView;
    private boolean continueIncrementing;
    private boolean continueDecrementing;
    private TextView textviewHistoryTags;
    private SwitchCompat togMulticast;
    private TableRow tableRowVis;
    private TableRow tableRowChannel;
    private View tableRowLamp;
    private SeekBar seekChannelIntensity;
    private TextView singleChanabel;

    /**
     * Serve per poter tenuto il bottone brightness
     *
     * @param cmd
     */
    private void startIncrementing(final Short cmd) {
        setIsIncrementing(true);
        new Thread(new Runnable() {
            public void run() {
                while (isIncrementing()) {
                    intensity += 5;
                    collected.issueSingleChannelCommand(Constants.Typicals.Souliss_T1n_BrightUp, togMulticast.isChecked());
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Log.e(Constants.TAG, "Error Thread.sleep:" + e.getMessage());
                    }
                }
            }
        }).start();
    }

    synchronized private void stopIncrementing() {
        setIsIncrementing(false);
        collected.issueRefresh();
    }

    synchronized private boolean isIncrementing() {
        return continueIncrementing;
    }

    /**
     * Serve per poter tenuto il bottone brightness
     *
     * @param cmd
     */
    private void startDecrementing(final Short cmd) {
        setIsDecrementing(true);
        new Thread(new Runnable() {
            public void run() {
                while (isDecrementing() && intensity > 5) {
                    intensity -= 5;
                    collected.issueSingleChannelCommand(Constants.Typicals.Souliss_T1n_BrightDown, togMulticast.isChecked());
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Log.e(Constants.TAG, "Error Thread.sleep:" + e.getMessage());
                    }
                }
            }
        }).start();
    }

    synchronized private void stopDecrementing() {
        setIsDecrementing(false);
        collected.issueRefresh();
    }

    /**
     * Per gestire tasto premuto
     *
     * @param newSetting
     */
    synchronized void setIsIncrementing(boolean newSetting) {
        continueIncrementing = newSetting;
    }

    synchronized private boolean isDecrementing() {
        return continueDecrementing;
    }

    synchronized void setIsDecrementing(boolean newSetting) {
        continueDecrementing = newSetting;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;
        opzioni = SoulissApp.getOpzioni();
        View ret = inflater.inflate(R.layout.frag_t19_singlechannel, container, false);
        datasource = new SoulissDBHelper(getActivity());
        SoulissDBHelper.open();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.get("TIPICO") != null) {
            collected = (SoulissTypical19AnalogChannel) extras.get("TIPICO");
        } else if (getArguments() != null) {
            collected = (SoulissTypical19AnalogChannel) getArguments().get("TIPICO");
        } else {
            try {
                // try emergency
                Log.w(Constants.TAG, "Attempting emergency load");
                collected = (SoulissTypical19AnalogChannel) datasource.getTypical(collected.getTypicalDTO()
                        .getNodeId(), collected.getTypicalDTO().getSlot());
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error retriving node:" + e.getMessage());
                return ret;
            }
        }

        super.setCollected(collected);
        collected.issueRefresh();
        /*super.actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
		super.actionBar.setCustomView(R.layout.custom_actionbar); // load
		super.actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM); // show
		super.actionBar.setDisplayHomeAsUpEnabled(true);
        refreshStatusIcon();*/

        assertTrue("TIPICO NULLO", collected instanceof SoulissTypical19AnalogChannel);
        collected.setPrefs(opzioni);

        Button buttPlus = (Button) ret.findViewById(R.id.buttonPlus);
        Button buttMinus = (Button) ret.findViewById(R.id.buttonMinus);
        togMulticast = (SwitchCompat) ret.findViewById(R.id.checkBoxMulticast);

        Button btOff = (Button) ret.findViewById(R.id.buttonTurnOff);
        Button btOn = (Button) ret.findViewById(R.id.buttonTurnOn);
        tableRowLamp = ret.findViewById(R.id.tableRowLamp);
        tableRowChannel = (TableRow) ret.findViewById(R.id.tableRowChannel);
        eqText = (TextView) ret.findViewById(R.id.textEqualizer);
        Button btFlash = (Button) ret.findViewById(R.id.flash);
        Button btSleep = (Button) ret.findViewById(R.id.sleep);
        Spinner modeSpinner = (Spinner) ret.findViewById(R.id.modeSpinner);
        tableRowVis = (TableRow) ret.findViewById(R.id.tableRowMusic);


        FrameLayout mVisualizerViewFrame = (FrameLayout) ret.findViewById(R.id.visualizerViewFrame);
        //permesso per la visualizer connessa all'audio o mic
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    Constants.MY_PERMISSIONS_RECORD_AUDIO);
            mVisualizerView = null;
        } else {
            inflater.inflate(R.layout.custom_visview, mVisualizerViewFrame);
            mVisualizerView = (VisualizerView) mVisualizerViewFrame.findViewById(R.id.visualizerView);
            //mVisualizerViewFrame.addView(mVisualizerView);
            mVisualizerView.setOpz(opzioni);
        }

        seekChannelIntensity = (SeekBar) ret.findViewById(R.id.channelRed);

        singleChanabel = (TextView) ret.findViewById(R.id.channelLabel);
        //	buttLamp = (ImageView) ret.findViewById(R.id.buttonLamp);
        btOff.setTag(Constants.Typicals.Souliss_T1n_OffCmd);
        btOn.setTag(Constants.Typicals.Souliss_T1n_OnCmd);
        buttPlus.setTag(Constants.Typicals.Souliss_T1n_BrightUp);
        buttMinus.setTag(Constants.Typicals.Souliss_T1n_BrightDown);
        btFlash.setTag(Constants.Typicals.Souliss_T1n_Flash);
        btSleep.setTag(Constants.Typicals.Souliss_T_related);
        TableRow infoFavs = (TableRow) ret.findViewById(R.id.tableRowFavInfo);
        TableRow infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);
        tagView = (SimpleTagRelativeLayout) ret.findViewById(R.id.tag_group);
        refreshTagsInfo();
        // CHANNEL Listeners
        seekChannelIntensity.setOnSeekBarChangeListener(new channelInputListener());

        final OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {// channels
                    tableRowVis.setVisibility(View.GONE);
                    mVisualizerView.setVisibility(View.GONE);
                    tableRowLamp.setVisibility(View.VISIBLE);
                    tableRowChannel.setVisibility(View.VISIBLE);
                    mVisualizerView.setEnabled(false);
                    eqText.setVisibility(View.GONE);
                    // TODO questi non vanno
                    seekChannelIntensity.setProgress(intensity);
                } else {// music
                    mVisualizerView.setFrag(T19SingleChannelLedFragment.this);
                    mVisualizerView.link(togMulticast.isChecked());
                    addBarGraphRenderers();
                    tableRowLamp.setVisibility(View.GONE);
                    mVisualizerView.setVisibility(View.VISIBLE);
                    tableRowVis.setVisibility(View.VISIBLE);
                    mVisualizerView.setEnabled(true);
                    mVisualizerView.link(togMulticast.isChecked());
                    eqText.setVisibility(View.VISIBLE);
                    tableRowChannel.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        // avoid auto call upon Creation with runnable
        // modeSpinner.post(new Runnable() {
        // public void run() {
        modeSpinner.setOnItemSelectedListener(lib);
        // }
        // });

        // Listener generico
        OnClickListener plus = new OnClickListener() {
            public void onClick(View v) {
                Short cmd = (Short) v.getTag();
                assertTrue(cmd != null);
                collected.issueSingleChannelCommand(cmd, togMulticast.isChecked());
                //collected.issueRefresh();
            }

        };

        // start thread x decremento
        OnTouchListener incListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Short cmd = (Short) v.getTag();
                assertTrue(cmd != null);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startIncrementing(cmd);
                        v.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        stopIncrementing();
                        v.setPressed(false);
                        break;
                }
                v.performClick();
                return true;
            }

        };
        OnTouchListener decListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Short cmd = (Short) v.getTag();
                assertTrue(cmd != null);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startDecrementing(cmd);
                        v.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        stopDecrementing();
                        v.setPressed(false);
                        break;
                }
                v.performClick();
                return true;
            }

        };
        buttPlus.setOnTouchListener(incListener);
        buttMinus.setOnTouchListener(decListener);
        btOff.setOnClickListener(plus);
        btOn.setOnClickListener(plus);
        btFlash.setOnClickListener(plus);
        btSleep.setOnClickListener(plus);

        String strDisease2Format = getResources().getString(R.string.Souliss_TRGB_eq);
        String strDisease2Msg = String.format(strDisease2Format, Constants.twoDecimalFormat.format(opzioni.getEqLow()),
                Constants.twoDecimalFormat.format(opzioni.getEqMed()),
                Constants.twoDecimalFormat.format(opzioni.getEqHigh()));
        eqText.setText(strDisease2Msg);

        return ret;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Rinomina nodo e scelta icona
        inflater.inflate(R.menu.t16_ctx_menu, menu);
        Log.i(Constants.TAG, "Inflated Equalizer menu");
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Methods for adding renderers to visualizer
    private void addBarGraphRenderers() {
        Paint paint = new Paint();
        paint.setStrokeWidth(50f);
        paint.setAntiAlias(false);
        paint.setColor(Color.argb(255, 156, 138, 252));
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(32, paint, false);
        mVisualizerView.addRenderer(barGraphRendererBottom);

        // TOP
        Paint paint2 = new Paint();
        paint2.setStrokeWidth(12f);
        paint2.setAntiAlias(false);
        paint2.setColor(Color.argb(255, 181, 11, 233));
        BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
        mVisualizerView.addRenderer(barGraphRendererTop);
    }

    public static T19SingleChannelLedFragment newInstance(int index, SoulissTypical content) {
        T19SingleChannelLedFragment f = new T19SingleChannelLedFragment();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.equalizer:
                AlertDialogHelper.equalizerDialog(getActivity(), eqText, this, getActivity()).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        SoulissDBHelper.open();
        if (collected != null) {
            collected = (SoulissTypical19AnalogChannel) datasource.getTypical(collected.getTypicalDTO()
                    .getNodeId(), collected.getSlot());
            collected.issueRefresh();
        }
        IntentFilter filtere = new IntentFilter();
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
            // SoulissNode coll = datasource.getSoulissNode();
            collected = (SoulissTypical19AnalogChannel) datasource.getTypical(collected
                    .getNodeId(), collected.getSlot());
            // Bundle extras = intent.getExtras();
            // Bundle vers = (Bundle) extras.get("NODES");
            int intensityReal = collected.getIntensity();
            Log.d(Constants.TAG, "Detected data arrival, intensity change to: " + intensityReal);
                singleChanabel.setText(getString(R.string.Souliss_T19_received) + " " + collected.getOutputDesc());
            seekChannelIntensity.setProgress(intensityReal);
            refreshStatusIcon();
        }
    };

    /**
     * Inner class representing the intensity Channels.
     */
    private class channelInputListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                // curva quadratica
                int val = seekChannelIntensity.getProgress();
			/*int out = (int) (val * val * 255 / 100 / 100);
			if (out > 255)
				out = 255;*/
                intensity = val;
                collected.issueSingleChannelCommand(Constants.Typicals.Souliss_T1n_Set, intensity,
                        togMulticast.isChecked());
                singleChanabel.setText(getString(R.string.Souliss_T19_set) + " " + val);
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        // solo per sicurezza
        public void onStopTrackingTouch(SeekBar seekBar) {
            //commented after https://github.com/souliss/soulissapp/issues/80
            //collected.issueRefresh();//
        }

    }

    /**
     * Souliss RGB light command Souliss OUTPUT Data is:
     * <p/>
     * <p/>
     * INPUT data 'read' from GUI
     */
    public void issueRGBCommand(final short val, final int r, final int g, final int b, final boolean multicast) {
        collected.issueSingleChannelCommand(val, (r + g + b) / 3, multicast);
    }

}
