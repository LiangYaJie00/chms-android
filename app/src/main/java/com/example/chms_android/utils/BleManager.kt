package com.example.chms_android.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.math.BigDecimal
import java.util.*

/**
 * 蓝牙BLE管理器
 */
class BleManager private constructor() {
    companion object {
        private const val TAG = "BleManager"
        
        // 常用的健康设备服务UUID
        val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")
        
        val BLOOD_PRESSURE_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
        val BLOOD_PRESSURE_MEASUREMENT_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb")
        
        val OXYGEN_SERVICE_UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")
        val OXYGEN_MEASUREMENT_UUID = UUID.fromString("00002A5E-0000-1000-8000-00805f9b34fb")
        
        // 权限请求码
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_LOCATION_PERMISSION = 2
        const val REQUEST_BLUETOOTH_PERMISSION = 3
        
        // 单例实例
        @Volatile
        private var instance: BleManager? = null
        
        fun getInstance(): BleManager {
            return instance ?: synchronized(this) {
                instance ?: BleManager().also { instance = it }
            }
        }
    }
    
    // 蓝牙适配器
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    
    // 扫描相关
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000 // 扫描时间10秒
    
    // 连接相关
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null
    
    // 回调接口
    private var scanCallback: OnScanCallback? = null
    private var connectionCallback: OnConnectionCallback? = null
    private var dataCallback: OnDataCallback? = null
    
    // 添加一个应用上下文的引用
    private lateinit var appContext: Context
    
