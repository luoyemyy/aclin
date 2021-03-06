package com.github.luoyemyy.aclin.ext

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * 点击editText之外的区域自动关闭键盘，并取消焦点
 */
fun Activity.autoCloseKeyboardAndClearFocus(ev: MotionEvent?) {
    val x = ev?.rawX?.toInt() ?: return
    val y = ev.rawY.toInt()
    val viewGroup = window.peekDecorView() as? ViewGroup ?: return
    if (x >= 0 && y >= 0 && !viewGroup.pointInEditText(x, y)) {
        hideKeyboard()
        (currentFocus as? EditText)?.clearFocus()
    }
}

/**
 * 关闭键盘
 */
fun Activity.hideKeyboard() {
    val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val focusView = currentFocus
    if (manager.isActive && focusView != null && focusView.windowToken != null) {
        manager.hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

/**
 * 判断坐标是否在 View 上
 */
fun View.pointInView(x: Int, y: Int): Boolean {
    val location = intArrayOf(0, 0)
    getLocationOnScreen(location)
    val rect = Rect(location[0], location[1], location[0] + width, location[1] + height)
    return rect.contains(x, y)
}

/**
 * 判断坐标是否在 ViewGroup 中的 EditText 上
 */
fun ViewGroup.pointInEditText(x: Int, y: Int): Boolean {
    (0 until childCount).forEach {
        val view = getChildAt(it)
        if (view.pointInView(x, y)) {
            return when (view) {
                is EditText -> true
                is ViewGroup -> view.pointInEditText(x, y)
                else -> false
            }
        }
    }
    return false
}