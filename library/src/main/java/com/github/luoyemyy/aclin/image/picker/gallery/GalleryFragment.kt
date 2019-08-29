package com.github.luoyemyy.aclin.image.picker.gallery

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryBucketBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryImageBinding
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.aclin.permission.PermissionManager
import com.github.luoyemyy.aclin.permission.requestPermission
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GalleryFragment : Fragment() {

    private lateinit var mBinding: AclinImagePickerGalleryBinding
    private lateinit var mPresenter: GalleryPresenter
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
            title = mPresenter.bucketsLiveData.submitMenuText()
            isEnabled = mPresenter.bucketsLiveData.enableSubmit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sure -> {
                mPresenter.bucketsLiveData.selectedImages()?.apply {
                    findNavController().navigate(R.id.action_galleryFragment_to_previewFragment, bundleOf("paths" to this))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePickerGalleryBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBottomBehavior = BottomSheetBehavior.from(mBinding.layoutBucket)
        bottomSheetState(false)
        mPresenter = getPresenter()
        mPresenter.selectBucketLiveData.observe(this, Observer {
            mBinding.entity = it
        })
        mPresenter.menuLiveData.observe(this, Observer {
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

            txtPreview.setOnClickListener {
                mPresenter.bucketsLiveData.selectedImages()?.apply {
                    findNavController().navigate(R.id.action_galleryFragment_to_previewFragment, bundleOf("paths" to this))
                }
            }
        }
        mPresenter.setupArgs(arguments)
        requestPermission(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_request)).granted {
            mPresenter.bucketsLiveData.loadInit(null)
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


    inner class ImageAdapter : FixedAdapter<Image, AclinImagePickerGalleryImageBinding>(this, mPresenter.imagesLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_image_picker_gallery_image
        }

        override fun bindItemEvents(binding: AclinImagePickerGalleryImageBinding, vh: VH<*>) {
            binding.apply {
                checkbox.setOnCheckedChangeListener { compoundButton, b ->
                    mPresenter.bucketsLiveData.selectImage(vh.adapterPosition, b).also {
                        if (it != b) {
                            compoundButton.isChecked = it
                        }
                    }
                }
            }
        }

        override fun createContentBinding(inflater: LayoutInflater, parent: ViewGroup,
            viewType: Int): AclinImagePickerGalleryImageBinding? {
            return super.createContentBinding(inflater, parent, viewType)?.apply {
                root.layoutParams.width = mPresenter.getImageSize()
                root.layoutParams.height = mPresenter.getImageSize()
            }
        }
    }

    inner class BucketAdapter : FixedAdapter<Bucket,AclinImagePickerGalleryBucketBinding>(this, mPresenter.bucketsLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_image_picker_gallery_bucket
        }

        override fun onItemViewClick(binding: AclinImagePickerGalleryBucketBinding, vh: VH<*>, view: View) {
            mPresenter.bucketsLiveData.selectBucket(getItem(vh.adapterPosition) as? Bucket)
            bottomSheetState(false)
        }
    }
}