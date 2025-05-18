package com.example.chms_android.ui.components.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import com.example.chms_android.R

/**
 * 加载对话框
 * 用于显示加载中状态，可自定义加载消息
 */
@SuppressLint("MissingInflatedId")
class LoadingDialog(context: Context) : Dialog(context) {
    
    private lateinit var messageTextView: TextView
    
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        setContentView(view)
        
        // 获取消息文本视图
        messageTextView = view.findViewById(R.id.tv_loading_message)
    }
    
    /**
     * 设置加载消息
     * @param message 要显示的消息文本
     */
    fun setMessage(message: String) {
        messageTextView.text = message
    }
}