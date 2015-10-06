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
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault;
import com.survivingwithandroid.weather.lib.exception.LocationProviderNotFoundException;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.IWeatherProvider;
import com.survivingwithandroid.weather.lib.provider.WeatherProviderFactory;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical31Heating;

import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_AsMeasured;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_Cooling;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_FanAuto;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_FanHigh;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_FanLow;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_FanMed;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_Heating;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_Set;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T3n_ShutOff;
import static junit.framework.Assert.assertTrue;


public class T31HeatingFragment extends AbstractTypicalFragment implements NumberPicker.OnValueChangeListener {
    private SoulissDBHelper datasource = new SoulissDBHelper(SoulissApp.getAppContext());
    private SoulissPreferenceHelper opzioni;


    private SoulissTypical31Heating collected;

    private Spinner functionSpinner;
    private Spinner fanSpiner;
    private TextView textviewStatus;
    private Button buttOn;
    private Button buttOff;
    private TableRow infoFavs;
    private TableRow infoTags;
    private Button asMeasuredButton;
    private NumberPicker tempSlider;
    private ImageView imageFan3;
    private ImageView imageFan2;
    private ImageView imageFan1;
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
                if (collected.getTypicalDTO().isFavourite()) {
                    infoFavs.setVisibility(View.VISIBLE);
                } else if (collected.getTypicalDTO().isTagged()) {
                    infoTags.setVisibility(View.VISIBLE);
                }
                Log.e(Constants.TAG, "Setting Temp Slider:" + (int) collected.getTemperatureSetpointVal());
                textviewStatus.setText(collected.getOutputLongDesc());

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

                functionSpinner.setSelection(collected.isCoolMode() ? 0 : 1);
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error receiving data. Fragment disposed?", e);
            }


        }
    };
    private WeatherClientDefault client;
    private City cityId;
    private TextView tempView;
    private TextView pressView;
    private TextView humView;
    private TextView windView;

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

    private void getWeather() {
        if (client != null && cityId != null) {

            WeatherRequest req = new WeatherRequest(opzioni.getHomeLongitude(), opzioni.getHomeLatitude());
            client.getCurrentCondition(req, new WeatherClient.WeatherEventListener() {
                @Override
                public void onConnectionError(Throwable t) {
                    Log.e(Constants.TAG, "Weather Error: " +cityId+ t);
                }

                @Override
                public void onWeatherError(WeatherLibException t) {
                    Log.e(Constants.TAG, "Weather Error: " + t);
                }

                @Override
                public void onWeatherRetrieved(CurrentWeather currentWeather) {
                    // Here we can use the weather information to upadte the view
                    tempView.setText(String.format("%.0f", currentWeather.weather.temperature.getTemp()));
                    pressView.setText(String.valueOf(currentWeather.weather.currentCondition.getPressure()));
                    windView.setText(String.valueOf(currentWeather.weather.wind.getSpeed()));
                    humView.setText(String.valueOf(currentWeather.weather.currentCondition.getHumidity()));

                }
            });
        } else {
            initWeatherClient();
        }
    }

    private void initWeatherClient() {
        client = WeatherClientDefault.getInstance();
        client.init(getActivity());

        WeatherConfig config = new WeatherConfig();
        config.unitSystem = WeatherConfig.UNIT_SYSTEM.M;
        config.lang = "en"; // If you want to use english
        config.maxResult = 5; // Max number of cities retrieved
        config.numDays = 6; // Max num of days in the forecast


        IWeatherProvider provider = null;
        try {
            //provider = WeatherProviderFactory.createProvider(new YahooProviderType(), config);
            provider = WeatherProviderFactory.createProvider(new OpenweathermapProviderType(), config);
            //provider = WeatherProviderFactory.createProvider(new WeatherUndergroundProviderType(), config);
            client.setProvider(provider);
            client.updateWeatherConfig(config);

            client.searchCityByLocation(WeatherClient.createDefaultCriteria(), new WeatherClient.CityEventListener() {

                @Override
                public void onCityListRetrieved(List<City> cityList) {
                    cityId = cityList.get(0);
                    Log.e(Constants.TAG, "Weather Ciry recv: " + cityList.get(0));
                    getWeather();
                }

                @Override
                public void onConnectionError(Throwable t) {
                    Log.e(Constants.TAG, "Weather Ciry err: " +t);
                }

                @Override
                public void onWeatherError(WeatherLibException wle) {
                    Log.e(Constants.TAG, "Weather  err: " +wle);
                }
            });
        } catch (LocationProviderNotFoundException lpnfe) {

        } catch (Throwable t) {
            // There's a problem
        }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflater.inflate(R.menu.queue_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWeatherClient();
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
        assertTrue("TIPICO NULLO", collected instanceof SoulissTypical);
        collected.setPrefs(opzioni);

        super.setCollected(collected);
        refreshStatusIcon();

        buttOn = (Button) ret.findViewById(R.id.buttonTurnOn);
        buttOff = (Button) ret.findViewById(R.id.buttonTurnOff);
        textviewStatus = (TextView) ret.findViewById(R.id.textviewStatus);
        tempSlider = (NumberPicker) ret.findViewById(R.id.tempSlider);
        functionSpinner = (Spinner) ret.findViewById(R.id.spinnerFunction);
        fanSpiner = (Spinner) ret.findViewById(R.id.spinnerFan);
        infoFavs = (TableRow) ret.findViewById(R.id.tableRowFavInfo);
        asMeasuredButton = (Button) ret.findViewById(R.id.asMeasuredButton);
        infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);
        imageFan1 = (ImageView) ret.findViewById(R.id.ImageFan1);
        imageFan2 = (ImageView) ret.findViewById(R.id.ImageFan2);
        imageFan3 = (ImageView) ret.findViewById(R.id.ImageFan3);
        tempView= (TextView) ret.findViewById(R.id.temperature);
        windView = (TextView) ret.findViewById(R.id.wind);
        pressView = (TextView) ret.findViewById(R.id.pressure);
        humView = (TextView) ret.findViewById(R.id.hum);

        final int[] spinnerFunVal = getResources().getIntArray(R.array.AirConFunctionValues);
        /**
         * LISTENER SPINNER DESTINATARIO, IN TESTATA
         */
        final OnItemSelectedListener lit = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {//fa cagare
                    collected.issueCommand(Souliss_T3n_Heating, null);
                } else {
                    collected.issueCommand(Souliss_T3n_Cooling, null);
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
                functionSpinner.setOnItemSelectedListener(lit);
                fanSpiner.setOnItemSelectedListener(lib);
            }
        });

        tempSlider.setMaxValue(100);
        tempSlider.setMinValue(0);
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
                    collected.issueCommand(Souliss_T3n_Heating, Float.valueOf(tempSlider.getValue()));
                else
                    collected.issueCommand(Souliss_T3n_Cooling, Float.valueOf(tempSlider.getValue()));
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
                    ft.remove(getFragmentManager().findFragmentById(R.id.detailPane));
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
        filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        getActivity().registerReceiver(datareceiver, filtere);
        if (collected.getTypicalDTO().isFavourite()) {
            infoFavs.setVisibility(View.VISIBLE);
        } else if (collected.getTypicalDTO().isTagged()) {
            infoTags.setVisibility(View.VISIBLE);
        }
        tempSlider.setValue(((int) collected.getTemperatureSetpointVal()));
        //Ask first refresh
        collected.issueRefresh();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        collected.issueCommand(Souliss_T3n_Set, Float.valueOf(tempSlider.getValue()));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getWeather();
    }

}
