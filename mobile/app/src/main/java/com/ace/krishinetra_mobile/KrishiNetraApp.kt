package com.ace.krishinetra_mobile

import android.app.Application
import com.ace.krishinetra_mobile.data.local.AppDatabase
import com.clerk.api.Clerk

class KrishiNetraApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    var isClerkEnabled: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        val clerkKey = BuildConfig.CLERK_PUBLISHABLE_KEY
        if (clerkKey.isNotBlank()) {
            Clerk.initialize(this, publishableKey = clerkKey)
            isClerkEnabled = true
        }
    }
}
