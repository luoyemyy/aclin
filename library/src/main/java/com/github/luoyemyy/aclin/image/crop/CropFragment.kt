package com.github.luoyemyy.aclin.image.crop

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinImageCropBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter

class CropFragment : OverrideMenuFragment() {
    private lateinit var mPresenter: Presenter
    private lateinit var mBinding: AclinImageCropBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinImageCropBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.image.observe(this, Observer {
            mBinding.entity = it
            mBinding.chipGroup.check(R.id.chip00)
        })

        mPresenter.setup(arguments)
    }

    class Presenter(var mApp: Application) : AbsPresenter(mApp) {
        val image = MutableLiveData<CropImage>()

        override fun setup(bundle: Bundle?) {
            image.value = bundle?.let {
                CropImage(R.id.chip11, 1f, it.getString("path", ""))
            }
        }
    }
}