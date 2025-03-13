package com.example.chms_android.features.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.Community

class CommunityActivityVM() : ViewModel() {
    val communityList: LiveData<List<Community>> get() = _communityList
    private val _communityList = MutableLiveData<List<Community>>()

    fun setCommunityList(communityList: List<Community>) {
        _communityList.value = communityList
    }
}