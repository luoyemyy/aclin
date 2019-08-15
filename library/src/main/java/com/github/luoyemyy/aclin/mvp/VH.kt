package com.github.luoyemyy.aclin.mvp

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class VH<BIND : ViewDataBinding>(var binding: BIND) : RecyclerView.ViewHolder(binding.root)
