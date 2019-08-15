package com.github.luoyemyy.aclin.app.mvp

import android.app.Application
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.databinding.ActivityMvpBinding
import com.github.luoyemyy.aclin.mvp.*
import kotlin.random.Random

class MvpActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMvpBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mvp)
        mPresenter = getPresenter()
        mAdapter = Adapter()
        mBinding.apply {
            recyclerView.setupLinear(mAdapter)
            //            mAdapter.enableSort(recyclerView)
            swipeRefreshLayout.setup(mPresenter)
        }
        mPresenter.loadInit(intent.extras)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mvp, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mPresenter.loadSearch(null)
        return super.onOptionsItemSelected(item)
    }

    inner class Adapter : BaseAdapter(this, mPresenter) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.activity_mvp_item
        }

        override fun getItemSortView(binding: ViewDataBinding): View? {
            return binding.root
        }

        override fun enableLoadMore(): Boolean {
            return true
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }
    }

    class Presenter(app: Application) : AbsListPresenter(app) {
        override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
            //            return (0..Random.nextInt(9)).map { TextItem(Random.nextInt(9).toString()) }
            return (0..9).map { TextItem(Random.nextInt(9).toString()) }
        }

    }
}