package com.example.chms_android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityVM(tvMain: String): ViewModel() {
    val tvMain: LiveData<String> get() = _tvMain
    private val _tvMain = MutableLiveData<String>()

    init {
        _tvMain.value = tvMain
    }

    fun changeTvMain() {
        val str = _tvMain.value ?: ""
        if (str.equals("111111")) {
            _tvMain.value = "222222"
        } else {
            _tvMain.value = "111111"
        }
    }

    fun clear() {
        _tvMain.value = ""
    }
}