package com.example.agarbattidryer

import android.app.Application
import com.example.agarbattidryer.di.AppContainer
import com.example.agarbattidryer.di.DefaultAppContainer

class AgarbattiDryerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
