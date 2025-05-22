package com.example.chms_android.features_doctor.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.chms_android.R
import com.example.chms_android.data.User
import com.example.chms_android.databinding.FragmentDoctorHomeBinding
import com.example.chms_android.features_doctor.activity.DoctorAdviceListActivity
import com.example.chms_android.features_doctor.activity.DoctorAppointmentListActivity
import com.example.chms_android.features_doctor.vm.DoctorHomeViewModel
import com.example.chms_android.utils.AccountUtil

class DoctorHomeFragment : Fragment() {
    private var _binding: FragmentDoctorHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DoctorHomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(DoctorHomeViewModel::class.java)

        initView()
        setupObservers()

        return binding.root
    }

    private fun initView() {
        val user = AccountUtil(requireContext()).getUser()
        updateUserInfo(user)

        // 初始化今日工作统计
        binding.tvTodayAppointments.text = "0"
        binding.tvTodayPatients.text = "0"
        binding.tvPendingReports.text = "0"

        // 设置点击监听器
        setupListeners()
        
        // 加载用户信息到ViewModel
        viewModel.loadUserInfo(requireContext())
    }

    private fun updateUserInfo(user: User?) {
        binding.tvDoctorWelcome.text = "欢迎，${(user?.name + "医生") ?: "医生"}"
        binding.tvDoctorCommunity.text = "所属社区：${user?.community ?: "未分配社区"}"
    }

    private fun setupObservers() {
        // 观察工作统计数据
        viewModel.todayAppointments.observe(viewLifecycleOwner) { count ->
            binding.tvTodayAppointments.text = count.toString()
        }

        viewModel.todayPatients.observe(viewLifecycleOwner) { count ->
            binding.tvTodayPatients.text = count.toString()
        }

        viewModel.pendingReports.observe(viewLifecycleOwner) { count ->
            binding.tvPendingReports.text = count.toString()
        }

        // 观察用户数据变化
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            updateUserInfo(user)
        }
    }

    private fun setupListeners() {
        // 卡片点击监听
        binding.cardAppointments.setOnClickListener {
            // 导航到预约管理
            navigateToFragment(R.id.nav_doctor_appointments)
        }

        binding.cardPatients.setOnClickListener {
            // 导航到患者管理
            navigateToFragment(R.id.nav_doctor_patients)
        }

        binding.cardReports.setOnClickListener {
            // 导航到报告审核
            // 这里可能需要创建一个新的报告审核Fragment
        }

        // 快捷功能点击监听
        binding.llPatientManagement.setOnClickListener {
            navigateToFragment(R.id.nav_doctor_patients)
        }

        binding.llAppointmentManagement.setOnClickListener {
//            navigateToFragment(R.id.nav_doctor_appointments)
            // 跳转到预约列表Activity
            val intent = Intent(requireContext(), DoctorAppointmentListActivity::class.java)
            startActivity(intent)
        }

        // 将报告审核改为健康建议管理
        binding.llHealthAdvice.setOnClickListener {
            // 跳转到健康建议列表页面
            val intent = Intent(requireContext(), DoctorAdviceListActivity::class.java)
            startActivity(intent)
        }

        // 健康数据点击监听
        binding.llCommunityHealth.setOnClickListener {
            // 导航到社区健康统计页面
        }

        binding.llHealthMonitoring.setOnClickListener {
            // 导航到健康监测数据页面
        }
    }

    private fun navigateToFragment(destinationId: Int) {
        // 使用导航选项，清除导航栈并防止目的地重复添加
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_doctor_home, false)  // 弹出到首页，但不包括首页本身
            .build()
        
        findNavController().navigate(destinationId, null, navOptions)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}