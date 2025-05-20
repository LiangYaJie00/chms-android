package com.example.chms_android.features_doctor.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityDoctorProfileEditBinding
import com.example.chms_android.features_doctor.adapter.DoctorProfilePagerAdapter
import com.example.chms_android.features_doctor.vm.DoctorProfileEditViewModel
import com.example.chms_android.utils.AndroidBug5497Workaround
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.util.Log

class DoctorProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorProfileEditBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewModel: DoctorProfileEditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDoctorProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(DoctorProfileEditViewModel::class.java)
        
        // 添加日志，打印 ViewModel 实例的哈希码，用于调试
        Log.d("DoctorProfileEdit", "Activity ViewModel instance: ${viewModel.hashCode()}")
        
        // 应用键盘遮挡修复
        AndroidBug5497Workaround.assistActivity(this)
        
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 设置窗口插入监听器
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 获取传递的医生ID
        val doctorId = intent.getIntExtra("DOCTOR_ID", -1)
        val userId = intent.getIntExtra("USER_ID", -1)
        
        // 加载医生数据
        if (doctorId > 0) {
            Log.d("DoctorProfileEdit", "Loading doctor by doctorId: $doctorId")
            viewModel.loadDoctorById(doctorId, this)
        } else if (userId > 0) {
            Log.d("DoctorProfileEdit", "Loading doctor by userId: $userId")
            viewModel.loadDoctorByUserId(userId, this)
        }
        
        // 设置ViewPager
        setupViewPager()
        
        // 观察ViewModel中的数据变化
        observeViewModel()
    }
    
    private fun setupViewPager() {
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout
        
        // 设置ViewPager适配器
        val pagerAdapter = DoctorProfilePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        // 将TabLayout与ViewPager2关联
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "预览"
                1 -> "编辑"
                else -> ""
            }
        }.attach()
    }
    
    private fun observeViewModel() {
        // 观察保存状态
        viewModel.saveStatus.observe(this) { success ->
            if (success) {
                // 保存成功，可以显示提示或返回上一页
                finish()
            }
        }
    }
}