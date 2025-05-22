package com.example.chms_android.features_doctor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.R
import com.example.chms_android.databinding.ItemTimeSlotBinding
import com.example.chms_android.dto.DoctorAvailableTimeDTO

class TimeSlotAdapter(
    private val timeSlots: List<DoctorAvailableTimeDTO>,
    private val listener: TimeSlotListener
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    
    interface TimeSlotListener {
        fun onDeleteTimeSlot(timeSlot: DoctorAvailableTimeDTO)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeSlotViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }
    
    override fun getItemCount(): Int = timeSlots.size
    
    inner class TimeSlotViewHolder(private val binding: ItemTimeSlotBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(timeSlot: DoctorAvailableTimeDTO) {
            binding.tvTimeSlot.text = "${timeSlot.startTime} - ${timeSlot.endTime}"
            
            // 根据是否已预约设置不同的背景颜色
            if (timeSlot.isBooked == true) {
                binding.cardTimeSlot.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.light_gray)
                )
                binding.tvStatus.text = "已预约"
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorAccent)
                )
                binding.btnDelete.isEnabled = false
            } else {
                binding.cardTimeSlot.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
                binding.tvStatus.text = "可预约"
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                )
                binding.btnDelete.isEnabled = true
            }
            
            binding.btnDelete.setOnClickListener {
                listener.onDeleteTimeSlot(timeSlot)
            }
        }
    }
}