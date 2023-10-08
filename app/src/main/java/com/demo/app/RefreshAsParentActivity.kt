package com.demo.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.demo.app.adapter.DemoBannerAdapter
import com.demo.app.adapter.VerticalAdapter
import com.demo.app.databinding.ActivityRefreshAsParentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.reezy.cosmo.bannerview.indicator.RectangleIndicator

class RefreshAsParentActivity : AppCompatActivity() {

    val binding by lazy { ActivityRefreshAsParentBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh_as_parent)


        binding.refresh.setOnRefreshListener {
            lifecycleScope.launch {
                delay(2000)
                binding.refresh.finish(true)
            }
        }

        binding.rv.adapter = VerticalAdapter(20)



        binding.banner.apply {
            lifecycle.addObserver(this)

            indicator = RectangleIndicator(context)

            val adapter = DemoBannerAdapter()
            adapter.submitList(listOf("One", "Two", "Three", "Four", "Five", "Six"))
            setup(adapter)
            start()

        }


    }


    fun onClick(view: View) {
        Toast.makeText(this, "permanent click", Toast.LENGTH_SHORT).show()
    }
}