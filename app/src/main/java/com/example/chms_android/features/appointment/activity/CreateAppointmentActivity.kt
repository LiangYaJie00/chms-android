package com.example.chms_android.features.appointment.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chms_android.R
import com.example.chms_android.data.AppointmentType
import com.example.chms_android.databinding.ActivityCreateAppointmentBinding
import com.example.chms_android.dto.AppointmentRequestDTO
import com.example.chms_android.dto.DoctorDTO
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.repo.DoctorAvailableTimeRepo
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateAppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAppointmentBinding
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private var selectedDoctor: DoctorDTO? = null
    private var selectedDate: String? = null
    private var availableTimeSlots: List<TimeSlotDTO> = emptyList()
    private var selectedTimeSlot: TimeSlotDTO? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        setupAppointmentTypeSpinner()
        setupDatePicker()
        loadDoctors()
        setupSubmitButton()
    }
    
    private fun setupAppointmentTypeSpinner() {
        val appointmentTypes = arrayOf("线下预约", "线上预约")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, appointmentTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAppointmentType.adapter = adapter
    }
    
    private fun setupDatePicker() {
        binding.etAppointmentDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    
                    selectedDate = dateFormat.format(calendar.time)
                    binding.etAppointmentDate.setText(selectedDate)
                    
                    // 清空时间段选择
                    binding.spinnerTimeSlot.adapter = null
                    selectedTimeSlot = null
                    
                    // 如果已选择医生，则加载可用时间段
                    if (selectedDoctor != null) {
                        loadAvailableTimeSlots()
                    }
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
    
    private fun loadDoctors() {
        binding.progressBar.visibility = View.VISIBLE
        
        // 从SessionManager获取用户的社区信息
        val community = AccountUtil(this).getUser()?.community
        
        if (community.isNullOrEmpty()) {
            // 如果没有社区信息，则获取所有医生
            DoctorRepo.getAllNetDoctors(
                context = this
            )
            
            // 从数据库获取所有医生
            DoctorRepo.getAllDoctorsFromDb(
                onComplete = { doctors ->
                    handleDoctorsLoaded(doctors.map { it.toDTO() })
                },
                onError = { exception ->
                    binding.progressBar.visibility = View.GONE
                    ToastUtil.show(this, "加载医生列表失败: ${exception.message}", Toast.LENGTH_SHORT)
                }
            )
        } else {
            // 如果有社区信息，则获取该社区的医生
            DoctorRepo.getDoctorsByCommunity(
                community = community,
                context = this,
                onSuccess = { doctors ->
                    handleDoctorsLoaded(doctors)
                },
                onError = { errorMsg ->
                    binding.progressBar.visibility = View.GONE
                    ToastUtil.show(this, "加载社区医生列表失败: $errorMsg", Toast.LENGTH_SHORT)
                    
                    // 如果获取社区医生失败，尝试获取所有医生作为备选
                    DoctorRepo.getAllNetDoctors(this)
                    
                    // 从数据库获取所有医生
                    DoctorRepo.getAllDoctorsFromDb(
                        onComplete = { allDoctors ->
                            handleDoctorsLoaded(allDoctors.map { it.toDTO() })
                        },
                        onError = { exception ->
                            binding.progressBar.visibility = View.GONE
                            ToastUtil.show(this, "加载医生列表失败: ${exception.message}", Toast.LENGTH_SHORT)
                        }
                    )
                }
            )
        }
    }

    // 提取处理医生数据的公共方法
    private fun handleDoctorsLoaded(doctors: List<DoctorDTO>) {
        binding.progressBar.visibility = View.GONE
        
        if (doctors.isEmpty()) {
            ToastUtil.show(this, "没有可用的医生", Toast.LENGTH_SHORT)
            return
        }
        
        val doctorNames = doctors.map { 
            // 使用正确的字段
            val specialtyInfo = it.specialization ?: ""
            val communityInfo = if (!it.communityName.isNullOrEmpty()) "(${it.communityName})" else ""
            "${it.name ?: "未知医生"} $specialtyInfo $communityInfo"
        }.toTypedArray()
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctorNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDoctor.adapter = adapter
        
        binding.spinnerDoctor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDoctor = doctors[position]
                
                // 如果已选择日期，则加载可用时间段
                if (selectedDate != null) {
                    loadAvailableTimeSlots()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedDoctor = null
            }
        }
    }
    
    private fun loadAvailableTimeSlots() {
        selectedDoctor?.userId?.let { doctorId ->
            selectedDate?.let { date ->
                binding.progressBar.visibility = View.VISIBLE
                
                DoctorAvailableTimeRepo.getAvailableTimesByDateNotBooked(
                    doctorId = doctorId,
                    date = date,
                    context = this,
                    onSuccess = { availableTimes ->
                        binding.progressBar.visibility = View.GONE
                        
                        // 转换为时间段列表
                        availableTimeSlots = availableTimes.map { 
                            TimeSlotDTO(it.startTime, it.endTime) 
                        }
                        
                        if (availableTimeSlots.isEmpty()) {
                            ToastUtil.show(this, "该日期没有可用的预约时间段", Toast.LENGTH_SHORT)
                            return@getAvailableTimesByDateNotBooked
                        }
                        
                        // 显示可用时间段
                        val timeSlotStrings = availableTimeSlots.map { 
                            "${it.startTime} - ${it.endTime}" 
                        }.toTypedArray()
                        
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlotStrings)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerTimeSlot.adapter = adapter
                        
                        binding.spinnerTimeSlot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                selectedTimeSlot = availableTimeSlots[position]
                            }
                            
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                selectedTimeSlot = null
                            }
                        }
                    },
                    onError = { errorMsg ->
                        binding.progressBar.visibility = View.GONE
                        ToastUtil.show(this, "加载可用时间段失败: $errorMsg", Toast.LENGTH_SHORT)
                    }
                )
            }
        }
    }
    
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateInput()) {
                createAppointment()
            }
        }
    }
    
    private fun validateInput(): Boolean {
        if (selectedDoctor == null) {
            ToastUtil.show(this, "请选择医生", Toast.LENGTH_SHORT)
            return false
        }
        
        if (selectedDate == null) {
            ToastUtil.show(this, "请选择预约日期", Toast.LENGTH_SHORT)
            return false
        }
        
        if (selectedTimeSlot == null) {
            ToastUtil.show(this, "请选择预约时间段", Toast.LENGTH_SHORT)
            return false
        }
        
        if (binding.etReason.text.toString().trim().isEmpty()) {
            ToastUtil.show(this, "请填写预约原因", Toast.LENGTH_SHORT)
            return false
        }
        
        return true
    }
    
    private fun createAppointment() {
        val patientId = AccountUtil(this).getUserId()
        val doctorId = selectedDoctor?.userId
        val appointmentDate = selectedDate
        val startTime = selectedTimeSlot?.startTime
        val endTime = selectedTimeSlot?.endTime
        val reason = binding.etReason.text.toString().trim()
        val appointmentType = if (binding.spinnerAppointmentType.selectedItemPosition == 0) {
            AppointmentType.OFFLINE
        } else {
            AppointmentType.ONLINE
        }
        
        if (patientId == null || doctorId == null || appointmentDate == null || 
            startTime == null || endTime == null) {
            ToastUtil.show(this, "创建预约失败: 参数不完整", Toast.LENGTH_SHORT)
            return
        }
        
        val requestDTO = AppointmentRequestDTO(
            patientId = patientId.toInt(),
            doctorId = doctorId,
            appointmentDate = appointmentDate,
            startTime = startTime,
            endTime = endTime,
            reason = reason,
            appointmentType = appointmentType
        )
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false
        
        AppointmentRepo.createAppointment(
            requestDTO = requestDTO,
            context = this,
            onSuccess = {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                
                ToastUtil.show(this, "预约创建成功，等待医生确认", Toast.LENGTH_SHORT)
                finish()
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                
                ToastUtil.show(this, "创建预约失败: $errorMsg", Toast.LENGTH_SHORT)
            }
        )
    }
    
    data class TimeSlotDTO(val startTime: String?, val endTime: String?)
}