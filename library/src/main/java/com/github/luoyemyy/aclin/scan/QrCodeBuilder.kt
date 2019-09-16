package com.github.luoyemyy.aclin.scan

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus

class QrCodeBuilder private constructor() {

    companion object {
        const val SCAN_PERCENT = 0.65f
        const val QR_CODE_RESULT = "qr_code_result"
    }

    private lateinit var mFragment: Fragment
    private var mQrCodeCallback: QrCodeCallback? = null
    private var mActionId = 0

    constructor(fragment: Fragment) : this() {
        mFragment = fragment
    }

    fun callback(callback: QrCodeCallback): QrCodeBuilder {
        mQrCodeCallback = callback
        return this
    }

    fun action(actionId: Int): QrCodeBuilder {
        mActionId = actionId
        return this
    }

    fun buildAndScan() {
        setBus(mFragment, QR_CODE_RESULT, BusResult {
            it.extra?.getString(QR_CODE_RESULT)?.apply {
                mQrCodeCallback?.invoke(this)
            }
        })
        mFragment.findNavController().navigate(mActionId)
    }

}