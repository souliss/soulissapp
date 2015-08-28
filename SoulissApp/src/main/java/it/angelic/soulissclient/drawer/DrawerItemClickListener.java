package it.angelic.soulissclient.drawer;

import it.angelic.soulissclient.ManualUDPTestActivity;
import it.angelic.soulissclient.NodeDetailActivity;
import it.angelic.soulissclient.NodesListActivity;
import it.angelic.soulissclient.PreferencesActivity;
import it.angelic.soulissclient.ProgramListActivity;
import it.angelic.soulissclient.SceneListActivity;
import it.angelic.soulissclient.TagListActivity;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.preferences.NetSettingsFragment;
import it.angelic.soulissclient.preferences.ServiceSettingsFragment;
import it.angelic.soulissclient.preferences.VisualSettingsFragment;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.preference.PreferenceActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class DrawerItemClickListener implements ListView.OnItemClickListener {

	Activity mActivity;
	private ListView mDrawerList;
    protected LinearLayout mDrawerLinear;
	private DrawerLayout mDrawerLayout;

	public DrawerItemClickListener(Activity mActivity, ListView mDrawerList, DrawerLayout mDrawerLayout, LinearLayout mDrawerLinear) {
		super();
		this.mActivity = mActivity;
		this.mDrawerList = mDrawerList;
		this.mDrawerLayout = mDrawerLayout;
        this.mDrawerLinear = mDrawerLinear;
	}

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		INavDrawerItem temp = (INavDrawerItem) parent.getItemAtPosition(position);
		selectItem(position, temp.getId());
		// Log.i("DEB", );
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position, int id) {
		SoulissDBHelper db = new SoulissDBHelper(mActivity);
		SoulissDBHelper.open();
		if (id >= 0) {// vai a dettaglio nodo
			if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// landscape
				Intent intent = new Intent();
				intent.setClass(mActivity, NodesListActivity.class);
				intent.putExtra("index", id);
				//intent.putExtra("NODO", db.getSoulissNode(id));
				mActivity.startActivity(intent);
				return;
			}// PORTRAIT
			Intent intent = new Intent();
			intent.setClass(mActivity, NodeDetailActivity.class);
			intent.putExtra("index", id);
			intent.putExtra("NODO", db.getSoulissNode(id));
			mActivity.startActivity(intent);
			return;
		} else {
			switch (id) {
			case DrawerMenuHelper.SCENES:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent = new Intent(mActivity, SceneListActivity.class);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				mActivity.startActivity(myIntent);
				break;
			case DrawerMenuHelper.PROGRAMS:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent2 = new Intent(mActivity, ProgramListActivity.class);
				myIntent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				mActivity.startActivity(myIntent2);
				break;
            case DrawerMenuHelper.TAGS:
                    mDrawerList.setItemChecked(position, true);
                    // setTitle(mPlanetTitles[position]);
                    mDrawerLayout.closeDrawer(mDrawerLinear);
                    Intent myIntent5d = new Intent(mActivity, TagListActivity.class);
                    myIntent5d.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    mActivity.startActivity(myIntent5d);
                    break;
			case DrawerMenuHelper.MANUAL:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent3 = new Intent(mActivity, NodesListActivity.class);
				myIntent3.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				mActivity.startActivity(myIntent3);
				break;
			case DrawerMenuHelper.SETTINGS_NET:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent4 = new Intent(mActivity, PreferencesActivity.class);
				myIntent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				myIntent4.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, NetSettingsFragment.class.getName());
				myIntent4.setAction("network_setup");
				mActivity.startActivity(myIntent4);
				break;
			case DrawerMenuHelper.SETTINGS_DB:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent5 = new Intent(mActivity, PreferencesActivity.class);
				myIntent5.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				myIntent5.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
				myIntent5.setAction("db_setup");
				mActivity.startActivity(myIntent5);
				break;
			case DrawerMenuHelper.SETTINGS_SERVICE:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent6 = new Intent(mActivity, PreferencesActivity.class);
				myIntent6.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				myIntent6.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ServiceSettingsFragment.class.getName());
				myIntent6.setAction("service_setup");
				mActivity.startActivity(myIntent6);
				break;
			case DrawerMenuHelper.SETTINGS_VISUAL:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent7 = new Intent(mActivity, PreferencesActivity.class);
				myIntent7.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				myIntent7.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, VisualSettingsFragment.class.getName());
				myIntent7.setAction("visual_setup");
				mActivity.startActivity(myIntent7);
				break;
			case DrawerMenuHelper.SETTINGS_UDPTEST:
				mDrawerList.setItemChecked(position, true);
				// setTitle(mPlanetTitles[position]);
				mDrawerLayout.closeDrawer(mDrawerLinear);
				Intent myIntent8 = new Intent(mActivity, ManualUDPTestActivity.class);
				myIntent8.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mActivity.startActivity(myIntent8);
				break;
			default:
				break;
			}
		}
		// Create a new fragment and specify the planet to show based on
		// position
		/*
		 * Fragment fragment = new PlanetFragment(); Bundle args = new Bundle();
		 * args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		 * fragment.setArguments(args);
		 * 
		 * // Insert the fragment by replacing any existing fragment
		 * FragmentManager fragmentManager = getFragmentManager();
		 * fragmentManager.beginTransaction() .replace(R.id.containervermegame,
		 * fragment) .commit();
		 */
		// Highlight the selected item, update the title, and close the drawer

		return;

	}
}