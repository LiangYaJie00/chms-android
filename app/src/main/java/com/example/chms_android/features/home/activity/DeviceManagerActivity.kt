package com.example.chms_android.features.home.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.data.Devices
import com.example.chms_android.databinding.ActivityDeviceManagerBinding
import com.example.chms_android.features.home.adapter.DevicesAdapter
import com.example.chms_android.features.home.vm.DeviceManagerActivityVM
import com.example.chms_android.utils.MyItemTouchCallback
import com.example.chms_android.utils.SPUtil
import com.example.chms_android.utils.ToastUtil

class DeviceManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceManagerBinding
    private lateinit var viewModel: DeviceManagerActivityVM
    private lateinit var spUtil: SPUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDeviceManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel = ViewModelProvider(this).get(DeviceManagerActivityVM::class.java)
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        // 初始化设备列表
        viewModel.initDevices(this)

        // 设置设备列表 recyclerView
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDevice.layoutManager = layoutManager

        val adapter = DevicesAdapter(this, viewModel.devicesList.value ?: ArrayList(), viewModel)
        binding.recyclerViewDevice.adapter = adapter

        val itemTouchCallback = MyItemTouchCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewDevice)

        viewModel.devicesList.observe(this, Observer { devicesList ->
            saveDevicesList(devicesList)
        })
    }


    fun addHeartRateDevice(view: View) {
        viewModel.addHeartRateDevice()
    }

    fun addWatch(view: View) {
        viewModel.addWatch()
    }

    fun addHeightMeter(view: View) {
        viewModel.addHeightMeter()
    }

    fun addOximeter(view: View) {
        viewModel.addOximeter()
    }

    fun addSphygmomanometer(view: View) {
        viewModel.addSphygmomanometer()
    }

    fun addBloodGlucoseMeter(view: View) {
        viewModel.addBloodGlucoseMeter()
    }

    fun addBodyFatScale(view: View) {
        viewModel.addBodyFatScale()
    }

    fun addThermometer(view: View) {
        viewModel.addThermometer()
    }

    private fun saveDevicesList(devicesList: ArrayList<Devices>) {
        spUtil = SPUtil(this)
        spUtil.putObjectList("DevicesList", devicesList)
        binding.recyclerViewDevice.adapter?.notifyDataSetChanged()
    }
}