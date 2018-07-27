package com.davidparkeredwards.washboard

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Created by davidedwards on 7/26/18.
 */
class WashboardApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this);
    }
}