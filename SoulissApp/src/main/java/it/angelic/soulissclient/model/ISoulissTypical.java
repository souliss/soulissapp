package it.angelic.soulissclient.model;

import it.angelic.soulissclient.adapters.TypicalsListAdapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

public interface ISoulissTypical extends ISoulissObject {
	/**
	 * Has to be overridden in child typicals
	 * 
	 * @param ble
	 * @param ctx
	 * @param parentIntent
	 * @param convertView
	 * @param parent
	 */
	public void getActionsLayout(final TypicalsListAdapter ble, Context ctx, final Intent parentIntent,
			View convertView, final ViewGroup parent);
	
	public ArrayList<SoulissCommand> getCommands(Context ctx) ;
	
	public String getOutputDesc();
	
	//every typical should have one ..
	public int getDefaultIconResourceId();
	
	
}
