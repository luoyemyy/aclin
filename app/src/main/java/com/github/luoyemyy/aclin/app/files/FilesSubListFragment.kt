package com.github.luoyemyy.aclin.app.files

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.common.api.getUserApi
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItem3Binding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItemBinding
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.*
import com.github.luoyemyy.aclin.mvp.decoration.LinearDecoration
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setup
import com.github.luoyemyy.aclin.mvp.ext.setupLinear


class FilesSubListFragment : Fragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter().apply {
                setup(viewLifecycleOwner, mPresenter.listLiveData)
            })
            swipeRefreshLayout.isEnabled = false
        }
        mPresenter.loadInit(arguments)
    }

    inner class Adapter : FixedAdapter<TextData, FragmentListItem3Binding>() {

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentListItem3Binding {
            return FragmentListItem3Binding.inflate(layoutInflater, parent, false)
        }

        override fun bindContentViewHolder(binding: FragmentListItem3Binding, data: TextData?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun onItemViewClick(binding: FragmentListItem3Binding, vh: VH<*>, view: View) {
            mPresenter.next(vh.adapterPosition)?.let {
                findNavController().navigate(R.id.action_filesSubListFragment_to_filesPlayerFragment, bundleOf("url" to it))
            }
        }
    }

    class Presenter(app: Application) : MvpPresenter(app) {

        val userApi = getUserApi()
        var basePath = "http://192.168.0.105/download"
        lateinit var path: String

        val listLiveData = object : ListLiveData<TextData>() {
            override fun getData(loadParams: LoadParams): List<TextData>? {
                return userApi.nestList(path).execute().body()?.map { TextData(it) }
            }
        }

        override fun loadData(bundle: Bundle?) {
            path = bundle?.getString("path") ?: ""
            listLiveData.loadStart()
        }

        fun next(position: Int): String? {
            val text = listLiveData.itemList()?.get(position)?.data?.text
            return "$basePath/$path/$text"
        }
    }
}