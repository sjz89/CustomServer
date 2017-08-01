package com.bielang.customserver.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.bielang.customserver.MyApplication;

/**
 * SharePreferences工具类
 * Created by Daylight on 2017/7/6.
 */

@SuppressWarnings("unused")
public class SharePreferencesUtil {
    private final static String DATA="data";


    public  static void putValue(Context context, String key, long value) {
        String account= String.valueOf(MyApplication.getInstance().getMyInfo().getId());
        SharedPreferences.Editor spe = context.getSharedPreferences(DATA, Context.MODE_PRIVATE).edit();
        spe.putLong(key, value);
        spe.apply();
    }

    public  static void putValue(Context context, String key, boolean value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(DATA, Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.apply();
    }

    public static void putValue(Context context, String key, String value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(DATA, Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.apply();
    }

    public static long getValue(Context context, String key, long defValue) {
        SharedPreferences sp = context.getSharedPreferences(DATA,Context.MODE_PRIVATE);
        return sp.getLong(key, defValue);

    }

    public static boolean getValue(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(DATA,Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    public static String getValue(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(DATA,Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }
    public static void removeValue(Context context,String key) {
        SharedPreferences.Editor sp = context.getSharedPreferences(DATA, Context.MODE_PRIVATE).edit();
        sp.remove(key);
        sp.apply();
    }
}
