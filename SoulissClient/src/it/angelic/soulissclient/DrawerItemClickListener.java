package it.angelic.soulissclient;

import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.preferences.NetSettingsFragment;
import it.angelic.soulissclient.preferences.ServiceSettingsFragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

class DrawerItemClickListener implements ListView.OnItemClickListener {
	
	Activity mActivity;
	private ListView mDrawerList ;
	private DrawerLayout mDrawerLayout;
	
	
	
	public DrawerItemClickListener(Activity mActivity, ListView mDrawerList, DrawerLayout mDrawerLayout) {
		super();
		this.mActivity = mActivity;
		this.mDrawerList = mDrawerList;
		this.mDrawerLayout = mDrawerLayout;
	}

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		selectItem(position);
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		
		switch (position) {
		case 0:
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			Intent myIntent = new Intent(mActivity, SceneListActivity.class);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mActivity.startActivity(myIntent);
			break;
		case 1:
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			Intent myIntent2 = new Intent(mActivity, ProgramListActivity.class);
			myIntent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mActivity.startActivity(myIntent2);
			break;
		case 2:
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			Intent myIntent3 = new Intent(mActivity, NodesListActivity.class);
			myIntent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mActivity.startActivity(myIntent3);
			break;
		case 3:
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			Intent myIntent4 = new Intent(mActivity, PreferencesActivity.class);
			myIntent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				AlertDialogHelper.setExtra(myIntent4, NetSettingsFragment.class.getName());
			// preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
			myIntent4.setAction("network_setup");
			mActivity.startActivity(myIntent4);
			break;
		case 4:
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			Intent myIntent5 = new Intent(mActivity, PreferencesActivity.class);
			myIntent5.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				AlertDialogHelper.setExtra(myIntent5, DbSettingsFragment.class.getName());
			// preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
			myIntent5.setAction("db_setup");
			mActivity.startActivity(myIntent5);
			break;
		case 5:
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			Intent myIntent6 = new Intent(mActivity, PreferencesActivity.class);
			myIntent6.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				AlertDialogHelper.setExtra(myIntent6, ServiceSettingsFragment.class.getName());
			// preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
			myIntent6.setAction("service_setup");
			mActivity.startActivity(myIntent6);
			break;
		case 6:
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			Intent myIntent7 = new Intent(mActivity, PreferencesActivity.class);
			myIntent7.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				AlertDialogHelper.setExtra(myIntent7, ServiceSettingsFragment.class.getName());
			// preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
			myIntent7.setAction("visual_setup");
			mActivity.startActivity(myIntent7);
			break;
		default:
			break;
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