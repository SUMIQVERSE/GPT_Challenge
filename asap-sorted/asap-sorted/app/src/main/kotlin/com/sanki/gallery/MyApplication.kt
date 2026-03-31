package com.sanki.gallery

import android.app.Application
import com.sanki.gallery.db.AppDatabase
import com.sanki.gallery.ai.DeepSearchEngine

import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup Crash Logger
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            Log.e("SankiCrash", "Global Crash: ${sw.toString()}")
            
            try {
                val crashFile = File(getExternalFilesDir(null), "sanki_crash_log.txt")
                crashFile.writeText("CRASH AT: ${System.currentTimeMillis()}\n\n${sw.toString()}")
            } catch (e: Exception) {
                // Ignore if we can't write
            }
            
            // Pass it back so Android kills it properly
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Initialize Database safely
        try {
            AppDatabase.getDatabase(this)
        } catch (e: Exception) {
            Log.e("SankiCrash", "Failed Database init", e)
        }

        // Initialize RunAnywhere SDK (lazy — only sets up platform context, no model loaded)
        try {
            DeepSearchEngine.initializeSDK(this)
        } catch (e: Exception) {
            Log.e("SankiCrash", "RunAnywhere SDK init failed (non-fatal)", e)
        }
    }
}
