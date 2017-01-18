package it.angelic.soulissclient.model;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.List;

public interface ISoulissTypical extends ISoulissObject {


    short getNodeId();

    ISoulissNode getParentNode();

    //void setParentNode(ISoulissNode parentNode);


    List<ISoulissCommand> getCommands(Context ctx);

    void getActionsLayout (Context ctx,
                           LinearLayout convertView);

    String getOutputDesc();
	
	//every typical should have one ..
    int getIconResourceId();


    short getSlot();
}
