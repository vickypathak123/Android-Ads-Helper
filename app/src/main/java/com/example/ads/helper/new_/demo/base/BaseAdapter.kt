package com.example.ads.helper.new_.demo.base

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

@Suppress("unused")
abstract class BaseAdapter<T>(var mList: MutableList<T>) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return onCreateHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (position >= 0) {
            onBindHolder(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    abstract fun onCreateHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>

    abstract fun onBindHolder(holder: BaseViewHolder<*>, position: Int)

    @SuppressLint("NotifyDataSetChanged")
    open fun addAllItems(fLists: Collection<T>, isReplaceAllItem: Boolean = true) {
        if (isReplaceAllItem) {
            removeAll()
        }
        mList.addAll(fLists)
        notifyDataSetChanged()
    }

    open fun addItem(fItem: T) {
        mList.add(fItem)
        notifyItemInserted(mList.size)
        notifyItemRangeChanged(0, itemCount)
    }

    open fun addItem(fItem: T, position: Int) {
        mList.add(position, fItem)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, itemCount)
    }

    open fun replaceItem(fItem: T, position: Int) {
        removeItem(position)
        addItem(fItem, position)
    }

    open fun removeItem(position: Int) {
        mList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun removeAll() {
        mList.removeAll(mList)
        mList.clear()
        notifyDataSetChanged()
    }

    open fun getItem(position: Int): T {
        return mList[position]
    }

//    open fun clearList() {
//        val totalSize = mList.size
//        mList.removeAll(mList)
//        mList.clear()
//        notifyItemRangeRemoved(0, totalSize)
//    }
}