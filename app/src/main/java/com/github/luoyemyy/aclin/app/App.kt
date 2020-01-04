package com.github.luoyemyy.aclin.app

import android.app.Application
import com.github.luoyemyy.aclin.app.common.db.Db
import com.github.luoyemyy.aclin.app.common.profile.Properties

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Db.initDb(this)
        AppInfo.init(this, BuildConfig.BUILD_TYPE, Properties())
    }
}