package com.github.luoyemyy.aclin.app.paging

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItem2Binding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.MvpAdapter
import com.github.luoyemyy.aclin.mvp.core.DataItem
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.LoadParams
import com.github.luoyemyy.aclin.mvp.core.TextData
import java.util.*

class PagingFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.mvp, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mPresenter.liveData.loadStart()
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = ViewModelProvider(this)[Presenter::class.java]
        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mPresenter.liveData.loadStart()
            mBinding.swipeRefreshLayout.isRefreshing = false
        }
        mBinding.recyclerView.setHasFixedSize(true)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mBinding.recyclerView.adapter = Adapter().apply {
            setup(this@PagingFragment, mPresenter.liveData)
        }
        mPresenter.liveData.loadStart()
    }

    inner class Adapter : MvpAdapter<TextData, FragmentListItem2Binding>() {
        override fun bindContentViewHolder(binding: FragmentListItem2Binding, data: TextData?, viewType: Int, position: Int) {
            binding.apply {
                entity = getItem(position)
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentListItem2Binding {
            return FragmentListItem2Binding.inflate(layoutInflater, parent, false)
        }
    }

    class Presenter(mApp: Application) : AndroidViewModel(mApp) {

        val liveData = object : ListLiveData<TextData>({ DataItem(it) }) {

            override fun getData(loadParams: LoadParams): List<TextData>? {
                Thread.sleep(500)
                return (0..4).map { TextData(Random().nextInt().toString()) }
            }
        }
    }

}