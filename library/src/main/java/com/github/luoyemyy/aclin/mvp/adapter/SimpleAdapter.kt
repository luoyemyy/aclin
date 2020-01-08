package com.github.luoyemyy.aclin.mvp.adapter

import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.mvp.core.MvpData

abstract class SimpleAdapter<T : MvpData> : MvpAdapter<T, ViewDataBinding>()