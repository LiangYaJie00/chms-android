package com.example.chms_android.features_doctor.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chms_android.data.Patient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DoctorPatientsViewModel : ViewModel() {

    private val _patients = MutableLiveData<List<Patient>>()
    val patients: LiveData<List<Patient>> = _patients

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadPatients() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 模拟网络延迟
                delay(1000)
                
                // 这里应该从API获取实际数据
                // 暂时使用模拟数据
                val mockPatients = createMockPatients()
                _patients.value = mockPatients
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "加载患者数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun createMockPatients(): List<Patient> {
        return listOf(
            Patient(
                id = "P001",
                name = "张三",
                phoneNumber = "13800138001",
                gender = "男",
                age = 45,
                address = "北京市海淀区",
                medicalHistory = "高血压，糖尿病"
            ),
            Patient(
                id = "P002",
                name = "李四",
                phoneNumber = "13900139002",
                gender = "女",
                age = 32,
                address = "北京市朝阳区",
                medicalHistory = "过敏性鼻炎"
            ),
            Patient(
                id = "P003",
                name = "王五",
                phoneNumber = "13700137003",
                gender = "男",
                age = 58,
                address = "北京市西城区",
                medicalHistory = "冠心病，高血脂"
            ),
            Patient(
                id = "P004",
                name = "赵六",
                phoneNumber = "13600136004",
                gender = "女",
                age = 27,
                address = "北京市东城区",
                medicalHistory = ""
            ),
            Patient(
                id = "P005",
                name = "钱七",
                phoneNumber = "13500135005",
                gender = "男",
                age = 41,
                address = "北京市丰台区",
                medicalHistory = "胃溃疡"
            )
        )
    }

    // 搜索患者
    fun searchPatients(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 模拟网络延迟
                delay(500)
                
                // 实际应用中应该调用API进行搜索
                // 这里简单模拟一下搜索逻辑
                val allPatients = createMockPatients()
                val filteredPatients = if (query.isEmpty()) {
                    allPatients
                } else {
                    allPatients.filter { 
                        it.name.contains(query, ignoreCase = true) || 
                        it.phoneNumber.contains(query) 
                    }
                }
                
                _patients.value = filteredPatients
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "搜索患者失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 添加患者
    fun addPatient(patient: Patient) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 模拟网络延迟
                delay(1000)
                
                // 实际应用中应该调用API添加患者
                // 这里简单模拟添加逻辑
                val currentList = _patients.value?.toMutableList() ?: mutableListOf()
                currentList.add(patient)
                _patients.value = currentList
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "添加患者失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}