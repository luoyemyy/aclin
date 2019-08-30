package com.github.luoyemyy.aclin.app.profile

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.github.luoyemyy.aclin.api.refreshApi
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.common.util.BusEvent
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.aclin.profile.Profile

class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter(requireContext()), true, LinearDecoration.middle(requireContext()))
            swipeRefreshLayout.setup(mPresenter.listLiveData)
        }
        mPresenter.listLiveData.loadInit(arguments)
    }

    inner class Adapter(private var context: Context) : SimpleAdapter(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_profile_item
        }

        override fun onItemViewClick(binding: ViewDataBinding, vh: VH<*>, view: View) {
            mPresenter.changeActive(vh.adapterPosition)
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }
    }

    class Presenter(private var mApp: Application) : AbsListPresenter(mApp) {

        override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return Profile.allTypes().map { ProfileItem(it.desc, it.isActive()) }
        }

        fun changeActive(selectPosition: Int) {
            val activePosition = Profile.activePosition()
            if (activePosition != selectPosition) {
                Profile.changeType(mApp, selectPosition) {
                    refreshApi()
                    postBus(BusEvent.PROFILE_CHANGE)
                    listLiveData.itemChange {
                        (it?.get(activePosition)as? ProfileItem)?.apply {
                            active = false
                            hasPayload()
                        }
                        (it?.get(selectPosition) as? ProfileItem)?.apply {
                            active = true
                            hasPayload()
                        }
                        true
                    }
                }
            }
        }
    }
}