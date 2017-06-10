package com.firebase.notification.test.ubiquobusiness;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by akain on 09/06/2017.
 */

public class UbiQuoBusinessUtils {
    public UbiQuoBusinessUtils(){

    }

    public static void changeStatusBarColor(Integer color, Activity activity){
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
        }
    }

    public static SharedPreferences.Editor putDoubleIntoEditor(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDoubleFromEditor(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public static void removeStatusBar(Activity activity){
        //rende la statusbar completamente invisibile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = activity.getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public static String hourFormatter(Integer hours, Integer minutes){
        String formattedHours;
        String formattedMinutes;
        if(hours<10 && minutes<10){
            formattedHours = "0"+hours;
            formattedMinutes = "0"+minutes;
            return formattedHours+":"+formattedMinutes;
        }
        if(hours<10 && minutes>=10){
            formattedHours = "0"+hours;
            formattedMinutes = String.valueOf(minutes);
            return formattedHours+":"+formattedMinutes;
        }
        if(hours>=10 && minutes<10){
            formattedHours = String.valueOf(hours);
            formattedMinutes = "0"+minutes;
            return formattedHours+":"+formattedMinutes;
        }

        if(hours>=10 && minutes>=10){
            formattedHours = String.valueOf(hours);
            formattedMinutes = String.valueOf(minutes);
            return formattedHours+":"+formattedMinutes;
        }

        return "00:00";

    }



}
