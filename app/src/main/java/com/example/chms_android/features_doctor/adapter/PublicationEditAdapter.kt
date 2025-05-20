package com.example.chms_android.features_doctor.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.databinding.ItemPublicationEditBinding
import com.example.chms_android.dto.PublicationDTO
import com.example.chms_android.features_doctor.dialog.AddPublicationDialog

class PublicationEditAdapter(
    publications: List<PublicationDTO>,
    private val onItemChanged: (List<PublicationDTO>) -> Unit,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<PublicationEditAdapter.PublicationViewHolder>() {

    // 创建一个新的可变列表，而不是直接使用传入的列表
    private val publicationList: MutableList<PublicationDTO> = ArrayList()
    
    // 添加日志标签
    private val TAG = "PublicationEditAdapter"
    
    init {
        // 在初始化时复制传入的列表
        publicationList.addAll(publications.map { it.copy() })
        Log.d(TAG, "Adapter initialized with ${publicationList.size} items")
    }

    inner class PublicationViewHolder(private val binding: ItemPublicationEditBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(publication: PublicationDTO, position: Int) {
            // 设置文本显示
            binding.tvPublicationTitle.text = publication.title ?: "未设置"
            binding.tvJournal.text = publication.journal ?: "未设置"
            binding.tvPublishDate.text = publication.publishDate ?: "未设置"

            // 设置整个项目的点击事件，打开编辑弹窗
            binding.layoutPublicationItem.setOnClickListener {
                showEditDialog(publication, position)
            }

            // 设置删除按钮点击事件
            binding.btnDelete.setOnClickListener {
                onItemDeleted(position)
            }
        }

        // 显示编辑弹窗
        private fun showEditDialog(publication: PublicationDTO, position: Int) {
            val context = binding.root.context
            val dialog = AddPublicationDialog(context)

            dialog.setDialogTitle("编辑出版物")
            
            // 设置初始值
            dialog.setInitialValues(
                publication.title ?: "",
                publication.journal ?: "",
                publication.publishDate ?: ""
            )
            
            // 设置确认按钮监听器
            dialog.setOnConfirmListener { title, journal, publishDate ->
                try {
                    // 更新数据
                    publicationList[position] = publicationList[position].copy(
                        title = title,
                        journal = journal,
                        publishDate = publishDate
                    )
                    
                    // 通知数据变化
                    notifyItemChanged(position)
                    
                    // 回调通知数据变化
                    onItemChanged(publicationList.map { it.copy() })
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating publication: ${e.message}", e)
                }
            }
            
            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicationViewHolder {
        val binding = ItemPublicationEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PublicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PublicationViewHolder, position: Int) {
        holder.bind(publicationList[position], position)
    }

    override fun getItemCount(): Int = publicationList.size

    fun updateData(newList: List<PublicationDTO>) {
        Log.d(TAG, "updateData: old size=${publicationList.size}, new size=${newList.size}")
        
        // 创建新列表的深拷贝，避免引用问题
        val copyList = newList.map { it.copy() }
        
        // 清空当前列表并添加新数据
        publicationList.clear()
        publicationList.addAll(copyList)
        
        Log.d(TAG, "updateData: after update, size=${publicationList.size}")
        notifyDataSetChanged()
    }
    
    fun addItem(publication: PublicationDTO) {
        Log.d(TAG, "addItem: before add, size=${publicationList.size}")
        try {
            publicationList.add(publication)
            notifyItemInserted(publicationList.size - 1)
            onItemChanged(publicationList)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding publication: ${e.message}", e)
        }
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < publicationList.size) {
            publicationList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, publicationList.size)
            onItemChanged(publicationList)
        }
    }
}