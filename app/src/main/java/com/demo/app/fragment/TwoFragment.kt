package com.demo.app.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.demo.app.R
import com.demo.app.adapter.VerticalAdapter
import com.demo.app.databinding.FragmentTwoBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TwoFragment: Fragment(R.layout.fragment_two) {


    val binding by lazy { FragmentTwoBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refresh.setOnRefreshListener {
            lifecycleScope.launch {
                delay(2000)
                binding.refresh.finish(true)
            }
        }

        binding.rv.adapter = VerticalAdapter(20)
    }
}