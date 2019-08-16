package com.github.luoyemyy.aclin.api

import com.github.luoyemyy.aclin.logger.Logger
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.net.URLDecoder

/**
 * 日志拦截器
 */
class LogInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return afterLog(chain.proceed(preLog(chain.request())))
    }

    private fun afterLog(response: Response): Response {
        val mediaType = response.body?.contentType()
        val content = response.body?.string() ?: ""
        Logger.i("LogInterceptor", "<<<<<<:$content")
        return response.newBuilder().body(content.toResponseBody(mediaType)).build()
    }

    private fun preLog(request: Request): Request {
        val method = request.method.toUpperCase()
        val url = URLDecoder.decode(request.url.toString(), "utf-8")
        val logBuilder = StringBuilder().append(">>>>>>:$method,$url")
        if (method.equals("POST", true)) {
            URLDecoder.decode(postBodyParam(request.body), "utf-8").apply {
                if (isNotEmpty()) {
                    logBuilder.append(",$this")
                }
            }
        }
        Logger.i("LogInterceptor", "$logBuilder")
        return request
    }


    private fun postBodyParam(body: RequestBody?): String {
        return when (body) {
            null -> ""
            is FormBody -> logFormBody(body)
            is MultipartBody -> logMultipartBody(body)
            else -> logOtherBody(body)
        }
    }

    private fun logOtherBody(body: RequestBody) = Buffer().run { body.writeTo(this);readUtf8() }

    private fun logFormBody(body: FormBody) =
        logOtherBody(body)//(0 until body.size()).joinToString("&") { "${body.name(it)}=${body.value(it)}" }

    private fun logMultipartBody(body: MultipartBody): String {
        return body.parts.joinToString("&") {
            val disposition = it.headers?.get("Content-Disposition")
            var name: String? = null
            var value: String? = null
            if (disposition != null) {
                //Content-Disposition: form-data;name="pic"; filename="photo.jpg"
                val arrays = disposition.split('"')
                if (arrays.size >= 2) {
                    name = arrays[1]
                    value = if (arrays.size >= 4 && disposition.contains("filename")) {
                        arrays[3]
                    } else {
                        logOtherBody(it.body)
                    }
                }
            }
            if (name != null && value != null) {
                "$name=$value"
            } else {
                ""
            }
        }
    }
}