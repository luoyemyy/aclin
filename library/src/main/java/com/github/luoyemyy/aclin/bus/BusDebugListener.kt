package com.github.luoyemyy.aclin.bus

interface BusDebugListener {
    fun onRegister(currentCallback: Bus.Callback, allCallbacks: List<Bus.Callback>)
    fun onUnRegister(currentCallback: Bus.Callback, allCallbacks: List<Bus.Callback>)
    fun onPost(event: String, callbacks: List<Bus.Callback>)
}