package com.demo.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.demo.app.databinding.ActivityScrollViewBinding
import com.demo.app.databinding.ActivityWebviewBinding

class WebViewActivity : AppCompatActivity(R.layout.activity_webview) {

    val binding by lazy { ActivityWebviewBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    private val methodComputeExtent by lazy {
        View::class.java.getDeclaredMethod("computeVerticalScrollExtent").apply {
            this.isAccessible = true
        }
    }
    private val methodComputeOffset by lazy {
        View::class.java.getDeclaredMethod("computeVerticalScrollOffset").apply {
            this.isAccessible = true
        }
    }
    private val methodComputeRange by lazy {
        View::class.java.getDeclaredMethod("computeVerticalScrollRange").apply {
            this.isAccessible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.web.initWebSettings()
        binding.web.loadUrl("https://www.oschina.net/")


        binding.bottom.setOnClickListener {
            Log.e("OoO", "web => ${binding.web.height}, ${binding.web.contentHeight},  ${methodComputeExtent.invoke(binding.web)},  ${methodComputeOffset.invoke(binding.web)}, ${methodComputeRange.invoke(binding.web)}")
        }
    }





    private fun WebView.initWebSettings() {

        // 存储(storage)
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        // 定位(location)
        settings.setGeolocationEnabled(true)

        // 缩放(zoom)
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        // 文件权限
        settings.allowContentAccess = true
        settings.allowFileAccess = true

        //
        settings.textZoom = 100

        @SuppressLint("SetJavaScriptEnabled")
        // 支持Javascript
        settings.javaScriptEnabled = true

        // 支持https
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // 页面自适应手机屏幕，支持viewport属性
        settings.useWideViewPort = true
        // 缩放页面，使页面宽度等于WebView宽度
        settings.loadWithOverviewMode = true

        // 是否自动加载图片
        settings.loadsImagesAutomatically = true
        // 禁止加载网络图片
        settings.blockNetworkImage = false
        // 禁止加载所有网络资源
        settings.blockNetworkLoads = false

    }
}