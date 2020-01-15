package com.github.luoyemyy.aclin.image.picker.gallery

import android.Manifest
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.databinding.AclinImagePickerImageBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerImageRecyclerBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupGrid
import com.github.luoyemyy.aclin.permission.PermissionManager
import com.github.luoyemyy.aclin.permission.requestPermission

class ImageFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinImagePickerImageBinding
    private lateinit var mGalleryPresenter: GalleryPresenter
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePickerImageBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.aclin_image_picker_gallery, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.sure)?.apply {
            val (enable, title) = mGalleryPresenter.getMenu()
            this.title = title
            this.isEnabled = enable
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sure -> {
                mGalleryPresenter.getSelectImages().apply {
                    postBus(GalleryBuilder.PICKER_RESULT, extra = bundleOf(GalleryBuilder.PICKER_RESULT to this))
                    findNavController().navigateUp()
                }
            }
            R.id.album -> {
                findNavController().navigate(R.id.action_imageFragment_to_bucketFragment)
            }
            R.id.preview -> {
                mGalleryPresenter.previewImages(0, true) { position, images ->
                    toPreview(images, position)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mGalleryPresenter = requireActivity().getPresenter()
        mGalleryPresenter.menuLiveData().observe(this, Observer {
            requireActivity().invalidateOptionsMenu()
        })
        mGalleryPresenter.titleLiveData().observe(this, Observer {
            setTitle()
        })
        setTitle()
        Adapter().also { adapter ->
            adapter.setup(this, mGalleryPresenter.imageLiveData())
            mBinding.apply {
                recyclerView.setupGrid(adapter, mPresenter.getImageSpan())
                recyclerView.setHasFixedSize(true)
            }
        }
        requestPermission(requireContext().getString(R.string.aclin_image_picker_gallery_permission_request), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .granted {
                    mGalleryPresenter.loadInit(arguments)
                }.denied {
                    PermissionManager.toSetting(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_failure))
                }.request()
    }

    override fun onDestroy() {
        super.onDestroy()
        mGalleryPresenter.clear()
    }

    private fun setTitle() {
        requireActivity().title = mGalleryPresenter.getTitle()
    }

    private fun toPreview(paths: ArrayList<String>, current: Int = 0) {
        if (paths.isNotEmpty()) {
            findNavController().navigate(R.id.action_imageFragment_to_previewFragment, bundleOf("paths" to paths, "current" to current))
        }
    }

    inner class Adapter : FixedAdapter<Image, AclinImagePickerImageRecyclerBinding>() {

        override fun getContentBinding(viewType: Int, parent: ViewGroup): AclinImagePickerImageRecyclerBinding {
            return AclinImagePickerImageRecyclerBinding.inflate(layoutInflater, parent, false).apply {
                root.layoutParams.width = mPresenter.getImageSize()
                root.layoutParams.height = mPresenter.getImageSize()
            }
        }

        override fun bindContentViewHolder(binding: AclinImagePickerImageRecyclerBinding, data: Image?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun bindItemEvents(binding: AclinImagePickerImageRecyclerBinding, vh: VH<*>) {
            binding.apply {
                checkbox.setOnCheckedChangeListener { compoundButton, b ->
                    getItem(vh.adapterPosition)?.also { image ->
                        mGalleryPresenter.selectImage(image, b).also {
                            if (it != b) {
                                compoundButton.isChecked = it
                            }
                        }
                    }
                }
            }
        }

        override fun onItemViewClick(binding: AclinImagePickerImageRecyclerBinding, vh: VH<*>, view: View) {
            mGalleryPresenter.previewImages(vh.adapterPosition, false) { position, images ->
                toPreview(images, position)
            }
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        private var mImageInfo: Pair<Int, Int>? = null

        fun getImageSpan(): Int = getImageInfo().first

        fun getImageSize(): Int = getImageInfo().second

        private fun getImageInfo() = mImageInfo ?: calculateImageItemSize(mApp).apply { mImageInfo = this }

        override fun loadData(bundle: Bundle?) {

        }
    }
}