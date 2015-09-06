package it.angelic.soulissclient.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;

import java.io.File;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.TagDetailActivity;
import it.angelic.soulissclient.fragments.TagDetailFragment;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;

public class TagRecyclerAdapter extends  RecyclerView.Adapter<TagRecyclerAdapter.TagViewHolder> {
	private LayoutInflater mInflater;
	private Context context;
	SoulissTag[] soulissTags;
	private SoulissPreferenceHelper opzioni;

	public TagRecyclerAdapter(Context context, SoulissTag[] versio, SoulissPreferenceHelper opts) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.soulissTags = versio;
		opzioni = opts;
	}


	public Object getItem(int position) {
		return soulissTags[position];
	}



	@Override
	public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	/*	View convertView = mInflater.inflate(R.layout.listview_scenes, parent, false);
		holder = new TagViewHolder();
		holder.textCmd = (TextView) convertView.findViewById(R.id.TextViewCommand);
		holder.textCmdWhen = (TextView) convertView.findViewById(R.id.TextViewCommandWhen);
		holder.textCmdInfo = (TextView) convertView.findViewById(R.id.TextViewCommandInfo);
		holder.image = (ImageView) convertView.findViewById(R.id.command_icon);
		convertView.setTag(holder);*/

		View itemView = LayoutInflater.
				from(parent.getContext()).
				inflate(R.layout.cardview_tag, parent, false);

		TagViewHolder hero =  new TagViewHolder(itemView);
		return hero;
	}

	@Override
	public void onBindViewHolder(TagViewHolder holder, int position) {
		List<SoulissTypical> appoggio = soulissTags[position].getAssignedTypicals();
		// SoulissCommandDTO dto = holder.data.getCommandDTO();
		// if (name == null || "".compareTo(name) == 0)
		// name = context.getString(appoggio.getAliasNameResId());
		holder.textCmd.setText(soulissTags[position].getName());
		String strMeatFormat = "Contains %1$d devices";
		holder.textCmdWhen.setText(String.format(strMeatFormat, appoggio.size()));
		holder.data = soulissTags[position];
		//holder.image.setImageResource(soulissTags[position].getIconResourceId());
        if ( holder.data.getImagePath() != null) {

            File picture = new File(TagDetailFragment.getRealPathFromURI(Uri.parse(holder.data.getImagePath())));

            // File picture = new File(Uri.parse(collectedTag.getImagePath()).getPath());
            if (picture.exists()) {
                //ImageView imageView = (ImageView)findViewById(R.id.imageView);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap myBitmap = BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
                Log.i(Constants.TAG, "bitmap size " + myBitmap.getRowBytes());
                holder.image.setImageBitmap(myBitmap);
            }
           /* try {
                mLogoImg.setImageURI(Uri.parse(collectedTag.getImagePath()));

            } catch (Exception e) {
                Log.d(TAG, "can't set logo", e);
            }*/
        }
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return soulissTags.length;
	}

	public SoulissTag[] getTags() {
		return soulissTags;
	}


	public static class TagViewHolder extends RecyclerView.ViewHolder   {

		TextView textCmd;
		TextView textCmdWhen;
		ImageView image;
		public SoulissTag data;

		public TagViewHolder(View itemView) {
			super(itemView);
            textCmd =  (TextView) itemView.findViewById(R.id.TextViewTagTitle);
            textCmdWhen = (TextView)  itemView.findViewById(R.id.TextViewTagDesc);
            image = (ImageView)  itemView.findViewById(R.id.imageViewTag);
		}

    }

	public void setTags(SoulissTag[] scene) {
		this.soulissTags = scene;
	}
}