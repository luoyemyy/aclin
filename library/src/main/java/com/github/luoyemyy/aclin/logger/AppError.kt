package com.github.luoyemyy.aclin.logger

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


class AppError private constructor(private val mApp: Application, private val mDefaultHandler: Thread.UncaughtExceptionHandler) :
    Thread.UncaughtExceptionHandler {

    //用来存储设备信息
    private var deviceInfo: String? = null

    override fun uncaughtException(thread: Thread?, ex: Throwable?) {
        if (handleException(ex) == null) {
            mDefaultHandler.uncaughtException(thread, ex)
        } else {
            //            val launcher = mApp.packageManager.getLaunchIntentForPackage(mApp.packageName)
            //            if (launcher != null) {
            //                launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            //                mApp.startActivity(launcher)
            //            }
            System.exit(0)
        }
    }

    private fun handleException(ex: Throwable?): String? {
        if (ex == null) {
            return null
        }
        if (deviceInfo == null) {
            //收集设备参数信息
            collectDeviceInfo(mApp)
        }
        //打印和保存日志文件
        val log = collectExceptionInfo(ex)
        logFile()?.apply {
            FileOutputStream(this).use { it.write(log.toByteArray()) }
        }
        Log.e("AppError", "handleException:  $log")
        return log
    }

    private fun logFile(): File? {
        val path = Logger.logPath ?: return null
        val logFileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
        return File.createTempFile(logFileName, ".log.txt", File(path))
    }

    @Suppress("DEPRECATION")
    private fun collectDeviceInfo(context: Context) {
        val stringBuilder = StringBuilder()
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            stringBuilder.append("versionCode").append("=").append(pi.versionCode).append("\n")
            stringBuilder.append("versionName").append("=").append(pi.versionName).append("\n")

            Build::class.java.declaredFields.forEach {
                it.isAccessible = true
                stringBuilder.append(it.name).append("=").append(it.get(null).toString()).append("\n")
            }

        } catch (e: Exception) {
            Log.e("AppError", "collectDeviceInfo", e)
        }
        deviceInfo = stringBuilder.toString()
    }

    private fun collectExceptionInfo(throwable: Throwable?): String {
        var ex = throwable
        val sb = StringBuilder()
        try {
            StringWriter().use { sw ->
                PrintWriter(sw).use {
                    do {
                        ex?.printStackTrace(it)
                        ex = ex?.cause
                    } while (ex != null)
                }

                sb.append(deviceInfo)
                sb.append(sw.buffer)
            }
        } catch (e: Exception) {
            Log.e("AppError", "collectExceptionInfo", e)
        }
        return sb.toString()
    }

    companion object {

        fun init(app: Application) {
            val appError = AppError(app, Thread.getDefaultUncaughtExceptionHandler())
            Thread.setDefaultUncaughtExceptionHandler(appError)
        }
    }
}