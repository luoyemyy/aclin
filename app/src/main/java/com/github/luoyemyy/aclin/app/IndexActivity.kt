package com.github.luoyemyy.aclin.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.aclin.app.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_index)
        setSupportActionBar(mBinding.toolbar)
        mBinding.toolbar.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }
}