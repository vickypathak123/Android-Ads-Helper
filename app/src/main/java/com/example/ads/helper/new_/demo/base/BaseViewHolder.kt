package com.example.ads.helper.new_.demo.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<VB: ViewBinding>(fBinding: VB): RecyclerView.ViewHolder(fBinding.root)