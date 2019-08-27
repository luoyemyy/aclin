package com.github.luoyemyy.aclin.logger

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinLoggerListBinding
import com.github.luoyemyy.aclin.databinding.AclinLoggerListItemBinding
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.mvp.*
import java.io.File
import java.lang.RuntimeException

class LoggerListFragment : Fragment() {

    private lateinit var mBinding: AclinLoggerListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinLoggerListBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.recyclerView.setupLinear(Adapter())
        mBinding.swipeRefreshLayout.setup(mPresenter.listLiveData)
        mPresenter.listLiveData.loadInit(arguments)

//        throw RuntimeException()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.aclin_logger, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.all -> mPresenter.selectAll()
            R.id.delete -> if (mPresenter.countSelect() == 0) {
                requireContext().toast(R.string.aclin_logger_menu_select_tip)
            } else {
                AlertDialog.Builder(requireContext()).setMessage(R.string.aclin_logger_menu_delete_tip)
                    .setNegativeButton(android.R.string.cancel, null).setPositiveButton(R.string.aclin_logger_menu_delete) { _, _ ->
                        mPresenter.deleteSelect()
                    }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class Adapter : AbsAdapter(this, mPresenter.listLiveData) {

        override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
            binding.setVariable(1, item)
            binding.executePendingBindings()
        }

        override fun bindContentPayload(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int,
            payloads: MutableList<Any>) {
            val bundle = payloads[0] as Bundle
            when (item) {
                is LoggerItem -> {
                    item.select = bundle.getBoolean("select")
                    binding.setVariable(1, item)
                }
            }
            binding.executePendingBindings()
        }

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_logger_list_item
        }

        override fun bindItemEvents(vh: VH<ViewDataBinding>) {
            (vh.binding as AclinLoggerListItemBinding).apply {
                checkbox.setOnCheckedChangeListener { _, b ->
                    mPresenter.select(vh.adapterPosition, b)
                }
            }
        }

        override fun onItemViewClick(vh: VH<ViewDataBinding>, view: View) {
            (getItem(vh.adapterPosition) as? LoggerItem)?.apply {
                findNavController().navigate(R.id.action_loggerListFragment_to_loggerPreviewFragment, bundleOf("path" to path))
            }
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
    }

    class Presenter(private var mApp: Application) : AbsPresenter(mApp) {

        val listLiveData = object : ListLiveData() {
            override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
                val path = Logger.logPath ?: return null
                return File(path).list()?.sortedDescending()?.map {
                    LoggerItem(it, File(path, it).absolutePath)
                }
            }
        }

        fun selectAll() {
            val selectAll = countSelect() == 0
            listLiveData.update { dataSet ->
                DataItemGroup(true, dataSet.getDataList().map { data ->
                    if (data is LoggerItem) {
                        data.select = selectAll
                    }
                    data
                })
            }
        }

        fun select(position: Int, select: Boolean) {
            listLiveData.change(position) { change, data ->
                change.payload = true
                change.data = bundleOf("select" to select)
            }
        }

        fun countSelect(): Int {
            return listLiveData.getDataSet().getDataList().count { it is LoggerItem && it.select }
        }

        fun deleteSelect() {
            listLiveData.getDataSet().getDataList().forEach {
                if (it is LoggerItem) {
                    File(it.path).delete()
                }
            }
            listLiveData.loadRefresh(false)
        }
    }
}