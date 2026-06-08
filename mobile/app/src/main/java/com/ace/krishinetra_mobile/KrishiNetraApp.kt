package com.ace.krishinetra_mobile

import android.app.Application
import com.ace.krishinetra_mobile.data.local.AppDatabase

class KrishiNetraApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
