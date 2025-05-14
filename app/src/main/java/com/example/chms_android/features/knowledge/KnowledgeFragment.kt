package com.example.chms_android.features.knowledge

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.R
import com.example.chms_android.databinding.FragmentKnowledgeBinding
import com.example.chms_android.features.knowledge.vm.KnowledgeFragmentVM

class KnowledgeFragment : Fragment() {
    private var _binding: FragmentKnowledgeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KnowledgeFragmentVM
    private var offlineBanner: View? = null
    private var isBannerVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKnowledgeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(KnowledgeFragmentVM::class.java)
        
        binding.wvFk.settings.javaScriptEnabled = true
        binding.wvFk.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        binding.wvFk.loadUrl("https://www.kepuchina.cn/health/")
        
        setupObservers()

        return binding.root
    }
    
    private fun setupObservers() {
        // 观察离线模式状态
        viewModel.isOfflineMode.observe(viewLifecycleOwner) { isOffline ->
            Log.d("KnowledgeFragment", "Offline mode changed: $isOffline, isBannerVisible=$isBannerVisible")
            if (isOffline && !isBannerVisible) {
                showOfflineBanner()
            } else if (!isOffline && isBannerVisible) {
                removeOfflineBanner()
            }
        }
        
        // 观察重试连接结果
        viewModel.connectionRetryResult.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                removeOfflineBanner()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 在视图完全创建后检查离线模式
        viewModel.checkOfflineMode(requireContext())
    }

    override fun onResume() {
        super.onResume()
        
        // 在Fragment恢复时也检查离线模式
        viewModel.checkOfflineMode(requireContext())
    }

    private fun showOfflineBanner() {
        try {
            Log.d("KnowledgeFragment", "Showing offline banner")
            
            // 创建离线模式横幅
            offlineBanner = layoutInflater.inflate(R.layout.offline_mode_banner, null)
            
            // 获取容器
            val container = binding.fragmentKnowledge
            
            if (container != null && container is LinearLayout) {
                // 在LinearLayout中，添加到索引1的位置（TitleBar之后）
                container.addView(offlineBanner, 1)
                
                // 设置重试按钮点击事件
                offlineBanner?.findViewById<Button>(R.id.btn_retry_connection)?.setOnClickListener {
                    viewModel.retryConnection(requireContext())
                }
                
                // 标记横幅已显示
                isBannerVisible = true
            } else {
                Log.e("KnowledgeFragment", "Container view is null or not a LinearLayout")
            }
        } catch (e: Exception) {
            Log.e("KnowledgeFragment", "Error showing offline banner: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun removeOfflineBanner() {
        try {
            Log.d("KnowledgeFragment", "Removing offline banner")
            
            if (offlineBanner != null) {
                val container = binding.fragmentKnowledge
                if (container != null && container is LinearLayout) {
                    container.removeView(offlineBanner)
                    offlineBanner = null
                    
                    // 标记横幅已移除
                    isBannerVisible = false
                }
            }
        } catch (e: Exception) {
            Log.e("KnowledgeFragment", "Error removing offline banner: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 在视图销毁时重置状态
        offlineBanner = null
        isBannerVisible = false
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            KnowledgeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}