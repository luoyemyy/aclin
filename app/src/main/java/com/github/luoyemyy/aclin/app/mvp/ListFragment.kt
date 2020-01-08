package com.github.luoyemyy.aclin.app.mvp

import android.app.Application
import android.os.Bundle
import android.view.*
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItemBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.MvpAdapter
import com.github.luoyemyy.aclin.mvp.core.*
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setup
import com.github.luoyemyy.aclin.mvp.ext.setupLinear
import kotlin.random.Random

class ListFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter().apply {
                setup(this@ListFragment, mPresenter.listLiveData)
            })
            swipeRefreshLayout.setup(mPresenter.listLiveData)
        }
        mPresenter.loadInit(arguments)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.mvp, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mPresenter.listLiveData.loadRefresh()
        return super.onOptionsItemSelected(item)
    }

    inner class Adapter : MvpAdapter<TextData, FragmentListItemBinding>() {

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentListItemBinding {
            return FragmentListItemBinding.inflate(layoutInflater, parent, false)
        }

        override fun bindContentViewHolder(binding: FragmentListItemBinding, data: TextData?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }
    }

    class Presenter(app: Application) : MvpPresenter(app) {

        val listLiveData = object : ListLiveData<TextData>() {

            override fun getData(loadParams: LoadParams): List<TextData>? {
                var random = Random.nextInt(9)
                if (random > 3) {
                    random = 9
                }
                Thread.sleep(1000)
                return (0..random).map { TextData(Random.nextDouble().toString()) }
            }
        }

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart()
        }
    }
}