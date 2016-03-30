package it.angelic.soulissclient.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import it.angelic.soulissclient.AbstractStatusedFragmentActivity;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDBTagHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTag;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.tagviewlib.OnSimpleTagDeleteListener;
import it.angelic.tagviewlib.SimpleTagRelativeLayout;
import it.angelic.tagviewlib.SimpleTagView;

public class AbstractTypicalFragment extends Fragment {
    protected TableRow infoTags;
    protected SimpleTagRelativeLayout tagView;
    protected SoulissPreferenceHelper opzioni;
    protected SoulissTypical collected;
    Toolbar actionBar;

    public AbstractTypicalFragment() {
        super();
        opzioni = SoulissApp.getOpzioni();
    }

    public SoulissTypical getCollected() {
        return collected;
    }

    void setCollected(SoulissTypical collected) {
        this.collected = collected;
    }

    @Override
    public void onStart() {
        super.onStart();
        actionBar = (Toolbar) getActivity().findViewById(R.id.my_awesome_toolbar);

        ((AbstractStatusedFragmentActivity) getActivity()).setSupportActionBar(actionBar);
        ((AbstractStatusedFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        refreshStatusIcon();
    }

    protected void refreshStatusIcon() {
        try {
            View ds = actionBar.getRootView();
            if (ds != null) {
                TextView info1 = (TextView) ds.findViewById(R.id.TextViewInfoStatus);
                TextView info2 = (TextView) ds.findViewById(R.id.TextViewInfo2);
                ImageButton online = (ImageButton) ds.findViewById(R.id.online_status_icon);
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

    protected void refreshTagsInfo() {
        tagView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (collected.getTypicalDTO().isTagged() || collected.getTypicalDTO().isFavourite()) {
                        SoulissDBTagHelper tagDb = new SoulissDBTagHelper(getContext());
                        List<SoulissTag> tags = tagDb.getTagsByTypicals(collected);
                        tagView.removeAll();
                        StringBuilder tagInfo = new StringBuilder();
                        tagInfo.append(getString(R.string.amongTags)).append("\n");
                        for (SoulissTag newT : tags) {
                            SimpleTagView nuovoTag = new SimpleTagView(getContext(), newT.getNiceName());

                            //nuovoTag.setColor(ContextCompat.getColor(getContext(), R.color.black_overlay));
                            nuovoTag.setFontAwesome("fa-tag");
                            nuovoTag.setDeletable(true);
                            Log.w(Constants.TAG, "adding tag to view: " + nuovoTag.getText());
                            tagView.addTag(nuovoTag);
                            //badgeView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.grey_alpha));
                            //tagInfo.append("-").append(newT.getNiceName()).append("\n");
                        }
                        infoTags.setVisibility(View.VISIBLE);
                        //textviewHistoryTags.setText(tagInfo.toString());
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, "FAIL refreshTagsInfo: " + e.getMessage());
                }

                tagView.setOnSimpleTagDeleteListener(new OnSimpleTagDeleteListener() {
                    @Override
                    public void onTagDeleted(SimpleTagView tag) {
                        final SoulissDBTagHelper tagDb = new SoulissDBTagHelper(getContext());

                        List<SoulissTag> toRemoveL = tagDb.getTagsByTypicals(collected);
                        for (SoulissTag tr : toRemoveL) {
                            if (tr.getName().equals(tag.getText())) {
                                Log.w(Constants.TAG, "Removing " + tr.getName() + " from " + collected.toString());
                                List<SoulissTypical> temp = tr.getAssignedTypicals();
                                //necessaria sta roba perche non ce equals?
                                //no, perche il DB fa da se. Ci sarebbe considerazione
                                //filosofica sul fatto che in realta equals e` quella,
                                //e in questo caso la terna nodeid:slot:tagId
                                tagDb.deleteTagTypicalNode(collected, tr);
                                Log.w(Constants.TAG, "Removed TAG from typical");
                            }
                        }
                        tagView.remove(tag);
                        //Toast.makeText(MainActivity.this, "\"" + tag.text + "\" deleted", Toast.LENGTH_SHORT).show();
                    }

                });

            }
        }, 500);//con calma
    }

}