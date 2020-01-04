package com.github.luoyemyy.aclin.image.preview

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.github.luoyemyy.aclin.databinding.AclinImagePreviewBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePreviewItemBinding
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.DataItem
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.TextData
import com.github.luoyemyy.aclin.mvp.ext.getPresenter

class PreviewFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinImagePreviewBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePreviewBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.viewPager.apply {
            adapter = Adapter().apply {
                setup(this@PreviewFragment, mPresenter.listLiveData)
            }
            offscreenPageLimit = 3
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    requireActivity().title = "${position + 1}/${mPresenter.count}"
                }
            })
        }
        mPresenter.loadInit(arguments)
    }

    inner class Adapter : FixedAdapter<TextData, AclinImagePreviewItemBinding>() {

        init {
            addUpdateListener { _, _ ->
                runOnMain {
                    mBinding.viewPager.setCurrentItem(mPresenter.defaultPosition, false)
                }
            }
        }

        override fun bindContentViewHolder(binding: AclinImagePreviewItemBinding, data: TextData?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): AclinImagePreviewItemBinding {
            return AclinImagePreviewItemBinding.inflate(layoutInflater, parent, false)
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        var count: Int = 0
        var defaultPosition: Int = 0
        val listLiveData = ListLiveData<TextData> { DataItem(it) }

        override fun loadData(bundle: Bundle?) {
            defaultPosition = bundle?.getInt("current", 0) ?: 0
            listLiveData.loadStart(bundle?.getStringArrayList("paths")?.map { TextData(it) }?.apply {
                count = size
            })
        }
    }
}