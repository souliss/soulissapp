package it.angelic.soulissclient.fragments;

import com.actionbarsherlock.app.SherlockListFragment;

public class AbstractTypicalFragment extends SherlockListFragment {

	public AbstractTypicalFragment() {
		super();
	}
	protected int getShownIndex() {
		if (getArguments() != null)
			return getArguments().getInt("index", 0);
		else
			return 0;
	}


}