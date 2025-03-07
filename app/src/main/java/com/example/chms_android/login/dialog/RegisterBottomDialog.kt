package com.example.chms_android.login.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chms_android.databinding.DialogBottomRegisterBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RegisterBottomDialog: BottomSheetDialogFragment() {
    private var _binding: DialogBottomRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogBottomRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // 获取底部的 View
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            // 获取其行为
            val behavior = BottomSheetBehavior.from(it)
            // 设置视图高度为屏幕高度的 80%
            val layoutParams = it.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.85).toInt()
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}