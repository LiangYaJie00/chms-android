package com.example.chms_android

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.utils.AccountUtil

class MainActivityVM(tvMain: String, context: Context): ViewModel() {
    val tvMain: LiveData<String> get() = _tvMain
    private val _tvMain = MutableLiveData<String>()
    val communityName: LiveData<String> get() = _communityName
    private val _communityName = MutableLiveData<String>()
    val name: LiveData<String> get() = _name
    private val _name = MutableLiveData<String>()

    init {
        _tvMain.value = tvMain
        val user = AccountUtil(context).getUser()
        _communityName.value = user?.community
        _name.value = user?.name
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