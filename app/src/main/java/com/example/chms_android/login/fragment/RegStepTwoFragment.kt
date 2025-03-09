package com.example.chms_android.login.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.data.Role
import com.example.chms_android.databinding.FragmentRegStepTwoBinding
import com.example.chms_android.login.vm.RegisterDialogVM
import com.example.chms_android.utils.ToastUtil

/**
 * A simple [Fragment] subclass.
 * Use the [RegStepTwoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegStepTwoFragment : Fragment() {
    private var _binding: FragmentRegStepTwoBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RegisterDialogVM
    private var role: Role = Role.consumer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment()).get(RegisterDialogVM::class.java)

        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRegStepTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSpinner()

        binding.tvDbrHaveAccount.setOnClickListener {
            viewModel.goToPreviousPage()
        }

        binding.btnDbrLogin.setOnClickListener {
            val name = binding.edtFrstName.text.toString()
            val phone = binding.edtFrstPhone.text.toString()
            viewModel.register(name, phone, role, requireActivity())
        }

    }

    private fun handleSpinner() {
        // 定义 Role 到中文文案的映射
        val fullRoleMap = mapOf(
            Role.consumer to "用户",
            Role.doctor to "医生",
            Role.manager to "管理者"
        )

        // 过滤掉不需要的项
        val filteredRoleMap = fullRoleMap.filterKeys { it != Role.manager }

        // 获取所有中文文案
        val roleNames = filteredRoleMap.values.toList()

        // 创建 ArrayAdapter 适配器
        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, // 标准的 Spinner 项布局
            roleNames
        )

        // 为下拉菜单指定布局
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 将适配器应用到 Spinner 上
        binding.spinnerFrst.adapter = adapter

        // 设置 Spinner 的选择事件监听器
        binding.spinnerFrst.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 根据选择位置获取对应的 Role
                role = filteredRoleMap.keys.toList()[position]
                val selectedRole = filteredRoleMap.keys.toList()[position]
//                filteredRoleMap[selectedRole]
                ToastUtil.show(requireActivity(), "选择的是：${selectedRole.name}")
                // 在这里做更多关于 Role 的逻辑处理
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 当没有选中任何项时触发
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegStepTwoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                RegStepTwoFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}