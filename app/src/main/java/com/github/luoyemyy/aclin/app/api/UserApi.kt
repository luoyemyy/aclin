package com.github.luoyemyy.aclin.app.api

import com.github.luoyemyy.aclin.app.api.entity.User
import io.reactivex.Single
import retrofit2.http.GET

interface UserApi {

    @GET("api/login")
    fun login(): Single<User>
}