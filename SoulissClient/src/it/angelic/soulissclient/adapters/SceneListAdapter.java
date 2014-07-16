package it.angelic.soulissclient.adapters;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissScene;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SceneListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	SoulissScene[] scene;
	private SoulissPreferenceHelper opzioni;

	public SceneListAdapter(Context context, SoulissScene[] versio,SoulissPreferenceHelper opts) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.scene = versio;
		opzioni = opts;
	}

	public int getCount() {
		return (int) scene.length;
	}

	public Object getItem(int position) {
		return scene[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public SoulissScene[] getScenes() {
		return scene;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		SceneViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_scenes, null);
			holder = new SceneViewHolder();
			holder.textCmd = (TextView) convertView.findViewById(R.id.TextViewCommand);
			holder.textCmdWhen = (TextView) convertView.findViewById(R.id.TextViewCommandWhen);
			holder.textCmdInfo = (TextView) convertView.findViewById(R.id.TextViewCommandInfo);
			holder.image = (ImageView) convertView.findViewById(R.id.command_icon);
			holder.data = scene[position];
			
			convertView.setTag(holder);
		} else {
			holder = (SceneViewHolder) convertView.getTag();
		}
		// holder.data.getCommand().getNodeId()
		if (opzioni.isLightThemeSelected()) {
			holder.textCmdWhen.setTextColor(context.getResources().getColor(R.color.black));
			holder.textCmd.setTextColor(context.getResources().getColor(R.color.black));
			holder.textCmdInfo.setTextColor(context.getResources().getColor(R.color.black));
		}
		holder.image.setImageResource(holder.data.getDefaultIconResourceId());
		holder.image.setColorFilter(context.getResources().getColor(color.aa_yellow),
				android.graphics.PorterDuff.Mode.SRC_ATOP);
		ArrayList<SoulissCommand> appoggio = holder.data.getCommandArray();
		// SoulissCommandDTO dto = holder.data.getCommandDTO();
		// if (name == null || "".compareTo(name) == 0)
		// name = context.getString(appoggio.getAliasNameResId());
		holder.textCmd.setText(holder.data.toString());
		
		
		String strMeatFormat = context.getString(R.string.scene_subtitle);
		holder.textCmdWhen.setText(String.format(strMeatFormat, appoggio.size()));
		
		//EMPTY space for info
		//holder.textCmdInfo.setText("Info 2");
		holder.textCmdInfo.setVisibility(View.GONE);

		if (opzioni.getTextFx()) {
			Animation a2 = AnimationUtils.loadAnimation(context, R.anim.scalerotale);
			a2.reset();
			a2.setStartOffset(100 * position);
			// Animazione immagine holder.image.clearAnimation();
			holder.image.startAnimation(a2);
		}

		return convertView;
	}

	public static class SceneViewHolder {
		TextView textCmd;
		TextView textCmdWhen;
		TextView textCmdInfo;
		ImageView image;
		public SoulissScene data;
	}

	public void setScenes(SoulissScene[] scene) {
		this.scene = scene;
	}
}