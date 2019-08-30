package com.github.luoyemyy.aclin.app

import android.app.Application
import com.github.luoyemyy.aclin.file.FileManager
import com.github.luoyemyy.aclin.file.getInnerFile
import com.github.luoyemyy.aclin.logger.AppError
import com.github.luoyemyy.aclin.logger.Logger
import com.github.luoyemyy.aclin.profile.Profile
import com.github.luoyemyy.aclin.profile.ProfileAdd
import com.github.luoyemyy.aclin.profile.ProfileProperties


/**
 *
/class App : Application() {
//    override fun onCreate() {
//        super.onCreate()
//        AppInfo.init(this,BuildConfig.BUILD_TYPE)
//    }
//}
 */
object AppInfo {

    lateinit var appInfo: String
    lateinit var fileProvider: String

    fun init(app: Application, buildType: String, profileAdd: ProfileAdd) {
        appInfo = "app_info"
        fileProvider = "${app.packageName}.FileProvider"
        AppError.init(app)
        FileManager.init(app)
        Profile.initType(app, buildType)
        ProfileProperties.initProperties(profileAdd)
        Logger.logPath = getInnerFile().dir(FileManager.LOG)?.absolutePath
    }
}