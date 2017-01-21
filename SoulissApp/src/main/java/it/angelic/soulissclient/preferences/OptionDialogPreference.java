package it.angelic.soulissclient.preferences;

import android.content.Context;
import android.content.ContextWrapper;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.model.LauncherElementEnum;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;

/**
 * The OptionDialogPreference will display a dialog, and will persist the
 * <code>true</code> when pressing the positive button and <code>false</code>
 * otherwise. It will persist to the android:key specified in xml-preference.
 */
public class OptionDialogPreference extends DialogPreference {

    private final Context context;
    SoulissDBLauncherHelper datasource;

    public OptionDialogPreference(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.context = context;
        datasource = new SoulissDBLauncherHelper(context);
    }

    @Override
    protected View onCreateDialogView() {
        // return super.onCreateDialogView();
        View dialoglayout = View.inflate(new ContextWrapper(context), R.layout.add_to_launcher_dialog, null);
        final Spinner outputNodeSpinner = (Spinner) dialoglayout.findViewById(R.id.elementType);
        final Spinner outputTYpSpinner = (Spinner) dialoglayout.findViewById(R.id.elementData);
        ArrayAdapter<LauncherElementEnum> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, LauncherElementEnum.values());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputNodeSpinner.setAdapter(adapter);

        ArrayAdapter<SoulissNode> adaptertyp = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, datasource.getAllNodes());
        adaptertyp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputTYpSpinner.setAdapter(adaptertyp);

        return dialoglayout;
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        persistBoolean(positiveResult);
    }

}