package it.angelic.soulissclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;

public class TagListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	SoulissTag[] soulissTags;
	private SoulissPreferenceHelper opzioni;

	public TagListAdapter(Context context, SoulissTag[] versio, SoulissPreferenceHelper opts) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.soulissTags = versio;
		opzioni = opts;
	}

	public int getCount() {
		return soulissTags.length;
	}

	public Object getItem(int position) {
		return soulissTags[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public SoulissTag[] getTags() {
		return soulissTags;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TagViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_scenes, parent, false);
			holder = new TagViewHolder();
			holder.textCmd = (TextView) convertView.findViewById(R.id.TextViewCommand);
			holder.textCmdWhen = (TextView) convertView.findViewById(R.id.TextViewCommandWhen);
			holder.textCmdInfo = (TextView) convertView.findViewById(R.id.TextViewCommandInfo);
			holder.image = (ImageView) convertView.findViewById(R.id.command_icon);
			convertView.setTag(holder);
		} else {
			holder = (TagViewHolder) convertView.getTag();
		}
        //fuori e vaffanculo
        holder.data = soulissTags[position];
		// holder.data.getCommand().getNodeId()
		if (opzioni.isLightThemeSelected()) {
			holder.textCmdWhen.setTextColor(context.getResources().getColor(color.black));
			holder.textCmd.setTextColor(context.getResources().getColor(color.black));
			holder.textCmdInfo.setTextColor(context.getResources().getColor(color.black));
		}
		holder.image.setImageResource(soulissTags[position].getIconResourceId());
		holder.image.setColorFilter(context.getResources().getColor(color.aa_yellow),
				android.graphics.PorterDuff.Mode.SRC_ATOP);
		List<SoulissTypical> appoggio = holder.data.getAssignedTypicals();
		// SoulissCommandDTO dto = holder.data.getCommandDTO();
		// if (name == null || "".compareTo(name) == 0)
		// name = context.getString(appoggio.getAliasNameResId());
		holder.textCmd.setText(soulissTags[position].getName());
		
		
		String strMeatFormat = "This Tag contains %1$d devices";
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



	public static class TagViewHolder {
		TextView textCmd;
		TextView textCmdWhen;
		TextView textCmdInfo;
		ImageView image;
		public SoulissTag data;
	}

	public void setScenes(SoulissTag[] scene) {
		this.soulissTags = scene;
	}
}