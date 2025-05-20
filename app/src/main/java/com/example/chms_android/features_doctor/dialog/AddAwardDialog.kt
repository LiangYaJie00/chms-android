package com.example.chms_android.features_doctor.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.example.chms_android.databinding.DialogAddAwardBinding
import com.example.chms_android.utils.DatePickerUtil
import com.example.chms_android.utils.ToastUtil

/**
 * 添加奖项对话框
 */
class AddAwardDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogAddAwardBinding
    private var onConfirmListener: ((String, String, String) -> Unit)? = null
    
    // 存储初始值，用于在onCreate之前设置
    private var initialTitle: String = ""
    private var initialDate: String = ""
    private var initialIssuer: String = ""
    private var hasInitialValues = false
    
    // 对话框标题
    private var dialogTitle: String = "添加奖项"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogAddAwardBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // 设置对话框宽度
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        
        // 设置对话框标题
        binding.tvAddAwardDialogTitle.text = dialogTitle
        
        // 如果有初始值，设置到UI
        if (hasInitialValues) {
            binding.etAwardTitle.setText(initialTitle)
            binding.etAwardDate.setText(initialDate)
            binding.etIssuer.setText(initialIssuer)
        }

        // 设置取消按钮
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 设置确认按钮
        binding.btnConfirm.setOnClickListener {
            val title = binding.etAwardTitle.text.toString().trim()
            val date = binding.etAwardDate.text.toString().trim()
            val issuer = binding.etIssuer.text.toString().trim()

            // 验证输入
            if (title.isEmpty()) {
                ToastUtil.show(context, "请输入奖项名称")
                return@setOnClickListener
            }

            // 调用回调
            onConfirmListener?.invoke(title, date, issuer)
            dismiss()
        }

        // 设置日期选择器
        binding.etAwardDate.setOnClickListener {
            DatePickerUtil.showDatePicker(context, binding.etAwardDate)
        }
    }
    
    /**
     * 设置对话框标题
     */
    fun setDialogTitle(title: String) {
        dialogTitle = title
        if (::binding.isInitialized) {
            binding.tvAddAwardDialogTitle.text = title
        }
    }
    
    /**
     * 设置初始值
     */
    fun setInitialValues(title: String, date: String, issuer: String) {
        if (::binding.isInitialized) {
            // 如果对话框已经初始化，直接设置值
            binding.etAwardTitle.setText(title)
            binding.etAwardDate.setText(date)
            binding.etIssuer.setText(issuer)
        } else {
            // 否则，保存值以便在onCreate中设置
            initialTitle = title
            initialDate = date
            initialIssuer = issuer
            hasInitialValues = true
        }
    }

    /**
     * 设置确认按钮点击监听器
     */
    fun setOnConfirmListener(listener: (String, String, String) -> Unit) {
        this.onConfirmListener = listener
    }
}