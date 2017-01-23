package it.angelic.soulissclient.util;

/**
 * Created by shine@angelic.it on 22/01/2017.
 */

public enum FontAwesomeEnum {
    fa_anchor, fa_android, fa_archive, fa_arrows, fa_arrows_alt, fa_arrow_circle_down, fa_arrow_circle_left, fa_arrow_circle_right, fa_arrow_circle_up, fa_asterisk, fa_automobile, fa_balance_scale, fa_ban, fa_bar_chart, fa_bath, fa_bed, fa_beer, fa_bell_o, fa_birthday_cake, fa_bolt, fa_book, fa_bookmark_o, fa_briefcase, fa_building_o, fa_bullseye, fa_calendar, fa_camera, fa_car, fa_check_square, fa_check_square_o, fa_certificate, fa_child, fa_circle_o, fa_clock_o, fa_close, fa_cloud, fa_code_fork, fa_codepen, fa_coffee, fa_cogs, fa_crosshairs, fa_cube, fa_cut, fa_cutlery, fa_dashboard, fa_diamond, fa_dot_circle_o, fa_envelope, fa_exclamation_triangle, fa_expand, fa_eye, fa_fax, fa_feed, fa_fire, fa_flag, fa_flask, fa_folder_open, fa_gift, fa_glass, fa_graduation_cap, fa_hand_paper_o, fa_headphones, fa_heart, fa_heartbeat, fa_history, fa_home, fa_hourglass_2, fa_image, fa_key, fa_life_saver, fa_lightbulb_o, fa_line_chart, fa_list_ol, fa_location_arrow, fa_lock, fa_magic, fa_magnet, fa_microchip, fa_minus, fa_mobile_phone, fa_moon_o, fa_paw, fa_plug, fa_plus, fa_power_off, fa_phone, fa_refresh, fa_repeat, fa_search, fa_shield, fa_sliders, fa_snowflake_o, fa_sort, fa_spoon, fa_star_o, fa_sun_o, fa_tag, fa_th, fa_thermometer_half, fa_thumb_tack, fa_tint, fa_toggle_off, fa_toggle_on, fa_tree, fa_umbrella, fa_unlock, fa_tv, fa_wifi, fa_window_maximize, fa_sign_in, fa_sign_out, fa_tags, fa_puzzle_piece;

    public String getFontName() {
        return toString().replaceAll("_", "-");
    }
}
