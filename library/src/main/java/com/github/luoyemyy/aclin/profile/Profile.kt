@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.github.luoyemyy.aclin.profile

import android.content.Context
import com.github.luoyemyy.aclin.ext.spfInt


/**
 * demo code
 *
//object ProfileProperties {
//
//    val API_URL = "api.url"
//
//    fun initProperties() {
//        Profile.add(API_URL, "http://127.0.0.1:8080/")
//    }
//}
//
//fun getApiUrl() = Profile.get(ProfileProperties.API_URL)
 *
 */
object Profile {

    data class Item<T>(private val dev: T, private val test: T, private val demo: T, private val pro: T) {
        fun get(): T {
            return when (active()) {
                ProfileType.DEV -> dev
                ProfileType.TEST -> test
                ProfileType.DEMO -> demo
                ProfileType.PRO -> pro
            }
        }
    }

    private const val ACTIVE_PROFILE = "active_profile"

    private val mItemMap = mutableMapOf<String, Item<Any>>()

    fun initType(context: Context, buildType: String) {
        val activeProfile = context.spfInt(ACTIVE_PROFILE)
        if (activeProfile == 0) {
            when (buildType) {
                "debug" -> ProfileType.DEV.active()
                "test" -> ProfileType.TEST.active()
                "demo" -> ProfileType.DEMO.active()
                "release" -> ProfileType.PRO.active()
                else -> ProfileType.DEV.active()
            }
            context.spfInt(ACTIVE_PROFILE, active().type)
        } else {
            when (activeProfile) {
                ProfileType.PRO.type -> ProfileType.PRO
                ProfileType.TEST.type -> ProfileType.TEST
                ProfileType.DEMO.type -> ProfileType.DEMO
                else -> ProfileType.DEV
            }.active()
        }
    }

    fun changeType(context: Context, index: Int, clear: () -> Unit) {
        changeType(context, allTypes()[index], clear)
    }

    fun changeType(context: Context, type: ProfileType, clear: () -> Unit) {
        context.spfInt(ACTIVE_PROFILE, type.type)
        type.active()
        clear()
    }

    fun allTypes() = arrayOf(ProfileType.DEV, ProfileType.TEST, ProfileType.DEMO, ProfileType.PRO)

    fun allTypeDesc() = allTypes().map { it.desc }.toTypedArray()

    fun active(): ProfileType {
        return allTypes().first { it.isActive() }
    }

    fun activePosition(): Int {
        return allTypes().indexOf(active())
    }

    fun <T : Any> add(key: String, dev: T, pro: T = dev, test: T = dev, demo: T = dev) {
        mItemMap.put(key, Item(dev, test, demo, pro))
    }

    inline fun <reified T> get(key: String): T {
        return getValue(key).let {
            (it as? T) ?: throw ClassCastException("value:$it,${it.javaClass.name} 不能转换为 ${T::class.java.name}")
        }
    }

    fun getValue(key: String): Any {
        return mItemMap[key]?.get() ?: throw NullPointerException("没找到 $key 对应的属性")
    }
}