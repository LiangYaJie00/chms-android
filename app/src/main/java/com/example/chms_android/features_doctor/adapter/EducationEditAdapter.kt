package com.example.chms_android.features_doctor.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.databinding.ItemEducationEditBinding
import com.example.chms_android.dto.EducationDTO
import com.example.chms_android.features_doctor.dialog.AddEducationDialog

class EducationEditAdapter(
    educations: List<EducationDTO>,
    private val onItemChanged: (List<EducationDTO>) -> Unit,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<EducationEditAdapter.EducationViewHolder>() {

    // 创建一个新的可变列表，而不是直接使用传入的列表
    private val educationList: MutableList<EducationDTO> = ArrayList()
    
    // 添加日志标签
    private val TAG = "EducationEditAdapter"
    
    init {
        // 在初始化时复制传入的列表
        educationList.addAll(educations.map { it.copy() })
        Log.d(TAG, "Adapter initialized with ${educationList.size} items")
    }

    inner class EducationViewHolder(private val binding: ItemEducationEditBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(education: EducationDTO, position: Int) {
            // 设置文本显示
            binding.tvSchool.text = education.school ?: "未设置"
            binding.tvDegree.text = education.degree ?: "未设置"
            binding.tvMajor.text = education.major ?: "未设置"
            binding.tvStart.text = education.start ?: "未设置"
            binding.tvEnd.text = education.end ?: "未设置"

            // 设置整个项目的点击事件，打开编辑弹窗
            binding.layoutEducationItem.setOnClickListener {
                showEditDialog(education, position)
            }

            // 设置删除按钮点击事件
            binding.btnDelete.setOnClickListener {
                onItemDeleted(position)
            }
        }

        // 显示编辑弹窗
        private fun showEditDialog(education: EducationDTO, position: Int) {
            val context = binding.root.context
            val dialog = AddEducationDialog(context)

            dialog.setDialogTitle("编辑教育经历")
            
            // 设置初始值
            dialog.setInitialValues(
                education.school ?: "",
                education.degree ?: "",
                education.major ?: "",
                education.start ?: "",
                education.end ?: ""
            )
            
            // 设置确认按钮监听器
            dialog.setOnConfirmListener { school, degree, major, start, end ->
                try {
                    // 更新数据
                    educationList[position] = educationList[position].copy(
                        school = school,
                        degree = degree,
                        major = major,
                        start = start,
                        end = end
                    )
                    
                    // 通知数据变化
                    notifyItemChanged(position)
                    
                    // 回调通知数据变化
                    onItemChanged(educationList.map { it.copy() })
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating education: ${e.message}", e)
                }
            }
            
            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EducationViewHolder {
        val binding = ItemEducationEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EducationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EducationViewHolder, position: Int) {
        holder.bind(educationList[position], position)
    }

    override fun getItemCount(): Int = educationList.size

    fun updateData(newList: List<EducationDTO>) {
        Log.d(TAG, "updateData: old size=${educationList.size}, new size=${newList.size}")
        
        // 创建新列表的深拷贝，避免引用问题
        val copyList = newList.map { it.copy() }
        
        // 清空当前列表并添加新数据
        educationList.clear()
        educationList.addAll(copyList)
        
        Log.d(TAG, "updateData: after update, size=${educationList.size}")
        notifyDataSetChanged()
    }
    
    fun addItem(education: EducationDTO) {
        Log.d(TAG, "addItem: before add, size=${educationList.size}")
        try {
            educationList.add(education.copy())
            Log.d(TAG, "addItem: after add, size=${educationList.size}")
            notifyItemInserted(educationList.size - 1)
            onItemChanged(educationList.map { it.copy() })
        } catch (e: Exception) {
            Log.e(TAG, "Error in addItem: ${e.message}", e)
        }
    }
    
    fun removeItem(position: Int) {
        Log.d(TAG, "removeItem: before remove, size=${educationList.size}, position=$position")
        if (position >= 0 && position < educationList.size) {
            educationList.removeAt(position)
            Log.d(TAG, "removeItem: after remove, size=${educationList.size}")
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, educationList.size)
            onItemChanged(educationList.map { it.copy() })
        } else {
            Log.e(TAG, "Invalid position for removal: $position, list size: ${educationList.size}")
        }
    }
}