package com.github.luoyemyy.aclin.image.picker.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.databinding.AclinImagePickerBucketBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerBucketRecyclerBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupLinear

class BucketFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinImagePickerBucketBinding
    private lateinit var mGalleryPresenter: GalleryPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePickerBucketBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mGalleryPresenter = requireActivity().getPresenter()
        Adapter().also { adapter ->
            adapter.setup(this, mGalleryPresenter.bucketLiveData())
            mBinding.recyclerView.setupLinear(adapter)
        }
        mGalleryPresenter.loadBuckets()
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
                mGalleryPresenter.selectBucket(id)
                findNavController().navigateUp()
            }
        }
    }
}