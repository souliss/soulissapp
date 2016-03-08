package it.angelic.soulissclient.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import it.angelic.soulissclient.AbstractStatusedFragmentActivity;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;

public class AbstractTypicalFragment extends Fragment {
	Toolbar actionBar;
	private SoulissPreferenceHelper opzioni;
	private SoulissTypical collected;

    public AbstractTypicalFragment() {
		super();
		opzioni = SoulissApp.getOpzioni();
	}

    @Override
    public void onStart() {
        super.onStart();
        actionBar = (Toolbar) getActivity().findViewById(R.id.my_awesome_toolbar);

        ((AbstractStatusedFragmentActivity)getActivity()).setSupportActionBar(actionBar);
        ((AbstractStatusedFragmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        refreshStatusIcon();
    }


	public void refreshStatusIcon() {
		try {
			View ds = actionBar.getRootView();
			if (ds != null) {
				TextView info1 = (TextView) ds.findViewById(R.id.TextViewInfoStatus);
				TextView info2 = (TextView) ds.findViewById(R.id.TextViewInfo2);
				ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
				TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);

				TextView actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
				if (collected != null) {
					actionTitle.setText(collected.getNiceName());
				}
				if (!opzioni.isSoulissReachable()) {
					online.setBackgroundResource(R.drawable.red);
					statusOnline.setTextColor(getResources().getColor(R.color.std_red));
					statusOnline.setText(R.string.offline);
				} else {
					online.setBackgroundResource(R.drawable.green);
					statusOnline.setTextColor(getResources().getColor(R.color.std_green));
					statusOnline.setText(R.string.Online);
				}
				statusOnline.invalidate();
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "FAIL refresh status icon: " + e.getMessage());
		}
	}
	public SoulissTypical getCollected() {
		return collected;
	}
	void setCollected(SoulissTypical collected) {
		this.collected = collected;
	}

}