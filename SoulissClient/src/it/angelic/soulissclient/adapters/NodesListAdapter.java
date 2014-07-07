package it.angelic.soulissclient.adapters;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NodesListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	SoulissNode[] nodi;
	private SoulissPreferenceHelper opzioni;

	public NodesListAdapter(Context context, SoulissNode[] versio, SoulissPreferenceHelper opts) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.nodi = versio;
		opzioni = opts;
	}

	public int getCount() {
		return (int) nodi.length;
	}

	public Object getItem(int position) {
		return nodi[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public SoulissNode[] getNodes() {
		return nodi;
	}

	public void setNodes(SoulissNode[] in) {
		nodi = in;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		NodeViewHolder holder;

		if (convertView == null) {
			// ProgressBar sfumata
			final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(Constants.roundedCorners, null, null));
			final LinearGradient gradient = new LinearGradient(0, 0, 250, 0, context.getResources().getColor(
					color.aa_red), context.getResources().getColor(color.aa_green),
					android.graphics.Shader.TileMode.CLAMP);

			convertView = mInflater.inflate(R.layout.listview, null);
			holder = new NodeViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.TextView01);
			holder.textTyp = (TextView) convertView.findViewById(R.id.TextViewTypicals);
			holder.textHlt = (TextView) convertView.findViewById(R.id.TextViewHealth);
			holder.image = (ImageView) convertView.findViewById(R.id.node_icon);
			holder.hlt = (ProgressBar) convertView.findViewById(R.id.progressBarHealth);
			holder.hlt.setIndeterminate(false);
			holder.hlt.setMax(50);
			holder.hlt.setProgress(20);
			holder.hlt.setProgress(0);
			holder.hlt.setMax((int) Constants.MAX_HEALTH);
			holder.hlt.setBackgroundResource(android.R.drawable.progress_horizontal);
			holder.imageRes = nodi[position].getDefaultIconResourceId();
			// pgDrawable.getPaint().setStrokeWidth(3);
			pgDrawable.getPaint().setDither(true);
			pgDrawable.getPaint().setShader(gradient);

			ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);

			// Rect bounds = holder.hlt.getProgressDrawable().getBounds();
			holder.hlt.setProgressDrawable(progress);
			// holder.hlt.getProgressDrawable().setBounds(bounds);

			convertView.setTag(holder);
		} else {

			holder = (NodeViewHolder) convertView.getTag();
			// ClipDrawable progress = new ClipDrawable(pgDrawable,
			// Gravity.LEFT, ClipDrawable.HORIZONTAL);
			// holder.hlt.setProgressDrawable(progress);
		}
		holder.text.setText(nodi[position].getNiceName());
		// holder.text.setTextAppearance(context, R.style.CodeFontTitle);

		// Progress = health
		holder.hlt.setProgress(0);
		holder.hlt.setProgress(nodi[position].getHealth());
		/* Dimensioni del testo settate dalle opzioni */
		// holder.textTyp.setTextSize(TypedValue.COMPLEX_UNIT_SP,holder.textTyp.getTextSize()
		// + opzioni.getListDimensTesto());
		// holder.textTyp.setTextAppearance(context, R.style.CodeFontMain);

		StringBuilder txtTypInfo = new StringBuilder(nodi[position].getActiveTypicals().size() + " "
				+ context.getString(R.string.manual_typicals));
		txtTypInfo.append(" - " + context.getString(R.string.update) + " "
				+ Constants.getTimeAgo(nodi[position].getRefreshedAt()));
		holder.textTyp.setText(txtTypInfo.toString());

		if (opzioni.isLightThemeSelected()) {
			holder.textTyp.setTextColor(context.getResources().getColor(R.color.black));
			holder.text.setTextColor(context.getResources().getColor(R.color.black));
			holder.textHlt.setTextColor(context.getResources().getColor(R.color.black));
		}

		/* Icona del nodo */
		holder.image.setImageResource(holder.imageRes);

		if (opzioni.getTextFx()) {
			Animation a2 = AnimationUtils.loadAnimation(context, R.anim.alpha);
			a2.reset();
			// a2.setStartTime(System.currentTimeMillis() + 400 * position);
			a2.setStartOffset(250 * position);
			// Animazione immagine holder.image.clearAnimation();
			holder.image.startAnimation(a2);
			// holder.text.clearAnimation();
			// holder.text.startAnimation(a2);
		}

		holder.data = nodi[position];

		return convertView;
	}

	public static class NodeViewHolder {
		TextView text;
		TextView textTyp;
		TextView textHlt;
		ProgressBar hlt;
		ImageView image;
		public SoulissNode data;
		int imageRes;
	}
}