package it.angelic.soulissclient.drawer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.util.FontAwesomeUtil;

public class DrawerMenuHelper {
	private static Context ctx = SoulissApp.getAppContext();
	
	public static final int SCENES=-1;
	public static final int PROGRAMS=-2;
	public static final int MANUAL=-3;
	public static final int SETTINGS_NET=-4;
	public static final int SETTINGS_DB=-5;
	public static final int SETTINGS_SERVICE=-6;
	public static final int SETTINGS_VISUAL=-7;
	public static final int SETTINGS_UDPTEST=-8;
    public static final int TAGS=-10;

	private INavDrawerItem[] getNodes() {
		ArrayList<INavDrawerItem> tmp = new ArrayList<>();

		SoulissDBHelper db = new SoulissDBHelper(ctx);
		SoulissDBHelper.open();
		List<SoulissNode> nodes = db.getAllNodes();
		// Aggiungi nodi
        for (SoulissNode object : nodes) {
            NavMenuItem item2 = new NavMenuItem();
			item2.setId(object.getNodeId());
			item2.setLabel(object.getNiceName());
			item2.setIcon(FontAwesomeUtil.remapIconResId(object.getIconResourceId()));
			item2.setUpdateActionBarTitle(false);
            tmp.add(item2);
        }

		INavDrawerItem[] tmpa = new INavDrawerItem[tmp.size()];
		tmp.toArray(tmpa);
		return tmpa;
	}
	public INavDrawerItem[] getStuff(){
		
		ArrayList<INavDrawerItem> tmp = new ArrayList<>();

		NavMenuSection it = NavMenuSection.create(-9, SoulissApp.getAppContext().getString(R.string.functions).toUpperCase());
		tmp.add(it);

		//if (apartFromMe != SCENE){
		NavMenuItem scenes = new NavMenuItem(SCENES, ctx.getString(R.string.scenes_title), FontAwesomeUtil.remapIconResId(R.drawable.lamp),
				false, ctx);
		tmp.add(scenes);
	//	}
		
		//if (apartFromMe != PROGRAMS){
		NavMenuItem pro = new NavMenuItem(PROGRAMS, ctx.getString(R.string.programs_title), FontAwesomeUtil.remapIconResId(R.drawable.remote),
				false, ctx);
		tmp.add(pro);

		NavMenuItem prore = new NavMenuItem(TAGS, ctx.getString(R.string.tag), FontAwesomeUtil.remapIconResId(R.drawable.tv),
				false, ctx);
        tmp.add(prore);
	//	}
		NavMenuItem man = new NavMenuItem(MANUAL, ctx.getString(R.string.manual_title), FontAwesomeUtil.remapIconResId(R.drawable.hand1),
				false, ctx);
		tmp.add(man);
		
		SoulissDBHelper db = new SoulissDBHelper(ctx);
		SoulissDBHelper.open();
		List<SoulissNode> nodes = db.getAllNodes();
		//Aggiungi nodi
        for (SoulissNode object : nodes) {
            NavMenuItem item2 = new NavMenuItem();
			item2.setId(object.getNodeId());
			item2.setLabel(object.getNiceName());
			item2.setIcon(FontAwesomeUtil.remapIconResId(object.getIconResourceId()));
			item2.setUpdateActionBarTitle(false);
            tmp.add(item2);
        }
		NavMenuSection it2 = NavMenuSection.create(-10, SoulissApp.getAppContext().getString(R.string.menu_options).toUpperCase());
		tmp.add(it2);
		NavMenuItem op2 = new NavMenuItem(SETTINGS_NET, ctx.getString(R.string.opt_net_home), "fa-wifi",
				false, ctx);
		tmp.add(op2);
		NavMenuItem op3 = new NavMenuItem(SETTINGS_DB, ctx.getString(R.string.opt_db), "fa-sitemap",
				false, ctx);
		tmp.add(op3);
		NavMenuItem op4 = new NavMenuItem(SETTINGS_SERVICE, ctx.getString(R.string.opt_service), "fa-spinner",
				false, ctx);
		tmp.add(op4);
		NavMenuItem op5 = new NavMenuItem(SETTINGS_VISUAL, ctx.getString(R.string.opt_visual), "fa-picture-o",
				false, ctx);
		tmp.add(op5);
		NavMenuItem op6 = new NavMenuItem(SETTINGS_UDPTEST, ctx.getString(R.string.menu_test_udp), "fa-gears",
				false, ctx);
		tmp.add(op6);
		
		INavDrawerItem[] tmpa = new INavDrawerItem[tmp.size()];
		tmp.toArray(tmpa);
		return tmpa;
	}


}
