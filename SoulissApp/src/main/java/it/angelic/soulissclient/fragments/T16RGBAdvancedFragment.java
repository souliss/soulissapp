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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.pheelicks.visualizer.VisualizerView;
import com.pheelicks.visualizer.renderer.BarGraphRenderer;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.db.SoulissDBHelper;
import it.angelic.soulissclient.model.typicals.SoulissTypical16AdvancedRGB;
import it.angelic.soulissclient.util.CCFAnimator;
import it.angelic.tagviewlib.SimpleTagRelativeLayout;

import static junit.framework.Assert.assertTrue;

public class T16RGBAdvancedFragment extends AbstractMusicVisualizerFragment {
    private TextView blueChanabel;
    private Button btFeedBackPatern;
    private Button btFlash;
    private Button btOff;
    private Button btOn;
    private Button btSleep;
    private Button btStartPattern;
    private Button btStopPattern;
    private Button btWhite;
    private Button buttMinus;
    private Button buttPlus;
    private CheckBox checkBoxScatter;
    private SoulissTypical16AdvancedRGB collected;
    private int color = 0;
    private ColorSeekBar colorSeekBar;
    private ColorSeekBar colorSeekBarStop;
    private RelativeLayout colorSwitchRelativeLayout;
    // private Runnable senderThread;
    private boolean continueDecrementing;
    private boolean continueIncrementing;
    private ColorPickerView cpv;
    private SoulissDBHelper datasource = new SoulissDBHelper(getActivity());
    // private SoulissTypical related;
    // Aggiorna il feedback
    private BroadcastReceiver datareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // SoulissNode coll = datasource.getSoulissNode();
            try {
                collected = (SoulissTypical16AdvancedRGB) datasource.getTypical(collected
                        .getNodeId(), collected.getSlot());
                // Bundle extras = intent.getExtras();
                // Bundle vers = (Bundle) extras.get("NODES");
                // color = collected.getColor();
                if (collected.getOutput() == Constants.Typicals.Souliss_T1n_OffCoil) {
                    cpv.setCenterColor(getResources().getColor(R.color.black));
                } else {
                    Log.d(Constants.TAG, "RGB Out:" + collected.getOutput());

                    cpv.setCenterColor(Color.argb(255, Color.red(collected.getColor()),
                            Color.green(collected.getColor()), Color.blue(collected.getColor())));
                    color = Color.argb(255, Color.red(collected.getColor()),
                            Color.green(collected.getColor()), Color.blue(collected.getColor()));
                    Log.d(Constants.TAG, "Detected data arrival, color change to: R" + Color.red(color)
                            + " G" + Color.green(color) + " B" + Color.blue(color));
                }
                cpv.invalidate();
            } catch (Exception e) {
                Log.e(Constants.TAG, "Errore broadcast Receive! " + e.getMessage());
            }
            refreshStatusIcon();
        }
    };
    // Color change listener.
    private OnColorChangedListener dialogColorChangedListener = null;
    private TextView eqText;
    private TextView greenChanabel;
    // private CheckBox checkMusic;
    private VisualizerView mVisualizerView;
    private FrameLayout mVisualizerViewFrame;
    private Spinner modeSpinner;
    private SoulissPreferenceHelper opzioni;
    private CCFAnimator patternAnimator;
    private Thread patternRunner;
    private boolean patternRunning;
    private TextView redChanabel;
    private SeekBar seekChannelBlue;
    private SeekBar seekChannelGreen;
    private SeekBar seekChannelRed;
    private SeekBar sliderSpeed;
    private TableRow tableRowChannel;
    private TableRow tableRowEq;
    private TableRow tableRowPatterns;
    private TableRow tableRowVis;
    private SwitchCompat togMulticast;

    public static T16RGBAdvancedFragment newInstance(int index, SoulissTypical content) {
        T16RGBAdvancedFragment f = new T16RGBAdvancedFragment();

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

    synchronized private boolean isDecrementing() {
        return continueDecrementing;
    }

    synchronized private boolean isIncrementing() {
        return continueIncrementing;
    }

    /**
     * ***********************************************************************
     * Souliss RGB light command Souliss OUTPUT Data is:
     * <p/>
     * <p/>
     * INPUT data 'read' from GUI
     * ************************************************************************
     */
    public void issueRGBCommand(final short val, final int r, final int g, final int b, final boolean multicast) {
        collected.issueRGBCommand(val, r, g, b, multicast);
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
        if (!opzioni.isDbConfigured()) {
            AlertDialogHelper.dbNotInitedDialog(getActivity());
        }
        patternAnimator = CCFAnimator.rgb(Color.RED, Color.BLUE);
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Rinomina nodo e scelta icona
        inflater.inflate(R.menu.t16_ctx_menu, menu);
        Log.i(Constants.TAG, "Inflated Equalizer menu");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;
        opzioni = SoulissApp.getOpzioni();
        View ret = inflater.inflate(R.layout.frag_t16_rgb, container, false);
        datasource = new SoulissDBHelper(getActivity());
        SoulissDBHelper.open();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.get("TIPICO") != null) {
            collected = (SoulissTypical16AdvancedRGB) extras.get("TIPICO");
        } else if (getArguments() != null) {
            collected = (SoulissTypical16AdvancedRGB) getArguments().get("TIPICO");
        } else {
            Log.e(Constants.TAG, "Error retriving node!");
            return ret;
        }
        assertTrue("TIPICO NULLO", collected != null);
        collected.setPrefs(opzioni);
        super.setCollected(collected);

        refreshStatusIcon();

        buttPlus = (Button) ret.findViewById(R.id.buttonPlus);
        buttMinus = (Button) ret.findViewById(R.id.buttonMinus);
        togMulticast = (SwitchCompat) ret.findViewById(R.id.checkBoxMulticast);
        togMulticast.setChecked(opzioni.isRgbSendAllDefault());
        btOff = (Button) ret.findViewById(R.id.buttonTurnOff);
        btOn = (Button) ret.findViewById(R.id.buttonTurnOn);
        btStartPattern = (Button) ret.findViewById(R.id.buttonStartPattern);
        btStopPattern = (Button) ret.findViewById(R.id.buttonStopPattern);
        btFeedBackPatern = (Button) ret.findViewById(R.id.buttonPatternFeedback);
        // checkMusic = (CheckBox) ret.findViewById(R.id.checkBoxMusic);
        tableRowChannel = (TableRow) ret.findViewById(R.id.tableRowChannel);
        tableRowPatterns = (TableRow) ret.findViewById(R.id.tableRowPatterns);
        tableRowEq = (TableRow) ret.findViewById(R.id.tableRowEqualizer);
        btWhite = (Button) ret.findViewById(R.id.white);
        btFlash = (Button) ret.findViewById(R.id.flash);
        btSleep = (Button) ret.findViewById(R.id.sleep);
        modeSpinner = (Spinner) ret.findViewById(R.id.modeSpinner);
        tableRowVis = (TableRow) ret.findViewById(R.id.tableRowMusic);
        tagView = (SimpleTagRelativeLayout) ret.findViewById(R.id.tag_group);
        sliderSpeed = (SeekBar) ret.findViewById(R.id.sliderSpeed);
        checkBoxScatter = (CheckBox) ret.findViewById(R.id.checkBoxScatter);
        mVisualizerViewFrame = (FrameLayout) ret.findViewById(R.id.visualizerViewFrame);
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
        colorSwitchRelativeLayout = (RelativeLayout) ret.findViewById(R.id.colorSwitch);

        seekChannelRed = (SeekBar) ret.findViewById(R.id.channelRed);
        seekChannelGreen = (SeekBar) ret.findViewById(R.id.channelGreen);
        seekChannelBlue = (SeekBar) ret.findViewById(R.id.channelBlue);

        redChanabel = (TextView) ret.findViewById(R.id.channelRedLabel);
        blueChanabel = (TextView) ret.findViewById(R.id.channelBlueLabel);
        greenChanabel = (TextView) ret.findViewById(R.id.channelGreenLabel);

        colorSeekBar = (ColorSeekBar) ret.findViewById(R.id.colorSlider);
        colorSeekBarStop = (ColorSeekBar) ret.findViewById(R.id.colorSliderStop);
        btOff.setTag(Constants.Typicals.Souliss_T1n_OffCmd);
        btOn.setTag(Constants.Typicals.Souliss_T1n_OnCmd);
        buttPlus.setTag(Constants.Typicals.Souliss_T1n_BrightUp);
        buttMinus.setTag(Constants.Typicals.Souliss_T1n_BrightDown);
        btFlash.setTag(Constants.Typicals.Souliss_T1n_Flash);
        btSleep.setTag(Constants.Typicals.Souliss_T_related);
        infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);

        eqText = (TextView) ret.findViewById(R.id.textEqualizer);

        // avoid auto call upon Creation with runnable
        seekChannelRed.post(new Runnable() {
            public void run() {
                // CHANNEL Listeners
                seekChannelRed.setOnSeekBarChangeListener(new channelInputListener());
                seekChannelGreen.setOnSeekBarChangeListener(new channelInputListener());
                seekChannelBlue.setOnSeekBarChangeListener(new channelInputListener());
            }
        });


        refreshTagsInfo();

        final OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {// cerchio RGB
                    tableRowVis.setVisibility(View.GONE);
                    tableRowPatterns.setVisibility(View.GONE);
                    mVisualizerViewFrame.setVisibility(View.GONE);
                    tableRowChannel.setVisibility(View.GONE);
                    patternRunning = false;
                    tableRowEq.setVisibility(View.INVISIBLE);
                    if (mVisualizerView != null) {
                        mVisualizerView.setVisibility(View.GONE);
                        mVisualizerView.setEnabled(false);
                    }
                    colorSwitchRelativeLayout.setVisibility(View.VISIBLE);
                    eqText.setVisibility(View.GONE);
                    cpv.setCenterColor(Color.argb(255, Color.red(color),
                            Color.green(color), Color.blue(color)));
                } else if (pos == 1) {// channels
                    tableRowPatterns.setVisibility(View.GONE);
                    tableRowVis.setVisibility(View.GONE);
                    if (mVisualizerView != null) {
                        mVisualizerView.setVisibility(View.GONE);
                        mVisualizerView.setEnabled(false);
                    }
                    mVisualizerViewFrame.setVisibility(View.GONE);
                    tableRowChannel.setVisibility(View.VISIBLE);
                    patternRunning = false;
                    colorSwitchRelativeLayout.setVisibility(View.GONE);
                    tableRowEq.setVisibility(View.INVISIBLE);
                    eqText.setVisibility(View.GONE);
                    // ok android 5
                    seekChannelRed.setProgress(0);
                    seekChannelRed.invalidate();
                    seekChannelGreen.setProgress(0);
                    seekChannelGreen.invalidate();
                    seekChannelBlue.setProgress(0);
                    seekChannelBlue.invalidate();
                    seekChannelRed.setProgress(Color.red(color));
                    seekChannelGreen.setProgress(Color.green(color));
                    seekChannelBlue.setProgress(Color.blue(color));
                    Log.i(Constants.TAG, "channel mode, color=" + Color.red(color) + " " + Color.green(color) + " " + Color.blue(color));
                    tableRowChannel.invalidate();
                } else if (pos == 2) {// music
                    tableRowPatterns.setVisibility(View.GONE);
                    patternRunning = false;
                    if (mVisualizerView != null) {
                        mVisualizerView.setFrag(T16RGBAdvancedFragment.this);
                        mVisualizerView.link(togMulticast.isChecked());
                        mVisualizerView.setVisibility(View.VISIBLE);
                        mVisualizerView.setEnabled(true);
                        //mVisualizerView.link(togMulticast.isChecked());
                        addBarGraphRenderers();
                    }
                    mVisualizerViewFrame.setVisibility(View.VISIBLE);
                    tableRowVis.setVisibility(View.VISIBLE);
                    colorSwitchRelativeLayout.setVisibility(View.GONE);

                    eqText.setVisibility(View.VISIBLE);
                    tableRowEq.setVisibility(View.VISIBLE);
                    tableRowChannel.setVisibility(View.GONE);
                } else {// patterns
                    tableRowPatterns.setVisibility(View.VISIBLE);
                    tableRowChannel.setVisibility(View.GONE);
                    tableRowVis.setVisibility(View.GONE);
                    patternRunning = true;
                    if (mVisualizerView != null) {
                        mVisualizerView.setVisibility(View.GONE);
                        mVisualizerView.setEnabled(false);
                    }
                    mVisualizerViewFrame.setVisibility(View.GONE);

                    colorSwitchRelativeLayout.setVisibility(View.GONE);
                    tableRowEq.setVisibility(View.INVISIBLE);
                    eqText.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        // avoid auto call upon Creation with runnable
        modeSpinner.post(new Runnable() {
            public void run() {
                modeSpinner.setOnItemSelectedListener(lib);
            }
        });

        // Listener generico
        OnClickListener plus = new OnClickListener() {
            public void onClick(View v) {
                Short cmd = (Short) v.getTag();
                assertTrue(cmd != null);
                issueRGBCommand(cmd, Color.red(color), Color.green(color), Color.blue(color), togMulticast.isChecked());

                new Thread(new Runnable() {
                    public void run() {
                        while (isIncrementing()) {
                            try {
                                Thread.sleep(250);
                                collected.issueRefresh();
                            } catch (InterruptedException e) {
                                Log.e(Constants.TAG, "Error Thread.sleep:");
                            }
                        }
                    }
                }).start();
            }
        };


        togMulticast.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    opzioni.setRgbSendAllDefault(true);
                } else {
                    opzioni.setRgbSendAllDefault(false);
                }
            }
        });

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

        // bianco manuale
        btWhite.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                issueRGBCommand(Constants.Typicals.Souliss_T1n_Set, 254, 254, 254,
                        togMulticast.isChecked());
            }
        });

        dialogColorChangedListener = new OnColorChangedListener() {
            /**
             * {@inheritDoc}
             */
            public void colorChanged(int c) {
                // Log.i(Constants.TAG, "color changed:" + c);
                color = c;
                collected.issueRGBCommand(Constants.Typicals.Souliss_T1n_Set,
                        Color.red(color), Color.green(color), Color.blue(color), togMulticast.isChecked());
                Log.d(Constants.TAG, "dialogColorChangedListener, color change to: R" + Color.red(color)
                        + " G" + Color.green(color) + " B" + Color.blue(color));
            }
        };
        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                patternAnimator = CCFAnimator.rgb(colorSeekBar.getColor(), colorSeekBarStop.getColor());
            }
        });
        colorSeekBarStop.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                patternAnimator = CCFAnimator.rgb(colorSeekBar.getColor(), colorSeekBarStop.getColor());
            }
        });
        //pattern animator
        btStartPattern.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                patternRunning = true;
                patternAnimator = CCFAnimator.rgb(colorSeekBar.getColor(), colorSeekBarStop.getColor());
                patternRunner = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int iterCount = 0;
                        float cnt = 0f;
                        float step = 0.01f;
                        boolean goinUp = true;
                        while (patternRunning) {
                            iterCount++;
                            try {
                                if (checkBoxScatter.isChecked())
                                    cnt = Constants.random.nextFloat();
                                color = patternAnimator.getColor(cnt);
                                //varia il colore solo ogni tot
                                if (iterCount % (11 - sliderSpeed.getProgress() / 10) == 0) {
                                    if (goinUp)
                                        cnt += step;
                                    else
                                        cnt -= step;

                                    collected.issueRGBCommand(Constants.Typicals.Souliss_T1n_Set,
                                            Color.red(color), Color.green(color), Color.blue(color), togMulticast.isChecked());
                                    Log.d(Constants.TAG, "dialogColorChangedListener, pattern color asc: " + goinUp
                                            + " cnt=" + cnt);
                                    if (getActivity() != null) {//could be detached
                                        getActivity().runOnUiThread(new Runnable() {
                                            public void run() {
                                                btFeedBackPatern
                                                        .setBackgroundColor(color);
                                            }
                                        });
                                    }
                                    if (cnt >= 1)
                                        goinUp = false;
                                    else if (cnt <= 0)
                                        goinUp = true;
                                    //slider max = 255
                                }
                                //do respiro almeno 50msec
                                Thread.sleep(150 - sliderSpeed.getProgress());
                            } catch (InterruptedException e) {
                                Log.e(Constants.TAG, "Error Thread.sleep:");
                            }
                        }
                    }
                });
                patternRunner.start();
            }
        });

        btStopPattern.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                patternRunning = false;
            }
        });

        //la prima volta prendo il colore dal typ
        color = (Color.argb(255, Color.red(collected.getColor()),
                Color.green(collected.getColor()), Color.blue(collected.getColor())));

        cpv = new ColorPickerView(getActivity(), dialogColorChangedListener, color, colorSwitchRelativeLayout, collected);

        colorSwitchRelativeLayout.addView(cpv);
        return ret;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVisualizerView != null)
            mVisualizerView.release();
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
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(datareceiver);
        //if (mVisualizerView != null)
        //    mVisualizerView.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        SoulissDBHelper.open();
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        getActivity().registerReceiver(datareceiver, filtere);

    }

    synchronized void setIsDecrementing(boolean newSetting) {
        continueDecrementing = newSetting;
    }

    /**
     * Per gestire tasto premuto
     *
     * @param newSetting
     */
    synchronized void setIsIncrementing(boolean newSetting) {
        continueIncrementing = newSetting;
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
                while (isDecrementing()) {
                    issueRGBCommand(cmd, Color.red(color), Color.green(color), Color.blue(color),
                            togMulticast.isChecked());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(Constants.TAG, "Error Thread.sleep:");
                    }
                }
            }
        }).start();
    }

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
                    issueRGBCommand(cmd, Color.red(color), Color.green(color), Color.blue(color),
                            togMulticast.isChecked());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(Constants.TAG, "Error Thread.sleep:");
                    }
                }
            }
        }).start();
    }

    synchronized private void stopDecrementing() {
        setIsDecrementing(false);
        collected.issueRefresh();
    }

    synchronized private void stopIncrementing() {
        setIsIncrementing(false);
        collected.issueRefresh();
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

    /**
     * Inner class representing the color Channels.
     */
    private class channelInputListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                Log.d(Constants.TAG, "onProgressChanged, color change to: R" + seekChannelRed.getProgress()
                        + " G" + seekChannelGreen.getProgress() + " B" + seekChannelBlue.getProgress() + " from user:" + fromUser);

                redChanabel.setText(getString(R.string.red) + " - " + Color.red(color));
                greenChanabel.setText(getString(R.string.green) + " - " + Color.green(color));
                blueChanabel.setText(getString(R.string.blue) + " - " + Color.blue(color));
                color = Color.argb(255, seekChannelRed.getProgress(), seekChannelGreen.getProgress(),
                        seekChannelBlue.getProgress());

                issueRGBCommand(Constants.Typicals.Souliss_T1n_Set, Color.red(color),
                        Color.green(color), Color.blue(color), togMulticast.isChecked());
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        // solo per sicurezza
        public void onStopTrackingTouch(SeekBar seekBar) {

            collected.issueRefresh();

        }

    }


}
