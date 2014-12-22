package it.angelic.soulissclient;

import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;

import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

public class SoulissWidgetConfig extends Activity {
	private SoulissNode[] nodiArray;
	private static final String TAG = "Souliss:WidgetConfig";
	private Button configOkButton;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private SoulissDBHelper datasource;
	private Spinner outputNodeSpinner;
	private Spinner outputCommandSpinner;
	private Spinner outputTypicalSpinner;
	private Button configCancelButton;
	private EditText widgetLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);

		setContentView(R.layout.soulisswidgetconfig);

		outputTypicalSpinner = (Spinner) findViewById(R.id.spinner3);
		outputCommandSpinner = (Spinner) findViewById(R.id.spinnerCommand);
		outputNodeSpinner = (Spinner) findViewById(R.id.spinner2);
		configOkButton = (Button) findViewById(R.id.buttonOkWidgetConfig);
		configCancelButton = (Button) findViewById(R.id.buttonCancelWidgetConfig);
		widgetLabel = (EditText)findViewById(R.id.editTextWidgetLabel);
		configOkButton.setOnClickListener(configOkButtonOnClickListener);
		configCancelButton.setOnClickListener(configCancelButtonOnClickListener);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
		datasource = new SoulissDBHelper(this);
		datasource.open();

		// prendo tipici dal DB
		List<SoulissNode> goer = datasource.getAllNodes();
		nodiArray = new SoulissNode[goer.size()];
		nodiArray = goer.toArray(nodiArray);
		
		fillNodeSpinner(outputNodeSpinner);
		
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
				List<SoulissTypical> re = nodiArray[(int) outputNodeSpinner.getSelectedItemId()].getActiveTypicals(SoulissWidgetConfig.this);
				
				fillCommandSpinner(outputCommandSpinner, re.get(pos));
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};
		outputTypicalSpinner.setOnItemSelectedListener(lib);
		
	}

	private Button.OnClickListener configOkButtonOnClickListener = new Button.OnClickListener() {

		private SharedPreferences customSharedPreference;

		@Override
		public void onClick(View arg0) {
			final Context context = SoulissWidgetConfig.this;
			customSharedPreference = context.getSharedPreferences("SoulissWidgetPrefs", Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = customSharedPreference.edit();
			SoulissCommand ccm = (SoulissCommand) outputCommandSpinner.getSelectedItem();
			if (outputTypicalSpinner.getSelectedItem() == null){
				Toast.makeText(context, getString(R.string.widget_cantsave)  , Toast.LENGTH_LONG).show();
				return;
			}
			
			editor.putInt(mAppWidgetId+"_NODE", ((SoulissNode)outputNodeSpinner.getSelectedItem()).getId());
			editor.putInt(mAppWidgetId+"_SLOT",((SoulissTypical)outputTypicalSpinner.getSelectedItem()).getTypicalDTO().getSlot());
			if (ccm != null)
				editor.putLong(mAppWidgetId+"_CMD",(ccm).getCommandDTO().getCommand());
			editor.putString(mAppWidgetId+"_NAME",widgetLabel.getText().toString());
			
			//editor.putString("COMMAND", outputCommandSpinner.getSelectedItem());
			editor.commit();
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widget_layout);
			appWidgetManager.updateAppWidget(mAppWidgetId, views);
			SoulissWidget.forcedUpdate(context, appWidgetManager, mAppWidgetId);
			
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			Uri data = Uri.withAppendedPath(
				    Uri.parse("W://widget/id/")
				    ,String.valueOf(mAppWidgetId));
			resultValue.setData(data);

			setResult(RESULT_OK, resultValue);
			finish();
		}
	};
	
	private Button.OnClickListener configCancelButtonOnClickListener = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			finish();
		}
	};
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
	 * popola spinner tipici in base al nodo. per es. la V non ha il neutro
	 * 
	 * @param tgt
	 * @param dec
	 */
	private void setTypicalSpinner(Spinner tgt, SoulissNode ref) {
		try {
			SoulissTypical[] strArray = new SoulissTypical[ref.getActiveTypicals().size()];
			ref.getActiveTypicals(this).toArray(strArray);

			ArrayAdapter<SoulissTypical> adapter = new ArrayAdapter<SoulissTypical>(this,
					android.R.layout.simple_spinner_item, strArray);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			tgt.setAdapter(adapter);

		} catch (Exception e) {
			Log.e(TAG, "Errore in setTypicalSpinner:" + e.getMessage(), e);
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
		// SoulissCommand[] etichette = new SoulissCommand[strArray.length];

		ArrayAdapter<SoulissCommand> adapter = new ArrayAdapter<SoulissCommand>(this,
				android.R.layout.simple_spinner_item, strArray);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tgt.setAdapter(adapter);
	}

}