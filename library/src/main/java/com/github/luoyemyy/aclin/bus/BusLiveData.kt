package com.github.luoyemyy.aclin.bus

import android.os.Bundle
import androidx.lifecycle.MutableLiveData

class BusLiveData : MutableLiveData<BusMsg>() {

    fun post(event: String, intValue: Int = 0, longValue: Long = 0L, boolValue: Boolean = false, stringValue: String? = null, extra: Bundle? = null) {
        postValue(BusMsg(event, intValue, longValue, boolValue, stringValue, extra))
    }
}