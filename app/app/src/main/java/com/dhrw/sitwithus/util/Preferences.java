package com.dhrw.sitwithus.util;


import android.content.Context;

public class Preferences {

    public static void setUserKey(Context context, String key) {
        context.getSharedPreferences("u", Context.MODE_PRIVATE).edit()
                .putString(Keys.USER_KEY, key).apply();
    }

    public static void setSearchKey(Context context, String key) {
        context.getSharedPreferences("u", Context.MODE_PRIVATE).edit()
                .putString(Keys.SEARCH_KEY, key).apply();
    }

    public static String getSearchKey(Context context) {
        return context.getSharedPreferences("u", Context.MODE_PRIVATE)
                .getString(Keys.SEARCH_KEY, null);
    }

    public static String getUserKey(Context context) {
        return context.getSharedPreferences("u", Context.MODE_PRIVATE)
                .getString(Keys.USER_KEY, null);
    }

    public static void setUsername(Context context, String key) {
        context.getSharedPreferences("u", Context.MODE_PRIVATE).edit()
                .putString(Keys.USERNAME, key).apply();
    }

    public static String getUsername(Context context) {
        return context.getSharedPreferences("u", Context.MODE_PRIVATE)
                .getString(Keys.USERNAME, null);
    }
}
