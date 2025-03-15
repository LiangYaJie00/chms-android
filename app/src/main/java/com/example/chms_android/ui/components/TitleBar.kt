package com.example.chms_android.ui.components

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
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

        val mode = attributes.getInteger(R.styleable.TitleBar_mode, 0)

        if (mode == 0) {
            // 获取并应用背景颜色
            val defaultBackgroundColor = ContextCompat.getColor(context, R.color.white)
            val titleBarBackgroundColor = attributes.getColor(R.styleable.TitleBar_titleBarBackground, defaultBackgroundColor)
            binding.root.setBackgroundColor(titleBarBackgroundColor)

            // 获取并应用标题的颜色
            val defaultColor = ContextCompat.getColor(context, R.color.black)
            val titleTextColor = attributes.getColor(R.styleable.TitleBar_titleTextColor, defaultColor)
            binding.tvTitleBarTitle.setTextColor(titleTextColor)

            // 设置backArrow按钮的图标
            val backArrowIconResId = attributes.getResourceId(R.styleable.TitleBar_backArrowIcon, R.drawable.baseline_arrow_back_24)
            if (backArrowIconResId != -1) {
                binding.ibTitleBarBack.setImageResource(backArrowIconResId)
            }
        } else {
            // 获取并应用背景颜色
            val defaultBackgroundColor = ContextCompat.getColor(context, R.color.actionbar_color)
            val titleBarBackgroundColor = attributes.getColor(R.styleable.TitleBar_titleBarBackground, defaultBackgroundColor)
            binding.root.setBackgroundColor(titleBarBackgroundColor)

            // 获取并应用标题的颜色
            val defaultColor = ContextCompat.getColor(context, R.color.white)
            val titleTextColor = attributes.getColor(R.styleable.TitleBar_titleTextColor, defaultColor)
            binding.tvTitleBarTitle.setTextColor(titleTextColor)

            // 设置backArrow按钮的图标
            val backArrowIconResId = attributes.getResourceId(R.styleable.TitleBar_backArrowIcon, R.drawable.baseline_arrow_back_white_24)
            if (backArrowIconResId != -1) {
                binding.ibTitleBarBack.setImageResource(backArrowIconResId)
            }
        }

        // 应用标题内容
        binding.tvTitleBarTitle.text = attributes.getString(R.styleable.TitleBar_titleText) ?: "标题"

        // 应用back按钮的显隐状态
        val showBackArrow = attributes.getBoolean(R.styleable.TitleBar_showBackArrow, true)
        binding.ibTitleBarBack.visibility = if (showBackArrow) VISIBLE else GONE

        // 释放 TypedArray 对象，以供后续重用
        attributes.recycle()

    }

    fun setTitleText(text: String) {
        binding.tvTitleBarTitle.text = text
    }

}