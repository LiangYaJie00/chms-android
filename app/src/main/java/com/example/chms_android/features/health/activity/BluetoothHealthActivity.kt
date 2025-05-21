package com.example.chms_android.features.health.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityBluetoothHealthBinding
import com.example.chms_android.features.health.adapter.BluetoothDeviceAdapter
import com.example.chms_android.features.health.viewmodel.BluetoothHealthViewModel
import com.example.chms_android.utils.BleManager
import com.example.chms_android.utils.ToastUtil
import java.math.BigDecimal

class BluetoothHealthActivity : AppCompatActivity(), BleManager.OnScanCallback, 
                                BleManager.OnConnectionCallback, BleManager.OnDataCallback {
    
    private lateinit var binding: ActivityBluetoothHealthBinding
    private lateinit var bleManager: BleManager
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private lateinit var viewModel: BluetoothHealthViewModel
    
    // 健康数据
    private var heartRate: Int = 0
    private var systolicPressure: Int = 0
    private var diastolicPressure: Int = 0
    private var arterialOxygenLevel: BigDecimal = BigDecimal.ZERO
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 初始化ViewModel
        viewModel = BluetoothHealthViewModel()
        
        // 初始化蓝牙管理器
        bleManager = BleManager.getInstance()
        if (!bleManager.initialize(this)) {
            ToastUtil.show(this, "设备不支持蓝牙")
            finish()
            return
        }
        
        // 设置数据回调
        bleManager.setDataCallback(this)
        
        // 初始化设备列表
        setupDeviceList()
        
        // 设置按钮点击事件
        setupButtons()
        
        // 检查权限并开启蓝牙
        checkPermissionsAndBluetooth()
    }
    
    private fun setupDeviceList() {
        deviceAdapter = BluetoothDeviceAdapter { device ->
            // 点击设备项时连接设备
            bleManager.connect(this, device, this)
        }
        
        binding.rvDevices.apply {
            layoutManager = LinearLayoutManager(this@BluetoothHealthActivity)
            adapter = deviceAdapter
        }
    }
    
    private fun setupButtons() {
        // 扫描按钮
        binding.btnScan.setOnClickListener {
            if (bleManager.isBluetoothEnabled()) {
                startScan()
            } else {
                bleManager.requestEnableBluetooth(this)
            }
        }
        
        // 停止扫描按钮
        binding.btnStopScan.setOnClickListener {
            bleManager.stopScan(this)
            binding.progressScan.visibility = View.GONE
            binding.btnScan.visibility = View.VISIBLE
            binding.btnStopScan.visibility = View.GONE
        }
        
        // 断开连接按钮
        binding.btnDisconnect.setOnClickListener {
            bleManager.disconnect()
            updateConnectionState(false)
        }
        
        // 保存健康数据按钮
        binding.btnSaveData.setOnClickListener {
            saveHealthData()
        }
        
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun checkPermissionsAndBluetooth() {
        // 检查位置权限
        if (!bleManager.checkLocationPermission(this)) {
            bleManager.requestLocationPermission(this)
            return
        }
        
        // 检查蓝牙权限（Android 12+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !bleManager.checkBluetoothPermissions(this)) {
            bleManager.requestBluetoothPermissions(this)
            return
        }
        
        // 检查蓝牙是否开启
        if (!bleManager.isBluetoothEnabled()) {
            bleManager.requestEnableBluetooth(this)
        }
    }
    
    private fun startScan() {
        deviceAdapter.clearDevices()
        binding.progressScan.visibility = View.VISIBLE
        binding.btnScan.visibility = View.GONE
        binding.btnStopScan.visibility = View.VISIBLE
        bleManager.startScan(this, this)
    }
    
    private fun updateConnectionState(connected: Boolean) {
        if (connected) {
            binding.layoutConnection.visibility = View.VISIBLE
            binding.layoutScan.visibility = View.GONE
        } else {
            binding.layoutConnection.visibility = View.GONE
            binding.layoutScan.visibility = View.VISIBLE
        }
    }
    
    private fun updateHealthData() {
        binding.tvHeartRate.text = "$heartRate 次/分"
        binding.tvBloodPressure.text = "$systolicPressure/$diastolicPressure mmHg"
        binding.tvOxygenLevel.text = "$arterialOxygenLevel%"
    }
    
    private fun saveHealthData() {
        // 使用ViewModel保存健康数据
        viewModel.saveHealthData(
            heartRate = heartRate,
            systolicPressure = systolicPressure,
            diastolicPressure = diastolicPressure,
            oxygenLevel = arterialOxygenLevel.toFloat(),
            context = this,
            onSuccess = {
                ToastUtil.show(this, "健康数据保存成功")
                finish()
            },
            onError = { errorMsg ->
                ToastUtil.show(this, "保存失败: $errorMsg")
            }
        )
    }
    
    // BleManager.OnScanCallback 实现
    override fun onScanStarted() {
        ToastUtil.show(this, "开始扫描设备")
    }
    
    override fun onDeviceFound(device: BluetoothDevice) {
        runOnUiThread {
            deviceAdapter.addDevice(device)
        }
    }
    
    override fun onScanFinished() {
        runOnUiThread {
            binding.progressScan.visibility = View.GONE
            binding.btnScan.visibility = View.VISIBLE
            binding.btnStopScan.visibility = View.GONE
            ToastUtil.show(this, "扫描完成")
        }
    }
    
    override fun onScanFailed(errorMessage: String) {
        runOnUiThread {
            binding.progressScan.visibility = View.GONE
            binding.btnScan.visibility = View.VISIBLE
            binding.btnStopScan.visibility = View.GONE
            ToastUtil.show(this, "扫描失败: $errorMessage")
        }
    }
    
    // BleManager.OnConnectionCallback 实现
    override fun onConnecting() {
        runOnUiThread {
            ToastUtil.show(this, "正在连接设备...")
        }
    }
    
    override fun onConnected() {
        runOnUiThread {
            ToastUtil.show(this, "设备连接成功")
            updateConnectionState(true)
        }
    }
    
    override fun onDisconnected() {
        runOnUiThread {
            ToastUtil.show(this, "设备已断开连接")
            updateConnectionState(false)
        }
    }
    
    override fun onConnectionFailed(errorMessage: String) {
        runOnUiThread {
            ToastUtil.show(this, "连接失败: $errorMessage")
            updateConnectionState(false)
        }
    }
    
    // BleManager.OnDataCallback 实现
    override fun onHeartRateReceived(heartRate: Int) {
        runOnUiThread {
            this.heartRate = heartRate
            updateHealthData()
        }
    }
    
    override fun onBloodPressureReceived(systolic: Int, diastolic: Int) {
        runOnUiThread {
            this.systolicPressure = systolic
            this.diastolicPressure = diastolic
            updateHealthData()
        }
    }
    
    override fun onOxygenLevelReceived(oxygenLevel: BigDecimal) {
        runOnUiThread {
            this.arterialOxygenLevel = oxygenLevel
            updateHealthData()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BleManager.REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 位置权限已授予，检查蓝牙权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!bleManager.checkBluetoothPermissions(this)) {
                            bleManager.requestBluetoothPermissions(this)
                        }
                    }
                } else {
                    ToastUtil.show(this, "需要位置权限才能扫描蓝牙设备")
                }
            }
            BleManager.REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // 蓝牙权限已授予
                    if (!bleManager.isBluetoothEnabled()) {
                        bleManager.requestEnableBluetooth(this)
                    }
                } else {
                    ToastUtil.show(this, "需要蓝牙权限才能使用此功能")
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BleManager.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // 蓝牙已开启
                startScan()
            } else {
                ToastUtil.show(this, "需要开启蓝牙才能使用此功能")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect()
        bleManager.stopScan(this)
    }
}
