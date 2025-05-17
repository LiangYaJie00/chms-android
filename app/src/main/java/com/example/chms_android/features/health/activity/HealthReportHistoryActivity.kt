package com.example.chms_android.features.health.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityHealthReportHistoryBinding
import com.example.chms_android.features.health.adapter.HealthReportAdapter
import com.example.chms_android.features.health.viewmodel.HealthReportHistoryViewModel
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class HealthReportHistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHealthReportHistoryBinding
    private lateinit var viewModel: HealthReportHistoryViewModel
    private lateinit var adapter: HealthReportAdapter
    private lateinit var loadingDialog: LoadingDialog
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthReportHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 设置窗口插入监听器
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(HealthReportHistoryViewModel::class.java)
        
        // 初始化加载对话框
        loadingDialog = LoadingDialog(this)
        
        // 初始化RecyclerView
        setupRecyclerView()
        
        // 设置下拉刷新
        setupSwipeRefresh()
        
        // 设置观察者
        setupObservers()
        
        // 加载数据
        loadData()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        adapter = HealthReportAdapter(this, emptyList())
        binding.recyclerViewReports.adapter = adapter
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
        
        // 设置刷新颜色
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.accent,
            R.color.primary_dark
        )
    }
    
    private fun setupObservers() {
        // 观察健康日报列表
        viewModel.healthReports.observe(this) { reports ->
            adapter.updateData(reports)
            
            // 如果列表为空，显示空视图
            if (reports.isEmpty()) {
                // 这里可以添加一个空视图
                ToastUtil.show(this, "暂无健康日报记录")
            }
        }
        
        // 观察加载状态
        viewModel.loadStatus.observe(this) { status ->
            when (status) {
                is HealthReportHistoryViewModel.LoadStatus.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        loadingDialog.show()
                    }
                }
                is HealthReportHistoryViewModel.LoadStatus.Success -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                is HealthReportHistoryViewModel.LoadStatus.Error -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                    ToastUtil.show(this, status.message)
                }
            }
        }
    }
    
    private fun loadData() {
        // 获取当前用户ID
        val userId = AccountUtil(this).getUserId().toInt()
        
        // 加载用户的所有健康日报
        viewModel.loadUserHealthReports(userId, this)
    }
}