package it.angelic.soulissclient.fragments;

import android.support.v4.app.ListFragment;

public class AbstractTypicalFragment extends ListFragment {

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