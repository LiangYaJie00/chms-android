package com.example.chms_android.features.knowledge

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.chms_android.R
import com.example.chms_android.databinding.FragmentKnowledgeBinding

class KnowledgeFragment : Fragment() {
    private var _binding: FragmentKnowledgeBinding? = null
    private val binding get() = _binding!!

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

//        // 实现点击返回不是返回到首页，而是回到桌面
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
//            OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                requireActivity().finish()
//            }
//        })

        return binding.root
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