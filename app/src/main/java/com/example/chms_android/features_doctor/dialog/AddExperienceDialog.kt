package com.example.chms_android.features_doctor.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.example.chms_android.databinding.DialogAddExperienceBinding
import com.example.chms_android.utils.DatePickerUtil
import com.example.chms_android.utils.ToastUtil

/**
 * 添加工作经历对话框
 */
class AddExperienceDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogAddExperienceBinding
    private var onConfirmListener: ((String, String, String, String) -> Unit)? = null
    
    // 存储初始值，用于在onCreate之前设置
    private var initialHospital: String = ""
    private var initialPosition: String = ""
    private var initialStart: String = ""
    private var initialEnd: String = ""
    private var hasInitialValues = false
    
    // 对话框标题
    private var dialogTitle: String = "添加工作经历"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogAddExperienceBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 设置对话框宽度
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        
        // 设置对话框标题
        binding.tvAddExperienceDialogTitle.text = dialogTitle

        // 如果有初始值，设置到UI
        if (hasInitialValues) {
            binding.etHospital.setText(initialHospital)
            binding.etPosition.setText(initialPosition)
            binding.etStartDate.setText(initialStart)
            binding.etEndDate.setText(initialEnd)
        }

        // 设置取消按钮
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 设置确认按钮
        binding.btnConfirm.setOnClickListener {
            val hospital = binding.etHospital.text.toString().trim()
            val position = binding.etPosition.text.toString().trim()
            val start = binding.etStartDate.text.toString().trim()
            val end = binding.etEndDate.text.toString().trim()

            // 验证输入
            if (hospital.isEmpty()) {
                ToastUtil.show(context, "请输入医院名称")
                return@setOnClickListener
            }

            // 调用回调
            onConfirmListener?.invoke(hospital, position, start, end)
            dismiss()
        }

        // 设置日期选择器
        binding.etStartDate.setOnClickListener {
            DatePickerUtil.showDatePicker(context, binding.etStartDate)
        }

        binding.etEndDate.setOnClickListener {
            DatePickerUtil.showDatePicker(context, binding.etEndDate)
        }
    }
    
    /**
     * 设置对话框标题
     */
    fun setDialogTitle(title: String) {
        dialogTitle = title
        if (::binding.isInitialized) {
            binding.tvAddExperienceDialogTitle.text = title
        }
    }

    /**
     * 设置初始值
     */
    fun setInitialValues(hospital: String, position: String, start: String, end: String) {
        if (::binding.isInitialized) {
            // 如果对话框已经初始化，直接设置值
            binding.etHospital.setText(hospital)
            binding.etPosition.setText(position)
            binding.etStartDate.setText(start)
            binding.etEndDate.setText(end)
        } else {
            // 否则，保存值以便在onCreate中设置
            initialHospital = hospital
            initialPosition = position
            initialStart = start
            initialEnd = end
            hasInitialValues = true
        }
    }

    /**
     * 设置确认按钮点击监听器
     */
    fun setOnConfirmListener(listener: (String, String, String, String) -> Unit) {
        this.onConfirmListener = listener
    }
}