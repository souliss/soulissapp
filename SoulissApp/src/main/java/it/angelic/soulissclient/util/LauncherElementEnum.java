package it.angelic.soulissclient.util;

import android.content.Context;

import it.angelic.soulissclient.R;

/**
 * Created by Ale on 06/10/2015.
 */
public enum LauncherElementEnum {
    STATIC_SCENES, STATIC_PROGRAMS, STATIC_MANUAL, STATIC_TAGS, TAG, STATIC_STATUS, STATIC_LOCATION, SERVIZI, NODE, TYPICAL, SCENE;

    public String toString(Context ctx) {
        switch (this) {
            case STATIC_SCENES:
                return ctx.getString(R.string.scenes_title);
            case STATIC_PROGRAMS:
                return ctx.getString(R.string.programs_title);
            case STATIC_MANUAL:
                return ctx.getString(R.string.manual_title);
            case STATIC_TAGS:
                return ctx.getString(R.string.tags);
            case STATIC_STATUS:
                return ctx.getString(R.string.status_souliss);
            case TYPICAL:
                return ctx.getString(R.string.typical);
            case SCENE:
                return ctx.getString(R.string.scene);
            case STATIC_LOCATION:
                return ctx.getString(R.string.position);
            case TAG:
                return ctx.getString(R.string.tag);
            default:
                return ctx.getString(R.string.programs_title);
        }
    }
}
