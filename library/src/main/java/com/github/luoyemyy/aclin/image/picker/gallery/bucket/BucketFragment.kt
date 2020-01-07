package com.github.luoyemyy.aclin.image.picker.gallery.bucket

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.databinding.AclinImagePickerBucketBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerBucketRecyclerBinding
import com.github.luoyemyy.aclin.ext.toList
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.image.picker.gallery.BUS_EVENT_SELECT_BUCKET
import com.github.luoyemyy.aclin.image.picker.gallery.Bucket
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.DataItem
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupLinear

class BucketFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinImagePickerBucketBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePickerBucketBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter().apply {
                setup(this@BucketFragment, mPresenter.listLiveData)
            })
        }
        mPresenter.loadInit(arguments)
    }

    inner class Adapter : FixedAdapter<Bucket, AclinImagePickerBucketRecyclerBinding>() {

        override fun bindContentViewHolder(binding: AclinImagePickerBucketRecyclerBinding, data: Bucket?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): AclinImagePickerBucketRecyclerBinding {
            return AclinImagePickerBucketRecyclerBinding.inflate(layoutInflater, parent, false)
        }

        override fun onItemViewClick(binding: AclinImagePickerBucketRecyclerBinding, vh: VH<*>, view: View) {
            getItem(vh.adapterPosition)?.apply {
                postBus(BUS_EVENT_SELECT_BUCKET, stringValue = id)
            }
            findNavController().navigateUp()
        }
    }

    class Presenter(app: Application) : MvpPresenter(app) {

        val listLiveData = ListLiveData<Bucket> { DataItem(it) }

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart(bundle?.getString("buckets")?.toList())
        }
    }
}