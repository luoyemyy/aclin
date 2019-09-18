package com.github.luoyemyy.aclin.logger

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.databinding.AclinLoggerListBinding
import com.github.luoyemyy.aclin.databinding.AclinLoggerListItemBinding
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import java.io.File

class LoggerListFragment : OverrideMenuFragment() {

    private lateinit var mBinding: AclinLoggerListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AclinLoggerListBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.recyclerView.setupLinear(Adapter())
        mBinding.swipeRefreshLayout.setup(mPresenter.listLiveData)
        mPresenter.listLiveData.loadInit(arguments)
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
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.aclin_logger_menu_delete_tip)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.aclin_logger_menu_delete) { _, _ ->
                        mPresenter.deleteSelect()
                    }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class Adapter : FixedAdapter<LoggerItem, AclinLoggerListItemBinding>(this, mPresenter.listLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.aclin_logger_list_item
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun bindItemEvents(binding: AclinLoggerListItemBinding, vh: VH<*>) {
            binding.checkbox.setOnCheckedChangeListener { _, b ->
                getContentItem(vh.adapterPosition)?.select = b
            }
        }

        override fun onItemViewClick(binding: AclinLoggerListItemBinding, vh: VH<*>, view: View) {
            (getItem(vh.adapterPosition) as? LoggerItem)?.apply {
                findNavController().navigate(R.id.action_loggerListFragment_to_loggerPreviewFragment, bundleOf("path" to path))
            }
        }
    }

    class Presenter(private var mApp: Application) : AbsListPresenter(mApp) {

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return files()
        }

        private fun files(): List<LoggerItem>? {
            val path = Logger.logPath ?: return null
            return File(path).list()?.sortedDescending()?.map {
                LoggerItem(it, File(path, it).absolutePath)
            }
        }

        fun selectAll() {
            val selectAll = countSelect() == 0
            listLiveData.itemChange { items, _ ->
                items?.forEach {
                    (it as? LoggerItem)?.apply {
                        select = selectAll
                        hasPayload()
                    }
                }
                true
            }
        }

        fun countSelect(): Int {
            return listLiveData.value?.data?.count { it is LoggerItem && it.select } ?: 0
        }

        fun deleteSelect() {
            listLiveData.itemDelete { _, dataSet ->
                val selectItems = dataSet.getContentList().filter { it is LoggerItem && it.select && File(it.path).delete() }
                if (selectItems.isEmpty()) {
                    false
                } else {
                    dataSet.getContentList().removeAll(selectItems)
                    true
                }
            }
        }
    }
}