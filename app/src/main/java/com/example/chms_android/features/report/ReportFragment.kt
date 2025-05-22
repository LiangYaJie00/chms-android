package com.example.chms_android.features.report

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.chms_android.MainActivity
import com.example.chms_android.R
import com.example.chms_android.databinding.FragmentReportBinding
import com.example.chms_android.features.report.activity.DailyReportActivity
import com.example.chms_android.features.analysis.activity.ReportAnalysisActivity
import com.example.chms_android.features.analysis.activity.ReportAnalysisShowActivity
import com.example.chms_android.features.health.activity.HealthReportHistoryActivity

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        
        // 检查是否处于离线模式
        val mainActivity = activity as MainActivity
        if (mainActivity.isInOfflineMode()) {
            showOfflineBanner()
        }
        
        binding.cvDailyReport.setOnClickListener {
//            val intent = Intent(context, DailyReportActivity::class.java).apply {
//                putExtra("status", 0)
//            }
//            startActivity(intent)
            val intent = Intent(context, HealthReportHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.cvMonthReport.setOnClickListener {
            val intent = Intent(context, DailyReportActivity::class.java).apply {
                putExtra("status", 1)
            }
            startActivity(intent)
        }

        binding.cvWeeklyReport.setOnClickListener {
            val intent = Intent(context, ReportAnalysisShowActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReportFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private fun showOfflineBanner() {
        try {
            // 创建离线模式横幅
            val offlineBanner = layoutInflater.inflate(R.layout.offline_mode_banner, null)
            
            // 获取容器
            val container = binding.fragmentReport
            
            if (container != null && container is ConstraintLayout) {
                // 添加横幅到ConstraintLayout
                container.addView(offlineBanner)
                
                // 设置横幅的布局参数
                val params = offlineBanner.layoutParams as ConstraintLayout.LayoutParams
                
                // 设置横幅的ID，以便在约束中引用
                offlineBanner.id = View.generateViewId()
                
                // 设置横幅的约束：顶部连接到TitleBar的底部
                params.topToBottom = R.id.titleBar_fr
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                
                // 应用布局参数
                offlineBanner.layoutParams = params
                
                // 更新cv_daily_report的约束，使其顶部连接到横幅的底部
                val dailyReportCard = container.findViewById<CardView>(R.id.cv_daily_report)
                val dailyReportParams = dailyReportCard.layoutParams as ConstraintLayout.LayoutParams
                dailyReportParams.topToBottom = offlineBanner.id
                dailyReportCard.layoutParams = dailyReportParams
                
                // 设置重试按钮点击事件
                offlineBanner.findViewById<Button>(R.id.btn_retry_connection)?.setOnClickListener {
                    val mainActivity = activity as MainActivity
                    mainActivity.checkServerAndReconnect { isConnected ->
                        if (isConnected) {
                            // 如果连接成功，移除横幅并恢复原始约束
                            container.removeView(offlineBanner)
                            
                            // 恢复cv_daily_report的约束
                            val restoredParams = dailyReportCard.layoutParams as ConstraintLayout.LayoutParams
                            restoredParams.topToBottom = R.id.titleBar_fr
                            dailyReportCard.layoutParams = restoredParams
                        }
                    }
                }
            } else {
                Log.e("ReportFragment", "Container view is null or not a ConstraintLayout")
            }
        } catch (e: Exception) {
            Log.e("ReportFragment", "Error showing offline banner: ${e.message}")
            e.printStackTrace()
        }
    }
}
