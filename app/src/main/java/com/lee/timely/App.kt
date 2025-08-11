package com.lee.timely

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.lee.timely.data.local.TimelyDatabase

class App : Application() {
    // Database instance
    val database: TimelyDatabase by lazy {
        TimelyDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        val app = FirebaseApp.initializeApp(this)
        if (app != null) {
            val auth = FirebaseAuth.getInstance(app)
            if (auth.currentUser == null) {
                auth.signInAnonymously()
            }
        } else {
            // Optionally log or handle Firebase not initialized
        }
    }
}