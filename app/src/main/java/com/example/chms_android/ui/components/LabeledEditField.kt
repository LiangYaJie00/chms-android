package com.example.chms_android.ui.components

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.chms_android.R

class LabeledEditField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val tvLabel: TextView
    private val etContent: EditText
    private val ivLock: ImageView
    
    private var isLocked: Boolean = false
    private var textChangeListener: ((String) -> Unit)? = null
    private var lockMessage: String = "此字段不可编辑" // 默认提示消息

    init {
        // 加载布局
        val view = LayoutInflater.from(context).inflate(R.layout.comp_labeled_edit_field, this, true)
        
        // 初始化视图
        tvLabel = view.findViewById(R.id.tv_label)
        etContent = view.findViewById(R.id.et_content)
        ivLock = view.findViewById(R.id.iv_lock)
        
        // 获取自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LabeledEditField)
        try {
            val labelText = typedArray.getString(R.styleable.LabeledEditField_labelText) ?: ""
            val hintText = typedArray.getString(R.styleable.LabeledEditField_hintText) ?: ""
            isLocked = typedArray.getBoolean(R.styleable.LabeledEditField_isLocked, false)
            lockMessage = typedArray.getString(R.styleable.LabeledEditField_lockMessage) ?: "此字段不可编辑"
            
            tvLabel.text = labelText
            etContent.hint = hintText
            updateLockState()
        } finally {
            typedArray.recycle()
        }
        
        // 设置文本变化监听器
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                textChangeListener?.invoke(s.toString())
            }
        })
        
        // 添加点击事件处理
        setupClickListener()
    }
    
    private fun setupClickListener() {
        // 为整个组件添加触摸事件监听
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && isLocked) {
                showLockedMessage()
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        
        // 为锁图标添加点击事件
        ivLock.setOnClickListener {
            if (isLocked) {
                showLockedMessage()
            }
        }
        
        // 为EditText添加点击事件拦截
        etContent.setOnClickListener {
            if (isLocked) {
                showLockedMessage()
            }
        }
    }
    
    private fun showLockedMessage() {
        AlertDialog.Builder(context)
            .setTitle("提示")
            .setMessage(lockMessage)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun updateLockState() {
        ivLock.visibility = if (isLocked) VISIBLE else GONE
        etContent.isEnabled = !isLocked
        etContent.isFocusable = !isLocked
        etContent.isFocusableInTouchMode = !isLocked
    }
    
    fun setLocked(locked: Boolean) {
        if (isLocked != locked) {
            isLocked = locked
            updateLockState()
        }
    }
    
    fun isLocked(): Boolean = isLocked
    
    fun setLockMessage(message: String) {
        lockMessage = message
    }
    
    fun getLockMessage(): String = lockMessage
    
    fun setLabelText(text: String) {
        tvLabel.text = text
    }
    
    fun getLabelText(): String = tvLabel.text.toString()
    
    fun setContent(text: String) {
        etContent.setText(text)
    }
    
    fun getContent(): String = etContent.text.toString()
    
    fun setHintText(hint: String) {
        etContent.hint = hint
    }
    
    fun getHintText(): String = etContent.hint.toString()
    
    fun setOnTextChangedListener(listener: (String) -> Unit) {
        textChangeListener = listener
    }
}