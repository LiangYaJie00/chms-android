package com.example.chms_android.features_doctor.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chms_android.data.Appointment
import com.example.chms_android.data.AppointmentStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DoctorAppointmentsViewModel : ViewModel() {

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 模拟网络延迟
                delay(1000)
                
                // 这里应该从API获取实际数据
                // 暂时使用模拟数据
                val mockAppointments = createMockAppointments()
                _appointments.value = mockAppointments
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "加载预约数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 模拟网络延迟
                delay(500)
                
                // 实际应用中应该调用API更新预约状态
                // 这里简单模拟更新逻辑
                val currentList = _appointments.value?.toMutableList() ?: mutableListOf()
                val appointmentIndex = currentList.indexOfFirst { it.id == appointmentId }
                
                if (appointmentIndex != -1) {
                    val appointment = currentList[appointmentIndex]
                    val updatedAppointment = appointment.copy(status = newStatus)
                    currentList[appointmentIndex] = updatedAppointment
                    _appointments.value = currentList
                }
                
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "更新预约状态失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun createMockAppointments(): List<Appointment> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // 今天
        val today = calendar.time
        
        // 明天
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrow = calendar.time
        
        // 后天
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dayAfterTomorrow = calendar.time
        
        return listOf(
            Appointment(
                id = "A001",
                patientId = "P001",
                patientName = "张三",
                doctorId = "D001",
                doctorName = "李医生",
                date = today,
                startTime = "09:00",
                endTime = "09:30",
                reason = "感冒，发热",
                status = AppointmentStatus.PENDING
            ),
            Appointment(
                id = "A002",
                patientId = "P002",
                patientName = "李四",
                doctorId = "D001",
                doctorName = "李医生",
                date = today,
                startTime = "10:00",
                endTime = "10:30",
                reason = "头痛，眩晕",
                status = AppointmentStatus.CONFIRMED
            ),
            Appointment(
                id = "A003",
                patientId = "P003",
                patientName = "王五",
                doctorId = "D001",
                doctorName = "李医生",
                date = tomorrow,
                startTime = "14:00",
                endTime = "14:30",
                reason = "复查血压",
                status = AppointmentStatus.CONFIRMED
            ),
            Appointment(
                id = "A004",
                patientId = "P004",
                patientName = "赵六",
                doctorId = "D001",
                doctorName = "李医生",
                date = tomorrow,
                startTime = "15:00",
                endTime = "15:30",
                reason = "咳嗽，胸闷",
                status = AppointmentStatus.PENDING
            ),
            Appointment(
                id = "A005",
                patientId = "P005",
                patientName = "钱七",
                doctorId = "D001",
                doctorName = "李医生",
                date = dayAfterTomorrow,
                startTime = "09:30",
                endTime = "10:00",
                reason = "腹痛，腹泻",
                status = AppointmentStatus.PENDING
            ),
            Appointment(
                id = "A006",
                patientId = "P001",
                patientName = "张三",
                doctorId = "D001",
                doctorName = "李医生",
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
                startTime = "11:00",
                endTime = "11:30",
                reason = "发热，咳嗽",
                status = AppointmentStatus.COMPLETED
            ),
            Appointment(
                id = "A007",
                patientId = "P002",
                patientName = "李四",
                doctorId = "D001",
                doctorName = "李医生",
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                startTime = "16:00",
                endTime = "16:30",
                reason = "皮疹，瘙痒",
                status = AppointmentStatus.CANCELLED
            )
        )
    }

    // 添加新预约
    fun addAppointment(appointment: Appointment) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 模拟网络延迟
                delay(1000)
                
                // 实际应用中应该调用API添加预约
                // 这里简单模拟添加逻辑
                val currentList = _appointments.value?.toMutableList() ?: mutableListOf()
                currentList.add(appointment)
                _appointments.value = currentList
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "添加预约失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 根据日期筛选预约
    fun filterAppointmentsByDate(date: Date) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 模拟网络延迟
                delay(500)
                
                // 实际应用中应该调用API筛选预约
                // 这里简单模拟筛选逻辑
                val allAppointments = createMockAppointments()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = dateFormat.format(date)
                
                val filteredAppointments = allAppointments.filter { 
                    dateFormat.format(it.date) == dateString 
                }
                
                _appointments.value = filteredAppointments
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "筛选预约失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}