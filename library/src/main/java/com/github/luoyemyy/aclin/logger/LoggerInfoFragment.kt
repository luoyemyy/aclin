package com.github.luoyemyy.aclin.logger

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.databinding.AclinLoggerInfoBinding
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import java.io.File
import java.io.FileReader

class LoggerInfoFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinLoggerInfoBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinLoggerInfoBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.textLiveData.observe(this, Observer {
            mBinding.entity = it
        })
        mPresenter.loadInit(arguments)
    }

    class Presenter(app: Application) : MvpPresenter(app) {
        val textLiveData = MutableLiveData<String>()

        override fun loadData(bundle: Bundle?) {
            val path = bundle?.getString("path") ?: return
            runOnThread {
                File(path).apply {
                    if (this.exists() && this.isFile) {
                        FileReader(this).use {
                            textLiveData.postValue(it.readText())
                        }
                    }
                }
            }
        }
    }
}