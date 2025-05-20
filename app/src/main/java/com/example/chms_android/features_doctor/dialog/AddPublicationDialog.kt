package com.example.chms_android.features_doctor.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.example.chms_android.databinding.DialogAddPublicationBinding
import com.example.chms_android.utils.DatePickerUtil
import com.example.chms_android.utils.ToastUtil

/**
 * 添加出版物对话框
 */
class AddPublicationDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogAddPublicationBinding
    private var onConfirmListener: ((String, String, String) -> Unit)? = null
    
    // 存储初始值，用于在onCreate之前设置
    private var initialTitle: String = ""
    private var initialJournal: String = ""
    private var initialPublishDate: String = ""
    private var hasInitialValues = false
    
    // 对话框标题
    private var dialogTitle: String = "添加出版物"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogAddPublicationBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // 设置对话框宽度
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        
        // 设置对话框标题
        binding.tvAddPublicationDialogTitle.text = dialogTitle

        // 如果有初始值，设置到UI
        if (hasInitialValues) {
            binding.etPublicationTitle.setText(initialTitle)
            binding.etJournal.setText(initialJournal)
            binding.etPublishDate.setText(initialPublishDate)
        }

        // 设置取消按钮
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 设置确认按钮
        binding.btnConfirm.setOnClickListener {
            val title = binding.etPublicationTitle.text.toString().trim()
            val journal = binding.etJournal.text.toString().trim()
            val publishDate = binding.etPublishDate.text.toString().trim()

            // 验证输入
            if (title.isEmpty()) {
                ToastUtil.show(context, "请输入出版物标题")
                return@setOnClickListener
            }

            // 调用回调
            onConfirmListener?.invoke(title, journal, publishDate)
            dismiss()
        }

        // 设置日期选择器
        binding.etPublishDate.setOnClickListener {
            DatePickerUtil.showDatePicker(context, binding.etPublishDate)
        }
    }
    
    /**
     * 设置对话框标题
     */
    fun setDialogTitle(title: String) {
        dialogTitle = title
        if (::binding.isInitialized) {
            binding.tvAddPublicationDialogTitle.text = title
        }
    }

    /**
     * 设置初始值
     */
    fun setInitialValues(title: String, journal: String, publishDate: String) {
        if (::binding.isInitialized) {
            // 如果对话框已经初始化，直接设置值
            binding.etPublicationTitle.setText(title)
            binding.etJournal.setText(journal)
            binding.etPublishDate.setText(publishDate)
        } else {
            // 否则，保存值以便在onCreate中设置
            initialTitle = title
            initialJournal = journal
            initialPublishDate = publishDate
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