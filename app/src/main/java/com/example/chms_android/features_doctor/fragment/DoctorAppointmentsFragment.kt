package com.example.chms_android.features_doctor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.databinding.FragmentDoctorAppointmentsBinding
import com.example.chms_android.features_doctor.adapter.AppointmentPagerAdapter
import com.example.chms_android.features_doctor.vm.DoctorAppointmentsViewModel
import com.example.chms_android.utils.ToastUtil
import com.google.android.material.tabs.TabLayoutMediator

class DoctorAppointmentsFragment : Fragment() {

    private var _binding: FragmentDoctorAppointmentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DoctorAppointmentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DoctorAppointmentsViewModel::class.java)

        setupViewPager()
        setupFab()
    }

    private fun setupViewPager() {
        // 创建适配器
        val pagerAdapter = AppointmentPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // 将TabLayout与ViewPager2关联
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "全部"
                1 -> "待确认"
                2 -> "已确认"
                3 -> "已完成"
                4 -> "已取消"
                else -> "全部"
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabAddAppointment.setOnClickListener {
            // 打开添加预约界面
            ToastUtil.show(requireContext(), "添加预约功能待实现", Toast.LENGTH_SHORT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}