package com.github.luoyemyy.aclin.logger

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.databinding.AclinLoggerInfoBinding
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter
import java.io.File
import java.io.FileReader

class LoggerInfoFragment : Fragment() {

    private lateinit var mBinding: AclinLoggerInfoBinding
    private lateinit var mPresenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinLoggerInfoBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.textLiveData.observe(this, Observer {
            mBinding.entity = it
        })
        mPresenter.setup(arguments)
    }

    class Presenter(private var mApp: Application) : AbsPresenter(mApp) {
        val textLiveData = MutableLiveData<String>()

        override fun setup(bundle: Bundle?) {
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