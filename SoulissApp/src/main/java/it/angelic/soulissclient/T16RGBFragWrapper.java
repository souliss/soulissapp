package it.angelic.soulissclient;

import static junit.framework.Assert.assertTrue;


import it.angelic.soulissclient.fragments.T16RGBAdvancedFragment;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

public class T16RGBFragWrapper extends AbstractStatusedFragmentActivity {
	private SoulissPreferenceHelper opzioni;
	private SoulissTypical collected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main_detailwrapper);
		// recuper nodo da extra
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// If the screen is now in landscape mode, we can show the
			// dialog in-line with the list so we don't need this activity.
			finish();
			return;
		}
		Bundle extras = getIntent().getExtras();

		if (extras != null && extras.get("TIPICO") != null)
			collected = (SoulissTypical) extras.get("TIPICO");
		assertTrue("TIPICO NULLO", collected != null);
        setActionBarInfo(collected.getNiceName());
		if (savedInstanceState == null) {
			T16RGBAdvancedFragment details = T16RGBAdvancedFragment.newInstance(collected.getTypicalDTO().getSlot(),
					collected);
			details.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().replace(R.id.detailPane, details).commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.t16_ctx_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ImageView icon = (ImageView) findViewById(R.id.node_icon);
		switch (item.getItemId()) {
			case R.id.equalizer:
				AlertDialogHelper.equalizerDialog(this, null).show();
				break;
			case android.R.id.home:
				finish();
				if (opzioni.isAnimationsEnabled())
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
