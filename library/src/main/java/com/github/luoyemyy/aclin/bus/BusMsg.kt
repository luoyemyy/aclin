package com.github.luoyemyy.aclin.bus

import android.os.Bundle

class BusMsg(
    val event: String,
    val intValue: Int = 0,
    val longValue: Long = 0,
    val boolValue: Boolean = false,
    val stringValue: String? = null,
    val extra: Bundle? = null
) {

    override fun toString(): String {
        return "(event=$event,intValue=$intValue,longValue=$longValue,boolValue=$boolValue,stringValue=$stringValue,extra=$extra)"
    }
}

