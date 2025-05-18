package com.example.chms_android.features_doctor.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.chms_android.R
import com.example.chms_android.data.User
import com.example.chms_android.databinding.FragmentDoctorProfileBinding
import com.example.chms_android.features_doctor.vm.DoctorProfileViewModel
import com.example.chms_android.features.mine.activity.UserEditActivity
import com.example.chms_android.ui.components.BadgeDrawable
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class DoctorProfileFragment : Fragment() {

    private var _binding: FragmentDoctorProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DoctorProfileViewModel
    private lateinit var accountUtil: AccountUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DoctorProfileViewModel::class.java)
        accountUtil = AccountUtil(requireContext())

        setupListeners()
        observeViewModel()
        
        // 加载用户信息
        viewModel.loadUserInfo(requireContext())
        // 加载统计数据
        viewModel.loadStatistics()
    }

    private fun updateUserInfo(user: User?) {
        if (user == null) {
            Toast.makeText(requireContext(), "用户信息加载失败", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 更新头像
        if (!user.avatar.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.avatar)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(binding.ivDoctorAvatar)
        } else {
            // 如果没有头像，加载默认头像
            Glide.with(this)
                .load(R.mipmap.avatar_doctor01)
                .into(binding.ivDoctorAvatar)
        }

        // 更新医生基本信息
        binding.tvDoctorName.text = user.name ?: "医生"
        binding.tvDoctorEmail.text = user.email ?: "doctor@example.com"
        binding.tvDoctorCommunity.text = user.community ?: "健康社区"
    }

    private fun setupListeners() {
        // 编辑个人资料
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireActivity(), UserEditActivity::class.java)
            startActivity(intent)
        }

        // 头像点击事件，跳转到个人资料编辑页面
        binding.ivDoctorAvatar.setOnClickListener {
            val intent = Intent(requireActivity(), UserEditActivity::class.java)
            startActivity(intent)
        }

        // 账号设置
        binding.llAccountSettings.setOnClickListener {
            ToastUtil.show(requireContext(), "账号设置功能待实现", Toast.LENGTH_SHORT)
        }

        // 消息通知
        binding.llNotificationSettings.setOnClickListener {
            ToastUtil.show(requireContext(), "消息通知功能待实现", Toast.LENGTH_SHORT)
        }

        // 隐私设置
        binding.llPrivacySettings.setOnClickListener {
            ToastUtil.show(requireContext(), "隐私设置功能待实现", Toast.LENGTH_SHORT)
        }

        // 关于应用
        binding.llAboutApp.setOnClickListener {
            ToastUtil.show(requireContext(), "关于应用功能待实现", Toast.LENGTH_SHORT)
        }

        // 退出登录
        binding.btnLogout.setOnClickListener {
            // 清除登录状态并跳转到登录页面
            accountUtil.logout(requireActivity())
        }
    }

    private fun observeViewModel() {
        // 观察用户数据变化
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            updateUserInfo(user)
        }
        
        // 观察统计数据变化
        viewModel.patientsCount.observe(viewLifecycleOwner) { count ->
            binding.tvPatientsCount.text = count.toString()
        }

        viewModel.appointmentsCount.observe(viewLifecycleOwner) { count ->
            binding.tvAppointmentsCount.text = count.toString()
        }

        viewModel.reportsCount.observe(viewLifecycleOwner) { count ->
            binding.tvReportsCount.text = count.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次恢复时刷新数据
        viewModel.loadStatistics()
        viewModel.loadUserInfo(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}