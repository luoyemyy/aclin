package com.github.luoyemyy.aclin.app.mvp

import android.Manifest
import android.app.Application
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.databinding.ActivityMvpBinding
import com.github.luoyemyy.aclin.mvp.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import kotlin.random.Random
import android.widget.Toast
import android.content.Intent


class MvpActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks{

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .setTitle("权限已经被您拒绝")
                .setRationale("如果不打开权限则无法使用该功能,点击确定去打开权限")
                .setRequestCode(10001)//用于onActivityResult回调做其它对应相关的操作
                .build()
                .show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10001) {
            Toast.makeText(this, " 从开启权限的页面转跳回来 ", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var mBinding: ActivityMvpBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mvp)
        mPresenter = getPresenter()
        mAdapter = Adapter()
        mBinding.apply {
            recyclerView.setupLinear(mAdapter)
            //            mAdapter.enableSort(recyclerView)
            swipeRefreshLayout.setup(mPresenter)
        }
        mPresenter.loadInit(intent.extras)

        EasyPermissions.requestPermissions(PermissionRequest.Builder(this, 1, Manifest.permission.CAMERA).setRationale("需要相机").build())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mvp, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mPresenter.loadSearch(null)
        return super.onOptionsItemSelected(item)
    }

    inner class Adapter : BaseAdapter(this, mPresenter) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.activity_mvp_item
        }

        override fun getItemSortView(binding: ViewDataBinding): View? {
            return binding.root
        }

        override fun enableLoadMore(): Boolean {
            return true
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }
    }

    class Presenter(app: Application) : AbsListPresenter(app) {
        override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return (0..Random.nextInt(9)).map { TextItem(Random.nextInt(9).toString()) }
//            return (0..9).map { TextItem(Random.nextInt(9).toString()) }
        }

    }
}