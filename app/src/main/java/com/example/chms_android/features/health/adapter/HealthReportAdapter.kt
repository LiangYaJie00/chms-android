package com.example.chms_android.features.health.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.R
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.features.health.activity.DailyHealthReportActivity
import java.text.SimpleDateFormat
import java.util.Locale

class HealthReportAdapter(
    private val context: Context,
    private var reports: List<DailyHealthReport>
) : RecyclerView.Adapter<HealthReportAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReportDate: TextView = view.findViewById(R.id.tvReportDate)
        val tvHeartRate: TextView = view.findViewById(R.id.tvHeartRate)
        val tvBloodPressure: TextView = view.findViewById(R.id.tvBloodPressure)
        val tvBodyTemperature: TextView = view.findViewById(R.id.tvBodyTemperature)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        
        // 设置日期
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        holder.tvReportDate.text = report.reportDate?.let { dateFormat.format(it) + "日报" } ?: "未知日期"
        
        // 设置心率
        holder.tvHeartRate.text = report.heartRate?.toString()?.plus(" bpm") ?: "未记录"
        
        // 设置血压
        holder.tvBloodPressure.text = if (report.bloodPressureSystolic != null && report.bloodPressureDiastolic != null) {
            "${report.bloodPressureSystolic}/${report.bloodPressureDiastolic}"
        } else {
            "未记录"
        }
        
        // 设置体温
        holder.tvBodyTemperature.text = report.bodyTemperature?.toString()?.plus("°C") ?: "未记录"
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DailyHealthReportActivity::class.java).apply {
                putExtra("reportId", report.reportId)
                putExtra("entryMode", 1) // 从报告条目进入
                
                // 判断是否是当天的报告
                val today = java.util.Calendar.getInstance().time
                val reportDate = report.reportDate
                val isTodayReport = if (reportDate != null) {
                    val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    todayFormat.format(today) == todayFormat.format(reportDate)
                } else {
                    false
                }
                
                putExtra("isTodayReport", isTodayReport)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = reports.size

    // 更新数据
    fun updateData(newReports: List<DailyHealthReport>) {
        reports = newReports
        notifyDataSetChanged()
    }
}