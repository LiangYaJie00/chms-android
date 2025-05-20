package com.example.chms_android.features_doctor.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.databinding.DialogAddAwardBinding
import com.example.chms_android.databinding.ItemAwardEditBinding
import com.example.chms_android.dto.AwardDTO
import com.example.chms_android.features_doctor.dialog.AddAwardDialog
import java.lang.Exception

class AwardEditAdapter(
    awards: List<AwardDTO>,
    private val onItemChanged: (List<AwardDTO>) -> Unit,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<AwardEditAdapter.AwardViewHolder>() {

    // 创建一个新的可变列表，而不是直接使用传入的列表
    private val awardList: MutableList<AwardDTO> = ArrayList()
    
    // 添加日志标签
    private val TAG = "AwardEditAdapter"
    
    init {
        // 在初始化时复制传入的列表
        awardList.addAll(awards.map { it.copy() })
        Log.d(TAG, "Adapter initialized with ${awardList.size} items")
    }

    inner class AwardViewHolder(private val binding: ItemAwardEditBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(award: AwardDTO, position: Int) {
            // 设置文本显示
            binding.tvAwardTitle.text = award.title ?: "未设置"
            binding.tvAwardDate.text = award.date ?: "未设置"
            binding.tvIssuer.text = award.issuer ?: "未设置"

            // 设置整个项目的点击事件，打开编辑弹窗
            binding.layoutAwardItem.setOnClickListener {
                showEditDialog(award, position)
            }

            // 设置删除按钮点击事件
            binding.btnDelete.setOnClickListener {
                onItemDeleted(position)
            }
        }

        // 显示编辑弹窗
        private fun showEditDialog(award: AwardDTO, position: Int) {
            val context = binding.root.context
            val dialog = AddAwardDialog(context)
            
            // 设置对话框标题
            dialog.setDialogTitle("编辑奖项")
            
            // 设置初始值
            dialog.setInitialValues(award.title ?: "", award.date ?: "", award.issuer ?: "")
            
            // 设置确认按钮监听器
            dialog.setOnConfirmListener { title, date, issuer ->
                try {
                    // 更新数据
                    awardList[position] = awardList[position].copy(
                        title = title,
                        date = date,
                        issuer = issuer
                    )
                    
                    // 通知数据变化
                    notifyItemChanged(position)
                    
                    // 回调通知数据变化
                    onItemChanged(awardList.map { it.copy() })
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating award: ${e.message}", e)
                }
            }
            
            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AwardViewHolder {
        val binding = ItemAwardEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AwardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AwardViewHolder, position: Int) {
        holder.bind(awardList[position], position)
    }

    override fun getItemCount(): Int = awardList.size

    fun updateData(newList: List<AwardDTO>) {
        Log.d(TAG, "updateData: old size=${awardList.size}, new size=${newList.size}")
        
        try {
            // 创建新列表的深拷贝，避免引用问题
            val copyList = newList.map { it.copy() }
            
            // 清空当前列表并添加新数据
            awardList.clear()
            awardList.addAll(copyList)
            
            Log.d(TAG, "updateData: after update, size=${awardList.size}")
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateData: ${e.message}", e)
        }
    }
    
    fun addItem(award: AwardDTO) {
        Log.d(TAG, "addItem: before add, size=${awardList.size}")
        try {
            awardList.add(award.copy())
            Log.d(TAG, "addItem: after add, size=${awardList.size}")
            notifyItemInserted(awardList.size - 1)
            onItemChanged(awardList.map { it.copy() })
        } catch (e: Exception) {
            Log.e(TAG, "Error in addItem: ${e.message}", e)
        }
    }
    
    fun removeItem(position: Int) {
        Log.d(TAG, "removeItem: before remove, size=${awardList.size}, position=$position")
        if (position >= 0 && position < awardList.size) {
            try {
                awardList.removeAt(position)
                Log.d(TAG, "removeItem: after remove, size=${awardList.size}")
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, awardList.size)
                onItemChanged(awardList.map { it.copy() })
            } catch (e: Exception) {
                Log.e(TAG, "Error in removeItem: ${e.message}", e)
            }
        } else {
            Log.e(TAG, "Invalid position for removal: $position, list size: ${awardList.size}")
        }
    }
}