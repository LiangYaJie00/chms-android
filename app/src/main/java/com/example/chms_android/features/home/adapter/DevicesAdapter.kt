package com.example.chms_android.features.home.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chms_android.R
import com.example.chms_android.data.Devices
import com.example.chms_android.features.home.vm.DeviceManagerActivityVM
import com.example.chms_android.utils.MyItemTouchCallback

class DevicesAdapter(
    val context: Context,
    private var devicesList: ArrayList<Devices>,
    private val viewModel: DeviceManagerActivityVM
) : RecyclerView.Adapter<DevicesAdapter.ViewHolder>(), MyItemTouchCallback.ItemTouchResultCallback{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.device_item, parent, false)
        val viewHolder = ViewHolder(view)
        // 测试点击事件是否准确
        viewHolder.imageDevice.setOnClickListener {
            Log.d(
                "DevicesAdapter",
                "position at recyclerView is ${viewHolder.adapterPosition}, and the content is ${devicesList[viewHolder.adapterPosition].name}"
            )
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DevicesAdapter.ViewHolder, position: Int) {
        val device = devicesList[position]
        holder.tvDeviceName.text = device.name
        Glide.with(context).load(device.imageId).into(holder.imageDevice)
    }

    override fun getItemCount(): Int {
        return devicesList.size ?: 0
    }

    override fun onItemMove(startPosition: Int, endPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemDelete(position: Int) {
        devicesList.removeAt(position)
        viewModel.updateDevicesList(devicesList)
        notifyItemRemoved(position)
    }

    fun setData(devices: List<Devices>) {
        this.devicesList.clear()
        this.devicesList.addAll(devices)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageDevice: ImageView = view.findViewById(R.id.image_device)
        val tvDeviceName: TextView = view.findViewById(R.id.tv_device_name)
    }
}