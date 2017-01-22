package it.angelic.soulissclient.util;

import android.content.Context;

import it.angelic.soulissclient.R;

/**
 * Created by Ale on 06/10/2015.
 */
public enum LauncherElementEnum {
    STATIC_SCENES, STATIC_PROGRAMS, STATIC_MANUAL, STATIC_TAGS, TAG, STATIC_STATUS, POSIZIONE, SERVIZI, NODE, TYPICAL, SCENE;

    public String toString(Context ctx) {
        switch (this) {
            case STATIC_SCENES:
                return ctx.getString(R.string.node);
            case STATIC_PROGRAMS:
                return ctx.getString(R.string.programs_title);
            case STATIC_MANUAL:
                return ctx.getString(R.string.manual_title);
            case STATIC_TAGS:
                return ctx.getString(R.string.tags);
            case STATIC_STATUS:
                return ctx.getString(R.string.status_souliss);
            default:
                return ctx.getString(R.string.programs_title);
        }
    }
}
