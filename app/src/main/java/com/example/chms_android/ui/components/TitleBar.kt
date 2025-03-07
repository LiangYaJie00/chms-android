package com.example.chms_android.ui.components

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.chms_android.R
import com.example.chms_android.databinding.CompTitleBarBinding

class TitleBar(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {
    private val binding: CompTitleBarBinding

    init {
        binding = CompTitleBarBinding.inflate(LayoutInflater.from(context), this, true)

        binding.ibTitleBarBack.setOnClickListener {
            val activity = context as Activity
            activity.finish()
        }

        // 读取自定义属性
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TitleBar)
        binding.tvTitleBarTitle.text = attributes.getString(R.styleable.TitleBar_titleText) ?: "标题"
        // 释放 TypedArray 对象，以供后续重用
        attributes.recycle()

    }

}