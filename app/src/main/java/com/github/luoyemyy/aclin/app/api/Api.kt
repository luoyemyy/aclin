package com.github.luoyemyy.aclin.app.api

import com.github.luoyemyy.aclin.api.AbsApiManager
import com.github.luoyemyy.aclin.app.profile.getApiUrl

class Api : AbsApiManager() {
    override fun baseUrl(): String {
        return getApiUrl()
    }
}

fun refreshApi() = Api().refresh()

fun getUserApi(): UserApi = Api().getApi()