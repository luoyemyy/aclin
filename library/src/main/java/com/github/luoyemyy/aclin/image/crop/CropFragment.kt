package com.github.luoyemyy.aclin.image.crop

import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinImageCropBinding
import com.github.luoyemyy.aclin.databinding.AclinImageCropImageBinding
import com.github.luoyemyy.aclin.databinding.AclinImageCropRatioCustomBinding
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.file.FileManager
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
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
                mBinding.cropView.crop {
                    mPresenter.saveCrop(it)
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
        mBinding.apply {
            chip00.setOnClickListener(this@CropFragment)
            chip11.setOnClickListener(this@CropFragment)
            chip169.setOnClickListener(this@CropFragment)
            chip34.setOnClickListener(this@CropFragment)
            chip43.setOnClickListener(this@CropFragment)
            chip916.setOnClickListener(this@CropFragment)
            recyclerView.setupLinear(Adapter(), false)
            recyclerView.setHasFixedSize(true)
        }
        mPresenter.loadInit(arguments)
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.chip11 -> mPresenter.fixedRatio(1f)
            mBinding.chip169 -> mPresenter.fixedRatio(16f / 9f)
            mBinding.chip34 -> mPresenter.fixedRatio(3f / 4f)
            mBinding.chip43 -> mPresenter.fixedRatio(4f / 3f)
            mBinding.chip916 -> mPresenter.fixedRatio(9f / 16f)
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

    inner class Adapter : FixedAdapter<CropImage, AclinImageCropImageBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_image_crop_image
        }

        override fun onItemViewClick(binding: AclinImageCropImageBinding, vh: VH<*>, view: View) {
            (getItem(vh.adapterPosition) as? CropImage)?.apply {
                mPresenter.setCropImage(this)
            }
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {
        val image = MutableLiveData<String>()
        val custom = MutableLiveData<String>()
        val ratio = MutableLiveData<Float>()
        val fixed = MutableLiveData<Boolean>()

        private lateinit var mCropArgs: CropArgs
        private lateinit var mCropImage: CropImage

        override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            mCropArgs = CropBuilder.parseCropArgs(bundle)
            runOnMain {
                fixed.value = mCropArgs.fixedRatio
                if (mCropArgs.images.isNotEmpty()) {
                    setCropImage(mCropArgs.images[0])
                }
            }
            return mCropArgs.images
        }

        fun saveCrop(bitmap: Bitmap) {
            FileManager.getInstance().image()?.apply {
                FileOutputStream(this).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    nextCropImage(absolutePath)
                }
            }
        }

        fun nextCropImage(cropPath: String) {
            mCropImage.crop = true
            mCropImage.cropPath = cropPath

//            var nextImage = mCropA


        }

        fun setCropImage(cropImage: CropImage) {
            mCropImage = cropImage
            image.value = cropImage.path()
            ratio.value = cropImage.ratio
            custom.value = if (mCropImage.customRatio == null) mApp.getString(R.string.aclin_image_crop_ratio_0_0) else mApp.getString(R.string.aclin_image_crop_ratio_custom_0_0, mCropImage.customRatio)
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