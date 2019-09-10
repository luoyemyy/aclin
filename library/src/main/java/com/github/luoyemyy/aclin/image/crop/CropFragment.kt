package com.github.luoyemyy.aclin.image.crop

import android.app.Application
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
import com.github.luoyemyy.aclin.databinding.AclinImageCropRatioCustomBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.image.picker.gallery.GalleryBuilder
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter

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
        mPresenter.canOp.observe(this, Observer {
            mBinding.canOp = it
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

    class Presenter(var mApp: Application) : AbsPresenter(mApp) {
        val image = MutableLiveData<String>()
        val custom = MutableLiveData<String>()
        val ratio = MutableLiveData<Float>()
        val canOp = MutableLiveData<Boolean>()

        override fun loadData(bundle: Bundle?) {
            image.value = bundle?.getString("path")
            ratio.value = bundle?.getFloat("ratio", 1f)
            canOp.value = bundle?.getBoolean("canOp", true)
            custom.value = mApp.getString(R.string.aclin_image_crop_ratio_0_0)
        }

        fun fixedRatio(ratioValue: Float) {
            ratio.value = ratioValue
            custom.value = mApp.getString(R.string.aclin_image_crop_ratio_0_0)
        }

        fun customRatio(w: Int, h: Int) {
            ratio.value = w * 1f / h
            custom.value = mApp.getString(R.string.aclin_image_crop_ratio_custom_0_0, w, h)
        }
    }
}