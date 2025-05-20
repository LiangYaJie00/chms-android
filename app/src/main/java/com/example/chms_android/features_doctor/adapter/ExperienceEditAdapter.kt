package com.example.chms_android.features_doctor.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.databinding.ItemExperienceEditBinding
import com.example.chms_android.dto.ExperienceDTO
import com.example.chms_android.features_doctor.dialog.AddExperienceDialog

class ExperienceEditAdapter(
    experiences: List<ExperienceDTO>,
    private val onItemChanged: (List<ExperienceDTO>) -> Unit,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<ExperienceEditAdapter.ExperienceViewHolder>() {

    // 创建一个新的可变列表，而不是直接使用传入的列表
    private val experienceList: MutableList<ExperienceDTO> = ArrayList()
    
    // 添加日志标签
    private val TAG = "ExperienceEditAdapter"
    
    init {
        // 在初始化时复制传入的列表
        experienceList.addAll(experiences.map { it.copy() })
        Log.d(TAG, "Adapter initialized with ${experienceList.size} items")
    }

    inner class ExperienceViewHolder(private val binding: ItemExperienceEditBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(experience: ExperienceDTO, position: Int) {
            // 设置文本显示
            binding.tvHospital.text = experience.hospital ?: "未设置"
            binding.tvPosition.text = experience.position ?: "未设置"
            binding.tvStart.text = experience.start ?: "未设置"
            binding.tvEnd.text = experience.end ?: "未设置"

            // 设置整个项目的点击事件，打开编辑弹窗
            binding.layoutExperienceItem.setOnClickListener {
                showEditDialog(experience, position)
            }

            // 设置删除按钮点击事件
            binding.btnDelete.setOnClickListener {
                onItemDeleted(position)
            }
        }

        // 显示编辑弹窗
        private fun showEditDialog(experience: ExperienceDTO, position: Int) {
            val context = binding.root.context
            val dialog = AddExperienceDialog(context)

            dialog.setDialogTitle("编辑工作经历")
            
            // 设置初始值
            dialog.setInitialValues(
                experience.hospital ?: "",
                experience.position ?: "",
                experience.start ?: "",
                experience.end ?: ""
            )
            
            // 设置确认按钮监听器
            dialog.setOnConfirmListener { hospital, position, start, end ->
                try {
                    // 更新数据
                    experienceList[adapterPosition] = experienceList[adapterPosition].copy(
                        hospital = hospital,
                        position = position,
                        start = start,
                        end = end
                    )
                    
                    // 通知数据变化
                    notifyItemChanged(adapterPosition)
                    
                    // 回调通知数据变化
                    onItemChanged(experienceList.map { it.copy() })
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating experience: ${e.message}", e)
                }
            }
            
            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val binding = ItemExperienceEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExperienceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        holder.bind(experienceList[position], position)
    }

    override fun getItemCount(): Int = experienceList.size

    fun updateData(newList: List<ExperienceDTO>) {
        Log.d(TAG, "updateData: old size=${experienceList.size}, new size=${newList.size}")
        
        // 创建新列表的深拷贝，避免引用问题
        val copyList = newList.map { it.copy() }
        
        // 清空当前列表并添加新数据
        experienceList.clear()
        experienceList.addAll(copyList)
        
        Log.d(TAG, "updateData: after update, size=${experienceList.size}")
        notifyDataSetChanged()
    }
    
    fun addItem(experience: ExperienceDTO) {
        Log.d(TAG, "addItem: before add, size=${experienceList.size}")
        try {
            experienceList.add(experience.copy())
            Log.d(TAG, "addItem: after add, size=${experienceList.size}")
            notifyItemInserted(experienceList.size - 1)
            onItemChanged(experienceList.map { it.copy() })
        } catch (e: Exception) {
            Log.e(TAG, "Error in addItem: ${e.message}", e)
        }
    }
    
    fun removeItem(position: Int) {
        Log.d(TAG, "removeItem: before remove, size=${experienceList.size}, position=$position")
        if (position >= 0 && position < experienceList.size) {
            experienceList.removeAt(position)
            Log.d(TAG, "removeItem: after remove, size=${experienceList.size}")
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, experienceList.size)
            onItemChanged(experienceList.map { it.copy() })
        } else {
            Log.e(TAG, "Invalid position for removal: $position, list size: ${experienceList.size}")
        }
    }
}