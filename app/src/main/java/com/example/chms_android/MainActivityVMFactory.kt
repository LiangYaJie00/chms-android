package com.example.chms_android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivityVMFactory(private val tvMain: String): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityVM(tvMain) as T
    }
}