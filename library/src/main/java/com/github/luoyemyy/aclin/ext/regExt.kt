@file:Suppress("unused")

package com.github.luoyemyy.aclin.ext

import java.util.regex.Pattern

/**
 * reg
 */
fun String?.isPhone(reg: String = RegExt.REG_PHONE): Boolean = RegExt.match(this, reg)

fun String?.isNumber(reg: String = RegExt.REG_NUMBER): Boolean = RegExt.match(this, reg)

fun String?.isDate(reg: String = RegExt.REG_DATE): Boolean = RegExt.match(this, reg)

fun String?.isDateTime(reg: String = RegExt.REG_DATE_TIME): Boolean = RegExt.match(this, reg)

fun String?.isReg(reg: String): Boolean = RegExt.match(this, reg)

object RegExt {

    const val REG_PHONE = "^1[3578]\\d{9}$"
    const val REG_NUMBER = "^\\d+$"
    const val REG_DATE = "^\\d{4}-\\d{1,2}-\\d{1,2}"
    const val REG_DATE_TIME = "^\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}"

    internal fun match(string: String?, reg: String): Boolean = if (string.isNullOrEmpty()) false else Pattern.compile(reg).matcher(string).matches()
}