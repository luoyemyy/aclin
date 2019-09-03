package com.github.luoyemyy.aclin.ext

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.PopupMenu
import java.lang.reflect.Method

object TouchInfo {
    private var touchX: Int = 0
    private var touchY: Int = 0

    fun touch(event: MotionEvent?) {
        touchX = event?.rawX?.toInt() ?: 0
        touchY = event?.rawY?.toInt() ?: 0
    }

    private fun getField(obj: Any): Any? {
        return obj::class.java.getDeclaredField("mPopup").apply { isAccessible = true }.get(obj)
    }

    private fun getMethod(obj: Any, vararg args: Class<*>): Method {
        return obj::class.java.getMethod("show", *args).apply { isAccessible = true }
    }

    fun showAnchor(popupMenu: PopupMenu, anchor: View) {
        val location = intArrayOf(0, 0)
        anchor.getLocationInWindow(location)
        val x = touchX - location[0]
        val y = touchY - location[1] - anchor.height

        val popup = getField(popupMenu) ?: return
        getMethod(popup, Int::class.java, Int::class.java).invoke(popup, x, y)
    }
}

fun popupMenu(context: Context, anchor: View, menuId: Int, listener: (Int) -> Unit): Boolean {
    PopupMenu(context, anchor).apply {
        inflate(menuId)
        setOnMenuItemClickListener { menuItem ->
            listener(menuItem.itemId)
            true
        }
        TouchInfo.showAnchor(this, anchor)
    }
    return true
}