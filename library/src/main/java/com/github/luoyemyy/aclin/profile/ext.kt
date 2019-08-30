package com.github.luoyemyy.aclin.profile

fun getApiUrl(): String = Profile.get(ProfileProperties.API_URL)

interface ProfileAdd {
    fun add()
}