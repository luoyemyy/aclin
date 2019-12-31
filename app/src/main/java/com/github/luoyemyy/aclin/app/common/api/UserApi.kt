package com.github.luoyemyy.aclin.app.common.api

import androidx.lifecycle.LiveData
import com.github.luoyemyy.aclin.app.common.api.entity.ApiUser
import io.reactivex.Single
import retrofit2.http.GET

interface UserApi {

    @GET("api/login")
    fun login(): LiveData<ApiUser>
}