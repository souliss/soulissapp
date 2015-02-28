package it.angelic.soulissclient.drawer;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.SoulissNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;

public class DrawerMenuHelper {
	private static Context ctx = SoulissClient.getAppContext();
	
	public static final int SCENES=-1;
	public static final int PROGRAMS=-2;
	public static final int MANUAL=-3;
	public static final int SETTINGS_NET=-4;
	public static final int SETTINGS_DB=-5;
	public static final int SETTINGS_SERVICE=-6;
	public static final int SETTINGS_VISUAL=-7;
	public static final int SETTINGS_UDPTEST=-8;

	private INavDrawerItem[] getNodes() {
		ArrayList<INavDrawerItem> tmp = new ArrayList<>();

		SoulissDBHelper db = new SoulissDBHelper(ctx);
		db.open();
		List<SoulissNode> nodes = db.getAllNodes();
		// Aggiungi nodi
		for (Iterator<SoulissNode> iterator = nodes.iterator(); iterator.hasNext();) {
			SoulissNode object = (SoulissNode) iterator.next();
			NavMenuItem item2 = new NavMenuItem();
			item2.setId(object.getId());
			item2.setLabel(object.getNiceName());
			item2.setIcon(object.getIconResourceId());
			item2.setUpdateActionBarTitle(false);
			tmp.add(item2);
		}

		INavDrawerItem[] tmpa = new INavDrawerItem[tmp.size()];
		tmp.toArray(tmpa);
		return tmpa;
	}
	public INavDrawerItem[] getStuff(){
		
		ArrayList<INavDrawerItem> tmp = new ArrayList<>();
		
		NavMenuSection it = NavMenuSection.create(-9, "FUNZIONI");
		tmp.add(it);
		
		//if (apartFromMe != SCENES){
		NavMenuItem scenes = new NavMenuItem(SCENES, ctx.getString(R.string.scenes_title), R.drawable.lamp,
				false, ctx);
		tmp.add(scenes);
	//	}
		
		//if (apartFromMe != PROGRAMS){
		NavMenuItem pro = new NavMenuItem(PROGRAMS, ctx.getString(R.string.programs_title), R.drawable.remote,
				false, ctx);
		tmp.add(pro);
	//	}
		NavMenuItem man = new NavMenuItem(MANUAL, ctx.getString(R.string.manual_title), R.drawable.hand,
				false, ctx);
		tmp.add(man);
		
		SoulissDBHelper db = new SoulissDBHelper(ctx);
		db.open();
		List<SoulissNode> nodes = db.getAllNodes();
		//Aggiungi nodi
		for (Iterator<SoulissNode>  iterator = nodes.iterator(); iterator.hasNext();) {
			SoulissNode object = (SoulissNode) iterator.next();
			NavMenuItem item2 = new NavMenuItem();
			item2.setId(object.getId());
	        item2.setLabel(object.getNiceName());
	        item2.setIcon(object.getIconResourceId());
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
		NavMenuItem op6 = new NavMenuItem(SETTINGS_UDPTEST, ctx.getString(R.string.menu_test_udp),android.R.drawable.ic_menu_agenda,
				false, ctx);
		tmp.add(op6);
		
		INavDrawerItem[] tmpa = new INavDrawerItem[tmp.size()];
		tmp.toArray(tmpa);
		return tmpa;
	}
	
	
}
