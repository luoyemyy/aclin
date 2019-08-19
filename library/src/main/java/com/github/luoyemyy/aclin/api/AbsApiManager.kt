package com.github.luoyemyy.aclin.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * demo code
 *
class Api : AbsApiManager() {
//
//    companion object {
//        private val api = Api().apply { initApi() }
//
//        fun getInstance() = api
//    }
//
//    override fun baseUrl(): String = getApiUrl()
//
//}
//
//fun refreshApi() = Api.getInstance().initApi()
//
//fun getUserApi(): UserApi = Api.getInstance().getApi()
 *
 */
abstract class AbsApiManager {

    private lateinit var mRetrofit: Retrofit

    fun initApi() {
        mRetrofit = createRetrofit()
    }

    fun getRetrofit(): Retrofit {
        return mRetrofit
    }

    inline fun <reified T> getApi(): T {
        return getRetrofit().create(T::class.java)
    }

    abstract fun baseUrl(): String

    open fun client(): OkHttpClient.Builder = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })

    open fun converter(): Converter.Factory? = GsonConverterFactory.create()

    open fun adapter(): CallAdapter.Factory? = RxJava2CallAdapterFactory.create()

    open fun createRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl()).client(client().build()).apply {
            converter()?.also {
                addConverterFactory(it)
            }
            adapter()?.also {
                addCallAdapterFactory(it)
            }
        }.build()
    }
}
