package com.example.ffintegrationandroidapp

import android.app.Application
import android.util.Log
import com.google.android.gms.net.CronetProviderInstaller
import org.chromium.net.CronetEngine

class MyApplication : Application() {

    private val TAG = this::class.java.simpleName

    companion object {
        lateinit var cronetEngine : CronetEngine
    }

    override fun onCreate() {
        super.onCreate()
        CronetProviderInstaller.installProvider(this).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Successfully installed Play Services provider: $it")
                cronetEngine = CronetEngine.Builder(this)
                    .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 10 * 1024 * 1024)
                    .build()
            } else {
                Log.w(TAG, "Unable to load Cronet from Play Services", it.exception)
            }
        }
    }

}