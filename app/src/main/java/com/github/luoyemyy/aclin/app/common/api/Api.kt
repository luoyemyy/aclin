package com.github.luoyemyy.aclin.app.common.api

import com.github.luoyemyy.aclin.api.AbsApiManager
import com.github.luoyemyy.aclin.app.profile.getApiUrl

class Api : AbsApiManager() {

    companion object {
        private val api = Api().apply { initApi() }
        fun getInstance() = api
    }

    override fun baseUrl(): String = getApiUrl()

}

fun refreshApi() = Api.getInstance().initApi()

fun getUserApi(): UserApi = Api.getInstance().getApi()