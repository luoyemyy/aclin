package com.github.luoyemyy.aclin.image.picker.gallery

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryBucketBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerGalleryImageBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupGrid
import com.github.luoyemyy.aclin.mvp.ext.setupLinear
import com.github.luoyemyy.aclin.permission.PermissionManager
import com.github.luoyemyy.aclin.permission.requestPermission
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GalleryFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinImagePickerGalleryBinding
    private lateinit var mPresenter: GalleryPresenter
    private lateinit var mBottomBehavior: BottomSheetBehavior<View>

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
                    postBus(GalleryBuilder.PICKER_RESULT, extra = bundleOf(GalleryBuilder.PICKER_RESULT to this))
                    findNavController().navigateUp()
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
            recyclerView.setupGrid(ImageAdapter().apply {
                setup(this@GalleryFragment, mPresenter.imagesLiveData)
            }, mPresenter.getImageSpan())
            recyclerView.setHasFixedSize(true)
            recyclerViewBucket.setupLinear(BucketAdapter().apply {
                setup(this@GalleryFragment, mPresenter.bucketsLiveData)
            })
            recyclerViewBucket.setHasFixedSize(true)

            txtSelectBucket.setOnClickListener {
                bottomSheetToggle()
            }

            txtPreview.setOnClickListener {
                mPresenter.bucketsLiveData.selectedImages()?.apply {
                    toPreview(this)
                }
            }
        }
        requestPermission(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_request))
                .granted {
                    mPresenter.loadInit(arguments)
                }
                .denied {
                    PermissionManager.toSetting(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_failure))
                }
                .buildAndRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun bottomSheetToggle() {
        when (mBottomBehavior.state) {
            BottomSheetBehavior.STATE_EXPANDED -> bottomSheetState(false)
            BottomSheetBehavior.STATE_HIDDEN -> bottomSheetState(true)
        }
    }

    private fun toPreview(paths: ArrayList<String>, current: Int = 0) {
        if (paths.isNotEmpty()) {
            findNavController().navigate(R.id.action_galleryFragment_to_previewFragment, bundleOf("paths" to paths, "current" to current))
        }
    }

    private fun bottomSheetState(show: Boolean) {
        mBottomBehavior.state = if (show) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_HIDDEN
    }


    inner class ImageAdapter : FixedAdapter<Image, AclinImagePickerGalleryImageBinding>() {

        override fun getContentBinding(viewType: Int, parent: ViewGroup): AclinImagePickerGalleryImageBinding {
            return AclinImagePickerGalleryImageBinding.inflate(layoutInflater, parent, false).apply {
                root.layoutParams.width = mPresenter.getImageSize()
                root.layoutParams.height = mPresenter.getImageSize()
            }
        }

        override fun bindContentViewHolder(binding: AclinImagePickerGalleryImageBinding, data: Image?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
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

        override fun onItemViewClick(binding: AclinImagePickerGalleryImageBinding, vh: VH<*>, view: View) {
            mPresenter.bucketsLiveData.previewImages(vh.adapterPosition) { position, images ->
                toPreview(images, position)
            }
        }
    }

    inner class BucketAdapter : FixedAdapter<Bucket, AclinImagePickerGalleryBucketBinding>() {

        override fun getContentBinding(viewType: Int, parent: ViewGroup): AclinImagePickerGalleryBucketBinding {
            return AclinImagePickerGalleryBucketBinding.inflate(layoutInflater, parent, false)
        }

        override fun bindContentViewHolder(binding: AclinImagePickerGalleryBucketBinding, data: Bucket?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun onItemViewClick(binding: AclinImagePickerGalleryBucketBinding, vh: VH<*>, view: View) {
            mPresenter.bucketsLiveData.selectBucket(getItem(vh.adapterPosition))
            bottomSheetState(false)
        }
    }
}