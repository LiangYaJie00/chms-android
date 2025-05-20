package com.example.chms_android.features_doctor.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.example.chms_android.databinding.DialogAddCertificateBinding
import com.example.chms_android.utils.DatePickerUtil
import com.example.chms_android.utils.ToastUtil

/**
 * 添加证书对话框
 */
class AddCertificateDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogAddCertificateBinding
    private var onConfirmListener: ((String, String) -> Unit)? = null
    
    // 存储初始值，用于在onCreate之前设置
    private var initialName: String = ""
    private var initialGetDate: String = ""
    private var hasInitialValues = false
    
    // 对话框标题
    private var dialogTitle: String = "添加证书"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogAddCertificateBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 设置对话框宽度
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        
        // 设置对话框标题
        binding.tvAddCertificateDialogTitle.text = dialogTitle
        
        // 如果有初始值，设置到UI
        if (hasInitialValues) {
            binding.etCertificateName.setText(initialName)
            binding.etGetDate.setText(initialGetDate)
        }

        // 设置取消按钮
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 设置确认按钮
        binding.btnConfirm.setOnClickListener {
            val name = binding.etCertificateName.text.toString().trim()
            val getDate = binding.etGetDate.text.toString().trim()

            // 验证输入
            if (name.isEmpty()) {
                ToastUtil.show(context, "请输入证书名称")
                return@setOnClickListener
            }

            // 调用回调
            onConfirmListener?.invoke(name, getDate)
            dismiss()
        }

        // 设置日期选择器
        binding.etGetDate.setOnClickListener {
            DatePickerUtil.showDatePicker(context, binding.etGetDate)
        }
    }
    
    /**
     * 设置对话框标题
     */
    fun setDialogTitle(title: String) {
        dialogTitle = title
        if (::binding.isInitialized) {
            binding.tvAddCertificateDialogTitle.text = title
        }
    }

    /**
     * 设置初始值
     */
    fun setInitialValues(name: String, getDate: String) {
        if (::binding.isInitialized) {
            // 如果对话框已经初始化，直接设置值
            binding.etCertificateName.setText(name)
            binding.etGetDate.setText(getDate)
        } else {
            // 否则，保存值以便在onCreate中设置
            initialName = name
            initialGetDate = getDate
            hasInitialValues = true
        }
    }

    /**
     * 设置确认按钮点击监听器
     */
    fun setOnConfirmListener(listener: (String, String) -> Unit) {
        this.onConfirmListener = listener
    }
}