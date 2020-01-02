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
import com.github.luoyemyy.aclin.mvp2.DataItem
import com.github.luoyemyy.aclin.mvp2.ListLiveData
import com.github.luoyemyy.aclin.mvp2.MvpAdapter
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
        mBinding.recyclerView.adapter = Adapter()
        mPresenter.liveData.loadStart()
    }

    inner class Adapter : MvpAdapter<String, FragmentListItem2Binding>(this, mPresenter.liveData) {
        override fun bindContentViewHolder(binding: FragmentListItem2Binding, data: String?, viewType: Int, position: Int) {
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

        val liveData = object : ListLiveData<String>({ DataItem(it) }) {
            override fun getStartData(): List<String>? {
                Thread.sleep(2000)
                return (0..4).map { Random().nextInt().toString() }
            }

            override fun getMoreData(): List<String>? {
                Thread.sleep(2000)
                return (0..1).map { Random().nextInt().toString() }
            }
        }
    }

}