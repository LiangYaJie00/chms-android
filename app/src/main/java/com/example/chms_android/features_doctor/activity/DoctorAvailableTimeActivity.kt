package com.example.chms_android.features_doctor.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityDoctorAvailableTimeBinding
import com.example.chms_android.databinding.DialogAddTimeSlotBinding
import com.example.chms_android.dto.DoctorAvailableTimeDTO
import com.example.chms_android.dto.DoctorAvailableTimeRequestDTO
import com.example.chms_android.features_doctor.adapter.TimeSlotAdapter
import com.example.chms_android.repo.DoctorAvailableTimeRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DoctorAvailableTimeActivity : AppCompatActivity(), TimeSlotAdapter.TimeSlotListener {
    private lateinit var binding: ActivityDoctorAvailableTimeBinding
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    private var selectedDate: String? = null
    private val timeSlots = mutableListOf<DoctorAvailableTimeDTO>()
    private lateinit var adapter: TimeSlotAdapter
    
    // 标记是否有未保存的更改
    private var hasUnsavedChanges = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorAvailableTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        setupDatePicker()
        setupRecyclerView()
        setupAddButton()
        setupSubmitButton()
    }
    
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    
                    selectedDate = dateFormat.format(calendar.time)
                    binding.etDate.setText(selectedDate)
                    
                    // 加载该日期的时间段
                    loadTimeSlots()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            
            // 设置最小日期为今天
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            
            datePickerDialog.show()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = TimeSlotAdapter(timeSlots, this)
        binding.recyclerViewTimeSlots.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTimeSlots.adapter = adapter
    }
    
    private fun loadTimeSlots() {
        val doctorId = AccountUtil(this).getUserId()
        if (doctorId == null || selectedDate == null) {
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        
        DoctorAvailableTimeRepo.getAvailableTimesByDate(
            doctorId = doctorId.toInt(),
            date = selectedDate!!,
            context = this,
            onSuccess = { availableTimes ->
                binding.progressBar.visibility = View.GONE
                
                timeSlots.clear()
                timeSlots.addAll(availableTimes)
                adapter.notifyDataSetChanged()
                
                updateEmptyView()
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                ToastUtil.show(this, "加载时间段失败: $errorMsg", Toast.LENGTH_SHORT)
                
                updateEmptyView()
            }
        )
    }
    
    private fun updateEmptyView() {
        if (timeSlots.isEmpty()) {
            binding.tvEmptyView.visibility = View.VISIBLE
            binding.recyclerViewTimeSlots.visibility = View.GONE
        } else {
            binding.tvEmptyView.visibility = View.GONE
            binding.recyclerViewTimeSlots.visibility = View.VISIBLE
        }
    }
    
    private fun setupAddButton() {
        binding.fabAddTimeSlot.setOnClickListener {
            if (selectedDate == null) {
                ToastUtil.show(this, "请先选择日期", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            
            showAddTimeSlotDialog()
        }
    }
    
    private fun showAddTimeSlotDialog() {
        val dialogBinding = DialogAddTimeSlotBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setTitle("添加时间段")
            .setView(dialogBinding.root)
            .setPositiveButton("添加", null)
            .setNegativeButton("取消", null)
            .create()
        
        dialogBinding.etStartTime.setOnClickListener {
            showTimePicker(dialogBinding.etStartTime)
        }
        
        dialogBinding.etEndTime.setOnClickListener {
            showTimePicker(dialogBinding.etEndTime)
        }
        
        dialog.show()
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val startTime = dialogBinding.etStartTime.text.toString()
            val endTime = dialogBinding.etEndTime.text.toString()
            
            if (startTime.isEmpty() || endTime.isEmpty()) {
                ToastUtil.show(this, "请选择开始和结束时间", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            
            if (startTime >= endTime) {
                ToastUtil.show(this, "结束时间必须晚于开始时间", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            
            // 检查时间段是否重叠
            if (isTimeSlotOverlapping(startTime, endTime)) {
                ToastUtil.show(this, "时间段与已有时间段重叠", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            
            // 创建一个新的时间段DTO并添加到列表中
            val newTimeSlot = DoctorAvailableTimeDTO(
                doctorId = AccountUtil(this).getUserId()?.toInt(),
                availableDate = selectedDate,
                startTime = startTime,
                endTime = endTime,
                isBooked = false // 表示未被预约
            )
            
            // 添加到本地列表
            timeSlots.add(newTimeSlot)
            adapter.notifyItemInserted(timeSlots.size - 1)
            updateEmptyView()
            
            // 标记有未保存的更改
            hasUnsavedChanges = true
            
            dialog.dismiss()
        }
    }
    
    private fun showTimePicker(editText: android.widget.EditText) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                editText.setText(time)
            },
            hour,
            minute,
            true
        )
        
        timePickerDialog.show()
    }
    
    private fun isTimeSlotOverlapping(startTime: String, endTime: String): Boolean {
        for (slot in timeSlots) {
            val existingStart = slot.startTime ?: continue
            val existingEnd = slot.endTime ?: continue
            
            // 检查是否重叠
            if ((startTime < existingEnd && endTime > existingStart)) {
                return true
            }
        }
        return false
    }
    
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (hasUnsavedChanges && selectedDate != null) {
                // 有未保存的更改，提交到服务器
                submitAllTimeSlots()
            } else {
                // 没有更改，直接返回
                finish()
            }
        }
    }
    
    private fun submitAllTimeSlots() {
        if (timeSlots.isEmpty()) {
            // 如果没有时间段，直接返回
            finish()
            return
        }
        
        val doctorId = AccountUtil(this).getUserId()?.toInt() ?: return
        
        // 将本地时间段列表转换为请求DTO中的TimeSlot列表
        val timeSlotList = timeSlots.map { 
            DoctorAvailableTimeRequestDTO.TimeSlot(
                startTime = it.startTime,
                endTime = it.endTime
            )
        }
        
        // 创建请求DTO
        val requestDTO = DoctorAvailableTimeRequestDTO(
            doctorId = doctorId,
            availableDate = selectedDate,
            timeSlots = timeSlotList
        )
        
        binding.progressBar.visibility = View.VISIBLE
        
        // 调用API一次性添加所有时间段
        DoctorAvailableTimeRepo.addAvailableTimes(
            requestDTO = requestDTO,
            context = this,
            onSuccess = { success ->
                binding.progressBar.visibility = View.GONE
                
                if (success) {
                    ToastUtil.show(this, "保存时间段成功", Toast.LENGTH_SHORT)
                    hasUnsavedChanges = false
                    finish()
                } else {
                    ToastUtil.show(this, "保存时间段失败", Toast.LENGTH_SHORT)
                }
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                ToastUtil.show(this, "保存时间段失败: $errorMsg", Toast.LENGTH_SHORT)
            }
        )
    }
    
    override fun onDeleteTimeSlot(timeSlot: DoctorAvailableTimeDTO) {
        // 找到要删除的时间段在列表中的索引
        val index = timeSlots.indexOfFirst { 
            it.startTime == timeSlot.startTime && it.endTime == timeSlot.endTime 
        }
        
        if (index != -1) {
            // 从本地列表中移除
            timeSlots.removeAt(index)
            adapter.notifyItemRemoved(index)
            updateEmptyView()
            
            // 标记有未保存的更改
            hasUnsavedChanges = true
        }
    }
    
    // 添加返回确认对话框
    override fun onBackPressed() {
        if (hasUnsavedChanges) {
            AlertDialog.Builder(this)
                .setTitle("未保存的更改")
                .setMessage("您有未保存的时间段更改，是否保存？")
                .setPositiveButton("保存") { _, _ ->
                    submitAllTimeSlots()
                }
                .setNegativeButton("放弃") { _, _ ->
                    super.onBackPressed()
                }
                .setNeutralButton("取消", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}
