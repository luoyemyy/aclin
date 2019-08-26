package com.github.luoyemyy.aclin.image.picker.gallery

import android.Manifest
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryImageBinding
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.aclin.permission.PermissionManager
import com.github.luoyemyy.aclin.permission.requestPermission
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GalleryFragment : Fragment() {

    private lateinit var mBinding: AclinImagePickerGalleryBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mBottomBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.aclin_image_picker_gallery, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.sure)?.apply {
            title = mPresenter.bucketLiveData.submitImageText()
            isEnabled = mPresenter.bucketLiveData.countSelectImage() > 0
        }
    }

    //    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //        return super.onOptionsItemSelected(item)
    //    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePickerGalleryBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBottomBehavior = BottomSheetBehavior.from(mBinding.layoutBucket)
        bottomSheetState(false)
        mPresenter = getPresenter()
        mPresenter.bucketLiveData.selectBucketLiveData.observe(this, Observer {
            mBinding.entity = it
        })
        mPresenter.bucketLiveData.menuLiveData.observe(this, Observer {
            requireActivity().invalidateOptionsMenu()
        })

        mBinding.apply {
            recyclerView.setupGrid(ImageAdapter(), mPresenter.getImageSpan())
            recyclerView.setHasFixedSize(true)
            recyclerViewBucket.setupLinear(BucketAdapter())
            recyclerViewBucket.setHasFixedSize(true)

            txtSelectBucket.setOnClickListener {
                bottomSheetToggle()
            }
        }
        requestPermission(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_request)).granted {
            mPresenter.bucketLiveData.loadInit(arguments)
        }.denied {
            PermissionManager.toSetting(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_failure))
        }.buildAndRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun bottomSheetToggle() {
        when (mBottomBehavior.state) {
            BottomSheetBehavior.STATE_EXPANDED -> bottomSheetState(false)
            BottomSheetBehavior.STATE_HIDDEN -> bottomSheetState(true)
        }
    }

    private fun bottomSheetState(show: Boolean) {
        mBottomBehavior.state = if (show) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_HIDDEN
    }


    inner class ImageAdapter : GalleryAdapter(this, mPresenter.bucketLiveData.imageLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_image_picker_gallery_image
        }

        override fun bindItemEvents(vh: VH<ViewDataBinding>) {
            (vh.binding as? AclinImagePickerGalleryImageBinding)?.apply {
                checkbox.setOnCheckedChangeListener { compoundButton, b ->
                    mPresenter.bucketLiveData.changeImage(vh.adapterPosition, b).also {
                        if (it != b) {
                            compoundButton.isChecked = it
                        }
                    }
                }
            }
        }

        override fun createContentBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
            return super.createContentBinding(inflater, parent, viewType)?.apply {
                root.layoutParams.width = mPresenter.getImageSize()
                root.layoutParams.height = mPresenter.getImageSize()
            }
        }
    }

    inner class BucketAdapter : GalleryAdapter(this, mPresenter.bucketLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_image_picker_gallery_bucket
        }

        override fun onItemViewClick(vh: VH<ViewDataBinding>, view: View) {
            mPresenter.bucketLiveData.changeBucket(vh.adapterPosition)
            bottomSheetState(false)
        }
    }

    class Presenter(private var mApp: Application) : AbsPresenter(mApp) {
        val bucketLiveData = BucketLiveData(mApp)

        private var mImageInfo: Pair<Int, Int>? = null

        fun getImageSpan(): Int {
            return getImageInfo().first
        }

        fun getImageSize(): Int {
            return getImageInfo().second
        }

        private fun getImageInfo() = mImageInfo ?: calculateImageItemSize()

        private fun calculateImageItemSize(): Pair<Int, Int> {
            val suggestSize = mApp.resources.displayMetrics.density * 80
            val screenWidth = mApp.resources.displayMetrics.widthPixels

            val span = (screenWidth / suggestSize).toInt()
            val size = screenWidth / span
            return Pair(span, size).apply {
                mImageInfo = this
            }
        }
    }
}