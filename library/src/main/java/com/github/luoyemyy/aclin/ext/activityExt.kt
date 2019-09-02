@file:Suppress("unused")

package com.github.luoyemyy.aclin.ext

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.luoyemyy.aclin.app.AppInfo
import java.io.File
import kotlin.math.roundToInt

fun View.hide() {
    visibility = View.GONE
}

fun View.hideInvisible() {
    visibility = View.INVISIBLE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun Activity.alert(
        @StringRes messageId: Int = 0,
        @StringRes okText: Int = android.R.string.ok,
        ok: () -> Unit = {}) {
    AlertDialog.Builder(this).setMessage(messageId).setPositiveButton(okText) { _, _ -> ok() }.show()
}

fun Activity.alert(
        message: String = "alert message",
        @StringRes okText: Int = android.R.string.ok,
        ok: () -> Unit = {}) {
    AlertDialog.Builder(this).setMessage(message).setPositiveButton(okText) { _, _ -> ok() }.show()
}

fun Activity.confirm(
        @StringRes titleId: Int = 0,
        @StringRes messageId: Int = 0,
        okText: Int = android.R.string.ok,
        ok: () -> Unit = {},
        cancelText: Int = android.R.string.cancel,
        cancel: () -> Unit = {}) {
    AlertDialog.Builder(this)
            .setTitle(titleId).setMessage(messageId)
            .setNegativeButton(cancelText) { _, _ -> cancel() }
            .setPositiveButton(okText) { _, _ -> ok() }
            .show()
}

fun Activity.confirm(
        title: String? = null,
        message: String? = null,
        okText: Int = android.R.string.ok,
        ok: () -> Unit = {},
        cancelText: Int = android.R.string.cancel,
        cancel: () -> Unit = {}) {
    AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(cancelText) { _, _ -> cancel() }
            .setPositiveButton(okText) { _, _ -> ok() }
            .show()
}

fun Activity.items(@ArrayRes itemId: Int, ok: (Int) -> Unit) {
    AlertDialog.Builder(this).setItems(itemId) { _, i -> ok(i) }.show()
}

fun Activity.items(items: Array<String>, ok: (Int) -> Unit) {
    AlertDialog.Builder(this).setItems(items) { _, i -> ok(i) }.show()
}

/**
 * context
 */
fun Context.dp2px(dp: Int) = (resources.displayMetrics.density * dp).roundToInt()

fun Context.toast(@StringRes messageId: Int = 0) = Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()

fun Context.toast(message: String = "toast message") = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.hasPermission(vararg permissions: String): Boolean {
    return if (permissions.isEmpty()) {
        false
    } else {
        permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

fun Context.uri(path: String): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(this, AppInfo.fileProvider, File(path))
    } else {
        Uri.parse(path)
    }
}