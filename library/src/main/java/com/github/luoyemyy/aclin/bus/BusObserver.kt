package com.github.luoyemyy.aclin.bus

import androidx.lifecycle.Observer

class BusObserver(private val mBusResult: BusResult, private val mEvents: List<String>) : Observer<BusMsg>, BusCallback {

    override fun busResult(msg: BusMsg) {
        mBusResult.busResult(msg)
    }

    override fun onChanged(it: BusMsg) {
        if (interceptEvent().contains(it.event)) {
            busResult(it)
        }
    }

    override fun interceptEvent(): List<String> {
        return mEvents
    }
}