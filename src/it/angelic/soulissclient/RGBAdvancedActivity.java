package it.angelic.soulissclient;

import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.adapters.TypicalsListAdapter;
import it.angelic.soulissclient.fragments.RGBAdvancedFragment;
import it.angelic.soulissclient.fragments.Typical1nFragment;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.typicals.SoulissTypical;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public class RGBAdvancedActivity extends FragmentActivity {
	private SoulissPreferenceHelper opzioni;
	private TypicalsListAdapter ta;
	private SoulissTypical collected;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		opzioni = SoulissClient.getOpzioni();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		// recuper nodo da extra
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// If the screen is now in landscape mode, we can show the
			// dialog in-line with the list so we don't need this activity.
			finish();
			return;
		}
		Bundle extras = getIntent().getExtras();

		// collected.setCtx(getActivity());

		if (extras != null && extras.get("TIPICO") != null)
			collected = (SoulissTypical) extras.get("TIPICO");
		assertTrue("TIPICO NULLO", collected != null);
		if (savedInstanceState == null) {
			RGBAdvancedFragment details = RGBAdvancedFragment.newInstance(collected.getTypicalDTO().getSlot(),
					collected);
			details.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().replace(android.R.id.content, details).commit();
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

}
