package com.example.chms_android.features_doctor.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.databinding.DialogAddCertificateBinding
import com.example.chms_android.databinding.ItemCertificateEditBinding
import com.example.chms_android.dto.CertificateDTO
import com.example.chms_android.features_doctor.dialog.AddCertificateDialog

class CertificateEditAdapter(
    certificates: List<CertificateDTO>,
    private val onItemChanged: (List<CertificateDTO>) -> Unit,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<CertificateEditAdapter.CertificateViewHolder>() {

    // 创建一个新的可变列表，而不是直接使用传入的列表
    private val certificateList: MutableList<CertificateDTO> = ArrayList()
    
    // 添加日志标签
    private val TAG = "CertificateEditAdapter"
    
    init {
        // 在初始化时复制传入的列表
        certificateList.addAll(certificates.map { it.copy() })
        Log.d(TAG, "Adapter initialized with ${certificateList.size} items")
    }

    inner class CertificateViewHolder(private val binding: ItemCertificateEditBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(certificate: CertificateDTO, position: Int) {
            // 设置文本显示
            binding.tvCertificateName.text = certificate.name ?: "未设置"
            binding.tvGetDate.text = certificate.getDate ?: "未设置"

            // 设置整个项目的点击事件，打开编辑弹窗
            binding.layoutCertificateItem.setOnClickListener {
                showEditDialog(certificate, position)
            }

            // 设置删除按钮点击事件
            binding.btnDelete.setOnClickListener {
                onItemDeleted(position)
            }
        }

        // 显示编辑弹窗
        private fun showEditDialog(certificate: CertificateDTO, position: Int) {
            val context = binding.root.context
            val dialog = AddCertificateDialog(context)

            dialog.setDialogTitle("编辑证书")

            // 设置初始值
            dialog.setInitialValues(certificate.name ?: "", certificate.getDate ?: "")
            
            // 设置确认按钮监听器
            dialog.setOnConfirmListener { name, getDate ->
                try {
                    // 更新数据
                    certificateList[position] = certificateList[position].copy(
                        name = name,
                        getDate = getDate
                    )
                    
                    // 通知数据变化
                    notifyItemChanged(position)
                    
                    // 回调通知数据变化
                    onItemChanged(certificateList.map { it.copy() })
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating certificate: ${e.message}", e)
                }
            }
            
            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateViewHolder {
        val binding = ItemCertificateEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CertificateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) {
        holder.bind(certificateList[position], position)
    }

    override fun getItemCount(): Int = certificateList.size

    fun updateData(newList: List<CertificateDTO>) {
        Log.d(TAG, "updateData: old size=${certificateList.size}, new size=${newList.size}")
        
        // 创建新列表的深拷贝，避免引用问题
        val copyList = newList.map { it.copy() }
        
        // 清空当前列表并添加新数据
        certificateList.clear()
        certificateList.addAll(copyList)
        
        Log.d(TAG, "updateData: after update, size=${certificateList.size}")
        notifyDataSetChanged()
    }
    
    fun addItem(certificate: CertificateDTO) {
        Log.d(TAG, "addItem: before add, size=${certificateList.size}")
        try {
            certificateList.add(certificate.copy())
            Log.d(TAG, "addItem: after add, size=${certificateList.size}")
            notifyItemInserted(certificateList.size - 1)
            onItemChanged(certificateList.map { it.copy() })
        } catch (e: Exception) {
            Log.e(TAG, "Error in addItem: ${e.message}", e)
        }
    }
    
    fun removeItem(position: Int) {
        Log.d(TAG, "removeItem: before remove, size=${certificateList.size}, position=$position")
        if (position >= 0 && position < certificateList.size) {
            certificateList.removeAt(position)
            Log.d(TAG, "removeItem: after remove, size=${certificateList.size}")
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, certificateList.size)
            onItemChanged(certificateList.map { it.copy() })
        } else {
            Log.e(TAG, "Invalid position for removal: $position, list size: ${certificateList.size}")
        }
    }
}