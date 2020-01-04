package com.github.luoyemyy.aclin.mvp

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class SimpleAdapter<T : MvpData>(owner: LifecycleOwner, liveData: ListLiveData<T>)
    : MvpAdapter<T, ViewDataBinding>(owner, liveData)