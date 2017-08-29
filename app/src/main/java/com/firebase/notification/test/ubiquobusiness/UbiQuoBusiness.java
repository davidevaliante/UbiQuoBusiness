package com.firebase.notification.test.ubiquobusiness;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by akain on 08/06/2017.
 */

public class UbiQuoBusiness extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        //inizializzatore per il caching di firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Inizializzatore della libreria per i font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Hero.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}
