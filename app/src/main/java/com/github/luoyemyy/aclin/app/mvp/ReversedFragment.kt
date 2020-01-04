package com.github.luoyemyy.aclin.app.mvp

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItemBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import kotlin.random.Random

class ReversedFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter(requireContext()))
            swipeRefreshLayout.isEnabled = false
        }
        mPresenter.loadInit(arguments)
    }

    inner class Adapter(private var context: Context) : ReversedAdapter<String, FragmentListItemBinding>(this, mPresenter.listLiveData) {

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentListItemBinding {
            return FragmentListItemBinding.inflate(layoutInflater, parent, false)
        }

        override fun bindContentViewHolder(binding: FragmentListItemBinding, data: String?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }
    }

    class Presenter(app: Application) : MvpPresenter(app) {

        val listLiveData = object : ListLiveData<String>({ DataItem(it) }) {

            override fun getData(loadParams: LoadParams): List<String>? {
                return (0 until 10).map { (Random.nextDouble().toString()) }
            }
        }

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart()
        }
    }
}