package com.github.luoyemyy.aclin.app.files

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.common.api.getUserApi
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItemBinding
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.*
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupLinear


class FilesListFragment : Fragment() {

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

    inner class Adapter : FixedAdapter<TextData, FragmentListItemBinding>() {

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
            mPresenter.sub(vh.adapterPosition)?.let {
                findNavController().navigate(R.id.action_filesListFragment_to_filesSubListFragment, bundleOf("path" to it))
            }
        }
    }

    class Presenter(app: Application) : MvpPresenter(app) {

        val userApi = getUserApi()

        val listLiveData = object : ListLiveData<TextData>() {
            override fun getData(loadParams: LoadParams): List<TextData>? {
                return userApi.list().execute().body()?.map { TextData(it) }
            }
        }

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart()
        }

        fun sub(position: Int): String? {
            return listLiveData.itemList()?.get(position)?.data?.text
        }
    }
}