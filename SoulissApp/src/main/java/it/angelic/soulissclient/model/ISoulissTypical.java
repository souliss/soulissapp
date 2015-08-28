package it.angelic.soulissclient.model;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.ArrayList;

public interface ISoulissTypical extends ISoulissObject {


    SoulissNode getParentNode();

    void setParentNode(SoulissNode parentNode);

	
	ArrayList<SoulissCommand> getCommands(Context ctx) ;

    void getActionsLayout (Context ctx,
                           LinearLayout convertView);

    String getOutputDesc();
	
	//every typical should have one ..
    int getIconResourceId();
	
	
}
