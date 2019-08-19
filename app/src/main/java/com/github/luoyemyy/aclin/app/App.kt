package com.github.luoyemyy.aclin.app

import android.app.Application
import com.github.luoyemyy.aclin.app.profile.ProfileProperties
import com.github.luoyemyy.aclin.app.db.Db
import com.github.luoyemyy.aclin.profile.Profile

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Db.initDb(this)
        AppInfo.init(this)
        Profile.initType(this, BuildConfig.BUILD_TYPE)
        ProfileProperties.initProperties()
    }
}