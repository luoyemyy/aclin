package com.github.luoyemyy.aclin.mvp

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.databinding.AclinEmptyBinding

abstract class AbsPresenter(app: Application) : AndroidViewModel(app)