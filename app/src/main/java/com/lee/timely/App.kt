package com.lee.timely

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class App : Application() {
    override fun onCreate() {
        super.onCreate()
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