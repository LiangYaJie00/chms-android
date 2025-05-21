package com.example.chms_android.features_doctor.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chms_android.R
import com.example.chms_android.common.Constants
import com.example.chms_android.common.enums.AdviceStatus
import com.example.chms_android.common.enums.AdviceType
import com.example.chms_android.data.Role
import com.example.chms_android.data.User
import com.example.chms_android.databinding.ActivityDoctorAdviceEditBinding
import com.example.chms_android.dto.DoctorAdviceDTO
import com.example.chms_android.features_doctor.dialog.SelectResidentDialog
import com.example.chms_android.repo.DoctorAdviceRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import java.sql.Timestamp
import java.util.Date

class DoctorAdviceEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorAdviceEditBinding
    private val TAG = "DoctorAdviceEdit"
    
    // 当前选中的居民
    private var selectedResident: User? = null
    
    // 当前编辑的建议ID（如果是编辑模式）
    private var adviceId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorAdviceEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 初始化建议类型下拉框
        setupAdviceTypeSpinner()
        
        // 检查是否从居民详情页传入了居民信息
        val resident = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("resident", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("resident") as? User
        }
        
        if (resident != null) {
            // 如果有传入居民，直接设置为选中
            selectedResident = resident
            binding.tvSelectedResident.text = resident.name
        }
        
        // 设置选择居民按钮
        binding.btnSelectResident.setOnClickListener {
            showSelectResidentDialog()
        }
        
        // 设置提交按钮
        binding.btnSubmit.setOnClickListener {
            submitAdvice()
        }
        
        // 检查是否是编辑模式
        adviceId = intent.getIntExtra("ADVICE_ID", 0)
        if (adviceId > 0) {
            // 编辑模式，加载现有建议数据
            binding.titleBar.setTitleText("编辑医生建议")
            loadAdviceData(adviceId)
        } else {
            // 新建模式
            binding.titleBar.setTitleText("新建医生建议")
        }
    }
    
    private fun setupAdviceTypeSpinner() {
        // 使用AdviceType枚举中的所有类型的显示名称（中文）
        val adviceTypes = AdviceType.values().map { it.displayName }
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            adviceTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAdviceType.adapter = adapter
    }
    
    private fun showSelectResidentDialog() {
        val dialog = SelectResidentDialog(this)
        dialog.setOnResidentSelectedListener { resident ->
            selectedResident = resident
            binding.tvSelectedResident.text = resident.name
        }
        dialog.show()
    }
    
    private fun loadAdviceData(adviceId: Int) {
        DoctorAdviceRepo.getAdviceById(
            adviceId = adviceId,
            context = this,
            onSuccess = { adviceDTO ->
                // 填充表单数据
                binding.etTitle.setText(adviceDTO.title)
                binding.etContent.setText(adviceDTO.content)
                
                // 设置建议类型
                val adviceType = adviceDTO.adviceType?.let { AdviceType.getByCode(it) }
                if (adviceType != null) {
                    val typeIndex = AdviceType.values().indexOf(adviceType)
                    if (typeIndex >= 0) {
                        binding.spinnerAdviceType.setSelection(typeIndex)
                    }
                }
                
                // 设置重要标记
                binding.cbImportant.isChecked = adviceDTO.isImportant == 1
                
                // 设置居民信息
                if (adviceDTO.residentId != null && adviceDTO.residentName != null) {
                    // 创建一个新的User对象，提供所有必需的参数
                    selectedResident = User(
                        userId = adviceDTO.residentId,
                        name = adviceDTO.residentName,
                        email = "", // 提供一个空字符串作为默认值
                        role = Role.consumer, // 提供一个默认角色值
                        isVerified = false // 默认未验证
                    )
                    binding.tvSelectedResident.text = adviceDTO.residentName
                }
            },
            onError = { errorMsg ->
                ToastUtil.show(this, "加载建议数据失败: $errorMsg")
                Log.e(TAG, "Error loading advice: $errorMsg")
            }
        )
    }
    
    private fun submitAdvice() {
        // 验证输入
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            ToastUtil.show(this, "请输入建议标题")
            return
        }
        
        val content = binding.etContent.text.toString().trim()
        if (content.isEmpty()) {
            ToastUtil.show(this, "请输入建议内容")
            return
        }
        
        if (selectedResident == null) {
            ToastUtil.show(this, "请选择居民")
            return
        }
        
        // 获取当前医生ID
        val doctorId = AccountUtil(this).getDoctorId()
        if (doctorId <= 0) {
            ToastUtil.show(this, "获取医生信息失败")
            return
        }
        
        // 获取选中的建议类型
        val selectedTypeIndex = binding.spinnerAdviceType.selectedItemPosition
        val adviceType = AdviceType.values()[selectedTypeIndex].code
        
        // 创建当前时间戳，使用服务器期望的格式
        val currentTime = Timestamp(System.currentTimeMillis())

        // 创建建议DTO
        val adviceDTO = DoctorAdviceDTO(
            adviceId = adviceId, // 如果是新建，ID为0
            doctorId = doctorId,
            residentId = selectedResident?.userId,
            residentName = selectedResident?.name,
            title = title,
            content = content,
            adviceType = adviceType,
            status = if (adviceId == 0) AdviceStatus.UNREAD.code else null, // 新建时设置为未读状态
            isImportant = if (binding.cbImportant.isChecked) 1 else 0,
            createdAt = if (adviceId == 0) currentTime else null // 新建时设置创建时间
        )
        
        Log.d(TAG, "Submitting advice: $adviceDTO")

        // 提交到服务器
        DoctorAdviceRepo.addOrUpdateAdvice(
            adviceDTO = adviceDTO,
            context = this,
            onSuccess = { resultDTO ->
                ToastUtil.show(this, "保存成功")
                
                // 返回结果给上一个界面
                val resultIntent = Intent()
                resultIntent.putExtra("ADVICE_ID", resultDTO.adviceId)
                setResult(RESULT_OK, resultIntent)
                
                finish()
            },
            onError = { errorMsg ->
                ToastUtil.show(this, "保存失败: $errorMsg")
                Log.e(TAG, "Error saving advice: $errorMsg")
            }
        )
    }
}