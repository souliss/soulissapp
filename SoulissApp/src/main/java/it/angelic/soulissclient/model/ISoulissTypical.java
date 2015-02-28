package it.angelic.soulissclient.model;

import it.angelic.soulissclient.adapters.TypicalsListAdapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;

public interface ISoulissTypical extends ISoulissObject {


    public SoulissNode getParentNode();

    public void setParentNode(SoulissNode parentNode);

	
	public ArrayList<SoulissCommand> getCommands(Context ctx) ;

    void getActionsLayout (Context ctx,
                           LinearLayout convertView);

    public String getOutputDesc();
	
	//every typical should have one ..
	public int getIconResourceId();
	
	
}
