package com.example.chms_android.features_doctor.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chms_android.R
import com.example.chms_android.common.enums.AdviceStatus
import com.example.chms_android.common.enums.AdviceType
import com.example.chms_android.databinding.ActivityDoctorAdviceDetailBinding
import com.example.chms_android.dto.DoctorAdviceDTO
import com.example.chms_android.repo.DoctorAdviceRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.DateUtil
import com.example.chms_android.utils.ToastUtil

class DoctorAdviceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorAdviceDetailBinding
    private val TAG = "DoctorAdviceDetail"
    
    private var adviceId: Int = 0
    private var currentAdvice: DoctorAdviceDTO? = null
    
    // 请求码
    private val REQUEST_EDIT_ADVICE = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorAdviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 获取建议ID
        adviceId = intent.getIntExtra("ADVICE_ID", 0)
        if (adviceId <= 0) {
            ToastUtil.show(this, "无效的建议ID")
            finish()
            return
        }
        
        // 设置按钮点击事件
        setupButtons()
        
        // 加载建议数据
        loadAdviceData()
    }
    
    private fun setupButtons() {
        // 编辑按钮
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, DoctorAdviceEditActivity::class.java).apply {
                putExtra("ADVICE_ID", adviceId)
            }
            startActivityForResult(intent, REQUEST_EDIT_ADVICE)
        }
    }
    
    private fun loadAdviceData() {
        DoctorAdviceRepo.getAdviceById(
            adviceId = adviceId,
            context = this,
            onSuccess = { adviceDTO ->
                currentAdvice = adviceDTO
                updateUI(adviceDTO)
                
                // 检查是否是居民用户查看，如果是且状态为未读，则自动更新为已读
                checkAndUpdateStatusForResident(adviceDTO)
            },
            onError = { errorMsg ->
                ToastUtil.show(this, "加载建议数据失败: $errorMsg")
                Log.e(TAG, "Error loading advice: $errorMsg")
            }
        )
    }
    
    private fun checkAndUpdateStatusForResident(advice: DoctorAdviceDTO) {
        val currentUserId = AccountUtil(this).getUser()?.userId ?: 0
        val isDoctorCreator = advice.doctorId == AccountUtil(this).getDoctorId()
        
        // 如果不是医生创建者，而是居民，且建议状态是未读，自动更新为已读
        if (!isDoctorCreator && currentUserId == advice.residentId && advice.status == AdviceStatus.UNREAD.code) {
            updateAdviceStatus(AdviceStatus.READ.code)
        }
    }
    
    private fun updateUI(advice: DoctorAdviceDTO) {
        // 设置标题
        binding.tvTitle.text = advice.title
        
        // 设置内容
        binding.tvContent.text = advice.content
        
        // 设置医生和居民信息
        binding.tvDoctorName.text = "医生：${advice.doctorName ?: "未知"}"
        binding.tvResidentName.text = "居民：${advice.residentName ?: "未知"}"
        
        // 设置日期
        val dateStr = advice.createdAt?.let { DateUtil.formatTimestamp(it) } ?: "未知时间"
        binding.tvDate.text = dateStr
        
        // 设置类型
        val typeStr = advice.adviceType?.let { AdviceType.getNameByCode(it) } ?: "未知类型"
        binding.tvType.text = "类型：$typeStr"
        
        // 设置状态
        val statusStr = advice.status?.let { AdviceStatus.getNameByCode(it) } ?: "未知状态"
        binding.tvStatus.text = statusStr
        
        // 设置状态标签的背景颜色
        when (advice.status) {
            AdviceStatus.UNREAD.code -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_unread)
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_unread_text))
            }
            AdviceStatus.READ.code -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_read)
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_read_text))
            }
            else -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_tag)
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.primary))
            }
        }
        
        // 设置重要标记
        if (advice.isImportant == 1) {
            binding.tvImportant.visibility = View.VISIBLE
        } else {
            binding.tvImportant.visibility = View.GONE
        }
        
        // 根据用户角色调整UI
        adjustUIByRole(advice)
    }
    
    private fun adjustUIByRole(advice: DoctorAdviceDTO) {
        val isDoctorCreator = advice.doctorId == AccountUtil(this).getDoctorId()
        
        // 如果当前用户是创建该建议的医生，显示编辑按钮
        if (isDoctorCreator) {
            binding.btnEdit.visibility = View.VISIBLE
        } else {
            // 如果不是创建者，隐藏编辑按钮
            binding.btnEdit.visibility = View.GONE
        }
    }
    
    private fun updateAdviceStatus(newStatus: Int) {
        DoctorAdviceRepo.updateAdviceStatus(
            adviceId = adviceId,
            status = newStatus,
            context = this,
            onSuccess = {
                Log.d(TAG, "建议状态已更新为: ${AdviceStatus.getNameByCode(newStatus)}")
                
                // 创建一个新的对象来替换当前对象
                currentAdvice?.let { advice ->
                    currentAdvice = advice.copy(status = newStatus)
                }
                
                // 更新UI
                currentAdvice?.let { updateUI(it) }
            },
            onError = { errorMsg ->
                Log.e(TAG, "Error updating status: $errorMsg")
            }
        )
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_EDIT_ADVICE && resultCode == RESULT_OK) {
            // 编辑成功后重新加载数据
            loadAdviceData()
        }
    }
}