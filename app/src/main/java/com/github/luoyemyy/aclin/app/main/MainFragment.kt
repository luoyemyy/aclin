package com.github.luoyemyy.aclin.app.main

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.common.util.BusEvent
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItemBinding
import com.github.luoyemyy.aclin.app.mvp.TextItem
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.addBus
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.aclin.profile.Profile

class MainFragment : Fragment(), BusResult {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mAdapter: Adapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mAdapter = Adapter()
        mBinding.apply {
            recyclerView.setupLinear(mAdapter)
            swipeRefreshLayout.setup(mPresenter.listLiveData)
        }
        addBus(this, BusEvent.PROFILE_CHANGE, this)
        mPresenter.listLiveData.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        when (msg.event) {
            BusEvent.PROFILE_CHANGE -> mPresenter.updateProfile()
        }
    }

    inner class Adapter : FixedAdapter<TextItem, FragmentListItemBinding>(this, mPresenter.listLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_list_item
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun onItemViewClick(binding: FragmentListItemBinding, vh: VH<*>, view: View) {
            val item = getItem(vh.adapterPosition) as? TextItem ?: return
            when (item.key) {
                "mvp" -> findNavController().navigate(R.id.action_mainFragment_to_mvpFragment)
                "profile" -> findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
                "permission" -> findNavController().navigate(R.id.action_mainFragment_to_permissionFragment)
                "image" -> findNavController().navigate(R.id.action_mainFragment_to_aclin_image)
                "logger" -> findNavController().navigate(R.id.action_mainFragment_to_aclin_logger)
            }
        }
    }

    class Presenter(private var mApp: Application) : AbsListPresenter(mApp) {

        override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return listOf(
                TextItem("mvp"),
                TextItem("profile", Profile.active().desc),
                TextItem("permission"),
                TextItem("image"),
                TextItem("logger")
            )
        }

        fun updateProfile() {
            listLiveData.itemChange { items,_ ->
                items?.forEach {
                    if (it is TextItem && it.key == "profile") {
                        it.value = Profile.active().desc
                        it.hasPayload()
                    }
                }
                true
            }
        }
    }
}