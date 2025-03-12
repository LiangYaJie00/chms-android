package com.example.chms_android.features.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.Doctor

class HomeFragmentVM(): ViewModel() {
    val doctorList: LiveData<List<Doctor>> get() = _doctorList
    private val _doctorList = MutableLiveData<List<Doctor>>()

    fun setDoctorList(doctorList: List<Doctor>) {
        _doctorList.value = doctorList
    }
}