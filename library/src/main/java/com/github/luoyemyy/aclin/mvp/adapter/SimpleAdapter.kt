package com.github.luoyemyy.aclin.mvp.adapter

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.MvpData

abstract class SimpleAdapter<T : MvpData> : MvpAdapter<T, ViewDataBinding>()