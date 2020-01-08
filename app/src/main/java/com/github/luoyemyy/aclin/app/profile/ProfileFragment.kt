package com.github.luoyemyy.aclin.app.profile

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.luoyemyy.aclin.api.refreshApi
import com.github.luoyemyy.aclin.app.common.util.BusEvent
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentProfileItemBinding
import com.github.luoyemyy.aclin.bus.BusLiveData
import com.github.luoyemyy.aclin.bus.getBusLiveData
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setup
import com.github.luoyemyy.aclin.mvp.ext.setupLinear
import com.github.luoyemyy.aclin.profile.Profile

class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.busLiveData = getBusLiveData()
        mBinding.apply {
            recyclerView.setupLinear(Adapter().apply {
                setup(this@ProfileFragment, mPresenter.listLiveData)
            })
            swipeRefreshLayout.setup(mPresenter.listLiveData)
        }
        mPresenter.loadInit(arguments)
    }

    inner class Adapter : FixedAdapter<ProfileItem, FragmentProfileItemBinding>() {

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentProfileItemBinding {
            return FragmentProfileItemBinding.inflate(layoutInflater, parent, false)
        }

        override fun bindContentViewHolder(binding: FragmentProfileItemBinding, data: ProfileItem?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun onItemViewClick(binding: FragmentProfileItemBinding, vh: VH<*>, view: View) {
            mPresenter.changeActive(vh.adapterPosition)
        }

    }

    class Presenter(private var mApp: Application) : MvpPresenter(mApp) {

        val listLiveData = ListLiveData<ProfileItem>()
        lateinit var busLiveData: BusLiveData

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart(Profile.allTypes().map { ProfileItem(it.desc, it.isActive()) })
        }

        fun changeActive(selectPosition: Int) {
            val activePosition = Profile.activePosition()
            if (activePosition != selectPosition) {
                Profile.changeType(mApp, selectPosition) {
                    refreshApi()
                    busLiveData.post(BusEvent.PROFILE_CHANGE)
                    listLiveData.itemChange { items, _ ->
                        (items?.get(activePosition))?.apply {
                            data?.active = false
                            hasPayload()
                        }
                        (items?.get(selectPosition))?.apply {
                            data?.active = true
                            hasPayload()
                        }
                        true
                    }
                }
            }
        }
    }
}