package com.example.chms_android.features_doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.R
import com.example.chms_android.common.enums.AdviceStatus
import com.example.chms_android.common.enums.AdviceType
import com.example.chms_android.data.Role
import com.example.chms_android.dto.DoctorAdviceDTO
import com.example.chms_android.utils.DateUtil
import com.example.chms_android.utils.AccountUtil

class DoctorAdviceAdapter(
    private val context: Context,
    private var advices: MutableList<DoctorAdviceDTO>,
    private val listener: OnAdviceClickListener
) : RecyclerView.Adapter<DoctorAdviceAdapter.AdviceViewHolder>() {

    // 获取当前用户角色
    private val accountUtil = AccountUtil(context)
    private val currentUserRole = accountUtil.getUser()?.role

    interface OnAdviceClickListener {
        fun onAdviceClick(advice: DoctorAdviceDTO)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdviceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_doctor_advice, parent, false)
        return AdviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdviceViewHolder, position: Int) {
        val advice = advices[position]
        holder.bind(advice)
    }

    override fun getItemCount(): Int = advices.size

    fun updateAdvices(newAdvices: List<DoctorAdviceDTO>) {
        advices.clear()
        advices.addAll(newAdvices)
        notifyDataSetChanged()
    }

    inner class AdviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvImportant: TextView = itemView.findViewById(R.id.tvImportant)
        private val tvRecipient: TextView = itemView.findViewById(R.id.tvRecipient)

        fun bind(advice: DoctorAdviceDTO) {
            // 设置标题和内容
            tvTitle.text = advice.title
            
            // 截断内容，最多显示50个字符
            val contentPreview = if (advice.content?.length ?: 0 > 50) {
                "${advice.content?.substring(0, 50)}..."
            } else {
                advice.content
            }
            tvContent.text = contentPreview
            
            // 设置日期
            val dateStr = advice.createdAt?.let { DateUtil.formatTimestamp(it) } ?: "未知时间"
            tvDate.text = dateStr
            
            // 设置类型
            val typeStr = advice.adviceType?.let { AdviceType.getNameByCode(it) } ?: "未知类型"
            tvType.text = typeStr
            
            // 设置状态
            val statusStr = advice.status?.let { AdviceStatus.getNameByCode(it) } ?: "未知状态"
            tvStatus.text = statusStr
            
            // 设置状态标签的背景颜色
            when (advice.status) {
                AdviceStatus.UNREAD.code -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_unread)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_unread_text))
                }
                AdviceStatus.READ.code -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_read)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_read_text))
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_tag)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primary))
                }
            }
            
            // 设置重要标记
            if (advice.isImportant == 1) {
                tvImportant.visibility = View.VISIBLE
            } else {
                tvImportant.visibility = View.GONE
            }
            
            // 根据当前用户角色动态设置接收者/发送者信息
            when (currentUserRole) {
                Role.doctor -> {
                    // 如果当前用户是医生，显示居民名字
                    tvRecipient.text = "居民：${advice.residentName ?: "未知"}"
                }
                Role.consumer -> {
                    // 如果当前用户是居民，显示医生名字
                    tvRecipient.text = "医生：${advice.doctorName ?: "未知"}"
                }
                else -> {
                    // 其他角色或未知角色，显示两者信息
                    tvRecipient.text = "医生：${advice.doctorName ?: "未知"} → 居民：${advice.residentName ?: "未知"}"
                }
            }
            
            // 设置点击事件
            cardView.setOnClickListener {
                listener.onAdviceClick(advice)
            }
        }
    }
}