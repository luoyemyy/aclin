package com.github.luoyemyy.aclin.image.preview

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinImagePreviewBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePreviewItemBinding
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*

class PreviewFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinImagePreviewBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePreviewBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.viewPager.apply {
            adapter = Adapter()
            offscreenPageLimit = 3
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    requireActivity().title = "${position + 1}/${mPresenter.count}"
                }
            })
        }
        mPresenter.listLiveData.loadInit(arguments)
    }

    inner class Adapter : FixedAdapter<TextItem, AclinImagePreviewItemBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_image_preview_item
        }

        override fun onCurrentListChanged(previousList: MutableList<DataItem>, currentList: MutableList<DataItem>) {
            runOnMain {
                mBinding.viewPager.setCurrentItem(mPresenter.defaultPosition, false)
            }
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {

        var defaultPosition: Int = 0
        var count: Int = 0

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            defaultPosition = bundle?.getInt("current", 0) ?: 0
            return bundle?.getStringArrayList("paths")?.map { TextItem(it) }?.apply {
                count = size
            }
        }
    }
}