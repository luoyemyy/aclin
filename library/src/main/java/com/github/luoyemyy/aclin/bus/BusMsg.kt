package com.github.luoyemyy.aclin.bus

import android.os.Bundle

class BusMsg(
    var event: String,
    var intValue: Int = 0,
    var longValue: Long = 0,
    var boolValue: Boolean = false,
    var stringValue: String? = null,
    var extra: Bundle? = null
) {

    override fun toString(): String {
        return "(event=$event,intValue=$intValue,longValue=$longValue,boolValue=$boolValue,stringValue=$stringValue,extra=$extra)"
    }
}

