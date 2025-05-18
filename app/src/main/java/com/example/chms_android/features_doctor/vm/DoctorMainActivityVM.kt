package com.example.chms_android.features_doctor.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.utils.AccountUtil

class DoctorMainActivityVM : ViewModel() {
    
    private val _doctorName = MutableLiveData<String>()
    val doctorName: LiveData<String> = _doctorName
    
    private val _doctorCommunity = MutableLiveData<String>()
    val doctorCommunity: LiveData<String> = _doctorCommunity
    
    fun loadDoctorInfo(accountUtil: AccountUtil) {
        val user = accountUtil.getUser()
        _doctorName.value = user?.name ?: "医生"
        _doctorCommunity.value = user?.community ?: "未分配社区"
    }
}