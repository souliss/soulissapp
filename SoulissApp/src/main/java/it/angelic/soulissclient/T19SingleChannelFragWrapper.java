package it.angelic.soulissclient;

import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.fragments.T19SingleChannelLedFragment;
import it.angelic.soulissclient.model.SoulissTypical;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class T19SingleChannelFragWrapper extends AbstractStatusedFragmentActivity {
	private SoulissTypical collected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
			T19SingleChannelLedFragment details = T19SingleChannelLedFragment.newInstance(collected.getTypicalDTO().getSlot(),
					collected);
			details.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().replace(R.id.detailPane, details).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			if (opzioni.isAnimationsEnabled())
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
