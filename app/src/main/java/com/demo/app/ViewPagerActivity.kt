package com.demo.app

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.demo.app.adapter.DemoBannerAdapter
import com.demo.app.databinding.ActivityViewPagerBinding
import com.demo.app.fragment.*
import me.reezy.cosmo.bannerview.indicator.RectangleIndicator
import me.reezy.cosmo.tabs.TabItem
import me.reezy.cosmo.tabs.setup

class ViewPagerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityViewPagerBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    private val items = arrayOf("One", "Two", "Three", "Four", "Five", "Six", "Eight", "Nine", "Ten")

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)


        val adapter = DemoBannerAdapter()
        adapter.submitList(listOf("One", "Two", "Three", "Four", "Five", "Six"))

        binding.banner.indicator = RectangleIndicator(this)
        binding.banner.bind(this).setup(adapter)
//        binding.banner.start()

        binding.pager.offscreenPageLimit = 30
        binding.pager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                 return when(position) {
                    0 -> OneFragment()
                    1 -> TwoFragment()
                    2 -> ThreeFragment()
                    3 -> FourFragment()
                    4 -> FiveFragment()
                    else -> Fragment()
                }
            }

            override fun getItemCount(): Int = items.size
        }

        binding.tabs.setup(items.map { TabItem(it, it) }, binding.pager) {
            textView?.textSize = 18f
            textView?.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
}