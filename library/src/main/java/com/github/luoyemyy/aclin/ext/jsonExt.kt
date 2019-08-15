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

fun String?.stringToJsonObject(): JsonObject? =
        if (this == null) null else JsonExt.jsonParser.parse(this).asJsonObject

fun String?.toJsonArray(): JsonArray? =
        if (this == null) null else JsonExt.jsonParser.parse(this).asJsonArray

inline fun <reified T> String?.toList(): List<T>? =
        if (this == null) null else JsonExt.json.fromJson<List<T>>(JsonExt.jsonParser.parse(this), JsonExt.ArrayListType(T::class.java))

inline fun <reified T> String?.toLinkedList(): List<T>? =
        if (this == null) null else JsonExt.json.fromJson<List<T>>(JsonExt.jsonParser.parse(this), JsonExt.LinkedListType(T::class.java))

inline fun <reified T> String?.toObject(): T? =
        if (this == null) null else JsonExt.json.fromJson(this, T::class.java)

fun <T> String?.toList(clazz: Class<T>): List<T>? =
        if (this == null) null else JsonExt.json.fromJson<List<T>>(JsonExt.jsonParser.parse(this), JsonExt.ArrayListType(clazz))

fun <T> String?.toLinkedList(clazz: Class<T>): List<T>? =
        if (this == null) null else JsonExt.json.fromJson<List<T>>(JsonExt.jsonParser.parse(this), JsonExt.LinkedListType(clazz))

fun <T> String?.toObject(clazz: Class<T>): T? =
        if (this == null) null else JsonExt.json.fromJson(this, clazz)

//*************************************************************************************/
//**************************************** T ******************************************/
//*************************************************************************************/

fun <T> T?.toJsonObject(): JsonObject? =
        if (this == null) null else JsonExt.json.toJsonTree(this).asJsonObject

fun <T> T?.toJsonString(): String? = if (this == null) null else JsonExt.json.toJson(this)


//*************************************************************************************/
//*************************************** List ****************************************/
//*************************************************************************************/
fun List<*>?.toJsonArray(): JsonArray? =
        if (this == null) null else JsonExt.json.toJsonTree(this).asJsonArray

//*************************************************************************************/
//********************************** JsonArray ****************************************/
//*************************************************************************************/

inline fun <reified T> JsonArray?.toList(): List<T>? =
        if (this == null) null else JsonExt.json.fromJson(this, JsonExt.ArrayListType(T::class.java))

inline fun <reified T> JsonArray?.toLinkedList(): List<T>? =
        if (this == null) null else JsonExt.json.fromJson(this, JsonExt.LinkedListType(T::class.java))

fun <T> JsonArray?.toList(clazz: Class<T>): List<T>? =
        if (this == null) null else JsonExt.json.fromJson(this, JsonExt.ArrayListType(clazz))

fun <T> JsonArray?.toLinkedList(clazz: Class<T>): List<T>? =
        if (this == null) null else JsonExt.json.fromJson(this, JsonExt.LinkedListType(clazz))

//*************************************************************************************/
//********************************** JsonObject ***************************************/
//*************************************************************************************/

inline fun <reified T> JsonObject?.toObject(): T? =
        if (this == null) null else JsonExt.json.fromJson(this, T::class.java)

fun <T> JsonObject?.toObject(clazz: Class<T>): T? =
        if (this == null) null else JsonExt.json.fromJson(this, clazz)

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
    val jsonParser = JsonParser()

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