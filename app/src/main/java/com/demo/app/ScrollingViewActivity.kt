package com.demo.app

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import com.demo.app.adapter.VerticalAdapter
import com.demo.app.databinding.ActivityScrollingViewBinding
import me.reezy.cosmo.scrollinglayout.ScrollingLayout

class ScrollingViewActivity : AppCompatActivity() {

    val binding by lazy { ActivityScrollingViewBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling_view)

        binding.rv.adapter = VerticalAdapter(20)

        val lp = (binding.rv.layoutParams as ScrollingLayout.LayoutParams)

        binding.matchParent.isChecked = lp.height == ViewGroup.LayoutParams.MATCH_PARENT
        binding.allowNestedScrolling.isChecked = lp.allowNestedScrolling
        binding.allowIntercept.isChecked = lp.allowIntercept
        binding.allowScrolling.isChecked = lp.allowScrolling


        binding.allowScrolling.setOnCheckedChangeListener { buttonView, isChecked ->
            update()
        }
        binding.allowNestedScrolling.setOnCheckedChangeListener { buttonView, isChecked ->
            update()
        }
        binding.allowIntercept.setOnCheckedChangeListener { buttonView, isChecked ->
            update()
        }
        binding.matchParent.setOnCheckedChangeListener { buttonView, isChecked ->
            update()
        }

    }

    private fun update() {
        binding.rv.updateLayoutParams<ScrollingLayout.LayoutParams> {
            height = if (binding.matchParent.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else (resources.displayMetrics.density * 300).toInt()
            allowIntercept = binding.allowIntercept.isChecked
            allowNestedScrolling = binding.allowNestedScrolling.isChecked
            allowScrolling = binding.allowScrolling.isChecked
            Log.e("OoO", "$allowIntercept, $allowNestedScrolling, $allowScrolling")
        }
        binding.scrolling.requestLayout()
    }
}