package com.vpadn.analytics;

import android.app.Application;

import com.vpon.sdk.VpdataAnalytics;

public class MainApplication extends Application {
    //TODO set your licenseKey & customerId
    private String licenseKey = "mock_license_key";
    private String customerId = "mock_custom_id";

    @Override
    public void onCreate() {
        super.onCreate();

        VpdataAnalytics vpdataAnalytics = VpdataAnalytics.INSTANCE;

        //just for debug mode, remember to set to false before app release!!!
        //need to be set before vpdataAnalytics.initialize
        vpdataAnalytics.setDebugMode(true);

        //set VpdataAnalytics.OptIn according to users agreement
        vpdataAnalytics.initialize(this, licenseKey, customerId, VpdataAnalytics.OptIn.DEFAULT);
    }
}
