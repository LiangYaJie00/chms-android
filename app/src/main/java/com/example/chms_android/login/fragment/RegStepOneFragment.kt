package com.example.chms_android.login.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.databinding.FragmentRegStepOneBinding
import com.example.chms_android.login.dialog.LoginBottomDialog
import com.example.chms_android.login.vm.RegisterDialogVM
import com.example.chms_android.ui.components.OnVerificationButtonClickListener
import com.example.chms_android.ui.components.VerificationCodeButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A simple [Fragment] subclass.
 * Use the [RegStepOneFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegStepOneFragment : Fragment() {
    private var _binding: FragmentRegStepOneBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RegisterDialogVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment()).get(RegisterDialogVM::class.java)

        // 参数接收
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFrsoSendCode.setOnVerificationButtonClickListener(object : OnVerificationButtonClickListener {
            override fun shouldStartCountDown(view: VerificationCodeButton): Boolean {
                val email = binding.edtFrsoEmail.text.toString()
                return viewModel.getCode(email, requireActivity())
            }
        })

        binding.tvFrsoHaveAccount.setOnClickListener {
            closeDialog()
            val loginBottomDialog = LoginBottomDialog()
            loginBottomDialog.show(requireActivity().supportFragmentManager, "LoginBottomDialog")
        }

        binding.btnFrsoNextStep.setOnClickListener {
            val email = binding.edtFrsoEmail.text.toString()
            val pwd = binding.edtFrsoPassword.text.toString()
            val pwdConfirm = binding.edtFrsoPasswordConfirm.text.toString()
            val code = binding.edtFrsoCode.text.toString()
            // 跳转到下一步页面
            viewModel.goToNextPage(email, pwd, pwdConfirm, code, requireActivity())
        }
    }

    // 关闭其所属的弹窗
    private fun closeDialog() {
        val fragment = activity?.supportFragmentManager?.findFragmentByTag("RegisterBottomDialog") as? BottomSheetDialogFragment
        fragment?.dismiss()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegStepOneFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic // 传递参数
        fun newInstance() =
            RegStepOneFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}