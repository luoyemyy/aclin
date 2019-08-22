package com.github.luoyemyy.aclin.app.main

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.common.util.BusEvent
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.mvp.BaseAdapter
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
        mAdapter = Adapter(requireContext())
        mBinding.apply {
            recyclerView.setupLinear(mAdapter)
            swipeRefreshLayout.setup(mPresenter)
        }
        addBus(this, BusEvent.PROFILE_CHANGE, this)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        when (msg.event) {
            BusEvent.PROFILE_CHANGE -> mPresenter.updateProfile()
        }
    }

    inner class Adapter(private var context: Context) : BaseAdapter(this, mPresenter) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_list_item
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun getItemClickViews(binding: ViewDataBinding): List<View> {
            return listOf(binding.root)
        }

        override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int, payloads: MutableList<Any>) {
            val bundle = payloads[0] as Bundle
            when {
                bundle.getString("type") == "profile" && item is TextItem -> item.also {
                    it.value = bundle.getString("value", "")
                    binding.setVariable(1, it)
                }
            }
            binding.executePendingBindings()
        }

        override fun onItemViewClick(vh: VH<ViewDataBinding>, view: View) {
            val item = getItem(vh.adapterPosition) as? TextItem ?: return
            when (item.key) {
                "mvp" -> findNavController().navigate(R.id.action_mainFragment_to_mvpFragment)
                "profile" -> findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
                "permission" -> findNavController().navigate(R.id.action_mainFragment_to_permissionFragment)
            }
        }
    }

    class Presenter(private var mApp: Application) : AbsListPresenter(mApp) {
        override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return listOf(
                TextItem("mvp"), TextItem("profile", Profile.active().desc), TextItem("permission")
            )
        }

        fun updateProfile() {
            change { bundle, dataItem ->
                if (dataItem is TextItem && dataItem.key == "profile") {
                    bundle.payloadEnable()
                    bundle.payloadType("profile")
                    bundle.putString("value", Profile.active().desc)
                    true
                } else {
                    false
                }
            }
        }
    }
}