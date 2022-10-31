package com.vpadn.analytics

import android.app.Application
import com.vpon.sdk.VpdataAnalytics

class MainApplication : Application() {
    //TODO set your licenseKey & customerId
    private val licenseKey = "mock_license_key"
    private val customerId = "mock_custom_id"

    override fun onCreate() {
        super.onCreate()

        val vpdataAnalytics = VpdataAnalytics

        //just for debug mode, remember to set to false before app release!!!
        //need to be set before vpdataAnalytics.initialize
        vpdataAnalytics.setDebugMode(true)

        //set VpdataAnalytics.OptIn according to users agreement
        vpdataAnalytics.initialize(this, licenseKey, customerId, VpdataAnalytics.OptIn.DEFAULT)
    }
}