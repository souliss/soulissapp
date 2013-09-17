package it.angelic.soulissclient.model;

import it.angelic.soulissclient.R;

import java.io.Serializable;
import java.util.ArrayList;

public class SoulissScene implements Serializable, ISoulissObject {


	private static final long serialVersionUID = 375896210748961219L;
	private ArrayList<SoulissCommand> commandArray;
	//private transient Context ctx;
	private String name = "Name not Set";
	private int iconId = R.drawable.soulisslogo;
	private int Id;


	public SoulissScene(int id) {
		super();
		//ctx = ct;
		Id = id;
		//this.commandDTO = new ArrayList<SoulissCommandDTO>();
		//this.parentTypical = parentTypical;
	}

	public int getType() {
		// typ o normal ?? rivedere politica comandi multipli
		return 666;
	}

	@Override
	public String toString() {
		return getNiceName();
	}

	public String getNiceName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return Id;
	}
	public int getDefaultIconResourceId() {
		return iconId;
	}
	public void setIconResourceId(int itemResId) {
		iconId = itemResId;
	}
	public ArrayList<SoulissCommand> getCommandArray() {
		return commandArray;
	}

	public void setCommandArray(ArrayList<SoulissCommand> commandArray) {
		this.commandArray = commandArray;
	}


}
