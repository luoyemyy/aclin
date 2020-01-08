package com.github.luoyemyy.aclin.image.crop

import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.databinding.AclinImageCropBinding
import com.github.luoyemyy.aclin.databinding.AclinImageCropImageBinding
import com.github.luoyemyy.aclin.databinding.AclinImageCropRatioCustomBinding
import com.github.luoyemyy.aclin.ext.show
import com.github.luoyemyy.aclin.file.FileManager
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupLinear
import java.io.FileOutputStream

class CropFragment : OverrideMenuFragment(), View.OnClickListener {

    private lateinit var mPresenter: Presenter
    private lateinit var mBinding: AclinImageCropBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImageCropBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.aclin_image_crop, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.yes -> {
                postBus(CropBuilder.CROP_RESULT, extra = bundleOf(CropBuilder.CROP_RESULT to mPresenter.allCropPaths()))
                findNavController().navigateUp()
            }
            R.id.crop -> {
                mBinding.cropView.crop {
                    mPresenter.saveCrop(it)
                }
            }
            R.id.reset -> {
                mPresenter.reset()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.yes).isVisible = mPresenter.isAllCrop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.image.observe(this, Observer {
            mBinding.path = it
        })
        mPresenter.custom.observe(this, Observer {
            mBinding.customRatio = it
        })
        mPresenter.ratio.observe(this, Observer {
            mBinding.cropView.setMaskRatio(it)
        })
        mPresenter.fixed.observe(this, Observer {
            mBinding.fixed = it
        })
        mPresenter.menu.observe(this, Observer {
            requireActivity().invalidateOptionsMenu()
        })
        mPresenter.list.observe(this, Observer {
            mBinding.recyclerView.show()
            mBinding.recyclerView.setupLinear(Adapter().apply {
                setup(this@CropFragment, mPresenter.listLiveData)
            }, false)
            mBinding.recyclerView.setHasFixedSize(true)
            mPresenter.loadInit(arguments)
        })
        mBinding.apply {
            chip00.setOnClickListener(this@CropFragment)
            chip11.setOnClickListener(this@CropFragment)
            chip169.setOnClickListener(this@CropFragment)
            chip34.setOnClickListener(this@CropFragment)
            chip43.setOnClickListener(this@CropFragment)
            chip916.setOnClickListener(this@CropFragment)
        }
        mPresenter.loadInit(arguments)
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.chip11 -> mPresenter.fixedRatio(CropBuilder.RATIO_1_1)
            mBinding.chip34 -> mPresenter.fixedRatio(CropBuilder.RATIO_3_4)
            mBinding.chip43 -> mPresenter.fixedRatio(CropBuilder.RATIO_4_3)
            mBinding.chip916 -> mPresenter.fixedRatio(CropBuilder.RATIO_9_16)
            mBinding.chip169 -> mPresenter.fixedRatio(CropBuilder.RATIO_16_9)
            mBinding.chip00 -> {
                val binding = AclinImageCropRatioCustomBinding.inflate(layoutInflater).apply {
                    ratioWidth.apply {
                        minValue = 1
                        maxValue = 10
                    }
                    ratioHeight.apply {
                        minValue = 1
                        maxValue = 10
                    }
                }
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.aclin_image_crop_ratio_custom_title)
                        .setView(binding.root)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            mPresenter.customRatio(binding.ratioWidth.value, binding.ratioHeight.value)
                        }.show()
            }
        }
    }

    inner class Adapter : FixedAdapter<CropImage, AclinImageCropImageBinding>() {

        override fun bindContentViewHolder(binding: AclinImageCropImageBinding, data: CropImage?, viewType: Int, position: Int) {
            binding.apply {
                entity = entity
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): AclinImageCropImageBinding {
            return AclinImageCropImageBinding.inflate(layoutInflater, parent, false)
        }

        override fun onItemViewClick(binding: AclinImageCropImageBinding, vh: VH<*>, view: View) {
            getItem(vh.adapterPosition)?.apply {
                mPresenter.setCropImage(this)
            }
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {
        val image = MutableLiveData<String>()
        val custom = MutableLiveData<String>()
        val ratio = MutableLiveData<Float>()
        val fixed = MutableLiveData<Boolean>()
        val menu = MutableLiveData<Boolean>()
        val list = MutableLiveData<Boolean>()

        val listLiveData = ListLiveData<CropImage>()

        private lateinit var mCropArgs: CropArgs
        private lateinit var mCropImage: CropImage

        override fun loadData(bundle: Bundle?) {
            mCropArgs = CropBuilder.parseCropArgs(bundle)
            fixed.value = mCropArgs.fixedRatio
            if (mCropArgs.images.isNotEmpty()) {
                setCropImage(mCropArgs.images[0])
            }
            if (mCropArgs.images.size > 1) {
                list.value = true
            }
            listLiveData.loadStart(mCropArgs.images)
        }

        fun allCropPaths(): ArrayList<String> {
            return mCropArgs.images.mapTo(arrayListOf()) { it.cropPath!! }
        }

        fun reset() {
            menu.value = true
            mCropImage.crop = false
            mCropImage.cropPath = null
            setCropImage(mCropImage)
        }

        fun saveCrop(bitmap: Bitmap) {
            FileManager.getInstance().image()?.apply {
                FileOutputStream(this).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    nextCropImage(absolutePath)
                }
            }
        }

        fun isAllCrop(): Boolean {
            return if (this::mCropImage.isInitialized) {
                mCropArgs.images.all { it.crop }
            } else false
        }

        private fun nextCropImage(cropPath: String) {
            mCropImage.crop = true
            mCropImage.cropPath = cropPath
            val nextImage = if (mCropArgs.images.any { !it.crop }) {
                findNextCropImage(mCropArgs.images.indexOf(mCropImage))
            } else null
            if (nextImage == null) {
                menu.value = true
                setCropImage(mCropImage)
            } else {
                setCropImage(nextImage)
            }
        }

        private fun findNextCropImage(index: Int): CropImage {
            val nextIndex = if (index == mCropArgs.images.size - 1) {
                0
            } else {
                index + 1
            }
            val image = mCropArgs.images[nextIndex]
            return if (image.crop) {
                findNextCropImage(nextIndex)
            } else {
                image
            }
        }

        fun setCropImage(cropImage: CropImage) {
            mCropImage = cropImage
            image.value = cropImage.path()
            ratio.value = cropImage.ratio
            custom.value =
                if (mCropImage.customRatio.isNullOrEmpty()) mApp.getString(R.string.aclin_image_crop_ratio_0_0) else mApp.getString(R.string.aclin_image_crop_ratio_custom_0_0, mCropImage.customRatio)
        }

        fun fixedRatio(ratioValue: Float) {
            mCropImage.ratio = ratioValue
            mCropImage.customRatio = null
            ratio.value = ratioValue
            custom.value = mApp.getString(R.string.aclin_image_crop_ratio_0_0)
        }

        fun customRatio(w: Int, h: Int) {
            mCropImage.ratio = w * 1f / h
            mCropImage.customRatio = "$w:$h"
            ratio.value = mCropImage.ratio
            custom.value = mApp.getString(R.string.aclin_image_crop_ratio_custom_0_0, mCropImage.customRatio)
        }
    }
}