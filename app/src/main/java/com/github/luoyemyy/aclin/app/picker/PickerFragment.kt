package com.github.luoyemyy.aclin.app.picker

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.databinding.FragmentListBinding
import com.github.luoyemyy.aclin.app.databinding.FragmentListItemBinding
import com.github.luoyemyy.aclin.ext.items
import com.github.luoyemyy.aclin.ext.popupMenu
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.image.crop.CropBuilder
import com.github.luoyemyy.aclin.image.picker.camera.CameraBuilder
import com.github.luoyemyy.aclin.image.picker.gallery.GalleryBuilder
import com.github.luoyemyy.aclin.mvp.*

class PickerFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter(requireContext()).apply { enablePopupMenu() })
            swipeRefreshLayout.setup(mPresenter.listLiveData)
        }
        mPresenter.listLiveData.loadInit(arguments)
    }

    private fun gallery() {
        GalleryBuilder(this)
                .callback {
                    requireContext().toast(it.joinToString(","))
                    CropBuilder(this)
                            .ratio(false, 1f)
                            .paths(it)
                            .action(R.id.action_pickerFragment_to_cropFragment)
                            .buildAndCrop()
                }
                .action(R.id.action_pickerFragment_to_aclin_image)
                .buildAndPicker()
    }

    private fun camera() {
        CameraBuilder(this)
                .callback {
                    requireContext().toast(it)
                }
                .buildAndCapture()
    }

    inner class Adapter(private var context: Context) : FixedAdapter<TextItem, FragmentListItemBinding>(this, mPresenter.listLiveData) {

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_list_item
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun bindItemEvents(binding: FragmentListItemBinding, vh: VH<*>) {
            binding.root.setOnLongClickListener {
                popupMenu(requireContext(), it, R.menu.picker) { itemId ->
                    when (itemId) {
                        R.id.gallery -> gallery()
                        R.id.camera -> camera()
                    }
                }
            }
        }

        override fun onItemViewClick(binding: FragmentListItemBinding, vh: VH<*>, view: View) {
            val item = getItem(vh.adapterPosition) as? TextItem ?: return
            when (item.text) {
                "gallery" -> gallery()
                "camera" -> camera()
                "gallery/camera" -> requireActivity().items(arrayOf("gallery", "camera")) {
                    when (it) {
                        0 -> gallery()
                        1 -> camera()
                    }
                }
            }
        }
    }

    class Presenter(private var mApp: Application) : AbsListPresenter(mApp) {

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return listOf(
                TextItem("gallery"),
                TextItem("camera"),
                TextItem("gallery/camera")
                         )
        }
    }
}