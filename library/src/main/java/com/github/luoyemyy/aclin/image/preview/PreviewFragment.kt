package com.github.luoyemyy.aclin.image.preview

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinImagePreviewBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePreviewItemBinding
import com.github.luoyemyy.aclin.mvp.*

class PreviewFragment : Fragment() {

    private lateinit var mBinding: AclinImagePreviewBinding
    private lateinit var mPresenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePreviewBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.viewPager.adapter = Adapter()
        mPresenter.setupArgs(arguments)
    }

    inner class Adapter : FixedAdapter<TextItem, AclinImagePreviewItemBinding>(this, mPresenter.images) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_image_preview_item
        }

        override fun onCurrentListChanged(previousList: MutableList<DataItem>, currentList: MutableList<DataItem>) {
            mBinding.viewPager.setCurrentItem(mPresenter.getCurrentPosition(), false)
        }
    }

    class Presenter(var mApp: Application) : AbsPresenter(mApp) {

        private var mDefaultPosition: Int = 0

        val images = object : ListLiveData() {
            override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
                mDefaultPosition = bundle?.getInt("current", 0) ?: 0
                return bundle?.getStringArrayList("paths")?.map { TextItem(it) }
            }
        }

        fun getCurrentPosition(): Int {
            return mDefaultPosition
        }
    }
}