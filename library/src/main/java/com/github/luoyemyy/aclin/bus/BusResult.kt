package com.github.luoyemyy.aclin.bus

import androidx.annotation.MainThread


interface BusResult {
    @MainThread
    fun busResult(event: String, msg: BusMsg)
}