package com.github.luoyemyy.aclin.bus

interface BusCallback : BusResult {
    fun interceptEvent(): List<String>
}