package com.demo.app.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import me.reezy.cosmo.bannerview.adapter.BannerAdapter

class DemoBannerAdapter : BannerAdapter<String>() {

    private val colors = arrayOf(Color.BLUE, Color.RED, Color.MAGENTA, Color.GREEN, Color.DKGRAY, Color.CYAN, Color.YELLOW)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        })
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val realPosition = when {
            itemCount <= 1 -> 0
            position == itemCount - 1 -> 0
            position == 0 -> itemCount - 2 - 1
            else -> position - 1
        }
        (holder.itemView as TextView).apply {
            @SuppressLint("SetTextI18n")
            text = "$realPosition\n\n${getItem(position)}"
            setBackgroundColor(colors[realPosition % colors.size])
        }
    }
}