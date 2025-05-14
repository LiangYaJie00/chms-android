package com.example.chms_android.features.health.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chms_android.features.health.fragment.HealthInfoEditFragment
import com.example.chms_android.features.health.fragment.HealthInfoViewFragment

class HealthInfoPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HealthInfoViewFragment()
            1 -> HealthInfoEditFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}