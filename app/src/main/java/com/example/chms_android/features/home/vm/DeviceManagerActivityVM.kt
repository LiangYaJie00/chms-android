package com.example.chms_android.features.home.vm

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.R
import com.example.chms_android.data.Devices
import com.example.chms_android.utils.SPUtil

class DeviceManagerActivityVM(): ViewModel() {
    val devicesList: LiveData<ArrayList<Devices>> get() = _devicesList
    private val _devicesList = MutableLiveData<ArrayList<Devices>>()
    private var count = 0
    private lateinit var spUtil: SPUtil

    fun initDevices(context: Context) {
        spUtil = SPUtil(context)
        _devicesList.value = ArrayList(spUtil.getObjectList("DevicesList", Devices::class.java))
    }

    // 更新设备列表的方法
    fun updateDevicesList(newList: List<Devices>) {
        _devicesList.value = ArrayList(newList)
    }

    fun addHeartRateDevice() {
        val device = Devices(++count, "心率仪", R.drawable.ic_heart_rate_color_32dp, 1)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }

    fun addWatch() {
        val device = Devices(++count, "手表", R.drawable.ic_watch_color_24dp, 2)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }

    fun addHeightMeter() {
        val device = Devices(++count, "身高测定仪", R.drawable.ic_high_color_24dp, 3)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }

    fun addOximeter() {
        val device = Devices(++count, "血氧仪", R.drawable.ic_blood_oxygen_color_24dp, 4)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }

    fun addSphygmomanometer() {
        val device = Devices(++count, "血压计", R.drawable.ic_blood_pressure_color_24dp, 5)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }

    fun addBloodGlucoseMeter() {
        val device = Devices(++count, "血糖仪", R.drawable.ic_blood_suger_color_24dp, 6)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }

    fun addBodyFatScale() {
        val device = Devices(++count, "体脂秤", R.drawable.ic_weight_color_24dp, 7)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }

    fun addThermometer() {
        val device = Devices(++count, "体温计", R.drawable.ic_thermometer_color_24dp, 8)
        val currentList = _devicesList.value ?: ArrayList()
        currentList.add(device)
        _devicesList.value = currentList
    }
}