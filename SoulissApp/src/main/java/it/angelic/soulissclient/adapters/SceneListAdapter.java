package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissCommand;
import it.angelic.soulissclient.model.SoulissScene;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;

public class SceneListAdapter extends BaseAdapter {
    List<SoulissScene> scenesList;
    private Activity context;
    private LayoutInflater mInflater;
    private SoulissPreferenceHelper opzioni;

    public SceneListAdapter(Activity context, List<SoulissScene> versio, SoulissPreferenceHelper opts) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.scenesList = versio;
        opzioni = opts;
    }

    public int getCount() {
        return scenesList.size();
    }

    public Object getItem(int position) {
        return scenesList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public List<SoulissScene> getScenes() {
        return scenesList;
    }

    public void setScenes(List<SoulissScene> scene) {
        this.scenesList = scene;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SceneViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_scenes, parent, false);
            holder = new SceneViewHolder();
            holder.textCmd = (TextView) convertView.findViewById(R.id.TextViewCommand);
            holder.textCmdWhen = (TextView) convertView.findViewById(R.id.TextViewCommandWhen);
            holder.textCmdInfo = (TextView) convertView.findViewById(R.id.TextViewCommandInfo);
            holder.image = (TextView) convertView.findViewById(R.id.command_icon);
            convertView.setTag(holder);
        } else {
            holder = (SceneViewHolder) convertView.getTag();
        }
        //fuori e vaffanculo
        holder.data = scenesList.get(position);
        // holder.data.getCommand().getNodeId()
        if (opzioni.isLightThemeSelected()) {
            holder.textCmdWhen.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.textCmd.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.textCmdInfo.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        if (scenesList.get(position).getIconResourceId() != 0)
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, scenesList.get(position).getIconResourceId());
        else
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_moon_o.getFontName());
        holder.image.setTextColor(ContextCompat.getColor(context, R.color.aa_yellow));

        ArrayList<SoulissCommand> appoggio = holder.data.getCommandArray();
        // name = context.getString(appoggio.getAliasNameResId());
        holder.textCmd.setText(scenesList.get(position).getNiceName());


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
        public SoulissScene data;
        TextView textCmd;
        TextView textCmdWhen;
        TextView textCmdInfo;
        TextView image;
    }
}