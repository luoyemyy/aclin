package com.github.luoyemyy.aclin.scan

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import com.github.luoyemyy.aclin.databinding.AclinScanBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter

class QrCodeFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinScanBinding
    //    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinScanBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //        mPresenter = getPresenter()
        //        mPresenter.textLiveData.observe(this, Observer {
        //        })

        val preview = Preview(PreviewConfig.Builder().build())
        val analysis = ImageAnalysis(ImageAnalysisConfig.Builder().build())

        preview.setOnPreviewOutputUpdateListener {
            mBinding.cameraView.surfaceTexture = it.surfaceTexture
        }

        analysis.setAnalyzer { image, rotationDegrees ->
            //            QrCode.parse(image.)
        }

        CameraX.bindToLifecycle(this, preview, analysis)

        //        mPresenter.loadInit(arguments)
    }

    class Presenter(private var mApp: Application) : AbsPresenter(mApp) {
        //        val textLiveData = MutableLiveData<String>()
        //
        override fun loadData(bundle: Bundle?) {
            //            val path = bundle?.getString("path") ?: return
            //            runOnThread {
            //                File(path).apply {
            //                    if (this.exists() && this.isFile) {
            //                        FileReader(this).use {
            //                            textLiveData.postValue(it.readText())
            //                        }
            //                    }
            //                }
            //            }
        }
    }
}