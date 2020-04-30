package com.github.luoyemyy.aclin.app.common.api

import androidx.lifecycle.LiveData
import com.github.luoyemyy.aclin.app.common.api.entity.ApiUser
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    @GET("api/login")
    fun login(): LiveData<ApiUser>

    @GET("download")
    fun list(): Call<List<String>>


    @GET("download")
    fun nestList(@Query("path") path: String): Call<List<String>>
}