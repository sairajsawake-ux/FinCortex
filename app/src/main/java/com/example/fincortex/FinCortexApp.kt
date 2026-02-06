package com.example.fincortex

import android.app.Application
import com.google.firebase.FirebaseApp

class FinCortexApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
