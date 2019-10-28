@file:Suppress("unused")

package com.github.luoyemyy.aclin.ext

import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * json
 */
//*************************************************************************************/
//********************************** String *******************************************/
//*************************************************************************************/

fun String?.stringToJsonObject(): JsonObject? {
    return if (this.isNullOrEmpty()) null else JsonParser.parseString(this).asJsonObject
}

fun String?.toJsonArray(): JsonArray? {
    return if (this.isNullOrEmpty()) null else JsonParser.parseString(this).asJsonArray
}

inline fun <reified T> String?.toObject(): T? {
    return if (this.isNullOrEmpty()) null else JsonExt.json.fromJson(this, T::class.java)
}

inline fun <reified T> String?.toList(): List<T>? {
    return if (this.isNullOrEmpty()) null
    else JsonExt.json.fromJson<List<T>>(JsonParser.parseString(this), JsonExt.ArrayListType(T::class.java))
}

inline fun <reified T> String?.toLinkedList(): List<T>? {
    return if (this.isNullOrEmpty()) null
    else JsonExt.json.fromJson<List<T>>(JsonParser.parseString(this), JsonExt.LinkedListType(T::class.java))
}

//*************************************************************************************/
//**************************************** Object *************************************/
//*************************************************************************************/

fun <T> T?.toJsonString(): String? = if (this == null) null else JsonExt.json.toJson(this)

fun <T> T?.toJsonObject(): JsonObject? = if (this == null) null else JsonExt.json.toJsonTree(this).asJsonObject

fun List<*>?.toJsonArray(): JsonArray? = if (this == null) null else JsonExt.json.toJsonTree(this).asJsonArray

//*************************************************************************************/
//********************************** JsonArray ****************************************/
//*************************************************************************************/

inline fun <reified T> JsonArray?.toList(): List<T>? =
    if (this == null) null else JsonExt.json.fromJson(this, JsonExt.ArrayListType(T::class.java))

inline fun <reified T> JsonArray?.toLinkedList(): List<T>? =
    if (this == null) null else JsonExt.json.fromJson(this, JsonExt.LinkedListType(T::class.java))

//*************************************************************************************/
//********************************** JsonObject ***************************************/
//*************************************************************************************/

inline fun <reified T> JsonObject?.toObject(): T? = if (this == null) null else JsonExt.json.fromJson(this, T::class.java)

fun <T> JsonObject.addObject(key: String, obj: T): JsonObject {
    this.add(key, obj.toJsonObject())
    return this
}

fun JsonObject.addArray(key: String, list: List<*>): JsonObject {
    this.add(key, list.toJsonArray())
    return this
}

object JsonExt {

    val json: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()

    class ArrayListType constructor(private val clazz: Class<*>) : ParameterizedType {

        override fun getActualTypeArguments(): Array<Type> = arrayOf(clazz)

        override fun getRawType(): Type = ArrayList::class.java

        override fun getOwnerType(): Type? = null
    }

    class LinkedListType constructor(private val clazz: Class<*>) : ParameterizedType {

        override fun getActualTypeArguments(): Array<Type> = arrayOf(clazz)

        override fun getRawType(): Type = LinkedList::class.java

        override fun getOwnerType(): Type? = null
    }
}