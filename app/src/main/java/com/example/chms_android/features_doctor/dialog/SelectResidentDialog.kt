package com.example.chms_android.features_doctor.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.data.User
import com.example.chms_android.databinding.DialogSelectResidentBinding
import com.example.chms_android.features_doctor.adapter.ResidentAdapter
import com.example.chms_android.repo.UserRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

/**
 * 选择居民对话框
 * 用于从居民列表中选择一个居民
 */
class SelectResidentDialog(context: Context) : Dialog(context), ResidentAdapter.OnResidentClickListener {
    
    private lateinit var binding: DialogSelectResidentBinding
    private lateinit var residentAdapter: ResidentAdapter
    private var onResidentSelectedListener: ((User) -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        binding = DialogSelectResidentBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // 设置对话框宽度
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.8).toInt()
        )
        
        setupRecyclerView()
        setupSearchView()
        
        // 设置关闭按钮
        binding.btnClose.setOnClickListener {
            dismiss()
        }
        
        // 加载居民数据
        loadResidents()
    }
    
    private fun setupRecyclerView() {
        residentAdapter = ResidentAdapter(context, emptyList(), this)
        binding.recyclerViewResidents.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = residentAdapter
        }
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                residentAdapter.filter.filter(newText)
                return true
            }
        })
    }
    
    private fun loadResidents() {
        // 显示加载进度条
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyView.visibility = View.GONE
        
        // 获取当前医生的社区
        val currentUser = AccountUtil(context).getUser()
        val community = currentUser?.community
        
        if (community != null) {
            UserRepo.getUsersByRoleAndCommunity(
                role = "consumer",
                community = community,
                context = context,
                onSuccess = { residents ->
                    binding.progressBar.visibility = View.GONE
                    
                    if (residents.isEmpty()) {
                        binding.tvEmptyView.visibility = View.VISIBLE
                    } else {
                        residentAdapter.updateResidents(residents)
                    }
                },
                onError = { errorMsg ->
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyView.visibility = View.VISIBLE
                    ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
                }
            )
        } else {
            binding.progressBar.visibility = View.GONE
            binding.tvEmptyView.visibility = View.VISIBLE
            ToastUtil.show(context, "无法获取医生所属社区信息", Toast.LENGTH_SHORT)
        }
    }
    
    /**
     * 设置居民选择监听器
     */
    fun setOnResidentSelectedListener(listener: (User) -> Unit) {
        onResidentSelectedListener = listener
    }
    
    override fun onResidentClick(resident: User) {
        // 当用户点击居民时，调用监听器并关闭对话框
        onResidentSelectedListener?.invoke(resident)
        dismiss()
    }
    
    // 实现ResidentAdapter.OnResidentClickListener接口的其他方法
    override fun onViewHealthRecord(resident: User) {
        // 在选择对话框中不需要这些功能
    }
    
    override fun onViewAppointments(resident: User) {
        // 在选择对话框中不需要这些功能
    }
    
    override fun onViewHealthReports(resident: User) {
        // 在选择对话框中不需要这些功能
    }
}