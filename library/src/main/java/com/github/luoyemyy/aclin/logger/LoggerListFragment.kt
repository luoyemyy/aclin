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
        mBinding.swipeRefreshLayout.setup(mPresenter.liveData)
        mPresenter.loadInit(arguments)
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

    inner class Adapter : FixedAdapter<LoggerItem, AclinLoggerListItemBinding>(this, mPresenter.liveData) {
        override fun bindContentViewHolder(binding: AclinLoggerListItemBinding, data: LoggerItem?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): AclinLoggerListItemBinding {
            return AclinLoggerListItemBinding.inflate(layoutInflater, parent, false)
        }

        override fun bindItemEvents(binding: AclinLoggerListItemBinding, vh: VH<*>) {
            binding.checkbox.setOnCheckedChangeListener { _, b ->
                getItem(vh.adapterPosition)?.select = b
            }
        }

        override fun onItemViewClick(binding: AclinLoggerListItemBinding, vh: VH<*>, view: View) {
            getItem(vh.adapterPosition)?.apply {
                findNavController().navigate(R.id.action_loggerListFragment_to_loggerPreviewFragment, bundleOf("path" to path))
            }
        }
    }

    class Presenter(private var mApp: Application) : MvpPresenter(mApp) {

        val liveData = object : ListLiveData<LoggerItem>({ DataItem(it) }) {
            override fun getStartData(): List<LoggerItem>? {
                return files()
            }
        }

        override fun loadData(bundle: Bundle?) {
            liveData.loadStart()
        }

        private fun files(): List<LoggerItem>? {
            val path = Logger.logPath ?: return null
            return File(path).list()?.sortedDescending()?.map {
                LoggerItem(it, File(path, it).absolutePath)
            }
        }

        fun selectAll() {
            val selectAll = countSelect() == 0
            liveData.itemChange { items, _ ->
                items?.forEach {
                    it.data?.apply {
                        select = selectAll
                        it.hasPayload()
                    }
                }
                true
            }
        }

        fun countSelect(): Int {
            return liveData.dataList().count { it.select }
        }

        fun deleteSelect() {
            liveData.itemChange { _, dataList ->
                val selectItems = dataList.filter { it.select && File(it.path).delete() }
                if (selectItems.isEmpty()) {
                    false
                } else {
                    dataList.removeAll(selectItems)
                    true
                }
            }
        }
    }
}