    // 初始化蓝牙适配器
    fun initialize(context: Context): Boolean {
        appContext = context.applicationContext
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            Log.e(TAG, "设备不支持蓝牙")
            return false
        }
        
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        return true
    }
    
    // 检查蓝牙是否开启
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    // 请求开启蓝牙
    fun requestEnableBluetooth(activity: Activity) {
        if (!isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_PERMISSION)
                    return
                }
            }
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
    
    // 检查位置权限
    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    
    // 请求位置权限
    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }
    
    // 检查蓝牙权限（Android 12+）
    fun checkBluetoothPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
    
    // 请求蓝牙权限（Android 12+）
    fun requestBluetoothPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }
    }
    
    // 开始扫描设备
    fun startScan(context: Context, callback: OnScanCallback) {
        if (!isBluetoothEnabled()) {
            callback.onScanFailed("蓝牙未开启")
            return
        }
        
        if (!checkLocationPermission(context)) {
            callback.onScanFailed("缺少位置权限")
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkBluetoothPermissions(context)) {
            callback.onScanFailed("缺少蓝牙权限")
            return
        }
        
        this.scanCallback = callback
        
        if (scanning) {
            return
        }
        
        // 设置扫描过滤器，只扫描健康设备
        val filters = ArrayList<ScanFilter>()
        // 可以根据需要添加过滤器，例如只扫描特定服务UUID的设备
        
        // 设置扫描参数
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        // 延迟停止扫描
        handler.postDelayed({
            if (scanning) {
                stopScan(context)
                callback.onScanFinished()
            }
        }, SCAN_PERIOD)
        
        scanning = true
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner?.startScan(filters, settings, leScanCallback)
            callback.onScanStarted()
        } else {
            callback.onScanFailed("缺少蓝牙扫描权限")
        }
    }
    
    // 停止扫描
    fun stopScan(context: Context) {
        if (scanning) {
            scanning = false
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner?.stopScan(leScanCallback)
            }
        }
    }
    
    // 连接设备
    fun connect(context: Context, device: BluetoothDevice, callback: OnConnectionCallback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            callback.onConnectionFailed("缺少蓝牙连接权限")
            return
        }
        
        this.connectionCallback = callback
        this.connectedDevice = device
        
        // 断开现有连接
        bluetoothGatt?.close()
        
        // 连接新设备
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        callback.onConnecting()
    }
    
    // 断开连接
    fun disconnect() {
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                bluetoothGatt?.disconnect()
            }
            bluetoothGatt?.close()
            bluetoothGatt = null
            connectedDevice = null
        }
    }
    
    // 设置数据回调
    fun setDataCallback(callback: OnDataCallback) {
        this.dataCallback = callback
    }
    
    // 扫描回调
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanCallback?.onDeviceFound(result.device)
        }
        
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanning = false
            scanCallback?.onScanFailed("扫描失败，错误码: $errorCode")
        }
    }
    
    // GATT回调
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "连接成功")
                    connectionCallback?.onConnected()
                    
                    // 连接成功后发现服务
                    if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "断开连接")
                    connectionCallback?.onDisconnected()
                    gatt.close()
                    bluetoothGatt = null
                }
            } else {
                Log.e(TAG, "连接状态变化失败: $status")
                connectionCallback?.onConnectionFailed("连接失败，错误码: $status")
                gatt.close()
                bluetoothGatt = null
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "发现服务成功")
                
                // 查找并订阅健康相关的特征
                subscribeToHealthCharacteristics(gatt)
            } else {
                Log.e(TAG, "发现服务失败: $status")
            }
        }
        
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            // 处理特征值变化
            when (characteristic.uuid) {
                HEART_RATE_MEASUREMENT_UUID -> {
                    val heartRate = parseHeartRate(characteristic)
                    dataCallback?.onHeartRateReceived(heartRate)
                }
                BLOOD_PRESSURE_MEASUREMENT_UUID -> {
                    val bloodPressure = parseBloodPressure(characteristic)
                    dataCallback?.onBloodPressureReceived(bloodPressure.first, bloodPressure.second)
                }
                OXYGEN_MEASUREMENT_UUID -> {
                    val oxygenLevel = parseOxygenLevel(characteristic)
                    dataCallback?.onOxygenLevelReceived(oxygenLevel)
                }
            }
        }
    }
    
    // 订阅健康特征
    private fun subscribeToHealthCharacteristics(gatt: BluetoothGatt) {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        // 心率服务
        val heartRateService = gatt.getService(HEART_RATE_SERVICE_UUID)
        if (heartRateService != null) {
            val heartRateChar = heartRateService.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)
            if (heartRateChar != null) {
                // 启用通知
                gatt.setCharacteristicNotification(heartRateChar, true)
                val descriptor = heartRateChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        }
        
        // 血压服务
        val bloodPressureService = gatt.getService(BLOOD_PRESSURE_SERVICE_UUID)
        if (bloodPressureService != null) {
            val bloodPressureChar = bloodPressureService.getCharacteristic(BLOOD_PRESSURE_MEASUREMENT_UUID)
            if (bloodPressureChar != null) {
                gatt.setCharacteristicNotification(bloodPressureChar, true)
                val descriptor = bloodPressureChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        }
        
        // 血氧服务
        val oxygenService = gatt.getService(OXYGEN_SERVICE_UUID)
        if (oxygenService != null) {
            val oxygenChar = oxygenService.getCharacteristic(OXYGEN_MEASUREMENT_UUID)
            if (oxygenChar != null) {
                gatt.setCharacteristicNotification(oxygenChar, true)
                val descriptor = oxygenChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        }
    }
    
    // 解析心率数据
    private fun parseHeartRate(characteristic: BluetoothGattCharacteristic): Int {
        val flag = characteristic.value[0].toInt()
        return if (flag and 0x01 != 0) {
            // 心率值格式为uint16
            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1)
        } else {
            // 心率值格式为uint8
            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1)
        } ?: 0
    }
    
    // 解析血压数据
    private fun parseBloodPressure(characteristic: BluetoothGattCharacteristic): Pair<Int, Int> {
        // 收缩压
        val systolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 1)?.toInt() ?: 0
        // 舒张压
        val diastolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 3)?.toInt() ?: 0
        
        return Pair(systolic, diastolic)
    }
    
    // 解析血氧数据
    private fun parseOxygenLevel(characteristic: BluetoothGattCharacteristic): BigDecimal {
        val value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1) ?: 0
        return BigDecimal(value.toString())
    }
    
    // 扫描回调接口
    interface OnScanCallback {
        fun onScanStarted()
        fun onDeviceFound(device: BluetoothDevice)
        fun onScanFinished()
        fun onScanFailed(errorMessage: String)
    }
    
    // 连接回调接口
    interface OnConnectionCallback {
        fun onConnecting()
        fun onConnected()
        fun onDisconnected()
        fun onConnectionFailed(errorMessage: String)
    }
    
    // 数据回调接口
    interface OnDataCallback {
        fun onHeartRateReceived(heartRate: Int)
        fun onBloodPressureReceived(systolic: Int, diastolic: Int)
        fun onOxygenLevelReceived(oxygenLevel: BigDecimal)
    }
}