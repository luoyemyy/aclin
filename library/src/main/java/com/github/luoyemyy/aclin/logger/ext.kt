package com.github.luoyemyy.aclin.logger

fun loge(tag: String, msg: String, e: Throwable? = null){
    Logger.e(tag, msg, e)
}

fun logi(tag: String, msg: String, e: Throwable? = null){
    Logger.i(tag, msg, e)
}

fun logd(tag: String, msg: String, e: Throwable? = null){
    Logger.d(tag, msg, e)
}

fun logw(tag: String, msg: String, e: Throwable? = null){
    Logger.w(tag, msg, e)
}
