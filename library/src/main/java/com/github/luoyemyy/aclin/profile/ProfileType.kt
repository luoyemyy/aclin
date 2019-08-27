package com.github.luoyemyy.aclin.profile

enum class ProfileType(val type: Int, val desc: String, private var active: Boolean = false) {

    DEV(1, "开发(dev)", true),
    TEST(2, "测试(test)"),
    DEMO(3, "演示(demo)"),
    PRO(4, "正式(prod)");

    fun isActive() = active

    internal fun active() {
        DEV.active = false
        TEST.active = false
        DEMO.active = false
        PRO.active = false
        active = true
    }

}