package com.github.luoyemyy.aclin.bus

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner

fun addBus(owner: LifecycleOwner, event: String, result: BusResult) {
    Bus.register(BusObserver(owner.lifecycle, result, event))
}

fun setBus(owner: LifecycleOwner, event: String, result: BusResult) {
    Bus.replaceRegister(BusObserver(owner.lifecycle, result, event))
}

//debug
fun debugBus(debugListener: BusDebugListener) {
    Bus.addDebug(debugListener)
}

fun postBus(event: String, intValue: Int = 0, longValue: Long = 0L, boolValue: Boolean = false, stringValue: String? = null, extra: Bundle? = null) {
    Bus.post(event, intValue, longValue, boolValue, stringValue, extra)
}