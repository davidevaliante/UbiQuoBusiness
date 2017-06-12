package com.firebase.notification.test.ubiquobusiness;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by akain on 09/06/2017.
 */

public class UbiQuoBusinessUtils {

    private static SharedPreferences userData;


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

    public static void refreshCurrentUserToken(Context context){

        //se l'auth non è null
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
        userData = context.getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
            final String userToken = FirebaseInstanceId.getInstance().getToken();
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //solo se userToken esiste ed utente ancora loggato
            if (!userId.isEmpty() && !userToken.isEmpty()) {
                final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Businesses")
                        .child(userId);
                DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference().child("Token").child(userId).child("user_token");

                //aggiorna shared preferences
                userData.edit().putString("USER_TOKEN", userToken).commit();
                //aggiorna nodo del database
                tokenReference.setValue(userToken);

                //aggiorna token nel profilo utente
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Business user = dataSnapshot.getValue(Business.class);
                        user.setToken(userToken);
                        userReference.setValue(user);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }



}
