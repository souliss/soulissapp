package it.angelic.soulissclient;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

public class DrawerMenuHelper {
	private static Context ctx = SoulissClient.getAppContext();
	final static private String IMG = "icon";
	final static private String TITLE = "title";

	// Keys used in Hashmap
	static String[] from = { IMG, TITLE };

	// Ids of views in listview_layout
	static int[] to = { R.id.dicon, R.id.dtitle };

	private static int[] mFlags = { R.drawable.lamp, R.drawable.remote, R.drawable.hand,
			android.R.drawable.ic_menu_mylocation, android.R.drawable.ic_menu_save, android.R.drawable.ic_menu_rotate,
			android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_agenda };
	private static String[] mCountries = { ctx.getString(R.string.scenes_title),
			ctx.getString(R.string.programs_title), ctx.getString(R.string.manual_title),
			ctx.getString(R.string.opt_network), ctx.getString(R.string.opt_db),
			ctx.getString(R.string.opt_service), ctx.getString(R.string.opt_visualdesc),
			ctx.getString(R.string.menu_test_udp) };

	public static ArrayList<HashMap<String, String>> getArray() {
		// Each row in the list stores country name, count and flag
		ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < 8; i++) {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put(IMG, Integer.toString(mFlags[i]));
			hm.put(TITLE, mCountries[i]);
			mList.add(hm);
		}
		return mList;
	}

	public static String[] getFrom() {
		return from;
	}

	public static int[] getTo() {
		return to;
	}
}
