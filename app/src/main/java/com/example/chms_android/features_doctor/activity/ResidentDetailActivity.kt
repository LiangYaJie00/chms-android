package com.example.chms_android.features_doctor.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.chms_android.R
import com.example.chms_android.data.User
import com.example.chms_android.databinding.ActivityResidentDetailBinding
import com.example.chms_android.features.analysis.activity.ReportAnalysisShowActivity
import com.example.chms_android.features.health.activity.HealthInfoActivity
import com.example.chms_android.features.health.activity.HealthReportHistoryActivity
import com.example.chms_android.features.health.activity.UserHealthInfoActivity
import com.example.chms_android.features.mine.activity.ReservationManageActivity
import com.example.chms_android.features.report.activity.DailyReportActivity
import com.example.chms_android.features_doctor.activity.DoctorAdviceEditActivity
import com.example.chms_android.utils.ToastUtil

class ResidentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResidentDetailBinding
    private var resident: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏颜色为primary
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        
        // 如果状态栏文字需要暗色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        
        // 获取传递的居民数据
        resident = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("resident", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("resident") as? User
        }
        
        if (resident == null) {
            ToastUtil.show(this, "获取居民信息失败", Toast.LENGTH_SHORT)
            finish()
            return
        }

        // 初始化界面
        initView()
        setupListeners()
    }

    private fun initView() {
        // 设置居民基本信息
        binding.tvResidentName.text = resident?.name
        
        // 性别处理 - 没有值时不展示
        if (resident?.gender != null) {
            binding.tvResidentGender.text = if (resident?.gender == 1) "男" else if (resident?.gender == 0) "女" else "未知"
        } else {
            binding.tvResidentGender.visibility = android.view.View.GONE
        }
        
        // 年龄处理 - 没有值时不展示
        if (resident?.age != null) {
            binding.tvResidentAge.text = "${resident?.age}岁"
        } else {
            binding.tvResidentAge.visibility = android.view.View.GONE
        }
        
        binding.tvResidentPhone.text = resident?.phone.toString()
        binding.tvResidentEmail.text = resident?.email ?: "未设置"
        binding.tvResidentCommunity.text = resident?.community

        // 加载头像
        if (resident?.avatar != null) {
            Glide.with(this)
                .load(resident?.avatar)
                .transform(CircleCrop())
                .into(binding.ivResidentAvatar)
        } else {
            Glide.with(this)
                .load(R.mipmap.avatar_default)
                .transform(CircleCrop())
                .into(binding.ivResidentAvatar)
        }
    }

    private fun setupListeners() {
        // 健康建议
        binding.cvHealthAdvice.setOnClickListener {
            val intent = Intent(this, DoctorAdviceEditActivity::class.java).apply {
                putExtra("resident", resident)
            }
            startActivity(intent)
        }
        
        // 健康档案
        binding.cvHealthRecord.setOnClickListener {
            // 使用UserHealthInfoActivity.start静态方法启动活动
            resident?.userId?.let { userId ->
                UserHealthInfoActivity.start(this, userId)
            } ?: run {
                ToastUtil.show(this, "用户ID不存在", Toast.LENGTH_SHORT)
            }
        }

        // 健康日报
        binding.cvDailyReport.setOnClickListener {
            val intent = Intent(this, HealthReportHistoryActivity::class.java).apply {
                putExtra("userId", resident?.userId)
            }
            startActivity(intent)
        }

        // 数据统计
        binding.cvDataStatistics.setOnClickListener {
            val intent = Intent(this, DailyReportActivity::class.java).apply {
                putExtra("userId", resident?.userId)
                putExtra("status", 0) // 0表示日报，1表示月报
            }
            startActivity(intent)
        }

        // 分析报告
        binding.cvReportAnalysis.setOnClickListener {
            val intent = Intent(this, ReportAnalysisShowActivity::class.java).apply {
                putExtra("userId", resident?.userId)
            }
            startActivity(intent)
        }

        // 预约管理
        binding.cvReservationManage.setOnClickListener {
            val intent = Intent(this, ReservationManageActivity::class.java).apply {
                putExtra("userId", resident?.userId)
            }
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
