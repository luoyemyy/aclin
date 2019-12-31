package com.github.luoyemyy.aclin.api

import com.github.luoyemyy.aclin.profile.getApiUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * demo code
 *
 * fun getUserApi(): UserApi = ApiManager.getInstance().getApi()
 *
 */
open class ApiManager {

    init {
        initApi()
    }

    companion object {
        private val api by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ApiManager()
        }

        fun getInstance() = api
    }

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

    open fun baseUrl(): String = getApiUrl()

    open fun client(): OkHttpClient.Builder {
        return OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }

    open fun converter(): Converter.Factory? = GsonConverterFactory.create()

    open fun adapter(): CallAdapter.Factory? = LiveDataCallAdapterFactory()

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
