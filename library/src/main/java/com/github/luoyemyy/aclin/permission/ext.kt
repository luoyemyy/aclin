package com.github.luoyemyy.aclin.permission

import androidx.fragment.app.Fragment

typealias PermissionCallback = (Array<String>) -> Unit

fun Fragment.requestPermission(rationale: String? = null, vararg perms: String) = PermissionManager.Builder(this, rationale, *perms)