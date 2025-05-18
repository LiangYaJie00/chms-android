package com.example.chms_android.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.example.chms_android.R

/**
 * 文本格式化工具类，提供各种文本格式化方法
 */
object TextFormatUtil {
    
    /**
     * 格式化文本，将**包裹的内容加粗并变为指定颜色
     * @param context 上下文
     * @param text 要格式化的文本
     * @param colorResId 高亮文本的颜色资源ID，默认为蓝色
     * @return 格式化后的文本
     */
    fun formatBoldText(context: Context, text: String, colorResId: Int = R.color.light_blue): CharSequence {
        // 如果文本中没有**，直接返回原文本
        if (!text.contains("**")) {
            return text
        }
        
        // 分割文本，按**分割
        val parts = text.split("**")
        
        // 创建SpannableStringBuilder
        val builder = SpannableStringBuilder()
        
        // 遍历所有部分，奇数索引的部分是需要加粗和变色的
        for (i in parts.indices) {
            val part = parts[i]
            if (part.isEmpty()) continue
            
            if (i % 2 == 1) {
                // 奇数索引，需要加粗和变色
                val spannable = SpannableString(part)
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, part.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, colorResId)),
                    0, part.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.append(spannable)
            } else {
                // 偶数索引，正常显示
                builder.append(part)
            }
        }
        
        return builder
    }
    
    /**
     * 格式化文本，将指定的标记包裹的内容应用样式
     * @param context 上下文
     * @param text 要格式化的文本
     * @param marker 标记字符串，如"**"
     * @param applyStyles 应用样式的函数
     * @return 格式化后的文本
     */
    fun formatTextWithMarker(
        context: Context, 
        text: String, 
        marker: String, 
        applyStyles: (SpannableString) -> Unit
    ): CharSequence {
        // 如果文本中没有标记，直接返回原文本
        if (!text.contains(marker)) {
            return text
        }
        
        // 分割文本，按标记分割
        val parts = text.split(marker)
        
        // 创建SpannableStringBuilder
        val builder = SpannableStringBuilder()
        
        // 遍历所有部分，奇数索引的部分是需要应用样式的
        for (i in parts.indices) {
            val part = parts[i]
            if (part.isEmpty()) continue
            
            if (i % 2 == 1) {
                // 奇数索引，需要应用样式
                val spannable = SpannableString(part)
                applyStyles(spannable)
                builder.append(spannable)
            } else {
                // 偶数索引，正常显示
                builder.append(part)
            }
        }
        
        return builder
    }
}