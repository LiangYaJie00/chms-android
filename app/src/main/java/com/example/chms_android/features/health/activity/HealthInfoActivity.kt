package com.example.chms_android.features.health.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityHealthInfoBinding
import com.example.chms_android.features.health.adapter.HealthInfoPagerAdapter
import com.example.chms_android.utils.AndroidBug5497Workaround
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HealthInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHealthInfoBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 应用键盘遮挡修复
        AndroidBug5497Workaround.assistActivity(this)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupViewPager()
    }
    
    private fun setupViewPager() {
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout
        
        // 设置ViewPager适配器
        val pagerAdapter = HealthInfoPagerAdapter(this)
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
}