package com.github.luoyemyy.aclin.app.profile

import com.github.luoyemyy.aclin.profile.Profile

object ProfileProperties {
    internal const val API_URL = "api.url"

    fun initProperties() {
        Profile.add(API_URL, "http://127.0.0.1:8080/")
    }
}

fun getApiUrl(): String = Profile.get(ProfileProperties.API_URL)