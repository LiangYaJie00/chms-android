package com.example.chms_android.features.health.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityDailyHealthReportBinding
import com.example.chms_android.features.health.adapter.DailyHealthReportPagerAdapter
import com.example.chms_android.features.health.viewmodel.DailyHealthReportViewModel
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 健康日报活动
 * 
 * 用于显示和编辑用户的健康日报信息
 * 支持三种模式：
 * 1. 从上报入口进入且未提交过报告：只显示编辑页面
 * 2. 从上报入口进入且已提交过报告，或者是当天的报告条目：显示预览和编辑页面
 * 3. 从非当天的报告条目进入：只显示预览页面
 */
class DailyHealthReportActivity : AppCompatActivity() {
    // UI组件
    private lateinit var binding: ActivityDailyHealthReportBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    
    // ViewModel
    private lateinit var viewModel: DailyHealthReportViewModel
    
    // 加载对话框
    private lateinit var loadingDialog: LoadingDialog

    // 状态变量
    private var reportId: Int = 0         // 报告ID，可以传入报告ID来加载特定报告
    private var entryMode: Int = 0        // 进入模式：0=从上报入口进入，1=从报告条目进入
    private var isTodayReport: Boolean = false  // 是否是当天的报告

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        getIntentData()
        initViewModel()
        initLoadingDialog()
        checkTodayReport()
    }

    /**
     * 初始化UI组件
     */
    private fun initUI() {
        enableEdgeToEdge()
        binding = ActivityDailyHealthReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        // 设置窗口插入监听器
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * 初始化加载对话框
     */
    private fun initLoadingDialog() {
        loadingDialog = LoadingDialog(this)
    }

    /**
     * 获取Intent传递的数据
     */
    private fun getIntentData() {
        reportId = intent.getIntExtra("reportId", 0)
        entryMode = intent.getIntExtra("entryMode", 0)
        isTodayReport = intent.getBooleanExtra("isTodayReport", false)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(DailyHealthReportViewModel::class.java)
        
        // 设置观察者
        setupObservers()
    }
    
    /**
     * 设置观察者
     */
    private fun setupObservers() {
        // 观察报告是否存在
        viewModel.hasTodayReport.observe(this) { exists ->
            if (exists) {
                // 如果已有报告，获取当天的报告
                val userId = AccountUtil(this).getUserId().toInt()
                val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                viewModel.fetchTodayReport(userId, todayDateStr, this)
            } else {
                // 如果没有报告，创建一个新的空报告
                if (loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }
                // 设置为未提交状态
                viewModel.setReportSubmitted(false)
                // 设置界面
                setupViewPager()
            }
        }
        
        // 观察加载状态
        viewModel.loadStatus.observe(this) { status ->
            when (status) {
                is DailyHealthReportViewModel.LoadStatus.Success -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    updateUIWithLatestReport()
                }
                is DailyHealthReportViewModel.LoadStatus.Error -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    setupViewPager()
                    ToastUtil.show(this, "获取报告失败: ${status.message}")
                }
                else -> { /* 加载中状态，不做处理 */ }
            }
        }
        
        // 观察健康日报数据变化
        viewModel.dailyHealthReport.observe(this) { report ->
            if (report != null && report.reportId > 0 && entryMode == 0) {
                // 更新reportId为当天报告的ID
                reportId = report.reportId
                // 设置为当天报告
                isTodayReport = true
            }
        }
    }

    /**
     * 检查当天是否已有报告
     */
    private fun checkTodayReport() {
        // 显示加载对话框
        loadingDialog.show()

        // 如果是从报告条目进入，直接加载指定的报告
        if (entryMode == 1 && reportId > 0) {
            viewModel.loadDailyHealthReport(this, reportId)
            return
        }

        // 获取当前用户ID
        val userId = AccountUtil(this).getUserId().toInt()
        
        // 检查当天是否已有报告
        viewModel.checkTodayReport(userId, this)
    }

    /**
     * 使用最新报告更新UI
     */
    private fun updateUIWithLatestReport() {
        // 设置界面
        setupViewPager()
    }

    /**
     * 设置ViewPager和TabLayout
     */
    private fun setupViewPager() {
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        when {
            // 情况1：从上报入口进入且未提交过报告
            entryMode == 0 && !viewModel.isReportSubmitted.value!! -> {
                setupEditOnlyMode()
            }

            // 情况2：从上报入口进入且已提交过报告，或者是当天的报告条目
            (entryMode == 0 && viewModel.isReportSubmitted.value!!) || isTodayReport -> {
                setupEditAndViewMode()
            }

            // 情况3：从非当天的报告条目进入
            else -> {
                setupViewOnlyMode()
            }
        }
        
        // 在这里加载健康日报数据，而不是在Fragment中
        if (reportId > 0) {
            viewModel.loadDailyHealthReport(this, reportId)
        }
    }

    /**
     * 设置仅编辑模式
     */
    private fun setupEditOnlyMode() {
        // 隐藏TabLayout，因为只能编辑不能预览
        tabLayout.visibility = View.GONE

        // 设置ViewPager适配器，只有编辑页面
        val pagerAdapter = DailyHealthReportPagerAdapter(this, reportId, showEditOnly = true)
        viewPager.adapter = pagerAdapter

        // 直接显示编辑页面
        viewPager.currentItem = 0

        // 修改标题
        binding.titleBar.setTitleText("填写健康日报")
    }

    /**
     * 设置编辑和预览模式
     */
    private fun setupEditAndViewMode() {
        // 显示TabLayout，可以在预览和编辑之间切换
        tabLayout.visibility = View.VISIBLE

        // 设置ViewPager适配器，显示预览和编辑页面
        val pagerAdapter = DailyHealthReportPagerAdapter(this, reportId)
        viewPager.adapter = pagerAdapter

        // 将TabLayout与ViewPager2关联
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "预览"
                1 -> "编辑"
                else -> ""
            }
        }.attach()

        // 默认显示预览页面
        viewPager.currentItem = 0

        // 修改标题
        binding.titleBar.setTitleText("健康日报")
    }

    /**
     * 设置仅预览模式
     */
    private fun setupViewOnlyMode() {
        // 隐藏TabLayout，因为只能预览不能编辑
        tabLayout.visibility = View.GONE

        // 设置ViewPager适配器，只有预览页面
        val pagerAdapter = DailyHealthReportPagerAdapter(this, reportId, showViewOnly = true)
        viewPager.adapter = pagerAdapter

        // 直接显示预览页面
        viewPager.currentItem = 0

        // 修改标题
        binding.titleBar.setTitleText("历史健康日报")
    }
}