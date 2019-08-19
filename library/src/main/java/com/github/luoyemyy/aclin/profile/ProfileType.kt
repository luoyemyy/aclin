package com.github.luoyemyy.aclin.profile

enum class ProfileType(val type: Int, val desc: String, private var active: Boolean = false) {

    DEV(1, "开发", true), TEST(2, "测试"), DEMO(3, "演示"), PRO(4, "正式");

    fun isActive() = active

    internal fun active() {
        DEV.active = false
        TEST.active = false
        DEMO.active = false
        PRO.active = false
        active = true
    }

}