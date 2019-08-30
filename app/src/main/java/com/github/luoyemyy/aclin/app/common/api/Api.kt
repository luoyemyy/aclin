package com.github.luoyemyy.aclin.app.common.api

import com.github.luoyemyy.aclin.api.ApiManager

fun getUserApi(): UserApi = ApiManager.getInstance().getApi()