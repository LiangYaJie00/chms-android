package com.example.chms_android.features.health.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chms_android.features.health.fragment.DailyHealthReportViewFragment
import com.example.chms_android.features.health.fragment.DailyHealthReportEditFragment

class DailyHealthReportPagerAdapter(
    activity: FragmentActivity, 
    private val reportId: Int = 0,
    private val showViewOnly: Boolean = false,
    private val showEditOnly: Boolean = false
) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = when {
        showViewOnly -> 1  // 只显示预览页面
        showEditOnly -> 1  // 只显示编辑页面
        else -> 2          // 显示预览和编辑页面
    }
    
    override fun createFragment(position: Int): Fragment {
        return when {
            showViewOnly -> DailyHealthReportViewFragment.newInstance(reportId)
            showEditOnly -> DailyHealthReportEditFragment.newInstance(reportId)
            else -> {
                when (position) {
                    0 -> DailyHealthReportViewFragment.newInstance(reportId)
                    1 -> DailyHealthReportEditFragment.newInstance(reportId)
                    else -> throw IllegalArgumentException("Invalid position: $position")
                }
            }
        }
    }
}