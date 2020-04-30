package com.github.luoyemyy.aclin.app.main

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.common.util.BusEvent
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItemBinding
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.*
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupLinear
import com.github.luoyemyy.aclin.profile.Profile
import com.github.luoyemyy.aclin.scan.QrCodeBuilder

class MainFragment : Fragment(), BusResult {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter().apply {
                setup(this@MainFragment, mPresenter.listLiveData)
            })
            swipeRefreshLayout.setOnRefreshListener {
                mPresenter.listLiveData.loadRefresh(mPresenter.getData())
            }
        }
        setBus(this, BusEvent.PROFILE_CHANGE)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        when (msg.event) {
            BusEvent.PROFILE_CHANGE -> mPresenter.updateProfile()
        }
    }

    inner class Adapter : FixedAdapter<TextData, FragmentListItemBinding>() {

        override fun notifyAfter(type: Int) {
            if (LoadParams.isRefresh(type)) {
                mBinding.swipeRefreshLayout.isRefreshing = false
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentListItemBinding {
            return FragmentListItemBinding.inflate(layoutInflater, parent, false)
        }

        override fun bindContentViewHolder(binding: FragmentListItemBinding, data: TextData?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun onItemViewClick(binding: FragmentListItemBinding, vh: VH<*>, view: View) {
            val text = getItem(vh.adapterPosition)?.text ?: return
            when (text.split(":")[0]) {
                "itemList" -> findNavController().navigate(R.id.action_mainFragment_to_mvpFragment)
                "itemList-reversed" -> findNavController().navigate(R.id.action_mainFragment_to_reversedFragment)
                "profile" -> findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
                "permission" -> findNavController().navigate(R.id.action_mainFragment_to_permissionFragment)
                "image" -> findNavController().navigate(R.id.action_mainFragment_to_pickerFragment)
                "logger" -> findNavController().navigate(R.id.action_mainFragment_to_aclin_logger)
                "paging" -> findNavController().navigate(R.id.action_mainFragment_to_pagingFragment)
                "files" -> findNavController().navigate(R.id.action_mainFragment_to_filesListFragment)
                "qrcode" -> QrCodeBuilder(this@MainFragment)
                        .action(R.id.action_mainFragment_to_qrCodeFragment)
                        .buildAndScan { requireContext().toast(it) }
            }
        }
    }

    class Presenter(app: Application) : MvpPresenter(app) {

        val listLiveData = ListLiveData<TextData>()

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart(getData())
        }

        fun getData(): List<TextData> {
            return listOf(
                "itemList",
                "itemList-reversed",
                "profile:${Profile.active().desc}",
                "permission",
                "image",
                "logger",
                "paging",
                "qrcode",
                "files").map { TextData(it) }
        }

        fun updateProfile() {
            listLiveData.itemChange { items, _ ->
                items?.forEach {
                    if (it.data?.text?.startsWith("profile") == true) {
                        it.data?.text = "profile:${Profile.active().desc}"
                        it.hasPayload()
                    }
                }
                true
            }
        }
    }
}