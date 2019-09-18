package com.github.luoyemyy.aclin.app.mvp

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.luoyemyy.aclin.app.R
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
        mPresenter.listLiveData.loadInit(arguments)
    }

    inner class Adapter(private var context: Context) : ReversedAdapter<TextItem, FragmentListItemBinding>(this, mPresenter.listLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_list_item
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }
    }

    class Presenter(private var mApp: Application) : AbsListPresenter(mApp) {

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            val length = if (paging.current() < 3L) 10 else 4
            return (0 until length).map { TextItem(Random.nextDouble().toString()) }
        }
    }
}