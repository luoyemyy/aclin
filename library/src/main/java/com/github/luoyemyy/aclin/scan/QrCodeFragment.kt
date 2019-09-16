package com.github.luoyemyy.aclin.scan

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.databinding.AclinScanBinding
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.logger.logd
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter

class QrCodeFragment : OverrideMenuFragment(), ImageAnalysis.Analyzer, Preview.OnPreviewOutputUpdateListener {

    private lateinit var mBinding: AclinScanBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinScanBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()

        val preview = Preview(PreviewConfig.Builder().build())
        val analysis = ImageAnalysis(ImageAnalysisConfig.Builder().build())

        preview.onPreviewOutputUpdateListener = this
        analysis.analyzer = this

        CameraX.bindToLifecycle(this, preview, analysis)

        mPresenter.loadInit(arguments)
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        if (mPresenter.nextAnalyze()) {
            val content = parse(image)
            if (content != null) {
                logd("QrCodeFragment", content)
                postBus(QrCodeBuilder.QR_CODE_RESULT, extra = bundleOf(QrCodeBuilder.QR_CODE_RESULT to content))
                findNavController().navigateUp()
            }
        }
    }

    override fun onUpdated(output: Preview.PreviewOutput?) {
        mBinding.cameraView.surfaceTexture = output?.surfaceTexture
    }

    class Presenter(private var mApp: Application) : AbsPresenter(mApp) {

        private var mLastAnalyzeTime = System.currentTimeMillis()

        fun nextAnalyze(): Boolean {
            val current = System.currentTimeMillis()
            if (current - mLastAnalyzeTime > 1000L) {
                mLastAnalyzeTime = current
                return true
            }
            return false
        }

        override fun loadData(bundle: Bundle?) {
        }
    }
}