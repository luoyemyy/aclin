package com.github.luoyemyy.aclin.bus

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider


fun FragmentActivity.setBus(busResult: BusResult,vararg events: String){
    getBusLiveData().also {
        it.observe(this, BusObserver(busResult, events.toList()))
    }
}

fun FragmentActivity.postBus(event: String, intValue: Int = 0, longValue: Long = 0L, boolValue: Boolean = false, stringValue: String? = null, extra: Bundle? = null) {
    getBusLiveData().postValue(BusMsg(event, intValue, longValue, boolValue, stringValue, extra))
}

fun FragmentActivity.getBusLiveData(): BusLiveData {
    return ViewModelProvider(this)[BusPresenter::class.java].busLiveData
}

fun Fragment.setBus(busResult: BusResult,vararg events: String) {
    getBusLiveData().also {
        it.removeObservers(this)
        it.observe(this, BusObserver(busResult, events.toList()))
    }
}

fun Fragment.postBus(event: String, intValue: Int = 0, longValue: Long = 0L, boolValue: Boolean = false, stringValue: String? = null, extra: Bundle? = null) {
    getBusLiveData().postValue(BusMsg(event, intValue, longValue, boolValue, stringValue, extra))
}

fun Fragment.getBusLiveData(): BusLiveData {
    return ViewModelProvider(this.requireActivity())[BusPresenter::class.java].busLiveData
}