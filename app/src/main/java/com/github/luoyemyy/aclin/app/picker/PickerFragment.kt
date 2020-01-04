package com.github.luoyemyy.aclin.app.picker

import android.app.Application
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
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.*
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setup
import com.github.luoyemyy.aclin.mvp.ext.setupLinear

class PickerFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentListBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter().apply {
                setup(this@PickerFragment, mPresenter.listLiveData)
            })
            swipeRefreshLayout.setup(mPresenter.listLiveData)
        }
        mPresenter.loadInit(arguments)
    }

    private fun gallery() {
        GalleryBuilder(this)
                .action(R.id.action_pickerFragment_to_aclin_image)
                .buildAndPicker {
                    requireContext().toast(it.joinToString(","))
                    CropBuilder(this)
                            .ratio(false, 1f)
                            .paths(it)
                            .action(R.id.action_pickerFragment_to_cropFragment)
                            .buildAndCrop()
                }
    }

    private fun camera() {
        CameraBuilder(this)
                .buildAndCapture {
                    requireContext().toast(it)
                }
    }

    inner class Adapter : FixedAdapter<TextData, FragmentListItemBinding>() {

        init {
            enablePopupMenu = true
        }

        override fun bindContentViewHolder(binding: FragmentListItemBinding, data: TextData?, viewType: Int, position: Int) {
            binding.apply {
                entity = getItem(position)
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentListItemBinding {
            return FragmentListItemBinding.inflate(layoutInflater, parent, false)
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
            when (getItem(vh.adapterPosition)?.text ?: return) {
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

    class Presenter(private var mApp: Application) : MvpPresenter(mApp) {

        val listLiveData = ListLiveData<TextData> { DataItem(it) }

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart(listOf(
                TextData("gallery"),
                TextData("camera"),
                TextData("gallery/camera")))
        }
    }
}