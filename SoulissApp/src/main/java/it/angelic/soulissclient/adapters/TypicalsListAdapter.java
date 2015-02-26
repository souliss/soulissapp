package it.angelic.soulissclient.adapters;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TypicalsListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	private SoulissTypical[] tipici;
	private SoulissPreferenceHelper opzioni;
	Intent parentIntent;
	// private SoulissDBHelper dataSource;
	SoulissDataService mBoundService;

	public TypicalsListAdapter(Context context, SoulissDataService serv, SoulissTypical[] versio, Intent forExtra,
			 SoulissPreferenceHelper op) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.tipici = versio;
		
		/*for (int i = 0; i < versio.length; i++) {
			if (versio[i].getCtx() == null)
				throw new RuntimeException("Non dovrebbe essere nullo, controlla il giro");
		}*/
		
		opzioni = op;
		parentIntent = forExtra;
		// dataSource = ds;
		mBoundService = serv;
	}

	public void setTypicals(SoulissTypical[] in) {
		tipici = in;
	}

	public void setmBoundService(SoulissDataService mBoundService2) {
		mBoundService = mBoundService2;
	}

	public SoulissDataService getmBoundService() {
		return mBoundService;
	}

	public int getCount() {
		// Hack lista vuota
		if (tipici == null || tipici.length == 0)
			return 1;
		return (int) tipici.length;
	}

	public Object getItem(int position) {
		return tipici[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public SoulissTypical[] getTypicals() {
		return tipici;
	}

	public View getView(int position, View convertView, final ViewGroup parent) {
		TypicalViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_typical, parent, false);
			final View ccopy = convertView;
			holder = new TypicalViewHolder();
			holder.expand = (ImageView) convertView.findViewById(R.id.imageButtonExpand);
			holder.textslot = (TextView) convertView.findViewById(R.id.TextViewSlot);
			holder.textUpdated = (TextView) convertView.findViewById(R.id.TextViewUpdated);
			holder.textStatus = (TextView) convertView.findViewById(R.id.textViewStatus);
			holder.textStatusVal = (TextView) convertView.findViewById(R.id.textViewStatusVal);
			holder.image = (ImageView) convertView.findViewById(R.id.node_icon);
            holder.imageFav = (ImageView) convertView.findViewById(R.id.imageButtonFav);
			
			holder.linearActionsLayout = (LinearLayout) convertView.findViewById(R.id.linearLayoutButtons);
			// linButton.removeAllViews();
			convertView.setTag(holder);
			holder.expand.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ccopy.performLongClick();

				}
			});
		} else {
			holder = (TypicalViewHolder) convertView.getTag();
		}
		//fuori da if-else per evitare #74
		if (tipici.length > 0) // magari e` vuoto
			holder.data = tipici[position];


		if (opzioni.isLightThemeSelected()) {
			holder.textslot.setTextColor(context.getResources().getColor(R.color.black));
			holder.textUpdated.setTextColor(context.getResources().getColor(R.color.black));
			holder.textStatus.setTextColor(context.getResources().getColor(R.color.black));
			holder.expand.setImageDrawable(context.getResources().getDrawable(R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha));
		}
		/* Nodo vuota */
		if (tipici.length == 0) {
			holder.image.setImageResource(android.R.drawable.ic_dialog_alert);
			holder.image.setColorFilter(context.getResources().getColor(R.color.aa_yellow),
					android.graphics.PorterDuff.Mode.SRC_ATOP);
			holder.textslot.setText(context.getResources().getString(R.string.node_empty));
			holder.textStatus.setText(context.getResources().getString(R.string.node_empty_desc));
			// holder.evidenza.setBackgroundColor(context.getResources().getColor(color.trans_black));
			return convertView;
		}
		/* INFO slot e Alias Name */
		holder.textslot.setText(tipici[position].getNiceName());
		holder.textUpdated.setText(context.getString(R.string.update) + " "
				+ Constants.getTimeAgo(tipici[position].getTypicalDTO().getRefreshedAt()) + " - "
				+ context.getString(R.string.manual_slot) + ": " + tipici[position].getSlot());
		holder.textStatus.setText(context.getResources().getString(R.string.typical).toUpperCase(Locale.getDefault())
				+ ": " + tipici[position].getTypicalDTO().getTypicalDec() + " - "
				+ context.getResources().getString(R.string.status));
		/* Icona del nodo */
		if (tipici[position].getDefaultIconResourceId() != 0){
			holder.image.setImageResource(tipici[position].getDefaultIconResourceId());
			}
        if (tipici[position].getTypicalDTO().getFavourite() > 0)
            holder.imageFav.setVisibility(View.VISIBLE);
        else
            holder.imageFav.setVisibility(View.INVISIBLE);
		// nascondi gli slot slave
		//if (!tipici[position].isRelated() && !tipici[position].isEmpty()) {
			// holder.textStatusVal.setText(tipici[position].getOutputDesc());
			// TODO remove following
			//holder.textStatusVal.setTextColor(context.getResources().getColor(R.color.std_green));
			tipici[position].setOutputDescView(holder.textStatusVal);
		/*	convertView.setVisibility(View.VISIBLE);
			holder.image.setVisibility(View.VISIBLE);
			holder.textslot.setVisibility(View.VISIBLE);
			holder.textUpdated.setVisibility(View.VISIBLE);
			holder.textStatus.setVisibility(View.VISIBLE);
			holder.textStatusVal.setVisibility(View.VISIBLE);
			holder.expand.setVisibility(View.VISIBLE);
		/*} else if (tipici[position].isRelated()) {
			convertView.setVisibility(View.GONE);
			
			holder.image.setVisibility(View.GONE);
			//holder.shader.setVisibility(View.GONE);
			holder.textslot.setVisibility(View.GONE);
			holder.textUpdated.setVisibility(View.GONE);
			holder.textStatus.setVisibility(View.GONE);
			holder.textStatusVal.setVisibility(View.GONE);
			holder.expand.setVisibility(View.GONE);
			return convertView;
		} else {
			// lascia slot vuoti
			return convertView;

		}*/
		/* Aggiunta dei comandi */
		// LinearLayout cont = (LinearLayout)
		// convertView.findViewById(R.id.linearLayoutButtons);
		holder.linearActionsLayout.removeAllViews();
		if (opzioni.isSoulissReachable()) {
			// richiama l'overloaded del tipico relativo
			tipici[position].getActionsLayout(context,holder.linearActionsLayout);
		} else {
			TextView na = new TextView(context);
			na.setText(context.getString(R.string.souliss_unavailable));
			if (opzioni.isLightThemeSelected()) {
				na.setTextColor(context.getResources().getColor(R.color.black));
			}
			holder.linearActionsLayout.addView(na);
		}
		// linearActionsLayout.setVisibility(View.VISIBLE);	

		if (opzioni.getTextFx()) {
			Animation a = AnimationUtils.loadAnimation(context, R.anim.scalerotale);
			a.reset();
			a.setStartOffset(100 * position);
			// Animazione testo sotto holder.text.clearAnimation();
			holder.image.startAnimation(a);
		}
		return convertView;
	}

	public static class TypicalViewHolder {
		public ImageView expand;
		View shader;
		TextView textStatus;
		TextView textStatusVal;
		TextView textslot;
		TextView textUpdated;
		LinearLayout linearActionsLayout;
		ImageView image;
        ImageView imageFav;
		public SoulissTypical data;
	}

}