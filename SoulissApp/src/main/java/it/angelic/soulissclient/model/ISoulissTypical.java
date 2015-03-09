package it.angelic.soulissclient.model;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.ArrayList;

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
