package it.angelic.soulissclient.drawer;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.R.drawable;
import it.angelic.soulissclient.R.string;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class DrawerMenuHelper {
	private static Context ctx = SoulissClient.getAppContext();
	final static private String IMG = "icon";
	final static private String TITLE = "title";
	
	public static final int SCENES=-1;
	public static final int PROGRAMS=-2;
	public static final int MANUAL=-3;
	public static final int SETTINGS_NET=-4;
	public static final int SETTINGS_DB=-5;
	public static final int SETTINGS_SERVICE=-6;
	public static final int SETTINGS_VISUAL=-7;

	private static int[] mFlags = { R.drawable.lamp, R.drawable.remote, R.drawable.hand,
			android.R.drawable.ic_menu_mylocation, android.R.drawable.ic_menu_save, android.R.drawable.ic_menu_rotate,
			android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_agenda };
	private static String[] mCountries = { ctx.getString(R.string.scenes_title),
			ctx.getString(R.string.programs_title), ctx.getString(R.string.manual_title),
			ctx.getString(R.string.opt_network), ctx.getString(R.string.opt_db),
			ctx.getString(R.string.opt_service), ctx.getString(R.string.opt_visualdesc),
			ctx.getString(R.string.menu_test_udp) };

/*	public static ArrayList<HashMap<String, String>> getArray() {
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
*/	
	public INavDrawerItem[] getStuff(){
		
		
		ArrayList<INavDrawerItem> tmp = new ArrayList<INavDrawerItem>();
		
		
		NavMenuSection it = NavMenuSection.create(-9, "FUNZIONI");
		tmp.add(it);
		
		NavMenuItem scenes = new NavMenuItem(SCENES, ctx.getString(R.string.scenes_title), R.drawable.lamp,
				false, ctx);
		tmp.add(scenes);
		
		NavMenuItem pro = new NavMenuItem(PROGRAMS, ctx.getString(R.string.programs_title), R.drawable.remote,
				false, ctx);
		tmp.add(pro);
		
		NavMenuItem man = new NavMenuItem(MANUAL, ctx.getString(R.string.manual_title), R.drawable.hand,
				false, ctx);
		tmp.add(man);
		
		SoulissDBHelper db = new SoulissDBHelper(ctx);
		db.open();
		List<SoulissNode> nodes = db.getAllNodes();
		for (Iterator<SoulissNode>  iterator = nodes.iterator(); iterator.hasNext();) {
			SoulissNode object = (SoulissNode) iterator.next();
			NavMenuItem item2 = new NavMenuItem();
			item2.setId(object.getId());
	        item2.setLabel(object.getNiceName());
	        item2.setIcon(object.getDefaultIconResourceId());
	        item2.setUpdateActionBarTitle(false);
			tmp.add(item2);
		}
		NavMenuSection it2 = NavMenuSection.create(-10, "OPZIONI");
		tmp.add(it2);
		NavMenuItem op2 = new NavMenuItem(SETTINGS_NET, ctx.getString(R.string.opt_net_home),android.R.drawable.ic_menu_mylocation,
				false, ctx);
		tmp.add(op2);
		NavMenuItem op3 = new NavMenuItem(SETTINGS_DB, ctx.getString(R.string.opt_db),android.R.drawable.ic_menu_save,
				false, ctx);
		tmp.add(op3);
		NavMenuItem op4 = new NavMenuItem(SETTINGS_SERVICE, ctx.getString(R.string.opt_service),android.R.drawable.ic_menu_rotate,
				false, ctx);
		tmp.add(op4);
		NavMenuItem op5 = new NavMenuItem(SETTINGS_VISUAL, ctx.getString(R.string.opt_visual),android.R.drawable.ic_menu_gallery,
				false, ctx);
		tmp.add(op5);
		
		INavDrawerItem[] tmpa = new INavDrawerItem[tmp.size()];
		tmp.toArray(tmpa);
		return tmpa;
	}
	
	
}
