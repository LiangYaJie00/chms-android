package com.example.chms_android.ui.components.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import com.example.chms_android.R

/**
 * 加载对话框
 */
class LoadingDialog(context: Context) : Dialog(context) {
    
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        setContentView(view)
    }
}