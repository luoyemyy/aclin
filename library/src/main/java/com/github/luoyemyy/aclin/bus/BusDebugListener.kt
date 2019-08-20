package com.github.luoyemyy.aclin.bus

interface BusDebugListener {
    fun onRegister(current: Bus.Callback, all: List<Bus.Callback>)
    fun onUnRegister(current: Bus.Callback, all: List<Bus.Callback>)
    fun onPost(event: String, match: List<Bus.Callback>)
}