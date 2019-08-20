package com.github.luoyemyy.aclin.app

import android.app.Application
import android.util.Log
import com.github.luoyemyy.aclin.app.common.db.Db
import com.github.luoyemyy.aclin.app.profile.ProfileProperties
import com.github.luoyemyy.aclin.bus.Bus
import com.github.luoyemyy.aclin.bus.BusDebugListener
import com.github.luoyemyy.aclin.bus.debugBus
import com.github.luoyemyy.aclin.profile.Profile

class App : Application(), BusDebugListener {
    override fun onCreate() {
        super.onCreate()
        Db.initDb(this)
        AppInfo.init(this)
        Profile.initType(this, BuildConfig.BUILD_TYPE)
        ProfileProperties.initProperties()

//        debugBus(this)
    }

    override fun onRegister(current: Bus.Callback, all: List<Bus.Callback>) {
        Log.e("App", "debugBus.onRegister: current=[${current.interceptEvent()}],all=[${all.joinToString(
                ",") { it.interceptEvent() }}]")
    }

    override fun onUnRegister(current: Bus.Callback, all: List<Bus.Callback>) {
        Log.e("App", "debugBus.onUnRegister: current=[${current.interceptEvent()}],all=[${all.joinToString(
                ",") { it.interceptEvent() }}]")
    }

    override fun onPost(event: String, match: List<Bus.Callback>) {
        Log.e("App", "debugBus.onPost: current=[$event],size=[${match.size}]")
    }
}