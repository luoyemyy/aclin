package com.github.luoyemyy.aclin.logger

import android.util.Log
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*

object Logger {

    var enableFileLog: Boolean = false
    var enableConsoleLog: Boolean = true
    var logPath: String? = null

    fun e(tag: String, msg: String, e: Throwable? = null) {
        log("E", tag, msg, e)
    }

    fun i(tag: String, msg: String, e: Throwable? = null) {
        log("I", tag, msg, e)
    }

    fun d(tag: String, msg: String, e: Throwable? = null) {
        log("D", tag, msg, e)
    }

    fun w(tag: String, msg: String, e: Throwable? = null) {
        log("W", tag, msg, e)
    }

    private fun log(level: String, tag: String, msg: String, e: Throwable?) {
        if (enableConsoleLog) {
            when (level) {
                "E" -> Log.e(tag, msg, e)
                "I" -> Log.i(tag, msg, e)
                "D" -> Log.d(tag, msg, e)
                "W" -> Log.w(tag, msg, e)
            }
        }
        if (enableFileLog) {
            val path = logPath ?: let {
                Log.w(tag, "logPath is null")
                return
            }
            write(e, Thread.currentThread().name, level, tag, msg, path)
        }
    }

    private fun write(throwable: Throwable?, threadName: String, level: String, tag: String, msg: String, path: String) {
        ThreadHandler.post {
            try {
                val (logFileName, logDateTime) = getDateKey()
                FileWriter("$path$logFileName.log.txt", true).use { sw ->
                    PrintWriter(sw, true).use { writer ->
                        writer.println()
                        writer.println("$logDateTime [$threadName]-$level/$tag:$msg")
                        var e = throwable
                        while (e != null) {
                            e.printStackTrace(writer)
                            e = e.cause
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e("LogWriter", "write:  ", e)
            }
        }
    }

    private fun getDateKey(): Pair<String, String> {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH) + 1
        val day = calender.get(Calendar.DAY_OF_MONTH)
        val hour = calender.get(Calendar.HOUR_OF_DAY)
        val minute = calender.get(Calendar.MINUTE)
        val second = calender.get(Calendar.SECOND)
        return Pair("$year-$month-$day", "$year-$month-$day $hour:$minute:$second")
    }
}
