package com.github.luoyemyy.aclin.app

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.app.databinding.ActivityMvpBinding
import com.github.luoyemyy.aclin.app.mvp.BaseAdapter
import com.github.luoyemyy.aclin.app.mvp.MvpActivity
import com.github.luoyemyy.aclin.app.mvp.TextItem
import com.github.luoyemyy.aclin.mvp.*

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMvpBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mvp)
        mPresenter = getPresenter()
        mAdapter = Adapter()
        mBinding.apply {
            swipeRefreshLayout.isEnabled = false
            recyclerView.setupLinear(mAdapter)
        }
        mPresenter.loadInit(intent.extras)
    }

    inner class Adapter : BaseAdapter(this, mPresenter) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.activity_mvp_item
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun getItemClickViews(binding: ViewDataBinding): List<View> {
            return listOf(binding.root)
        }

        override fun onItemViewClick(vh: VH<ViewDataBinding>, view: View) {
            when (vh.adapterPosition) {
                0 -> startActivity(Intent(this@MainActivity, MvpActivity::class.java))
            }
        }
    }

    class Presenter(app: Application) : AbsListPresenter(app) {
        override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return listOf(TextItem("mvp"))
        }

    }
}
