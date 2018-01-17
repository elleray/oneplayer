/*
 * *
 *  Copyright (c) 2015. Dingtone, inc. All rights reserved.
 * /
 */

package xy.media.oneplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by yangwenjie on 16/7/22.
 */
public class SharePreferenceUtil {
    public static final String RECENT_PLAYING_VIDEO = "recent_playing_video";
    public static final String GODAP_APK_PATH = "godap_apk_path";
    public static final String FIRST_LOAD_GODAP_MUSIC_TO_SEARCH_LOCAL = "first_load_godap_music_to_search_local";
    public static final String MUSIC_PLAY_ORDER = "music_play_order";

    public static String getPrefString(Context context, String key,
                                       final String defaultValue) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getString(key, defaultValue);
    }

    public static void setPrefString(Context context, final String key,
                                     final String value) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        settings.edit().putString(key, value).commit();
    }

    public static boolean getPrefBoolean(Context context, final String key,
                                         final boolean defaultValue) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getBoolean(key, defaultValue);
    }

    public static void setPrefBoolean(Context context, final String key,
                                      final boolean value) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        settings.edit().putBoolean(key, value).commit();
    }

    public static void setPrefInt(Context context, final String key,
                                  final int value) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        settings.edit().putInt(key, value).commit();
    }

    public static int getPrefInt(Context context, final String key,
                                 final int defaultValue) {
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getInt(key, defaultValue);
    }

    public static void setRecentPlayingVideo(Context context, final String path){
        setPrefString(context, RECENT_PLAYING_VIDEO, path);
    }

    public static String getRecentPlayingVideo(Context context){
        return getPrefString(context, RECENT_PLAYING_VIDEO, "");
    }
}