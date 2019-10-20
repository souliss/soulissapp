package it.angelic.soulissclient.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Spinner;

import androidx.preference.DialogPreference;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.util.LauncherElementEnum;

/**
 * The OptionDialogPreference will display a dialog, and will persist the
 * <code>true</code> when pressing the positive button and <code>false</code>
 * otherwise. It will persist to the android:key specified in xml-preference.
 */
public class LauncherAddCustomPreference extends DialogPreference implements DialogInterface.OnClickListener {

    private static LauncherElementEnum[] statArr = new LauncherElementEnum[]{LauncherElementEnum.NODE, LauncherElementEnum.TYPICAL, LauncherElementEnum.SCENE, LauncherElementEnum.TAG};
    private Context context;
    SoulissDBLauncherHelper datasource;
    private Spinner outputTYpSpinner;
    private Spinner typeSpinner;

    public LauncherAddCustomPreference(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.context = context;
        datasource = new SoulissDBLauncherHelper(context);
    }

    public LauncherAddCustomPreference(Context context) {
        this(context, null);
    }


    public LauncherAddCustomPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public LauncherAddCustomPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.i(Constants.TAG, "onCLick() vuoto, il lavoro e` nel onClose()");
        super.onClick();
    }

}