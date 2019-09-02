package com.github.luoyemyy.aclin.ext

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.PopupMenu
import java.lang.reflect.Method

object TouchInfo {
    internal var touchX: Int = 0
    internal var touchY: Int = 0

    /**
     * 在Activity#dispatchTouchEvent方法内执行该方法，记录每次按下的位置
     */
    fun touch(event: MotionEvent?) {
        touchX = event?.rawX?.toInt() ?: 0
        touchY = event?.rawY?.toInt() ?: 0
    }
}

fun getMethod(obj: Any, name: String, vararg args: Class<*>): Method {
    return obj::class.java.getMethod(name, *args).apply { isAccessible = true }
}

fun getField(obj: Any, name: String): Any? {
    return obj::class.java.getDeclaredField(name).apply { isAccessible = true }.get(obj)
}

fun PopupMenu.showAnchor(anchor: View) {
    val location = intArrayOf(0, 0)
    anchor.getLocationInWindow(location)
    val x = TouchInfo.touchX - location[0]
    val y = TouchInfo.touchY - location[1] - anchor.height

    val popup = getField(this, "mPopup") ?: return
    getMethod(popup, "show", Int::class.java, Int::class.java).invoke(popup, x, y)
}

fun popupMenu(context: Context, anchor: View, menuId: Int, listener: (Int) -> Unit): Boolean {
    PopupMenu(context, anchor).apply {
        inflate(menuId)
        setOnMenuItemClickListener { menuItem ->
            listener(menuItem.itemId)
            true
        }
        showAnchor(anchor)
    }
    return true
}