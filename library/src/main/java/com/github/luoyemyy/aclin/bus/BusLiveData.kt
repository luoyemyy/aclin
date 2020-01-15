package com.github.luoyemyy.aclin.bus

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class BusLiveData : MutableLiveData<BusMsg>() {

    private val mResults: MutableMap<String, Observer<BusMsg>> = mutableMapOf()

    override fun observe(owner: LifecycleOwner, observer: Observer<in BusMsg>) {
        (observer as? BusObserver)?.interceptEvent()?.joinToString(",")?.apply {
            mResults.put(this, observer)?.also {
                super.removeObserver(it)
            }
        }
        super.observe(owner, observer)
    }

    override fun removeObserver(observer: Observer<in BusMsg>) {
        (observer as? BusObserver)?.interceptEvent()?.joinToString(",")?.apply {
            mResults.remove(this)
        }
        super.removeObserver(observer)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        mResults.clear()
        super.removeObservers(owner)
    }

    fun post(event: String, intValue: Int = 0, longValue: Long = 0L, boolValue: Boolean = false, stringValue: String? = null, extra: Bundle? = null) {
        postValue(BusMsg(event, intValue, longValue, boolValue, stringValue, extra))
    }
}