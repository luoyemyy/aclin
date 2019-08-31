package com.github.luoyemyy.aclin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

open class OverrideMenuFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}