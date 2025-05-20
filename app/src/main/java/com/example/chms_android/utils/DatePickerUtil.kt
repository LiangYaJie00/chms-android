package com.example.chms_android.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期选择器工具类
 */
object DatePickerUtil {
    
    /**
     * 显示日期选择器对话框
     *
     * @param context 上下文
     * @param editText 显示选择日期的输入框
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @param initialDate 初始日期，默认为当前日期或输入框中已有的日期
     * @param minDate 最小可选日期，默认为null（不限制）
     * @param maxDate 最大可选日期，默认为null（不限制）
     */
    fun showDatePicker(
        context: Context,
        editText: EditText,
        format: String = "yyyy-MM-dd",
        initialDate: Date? = null,
        minDate: Date? = null,
        maxDate: Date? = null
    ) {
        // 创建日历实例
        val calendar = Calendar.getInstance()
        
        // 设置初始日期
        if (initialDate != null) {
            // 使用传入的初始日期
            calendar.time = initialDate
        } else if (editText.text.isNotEmpty()) {
            // 尝试解析输入框中已有的日期
            try {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                val date = dateFormat.parse(editText.text.toString())
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                // 解析失败，使用当前日期
                calendar.time = Date()
            }
        }
        
        // 获取年、月、日
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // 创建日期选择器对话框
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 更新日历
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                
                // 格式化日期并设置到输入框
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                editText.setText(dateFormat.format(calendar.time))
            },
            year, month, day
        )
        
        // 设置最小日期（如果有）
        minDate?.let {
            datePickerDialog.datePicker.minDate = it.time
        }
        
        // 设置最大日期（如果有）
        maxDate?.let {
            datePickerDialog.datePicker.maxDate = it.time
        }
        
        // 显示对话框
        datePickerDialog.show()
    }
    
    /**
     * 显示日期选择器对话框，并返回选择的日期
     *
     * @param context 上下文
     * @param initialDate 初始日期，默认为当前日期
     * @param onDateSelected 日期选择回调
     * @param minDate 最小可选日期，默认为null（不限制）
     * @param maxDate 最大可选日期，默认为null（不限制）
     */
    fun showDatePicker(
        context: Context,
        initialDate: Date? = null,
        onDateSelected: (Date) -> Unit,
        minDate: Date? = null,
        maxDate: Date? = null
    ) {
        // 创建日历实例
        val calendar = Calendar.getInstance()
        
        // 设置初始日期
        if (initialDate != null) {
            calendar.time = initialDate
        }
        
        // 获取年、月、日
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // 创建日期选择器对话框
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 更新日历
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                
                // 调用回调函数
                onDateSelected(calendar.time)
            },
            year, month, day
        )
        
        // 设置最小日期（如果有）
        minDate?.let {
            datePickerDialog.datePicker.minDate = it.time
        }
        
        // 设置最大日期（如果有）
        maxDate?.let {
            datePickerDialog.datePicker.maxDate = it.time
        }
        
        // 显示对话框
        datePickerDialog.show()
    }
    
    /**
     * 格式化日期为字符串
     *
     * @param date 日期
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @return 格式化后的日期字符串
     */
    fun formatDate(date: Date, format: String = "yyyy-MM-dd"): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }
    
    /**
     * 解析日期字符串为Date对象
     *
     * @param dateString 日期字符串
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @return 解析后的Date对象，解析失败返回null
     */
    fun parseDate(dateString: String, format: String = "yyyy-MM-dd"): Date? {
        return try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}