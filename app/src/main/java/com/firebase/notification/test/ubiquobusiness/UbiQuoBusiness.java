package com.firebase.notification.test.ubiquobusiness;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by akain on 08/06/2017.
 */

public class UbiQuoBusiness extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Hero.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

    }
}
