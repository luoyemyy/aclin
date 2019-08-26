package com.github.luoyemyy.aclin.permission

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

typealias PermissionCallback = (Array<String>) -> Unit

fun requestPermission(fragment: Fragment, rationale: String? = null) = PermissionManager.Builder(fragment, rationale)
fun requestPermission(activity: FragmentActivity, rationale: String? = null) = PermissionManager.Builder(activity, rationale)