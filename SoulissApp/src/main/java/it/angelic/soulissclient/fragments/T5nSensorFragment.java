package it.angelic.soulissclient.fragments;

import android.graphics.LinearGradient;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.R.color;
import it.angelic.soulissclient.SoulissApp;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.model.ISoulissTypicalSensor;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.SoulissUtils;
import it.angelic.tagviewlib.SimpleTagRelativeLayout;

import static junit.framework.Assert.assertTrue;

public class T5nSensorFragment extends AbstractTypicalFragment {

    private SoulissDBHelper datasource;
    private ImageView icon;
    private TextView nodeinfo;
    private ProgressBar par;
    private TextView upda;

    public static T5nSensorFragment newInstance(int index, SoulissTypical content) {
        T5nSensorFragment f = new T5nSensorFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        // Ci metto il nodo dentro
        if (content != null) {
            args.putSerializable("TIPICO", content);
        }
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        if (opzioni.isLightThemeSelected())
            getActivity().setTheme(R.style.LightThemeSelector);
        else
            getActivity().setTheme(R.style.DarkThemeSelector);
        super.onActivityCreated(savedInstanceState);
        // getActivity().setContentView(R.layout.main_typicaldetail);
        setHasOptionsMenu(true);

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.get("TIPICO") != null) {
            collected = (SoulissTypical) extras.get("TIPICO");
        } else if (getArguments() != null) {
            collected = (SoulissTypical) getArguments().get("TIPICO");
        } else {
            Log.e(Constants.TAG, "Error retriving Typical Detail:");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;

        datasource = new SoulissDBHelper(getActivity());
        SoulissDBHelper.open();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.get("TIPICO") != null) {
            collected = (SoulissTypical) extras.get("TIPICO");
        } else if (getArguments() != null) {
            collected = (SoulissTypical) getArguments().get("TIPICO");
        } else {
            Log.e(Constants.TAG, "Error retriving Typical Detail:");

        }
        View ret = inflater.inflate(R.layout.frag_t5n_sensordetail, container, false);
        nodeinfo = (TextView) ret.findViewById(R.id.TextViewTypNodeInfo);
        icon = (ImageView) ret.findViewById(R.id.typ_icon);
        upda = (TextView) ret.findViewById(R.id.TextViewTypUpdate);
        par = (ProgressBar) ret.findViewById(R.id.progressBarTypNodo);
        infoTags = (TableRow) ret.findViewById(R.id.tableRowTagInfo);
        tagView = (SimpleTagRelativeLayout) ret.findViewById(R.id.tag_group);
        assertTrue("TIPICO NULLO", collected != null);


        android.support.v4.app.FragmentManager manager = getActivity().getSupportFragmentManager();
        //Fragment details = manager.findFragmentById(R.id.hvacChart);

        ChartFragment NewFrag = ChartFragment.newInstance((ISoulissTypicalSensor) collected);
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.hvacChart, NewFrag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();


        refreshTagsInfo();

        //Setta STATUS BAR
        super.setCollected(collected);

        refreshStatusIcon();

        nodeinfo.setText(collected.getParentNode().getNiceName() + " - "
                + getResources().getString(R.string.slot) + " " + collected.getTypicalDTO().getSlot()
                + " - " + getContext().getString(R.string.reading) + " " + String.format(java.util.Locale.US, "%.2f", ((ISoulissTypicalSensor) collected).getOutputFloat()));


        par.setMax(Constants.MAX_HEALTH);

        // ProgressBar sfumata
        final ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(Constants.roundedCorners, null, null));
        final LinearGradient gradient = new LinearGradient(0, 0, 250, 0, getResources().getColor(color.aa_red),
                getResources().getColor(color.aa_green), android.graphics.Shader.TileMode.CLAMP);
        pgDrawable.getPaint().setStrokeWidth(3);
        pgDrawable.getPaint().setDither(true);
        pgDrawable.getPaint().setShader(gradient);

        ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        par.setBackgroundResource(android.R.drawable.progress_horizontal);
        par.setProgressDrawable(progress);
        par.setMax(50);
        par.setProgress(20);
        par.setProgress(0); // <-- BUG Android
        par.setMax(Constants.MAX_HEALTH);
        par.setProgress(collected.getParentNode().getHealth());
        upda.setText(getResources().getString(R.string.update) + " "
                + SoulissUtils.getTimeAgo(collected.getTypicalDTO().getRefreshedAt()));

        icon.setImageResource(collected.getIconResourceId());

        return ret;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // datasource.close();
    }


}
