package com.takitareq.linkbox

import android.app.Application
import com.google.android.gms.ads.MobileAds

class LinkBoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdMob
        MobileAds.initialize(this) {
            // AdMob initialization completed
        }
    }
}