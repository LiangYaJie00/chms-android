package com.example.chms_android.features.health.adapter

import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.databinding.ItemBluetoothDeviceBinding

class BluetoothDeviceAdapter(private val onDeviceClick: (BluetoothDevice) -> Unit) : 
    RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {
    
    private val devices = mutableListOf<BluetoothDevice>()
    
    fun addDevice(device: BluetoothDevice) {
        // 检查设备是否已存在
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }
    
    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemBluetoothDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class DeviceViewHolder(private val binding: ItemBluetoothDeviceBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(device: BluetoothDevice) {
            // 获取设备名称，如果没有则显示MAC地址
            val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(binding.root.context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    device.name ?: "未知设备"
                } else {
                    "未知设备"
                }
            } else {
                device.name ?: "未知设备"
            }
            
            binding.tvDeviceName.text = deviceName
            binding.tvDeviceAddress.text = device.address
            
            binding.root.setOnClickListener {
                onDeviceClick(device)
            }
        }
    }
}