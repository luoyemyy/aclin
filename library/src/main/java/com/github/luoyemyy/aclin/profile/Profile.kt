@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.github.luoyemyy.aclin.profile


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
            return when (currentType()) {
                ProfileType.DEV -> dev
                ProfileType.TEST -> test
                ProfileType.DEMO -> demo
                ProfileType.PRO -> pro
            }
        }
    }

    private val mItemMap = mutableMapOf<String, Item<Any>>()

    fun initType(buildType: String) {
        when (buildType) {
            "debug" -> ProfileType.DEV.active()
            "test" -> ProfileType.TEST.active()
            "demo" -> ProfileType.DEMO.active()
            "release" -> ProfileType.PRO.active()
            else -> ProfileType.DEV.active()
        }
    }

    fun changeType(type: ProfileType, clear: () -> Unit) {
        type.active()
        clear()
    }

    fun allTypes() = arrayOf(ProfileType.DEV, ProfileType.TEST, ProfileType.DEMO, ProfileType.PRO)

    fun currentType(): ProfileType {
        return when {
            ProfileType.TEST.isActive() -> ProfileType.TEST
            ProfileType.DEMO.isActive() -> ProfileType.DEMO
            ProfileType.PRO.isActive() -> ProfileType.PRO
            else -> ProfileType.DEV
        }
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