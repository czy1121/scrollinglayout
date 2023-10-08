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
import com.demo.app.databinding.ActivityRefreshAsChildBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.reezy.cosmo.bannerview.indicator.RectangleIndicator

class RefreshAsChildActivity : AppCompatActivity() {

    val binding by lazy { ActivityRefreshAsChildBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh_as_child)


        binding.refresh.setOnRefreshListener {
            lifecycleScope.launch {
                delay(2000)
                binding.refresh.finish(true)
            }
        }

        binding.rv.adapter = VerticalAdapter(20)


        val adapter = DemoBannerAdapter()
        adapter.submitList(listOf("One", "Two", "Three", "Four", "Five", "Six"))

        binding.banner.indicator = RectangleIndicator(this)
        binding.banner.bind(this).setup(adapter)
//        binding.banner.start()



    }


    fun onClick(view: View) {
        Toast.makeText(this, "permanent click", Toast.LENGTH_SHORT).show()
    }
}