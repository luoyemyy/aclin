package com.github.luoyemyy.aclin.image.picker.gallery.image

import android.Manifest
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.bus.setBus
import com.github.luoyemyy.aclin.databinding.AclinImagePickerImageBinding
import com.github.luoyemyy.aclin.databinding.AclinImagePickerImageRecyclerBinding
import com.github.luoyemyy.aclin.ext.spfString
import com.github.luoyemyy.aclin.ext.toJsonString
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.image.picker.gallery.*
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.*
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupGrid
import com.github.luoyemyy.aclin.permission.PermissionManager
import com.github.luoyemyy.aclin.permission.requestPermission

class ImageFragment : OverrideMenuFragment(), BusResult {

    private lateinit var mBinding: AclinImagePickerImageBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImagePickerImageBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.aclin_image_picker_gallery, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.sure)?.apply {
            val (enable, title) = mPresenter.getMenu()
            this.title = title
            this.isEnabled = enable
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sure -> {
                mPresenter.getSelectImages().apply {
                    postBus(GalleryBuilder.PICKER_RESULT, extra = bundleOf(GalleryBuilder.PICKER_RESULT to this))
                    findNavController().navigateUp()
                }
            }
            R.id.album -> {
                findNavController().navigate(R.id.action_imageFragment_to_bucketFragment, bundleOf("buckets" to mPresenter.getBucketJson()))
            }
            R.id.preview -> {
                mPresenter.previewImages(0, true) { position, images ->
                    toPreview(images, position)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.menuLiveData.observe(this, Observer {
            requireActivity().invalidateOptionsMenu()
        })
        mPresenter.titleLiveData.observe(this, Observer {
            requireActivity().title = mPresenter.getTitle()
        })

        mBinding.apply {
            recyclerView.setupGrid(Adapter().apply {
                setup(this@ImageFragment, mPresenter.listLiveData)
            }, mPresenter.getImageSpan())
            recyclerView.setHasFixedSize(true)
        }
        setBus(this, BUS_EVENT_SELECT_BUCKET)
        requestPermission(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_request))
                .granted {
                    mPresenter.loadInit(arguments)
                }
                .denied {
                    PermissionManager.toSetting(this, requireContext().getString(R.string.aclin_image_picker_gallery_permission_failure))
                }
                .buildAndRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun busResult(msg: BusMsg) {
        if (BUS_EVENT_SELECT_BUCKET == msg.event) {
            mPresenter.selectBucket(msg.stringValue)
        }
    }

    private fun toPreview(paths: ArrayList<String>, current: Int = 0) {
        if (paths.isNotEmpty()) {
            findNavController().navigate(R.id.action_imageFragment_to_previewFragment, bundleOf("paths" to paths, "current" to current))
        }
    }

    inner class Adapter : FixedAdapter<Image, AclinImagePickerImageRecyclerBinding>() {

        init {
            enableInit = false
        }

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
                        mPresenter.selectImage(image, b).also {
                            if (it != b) {
                                compoundButton.isChecked = it
                            }
                        }
                    }
                }
            }
        }

        override fun onItemViewClick(binding: AclinImagePickerImageRecyclerBinding, vh: VH<*>, view: View) {
            mPresenter.previewImages(vh.adapterPosition, false) { position, images ->
                toPreview(images, position)
            }
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        private var mImageInfo: Pair<Int, Int>? = null
        private var mBuckets: List<Bucket>? = null
        private val mModel = ImageModel(mApp)
        private var mMinSelect: Int = 0
        private var mMaxSelect: Int = 0
        var selectBucket: Bucket? = null
        val menuLiveData = MutableLiveData<Boolean>()
        val titleLiveData = MutableLiveData<Boolean>()
        val listLiveData = object : ListLiveData<Image>({ DataItem(it) }) {
            override fun getData(loadParams: LoadParams): List<Image>? {
                if (loadParams.isStart()) {
                    mBuckets = mModel.getBuckets()
                    setDefaultBucket()
                }
                return selectBucket?.images
            }
        }

        fun getImageSpan(): Int = getImageInfo().first

        fun getImageSize(): Int = getImageInfo().second

        private fun getImageInfo() = mImageInfo ?: calculateImageItemSize(mApp).apply { mImageInfo = this }

        override fun loadData(bundle: Bundle?) {
            mMinSelect = GalleryBuilder.parseMinSelect(bundle)
            mMaxSelect = GalleryBuilder.parseMaxSelect(bundle)
            listLiveData.loadStart()
        }

        fun getBucketJson(): String? {
            return mBuckets?.toJsonString()
        }

        private fun countSelect(): Int {
            return mBuckets?.firstOrNull()?.images?.count { it.select } ?: 0
        }

        fun getMenu(): Pair<Boolean, String> {
            val count = countSelect()
            return Pair(count in mMinSelect..mMaxSelect, mApp.getString(R.string.aclin_image_picker_gallery_menu_sure, count, mMaxSelect))
        }

        fun getTitle(): String {
            return selectBucket?.name ?: ""
        }

        private fun setDefaultBucket() {
            (mApp.spfString(LAST_SELECT_BUCKET)?.let { lastSelectBucketId ->
                mBuckets?.find { it.id == lastSelectBucketId }
            } ?: let {
                mBuckets?.find { it.id == ImageModel.BUCKET_ALL }
            })?.also {
                selectBucket(it)
            }
        }

        private fun selectBucket(bucket: Bucket) {
            selectBucket?.select = false
            selectBucket = bucket
            selectBucket?.select = true
            titleLiveData.postValue(true)
        }

        fun selectBucket(id: String?) {
            id?.also {
                mBuckets?.find { it.id == id }?.also { bucket ->
                    mApp.spfString(LAST_SELECT_BUCKET, id)
                    selectBucket(bucket)
                    listLiveData.loadRefresh()
                }
            }
        }

        fun selectImage(image: Image, select: Boolean): Boolean {
            return if (select) {
                val count = countSelect()
                if (count >= mMaxSelect) {
                    mApp.toast(mApp.getString(R.string.aclin_image_picker_gallery_select_limit_max, mMaxSelect))
                    false
                } else {
                    image.select = true
                    menuLiveData.postValue(true)
                    managerSelectImage(image)
                    select
                }
            } else {
                image.select = false
                menuLiveData.postValue(true)
                managerSelectImage(image)
                select
            }
        }

        fun getSelectImages(): ArrayList<String> {
            return mBuckets?.find { it.id == ImageModel.BUCKET_SELECT }?.images?.mapTo(arrayListOf()) { it.path } ?: arrayListOf()
        }

        private fun managerSelectImage(image: Image) {
            mBuckets?.find { it.id == ImageModel.BUCKET_SELECT }?.images?.apply {
                if (image.select) {
                    if (!contains(image)) {
                        add(image)
                    }
                } else {
                    remove(image)
                }
            }
        }

        fun previewImages(position: Int, select: Boolean, callback: (Int, ArrayList<String>) -> Unit) {
            if (select) {
                mBuckets?.find { it.id == ImageModel.BUCKET_SELECT }?.images
            } else {
                selectBucket?.images
            }?.mapTo(arrayListOf()) { it.path }?.apply {
                if (this.isNotEmpty()) {
                    callback(position, this)
                }
            }
        }
    }
}