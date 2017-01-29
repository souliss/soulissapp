package it.angelic.soulissclient.preferences;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.model.LauncherElement;
import it.angelic.soulissclient.model.db.SoulissDBLauncherHelper;

public class LauncherListPreference extends MultiSelectListPreference {


    private final SoulissDBLauncherHelper dbLauncher;

    public LauncherListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entriesValues = new ArrayList<CharSequence>();

        dbLauncher = new SoulissDBLauncherHelper(context);
        List<LauncherElement> launcherItems = dbLauncher.getLauncherItems(context);

        for (LauncherElement el : launcherItems) {
            entries.add(el.getTitle());
            entriesValues.add("" + el.getId());
        }

        setEntries(entries.toArray(new CharSequence[]{}));
        setEntryValues(entriesValues.toArray(new CharSequence[]{}));
    }


    @Override
    public OnPreferenceChangeListener getOnPreferenceChangeListener() {

        return (new
                        Preference.OnPreferenceChangeListener() {
                            public boolean onPreferenceChange(Preference preference, Object newValue) {
                                Log.d(Constants.TAG, "LauncheronPreferenceChange(), preference" + preference + " newValue: " + newValue.toString());
                                final String val = newValue.toString();
                                int index = findIndexOfValue(val);
                                // preference.getPersistedStringSet()
                                //     setEnabled(true);

                                return true;
                            }
                        });
    }


}