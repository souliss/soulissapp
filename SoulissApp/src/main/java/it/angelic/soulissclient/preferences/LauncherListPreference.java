package it.angelic.soulissclient.preferences;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import it.angelic.soulissclient.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.model.LauncherElement;

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
}