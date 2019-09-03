@file:Suppress("unused")

package com.github.luoyemyy.aclin.ext

import android.util.Log
import java.security.MessageDigest

/**
 * md5
 */
fun String?.md5(): String? {
    if (this.isNullOrEmpty()) return null
    return try {
        val messageDigest = MessageDigest.getInstance("md5")
        messageDigest.update(this.toByteArray())
        val bytes = messageDigest.digest()
        val stringBuffer = StringBuilder(2 * bytes.size)
        bytes.forEach {
            val x = it.toInt() and 0xff
            if (x <= 0xf) {
                stringBuffer.append(0)
            }
            stringBuffer.append(Integer.toHexString(x))
        }
        return stringBuffer.toString().toUpperCase()
    } catch (e: Throwable) {
        Log.e("Md5", "md5", e)
        null
    }
}