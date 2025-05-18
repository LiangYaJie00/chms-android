package com.example.chms_android.features.analysis.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityReportAnalysisShowBinding
import com.example.chms_android.features.analysis.adapter.ReportAnalysisAdapter
import com.example.chms_android.features.analysis.viewmodel.ReportAnalysisShowViewModel
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class ReportAnalysisShowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportAnalysisShowBinding
    private lateinit var viewModel: ReportAnalysisShowViewModel
    private lateinit var adapter: ReportAnalysisAdapter
    private lateinit var loadingDialog: LoadingDialog
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportAnalysisShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(ReportAnalysisShowViewModel::class.java)
        
        // 初始化加载对话框
        loadingDialog = LoadingDialog(this)
        loadingDialog.setMessage("正在加载分析报告...")
        
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
        adapter = ReportAnalysisAdapter(this, emptyList())
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
        // 观察分析报告列表
        viewModel.reportAnalysisList.observe(this) { reports ->
            adapter.updateData(reports)
            
            // 如果列表为空，显示空视图
            if (reports.isEmpty()) {
                binding.tvEmptyView.visibility = View.VISIBLE
            } else {
                binding.tvEmptyView.visibility = View.GONE
            }
        }
        
        // 观察加载状态
        viewModel.loadStatus.observe(this) { status ->
            when (status) {
                is ReportAnalysisShowViewModel.LoadStatus.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        loadingDialog.show()
                    }
                }
                is ReportAnalysisShowViewModel.LoadStatus.Success -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                is ReportAnalysisShowViewModel.LoadStatus.Error -> {
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
        
        // 加载用户的所有分析报告
        viewModel.loadUserReportAnalysis(userId, this)
    }
}