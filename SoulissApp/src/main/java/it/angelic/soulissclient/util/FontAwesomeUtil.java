package it.angelic.soulissclient.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import it.angelic.soulissclient.MainActivity;
import it.angelic.soulissclient.R;
import it.angelic.tagviewlib.FontNotFoundException;
import it.angelic.tagviewlib.SimpleTagViewUtils;

/**
 * Created by shine@angelic.it on 17/01/2017.
 */

public class FontAwesomeUtil extends SimpleTagViewUtils {

    public static String translateAwesomeCode(Context context, String fontName) throws FontNotFoundException {
        int codeidx;
        Log.d("SimpleTagView", "translateAwesomeCode set for: " + fontName);
        try {
            if (fontName.startsWith("&")) {
                codeidx = SimpleTagViewUtils.getAwesomeCodes(context).indexOf(fontName);
            } else {//try translate
                //codes according to http://fortawesome.github.io/Font-Awesome/cheatsheet/
                codeidx = SimpleTagViewUtils.getAwesomeNames(context).indexOf(fontName);
            }
            return SimpleTagViewUtils.getAwesomeCodes(context).get(codeidx);
        } catch (FontNotFoundException | ArrayIndexOutOfBoundsException | NullPointerException fr) {
            throw new FontNotFoundException("Font with code not found: " + fontName);
        }
    }

    public static void prepareMenuFontAweTextView(Activity context, TextView txtAwesome, String faCode) {
        txtAwesome.setTypeface(FontAwesomeUtil.getAwesomeTypeface(context));
        String code = FontAwesomeUtil.translateAwesomeCode(context, faCode);
        //content.setFontAwesomeCode(code);
        txtAwesome.setText(code);
        txtAwesome.setTextSize(42);
    }
    public static void prepareFontAweTextView(Activity context, TextView txtAwesome, @NonNull String faCode) {
        txtAwesome.setTypeface(FontAwesomeUtil.getAwesomeTypeface(context));
        String code = FontAwesomeUtil.translateAwesomeCode(context, faCode);
        //content.setFontAwesomeCode(code);
        txtAwesome.setText(code);
        txtAwesome.setTextSize(64);
    }

    public static void prepareMiniFontAweTextView(MainActivity mainActivity, TextView txtAwesome, String s) {
        txtAwesome.setTypeface(FontAwesomeUtil.getAwesomeTypeface(mainActivity));
        String code = FontAwesomeUtil.translateAwesomeCode(mainActivity, s);
        //content.setFontAwesomeCode(code);
        txtAwesome.setText(code);
        txtAwesome.setTextSize(24);
    }

    /**
     * Catena rimappaggio vecchie icone
     *
     * @author asfodel
     *
     * @param oldResId
     * @return
     */
    public static String remapIconResId(int oldResId) {
        switch (oldResId) {
            case R.drawable.fan:
                return "fa-plug";
            case R.drawable.plug:
                return "fa-plug";
            case R.drawable.square:
                return "fa-cube";
            case R.drawable.baby1:
                return "fa-child";
            case R.drawable.analog1:
                return "fa-line-chart";
            case R.drawable.raindrop:
                return "fa-tint";
            case R.drawable.bathtub1:
                return "fa-bath";
            case R.drawable.bedroom1:
                return "fa-bed";
            case R.drawable.bell1:
                return "fa-bell";
            case R.drawable.button1:
                return "fa-dot-circle-o";
            case R.drawable.cabinet1:
                return "fa-archive";
            case R.drawable.cafe1:
                return "fa-beer";
            case R.drawable.candle1:
                return "fa-star-o";
            case R.drawable.car1:
                return "fa-car";
            case R.drawable.chandelier1:

            case R.drawable.check1:
                return "fa-check-circle";
            case R.drawable.envelope1:
                return "fa-envelope";
            case R.drawable.exit1:
                return "fa-sign-out";
            case R.drawable.faucet1:
                return "fa-shower";
            case R.drawable.favorites2:
                return "fa-star-o";
            case R.drawable.filmmaker1:
                return "fa-film";
            case R.drawable.fire1:
                return "fa-fire";
            case R.drawable.flag1:
                return "fa-flag";
            case R.drawable.flower:
                return "fa-tree";
            case R.drawable.fork1:
                return "fa-cutlery";
            case R.drawable.frame1:
                return "fa-image";
            case R.drawable.gauge1:
                return "fa-level-down";
            case R.drawable.gauge2:
                return "fa-level-up";
            case R.drawable.home1:
                return "fa-home";
            case R.drawable.home21:
                return "fa-home";
            case R.drawable.home31:
                return "fa-home";
            case R.drawable.illumination17:
            case R.drawable.knife1:
            case R.drawable.lamp:
                return "fa-lightbulb-o";
            case R.drawable.light_off:
                return "fa-toggle-off";
            case R.drawable.light_on:
                return "fa-toggle-on";
            case R.drawable.lighthouse1:
                return "fa-magic";
            case R.drawable.lightning1:
                return "fa-flash";
            case R.drawable.limit1:
                return "fa-codepen";
            case R.drawable.lock1:
                return "fa-unlock";
            case R.drawable.locked1:
                return "fa-lock";
            case R.drawable.mark1:
                return "fa-remove";
            case R.drawable.moon:
                return "fa-moon";
            case R.drawable.pot:
                return "fa-spoon";
            case R.drawable.power:
                return "fa-power-off";
            case R.drawable.robot:
                return "fa-android";
            case R.drawable.setpoint:
                return "fa-tachometer";
            case R.drawable.shield1:
                return "fa-shield";
            case R.drawable.souliss_node:
                return "fa-square";
            case R.drawable.snow1:
                return "fa-snowflake-o";
            case R.drawable.sos:
                return "fa-life-ring";
            case R.drawable.stairs:
                return "fa-list-ol";
            case R.drawable.stove1:
                return "fa-glass";
            case R.drawable.student1:
                return "fa-graduation-cap";
            case R.drawable.sun:
                return "fa-sun-o";
            case R.drawable.timer:
                return "fa-history";
            case R.drawable.tag1:
                return "fa-tag";
            case R.drawable.tv:
                return "fa-tv";
            case R.drawable.twitter:
                return "fa-twitter";
            case R.drawable.warn:
                return "fa-warning";
            case R.drawable.window:
                return "fa-window-maximize";
            default:
                return "fa-square-o";
        }

    }


}
