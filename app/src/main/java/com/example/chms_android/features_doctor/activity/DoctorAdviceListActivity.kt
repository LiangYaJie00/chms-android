package com.example.chms_android.features_doctor.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.data.Role
import com.example.chms_android.databinding.ActivityDoctorAdviceListBinding
import com.example.chms_android.dto.DoctorAdviceDTO
import com.example.chms_android.features_doctor.adapter.DoctorAdviceAdapter
import com.example.chms_android.repo.DoctorAdviceRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class DoctorAdviceListActivity : AppCompatActivity(), DoctorAdviceAdapter.OnAdviceClickListener {
    private lateinit var binding: ActivityDoctorAdviceListBinding
    private lateinit var adviceAdapter: DoctorAdviceAdapter
    private val TAG = "DoctorAdviceList"
    
    // 用户ID，可以是医生ID或居民ID
    private var userId: Int = 0
    
    // 用户角色
    private var userRole: Role? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorAdviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 初始化用户信息
        initUserInfo()
        
        // 设置标题栏
        setupTitleBar()
        
        // 设置RecyclerView
        setupRecyclerView()
        
        // 设置下拉刷新
        setupSwipeRefresh()
        
        // 设置悬浮按钮
        setupFab()
        
        // 加载建议列表
        loadAdviceList()
    }
    
    private fun initUserInfo() {
        val accountUtil = AccountUtil(this)
        userId = accountUtil.getUserId().toInt()
        userRole = accountUtil.getUser()?.role
        
        // 如果传入了特定用户ID，则使用传入的ID
        val intentUserId = intent.getIntExtra("USER_ID", 0)
        if (intentUserId > 0) {
            userId = intentUserId
            // 如果是查看特定居民的建议，则标记为居民角色
            userRole = Role.consumer
        }
    }
    
    private fun setupTitleBar() {
        // 根据用户角色设置标题
        val titleText = when (userRole) {
            Role.doctor -> "我的医生建议"
            Role.consumer -> "我收到的建议"
            else -> "医生建议列表"
        }
        
        binding.titleBar.setTitleText(titleText)
    }
    
    private fun setupRecyclerView() {
        adviceAdapter = DoctorAdviceAdapter(this, mutableListOf(), this)
        binding.recyclerViewAdvice.apply {
            layoutManager = LinearLayoutManager(this@DoctorAdviceListActivity)
            adapter = adviceAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadAdviceList()
        }
        
        // 设置刷新指示器颜色
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.primary_dark,
            R.color.accent
        )
    }
    
    private fun setupFab() {
        // 只有医生才能创建新建议
        if (userRole == Role.doctor) {
            binding.fabAddAdvice.visibility = View.VISIBLE
            binding.fabAddAdvice.setOnClickListener {
                val intent = Intent(this, DoctorAdviceEditActivity::class.java)
                startActivity(intent)
            }
        } else {
            binding.fabAddAdvice.visibility = View.GONE
        }
    }
    
    private fun loadAdviceList() {
        // 显示加载中
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyView.visibility = View.GONE
        
        when (userRole) {
            Role.doctor -> {
                // 医生查看自己创建的建议
                loadDoctorAdvices()
            }
            Role.consumer -> {
                // 居民查看收到的建议
                loadResidentAdvices()
            }
            else -> {
                // 未知角色，显示错误
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyView.visibility = View.VISIBLE
                binding.tvEmptyView.text = "无法确定用户角色"
            }
        }
    }
    
    private fun loadDoctorAdvices() {
        DoctorAdviceRepo.getAdvicesByDoctorId(
            doctorId = AccountUtil(this).getDoctorId(),
            context = this,
            onSuccess = { advices ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                
                if (advices.isEmpty()) {
                    binding.tvEmptyView.visibility = View.VISIBLE
                    binding.tvEmptyView.text = "您还没有创建任何建议"
                } else {
                    binding.tvEmptyView.visibility = View.GONE
                    adviceAdapter.updateAdvices(advices)
                }
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                binding.tvEmptyView.visibility = View.VISIBLE
                binding.tvEmptyView.text = "加载失败: $errorMsg"
                
                Log.e(TAG, "Error loading doctor advices: $errorMsg")
            }
        )
    }
    
    private fun loadResidentAdvices() {
        DoctorAdviceRepo.getAdvicesByResidentId(
            residentId = userId,
            context = this,
            onSuccess = { advices ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                
                if (advices.isEmpty()) {
                    binding.tvEmptyView.visibility = View.VISIBLE
                    binding.tvEmptyView.text = "您还没有收到任何建议"
                } else {
                    binding.tvEmptyView.visibility = View.GONE
                    adviceAdapter.updateAdvices(advices)
                }
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                binding.tvEmptyView.visibility = View.VISIBLE
                binding.tvEmptyView.text = "加载失败: $errorMsg"
                
                Log.e(TAG, "Error loading resident advices: $errorMsg")
            }
        )
    }
    
    override fun onAdviceClick(advice: DoctorAdviceDTO) {
        // 点击建议项，跳转到详情页
        val intent = Intent(this, DoctorAdviceDetailActivity::class.java).apply {
            putExtra("ADVICE_ID", advice.adviceId)
        }
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // 每次恢复时刷新数据
        loadAdviceList()
    }
}