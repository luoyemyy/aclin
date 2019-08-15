@file:Suppress("unused")

package com.github.luoyemyy.aclin.ext

import java.text.SimpleDateFormat
import java.util.*

/**
 * date
 */

fun Date?.formatDate(sdf: SimpleDateFormat = DateExt.sdfYMD()): String? = if (this == null) null else sdf.format(this)

fun Date?.formatDateTime(): String? = this.formatDate(DateExt.sdfYMDHMS())

fun String?.parseDate(sdf: SimpleDateFormat = DateExt.sdfYMD()): Date? = if (this == null || this.isEmpty()) null else sdf.parse(this)

fun String?.parseDateTime(): Date? = this.parseDate(DateExt.sdfYMDHMS())

object DateExt {

    const val YMDHMS = "yyyy-MM-dd HH:mm:ss"
    const val YMD = "yyyy-MM-dd"

    fun sdfYMD() = SimpleDateFormat(YMD, Locale.getDefault())
    fun sdfYMDHMS() = SimpleDateFormat(YMDHMS, Locale.getDefault())

}