package com.example.chms_android.features.analysis.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.R
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.features.analysis.activity.ReportAnalysisActivity
import java.text.SimpleDateFormat
import java.util.Locale

class ReportAnalysisAdapter(
    private val context: Context,
    private var reportAnalysisList: List<ReportAnalysis>
) : RecyclerView.Adapter<ReportAnalysisAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardViewReportAnalysis)
        val tvReportDate: TextView = view.findViewById(R.id.tvReportAnalysisDate)
        val tvCreationMethod: TextView = view.findViewById(R.id.tvCreationMethod)
        val tvReportSummary: TextView = view.findViewById(R.id.tvReportSummary)
        val tvCreatedTime: TextView = view.findViewById(R.id.tvCreatedTime)
        val tvViewDetails: TextView = view.findViewById(R.id.tvViewDetails)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_analysis, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reportAnalysis = reportAnalysisList[position]
        
        // 设置日期标题（格式：2025年03月28日分析报告）
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        val dateStr = when {
            reportAnalysis.reportDate != null -> dateFormat.format(reportAnalysis.reportDate)
            reportAnalysis.createdAt != null -> dateFormat.format(reportAnalysis.createdAt)
            else -> "未知日期"
        }
        holder.tvReportDate.text = dateStr + "分析报告"
        
        // 设置创建方式（这里假设都是AI生成，如果有其他方式可以根据数据设置）
        holder.tvCreationMethod.text = "AI生成"
        
        // 设置摘要（取前50个字符）
        val summary = reportAnalysis.answer?.let {
            if (it.length > 50) it.substring(0, 50) + "..." else it
        } ?: "暂无分析内容"
        holder.tvReportSummary.text = summary
        
        // 设置创建时间
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val createdTimeStr = reportAnalysis.createdAt?.let {
            timeFormat.format(it)
        } ?: "未知"
        holder.tvCreatedTime.text = createdTimeStr
        
        // 设置详情按钮点击事件
        holder.tvViewDetails.setOnClickListener {
            val intent = Intent(context, ReportAnalysisActivity::class.java).apply {
                putExtra("reportAnalysis", reportAnalysis)
            }
            context.startActivity(intent)
        }
        
        // 设置卡片点击事件
        holder.cardView.setOnClickListener {
            val intent = Intent(context, ReportAnalysisActivity::class.java).apply {
                putExtra("reportAnalysis", reportAnalysis)
            }
            context.startActivity(intent)
        }
    }
    
    override fun getItemCount() = reportAnalysisList.size
    
    fun updateData(newList: List<ReportAnalysis>) {
        reportAnalysisList = newList
        notifyDataSetChanged()
    }
}