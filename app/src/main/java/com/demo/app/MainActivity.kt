package com.demo.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import com.demo.app.databinding.ActivityMainBinding
import me.reezy.cosmo.pullrefresh.PullRefreshLayout
import me.reezy.cosmo.pullrefresh.simple.SimpleHeader

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    init {
        PullRefreshLayout.setDefaultHeaderFactory {
            SimpleHeader(it.context)
        }
    }

    val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        binding.btnScrollingView.setOnClickListener {
            startActivity(Intent(this, ScrollingViewActivity::class.java))
        }
        binding.btnSticky.setOnClickListener {
            startActivity(Intent(this, StickyActivity::class.java))
        }

        binding.btnHeightMatchParent.setOnClickListener {
            startActivity(Intent(this, HeightMatchParentActivity::class.java))
        }
        binding.btnHeightExactly.setOnClickListener {
            startActivity(Intent(this, HeightExactlyActivity::class.java))
        }

        binding.btnRefreshAsParent.setOnClickListener {
            startActivity(Intent(this, RefreshAsParentActivity::class.java))
        }
        binding.btnRefreshAsChild.setOnClickListener {
            startActivity(Intent(this, RefreshAsChildActivity::class.java))
        }

        binding.btnViewPager.setOnClickListener {
            startActivity(Intent(this, ViewPagerActivity::class.java))
        }
        binding.btnScrollView.setOnClickListener {
            startActivity(Intent(this, ScrollViewActivity::class.java))
        }
        binding.btnWebView.setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java))
        }
    }
}