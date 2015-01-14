package it.angelic.soulissclient.fragments;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
public class AbstractTypicalFragment extends SherlockFragment {
	protected ActionBar actionBar;
	protected SoulissPreferenceHelper opzioni;
	private SoulissTypical collected;
	public AbstractTypicalFragment() {
		super();
		opzioni = SoulissClient.getOpzioni();
		
	}
	protected  void refreshStatusIcon() {
		try {
			View ds = actionBar.getCustomView();
			if (ds != null) {
				ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
				TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);
				TextView actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
				actionTitle.setText(collected.getNiceName());

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
			e.printStackTrace();
		}
	}
	public SoulissTypical getCollected() {
		return collected;
	}
	public void setCollected(SoulissTypical collected) {
		this.collected = collected;
	}

}