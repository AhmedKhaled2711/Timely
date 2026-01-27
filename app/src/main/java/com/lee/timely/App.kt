package com.lee.timely

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase
            // Note: Firebase will auto-initialize if google-services.json is present
            // This explicit initialization ensures proper setup
            val app = FirebaseApp.initializeApp(this)
            
            if (app != null) {
                val auth = FirebaseAuth.getInstance(app)
                
                // Sign in anonymously for Firestore access (if not already signed in)
                // This is required for license key validation
                if (auth.currentUser == null) {
                    auth.signInAnonymously()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Anonymous authentication successful")
                            } else {
                                val exception = task.exception
                                Log.e(TAG, "Anonymous authentication failed", exception)
                                // Note: App can still function, but Firestore operations may fail
                                // The activation screen will handle this gracefully
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Anonymous authentication error", exception)
                        }
                } else {
                    Log.d(TAG, "User already authenticated: ${auth.currentUser?.uid}")
                }
            } else {
                Log.w(TAG, "Firebase initialization returned null. Check google-services.json configuration.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
            // App can still function for local-only features
            // Firebase-dependent features will show appropriate error messages
        }
    }
    
    companion object {
        private const val TAG = "TimelyApp"
    }
} 