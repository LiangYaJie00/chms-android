package com.example.chms_android.features.mine

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.chms_android.R
import com.example.chms_android.databinding.FragmentMineBinding
import com.example.chms_android.features.health.activity.HealthInfoActivity
import com.example.chms_android.features.health.activity.HealthReportHistoryActivity
import com.example.chms_android.features.mine.activity.ReservationActivity
import com.example.chms_android.features.mine.activity.ReservationManageActivity
import com.example.chms_android.features.mine.activity.UserEditActivity
import com.example.chms_android.features.report.activity.DailyReportActivity
import com.example.chms_android.features.analysis.activity.ReportAnalysisShowActivity
import com.example.chms_android.features.appointment.activity.PatientAppointmentListActivity
import com.example.chms_android.features_doctor.activity.DoctorAdviceListActivity
import com.example.chms_android.login.activity.LoginActivity
import com.example.chms_android.utils.AccountUtil

class MineFragment : Fragment() {
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    
    // 添加ViewModel
    private lateinit var viewModel: MineViewModel
    private var offlineBanner: View? = null
    private var isBannerVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(MineViewModel::class.java)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 清除binding引用，避免内存泄漏
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMineBinding.inflate(inflater, container, false)

        // 设置观察者
        setupObservers()
        
        // 设置监听器
        setupListeners()

        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 在视图完全创建后检查离线模式
        viewModel.checkOfflineMode(requireContext())
    }
    
    override fun onResume() {
        super.onResume()
        // 每次页面可见时刷新用户信息
        viewModel.loadUserInfo(requireContext())
        
        // 在Fragment恢复时也检查离线模式
        viewModel.checkOfflineMode(requireContext())
    }
    
    // 设置观察者
    private fun setupObservers() {
        // 观察用户数据变化
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // 更新头像
                if (!user.avatar.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(user.avatar)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(binding.civMineAvatar)
                } else {
                    binding.civMineAvatar.setImageResource(R.drawable.default_avatar)
                }
                
                // 更新用户名和邮箱
                binding.tvMineName.text = user.name
                binding.tvMineEmail.text = user.email
                binding.tvMineCommunity.text = user.community ?: "未选择社区"
            }
        }
        
        // 观察离线模式状态
        viewModel.isOfflineMode.observe(viewLifecycleOwner) { isOffline ->
            Log.d("MineFragment", "Offline mode changed: $isOffline, isBannerVisible=$isBannerVisible")
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

    // 监听器设置
    private fun setupListeners() {
        // 个人资料编辑
        binding.ibMineEditUser.setOnClickListener {
            val intent = Intent(requireActivity(), UserEditActivity::class.java)
            startActivity(intent)
        }
        
        // 头像点击事件，跳转到个人资料编辑页面
        binding.civMineAvatar.setOnClickListener {
            val intent = Intent(requireActivity(), UserEditActivity::class.java)
            startActivity(intent)
        }

        // 健康档案
        binding.rcFmHealthRecord.setOnClickListener {
            val intent = Intent(requireActivity(), HealthInfoActivity::class.java)
            startActivity(intent)
        }

        // 健康日报
        binding.rcFmDailyReport.setOnClickListener {
            val intent = Intent(requireActivity(), HealthReportHistoryActivity::class.java)
            startActivity(intent)
        }

        // 数据统计
        binding.rcFmDataStatistics.setOnClickListener {
            val intent = Intent(requireActivity(), DailyReportActivity::class.java)
            intent.putExtra("status", 1) // 0表示日报，1表示月报
            startActivity(intent)
        }

        // 分析报告
        binding.rcFmReportManage.setOnClickListener {
            val intent = Intent(requireActivity(), ReportAnalysisShowActivity::class.java)
            startActivity(intent)
        }

        // 在线就诊
        binding.rcFmReservation.setOnClickListener {
            val intent = Intent(requireActivity(), ReservationActivity::class.java)
            startActivity(intent)
        }

        // 预约管理
        binding.rcFmReservationManage.setOnClickListener {
            val intent = Intent(requireActivity(), PatientAppointmentListActivity::class.java)
            startActivity(intent)
        }
        
        // 医生建议 - 新增条目
        binding.rcFmDoctorAdvice.setOnClickListener {
            val intent = Intent(requireActivity(), DoctorAdviceListActivity::class.java)
            startActivity(intent)
        }
        
        // 退出登录
        binding.btnMineLogout.setOnClickListener {
            viewModel.logout(requireContext())
        }
    }
    
    private fun showOfflineBanner() {
        try {
            Log.d("MineFragment", "Showing offline banner")
            
            // 创建离线模式横幅
            offlineBanner = layoutInflater.inflate(R.layout.offline_mode_banner, null)
            
            // 获取容器 - MineFragment使用CoordinatorLayout
            val scrollView = binding.root.findViewById<NestedScrollView>(R.id.nestedScrollView_mine)
            val linearLayout = scrollView?.getChildAt(0) as? LinearLayout
            
            if (linearLayout != null) {
                // 在LinearLayout中，添加到索引0的位置（最顶部）
                linearLayout.addView(offlineBanner, 0)
                
                // 设置重试按钮点击事件
                offlineBanner?.findViewById<Button>(R.id.btn_retry_connection)?.setOnClickListener {
                    viewModel.retryConnection(requireContext())
                }
                
                // 标记横幅已显示
                isBannerVisible = true
            } else {
                Log.e("MineFragment", "LinearLayout not found in NestedScrollView")
            }
        } catch (e: Exception) {
            Log.e("MineFragment", "Error showing offline banner: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun removeOfflineBanner() {
        try {
            Log.d("MineFragment", "Removing offline banner")
            
            if (offlineBanner != null) {
                val scrollView = binding.root.findViewById<NestedScrollView>(R.id.nestedScrollView_mine)
                val linearLayout = scrollView?.getChildAt(0) as? LinearLayout
                
                if (linearLayout != null) {
                    linearLayout.removeView(offlineBanner)
                    offlineBanner = null
                    
                    // 标记横幅已移除
                    isBannerVisible = false
                }
            }
        } catch (e: Exception) {
            Log.e("MineFragment", "Error removing offline banner: ${e.message}")
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
        fun newInstance(param1: String, param2: String) =
            MineFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}