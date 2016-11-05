package com.crossover.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sumit on 11/5/16.
 * Helper class to store data in shared preferences.
 */

public class SharedPrefData {

    private static String SETTINGS_NAME = "crossoverSettings";

    public static final String ACCESS_TOKEN = "accessToken";

    public static void setTextData(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getTextData(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        String defaultStr = "";
        String value = pref.getString(key, defaultStr);
        return value;
    }
}
