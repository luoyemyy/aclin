@file:Suppress("unused")

package com.github.luoyemyy.aclin.ext

import java.text.SimpleDateFormat
import java.util.*

/**
 * date
 */

fun Date?.formatDate(sdf: SimpleDateFormat = DateExt.sdfymd()): String? = if (this == null) null else sdf.format(this)

fun Date?.formatDateTime(): String? = this.formatDate(DateExt.sdfymdhms())

fun String?.parseDate(sdf: SimpleDateFormat = DateExt.sdfymd()): Date? = if (this.isNullOrEmpty()) null else sdf.parse(this)

fun String?.parseDateTime(): Date? = this.parseDate(DateExt.sdfymdhms())

object DateExt {

    const val YMDHMS = "yyyy-MM-dd HH:mm:ss"
    const val YMD = "yyyy-MM-dd"

    fun sdfymd() = SimpleDateFormat(YMD, Locale.getDefault())
    fun sdfymdhms() = SimpleDateFormat(YMDHMS, Locale.getDefault())

}