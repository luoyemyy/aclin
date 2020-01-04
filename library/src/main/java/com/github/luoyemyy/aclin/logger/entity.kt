package com.github.luoyemyy.aclin.logger

import com.github.luoyemyy.aclin.mvp.MvpData

data class LoggerItem(val text: String, val path: String, var select: Boolean = false) : MvpData()