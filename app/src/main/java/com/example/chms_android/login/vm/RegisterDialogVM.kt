package com.example.chms_android.login.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterDialogVM: ViewModel() {
    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> get() = _currentPage

    // 跳转到下一步页面（RegStepTwoFragment）
    fun goToNextPage() {
        _currentPage.value = 1
    }

    fun goToPreviousPage() {
        _currentPage.value = 0
    }
}