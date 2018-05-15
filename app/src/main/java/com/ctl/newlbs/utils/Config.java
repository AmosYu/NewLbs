package com.ctl.newlbs.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2018/5/14/014.
 */

public class Config {

    private static final String CONFIG_FILENAME = "LAST_SETTING";
    private static final String CONFIG_SECS_CYCLE = "CONFIG_SECS_CYCLE";
    private static final String CONFIG_DEV_ID = "CONFIG_DEV_ID";

    public static void setCollectionCycle(Context context,String secs){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CONFIG_SECS_CYCLE, secs);
        editor.commit();
    }

    public static String getCollectionCycle(Context context){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILENAME, Context.MODE_PRIVATE);
        String secs = sp.getString(CONFIG_SECS_CYCLE, "3");
        return secs;
    }



    public static void setDevId(Context context,String id){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CONFIG_DEV_ID, id);
        editor.commit();
    }

    public static String getDevId(Context context){
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILENAME, Context.MODE_PRIVATE);
        String secs = sp.getString(CONFIG_DEV_ID, "00");
        return secs;
    }
}
