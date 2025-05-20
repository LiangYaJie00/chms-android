package com.example.chms_android.features_doctor

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityDoctorMainBinding
import com.example.chms_android.features_doctor.vm.DoctorMainActivityVM
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.NotificationPermissionUtil

/**
 * 医生主界面Activity
 * 负责医生角色的主要功能导航和界面展示
 */
class DoctorMainActivity : AppCompatActivity() {
    // 视图绑定
    private lateinit var binding: ActivityDoctorMainBinding
    
    // ViewModel
    private lateinit var doctorMainActivityVM: DoctorMainActivityVM
    
    // SharedPreferences
    private lateinit var sp: SharedPreferences
    
    // 离线模式标志
    private var isOfflineMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化视图
        initView()
        
        // 初始化导航
        initNavView()
        
        // 初始化数据
        initData()
        
        // 初始化监听器
        initListeners()
        
        // 观察ViewModel数据变化
        observeViewModel()
        
        // 检查并请求通知权限
        checkNotificationPermission()
    }
    
    /**
     * 检查并请求通知权限
     */
    private fun checkNotificationPermission() {
        NotificationPermissionUtil.requestPermissionInActivity(
            activity = this,
            onGranted = {
                Log.d("DoctorMainActivity", "通知权限已授予")
                // 可以在这里执行需要通知权限的操作，如初始化推送服务
            },
            onDenied = {
                Log.d("DoctorMainActivity", "用户拒绝了通知权限")
                // 可以在这里处理用户拒绝权限的情况
            }
        )
    }
    
    /**
     * 初始化视图
     */
    private fun initView() {
        // 初始化 View Binding
        binding = ActivityDoctorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 安全地隐藏 ActionBar
        supportActionBar?.hide()
    }
    
    /**
     * 初始化数据
     */
    private fun initData() {
        // 检查是否处于离线模式
        isOfflineMode = intent.getBooleanExtra("OFFLINE_MODE", false)
        
        // 初始化SharedPreferences
        sp = getPreferences(Context.MODE_PRIVATE)
        
        // 初始化ViewModel
        doctorMainActivityVM = ViewModelProvider(this).get(DoctorMainActivityVM::class.java)
        
        // 加载医生信息
        loadDoctorInfo()
        
        Log.d("DoctorMainActivity", "activity onCreate")
    }
    
    /**
     * 加载医生信息
     */
    private fun loadDoctorInfo() {
        val accountUtil = AccountUtil(this)
        doctorMainActivityVM.loadDoctorInfo(accountUtil)
    }

    /**
     * 初始化导航视图
     */
    private fun initNavView() {
        val navController = findNavController(R.id.fragment_doctor_main)
        
        // 设置底部导航栏与导航控制器的关联
        binding.navDoctorMainView.setupWithNavController(navController)
        
        // 自定义底部导航栏的点击事件处理
        binding.navDoctorMainView.setOnItemSelectedListener { item ->
            // 创建导航选项，清除回退栈
            val navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)  // 确保目标Fragment只有一个实例
                .setPopUpTo(navController.graph.startDestinationId, false)  // 弹出到起始目的地
                .build()
                
            // 根据选中的菜单项进行导航
            when (item.itemId) {
                R.id.nav_doctor_home -> {
                    navController.navigate(R.id.nav_doctor_home, null, navOptions)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_doctor_patients -> {
                    navController.navigate(R.id.nav_doctor_patients, null, navOptions)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_doctor_appointments -> {
                    navController.navigate(R.id.nav_doctor_appointments, null, navOptions)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_doctor_profile -> {
                    navController.navigate(R.id.nav_doctor_profile, null, navOptions)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    /**
     * 观察ViewModel中的数据变化
     */
    private fun observeViewModel() {
        // 观察医生姓名变化
        doctorMainActivityVM.doctorName.observe(this) { name ->
            // 更新UI
        }
        
        // 观察医生社区变化
        doctorMainActivityVM.doctorCommunity.observe(this) { community ->
            // 更新UI
        }
    }

    /**
     * 初始化各种事件监听器
     */
    private fun initListeners() {
        // 可以在这里添加其他UI元素的点击监听器
    }
    
    /**
     * 提供离线模式状态检查方法
     * @return 是否处于离线模式
     */
    fun isInOfflineMode(): Boolean {
        return isOfflineMode
    }
}