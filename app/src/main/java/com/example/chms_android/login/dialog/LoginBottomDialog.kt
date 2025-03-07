package com.example.chms_android.login.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.R
import com.example.chms_android.databinding.DialogBottomLoginBinding
import com.example.chms_android.login.vm.LoginActivityVM
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoginBottomDialog: BottomSheetDialogFragment() {
    private var _binding: DialogBottomLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginActivityVM: LoginActivityVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginActivityVM = ViewModelProvider(requireActivity()).get(LoginActivityVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogBottomLoginBinding.inflate(inflater, container, false)
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


        // 忘记密码操作
        binding.tvDblForget.setOnClickListener {

        }

        // 没有账户的操作（跳转到注册界面）
        binding.tvDblNotAccount.setOnClickListener {

        }

        // 登录操作
        binding.btnDblLogin.setOnClickListener {
            val email = binding.edtDblEmail.text.toString()
            val password = binding.edtDblPassword.text.toString()

            loginActivityVM.login(email, password, requireActivity())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding property when the view is destroyed
        _binding = null
    }
}