package com.github.luoyemyy.aclin.app

import android.app.Application
import com.github.luoyemyy.aclin.file.FileManager
import com.github.luoyemyy.aclin.file.getInnerFile
import com.github.luoyemyy.aclin.logger.AppError
import com.github.luoyemyy.aclin.logger.Logger


/**
 *
/class App : Application() {
//    override fun onCreate() {
//        super.onCreate()
//        AppInfo.init(this)
//        Profile.initType(this,BuildConfig.BUILD_TYPE)
//        ProfileProperties.initProperties()
//    }
//}
 */
object AppInfo {

    lateinit var appInfo: String

    fun init(app: Application) {
        appInfo = "app_info"
        FileManager.init(app)
        AppError.init(app)
        Logger.logPath = getInnerFile().dir(FileManager.LOG)?.absolutePath
    }
}