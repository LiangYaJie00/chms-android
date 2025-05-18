package com.example.chms_android.features_doctor.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.features_doctor.fragment.AppointmentListFragment

class AppointmentPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AppointmentListFragment.newInstance(null) // 全部预约
            1 -> AppointmentListFragment.newInstance(AppointmentStatus.PENDING) // 待确认
            2 -> AppointmentListFragment.newInstance(AppointmentStatus.CONFIRMED) // 已确认
            3 -> AppointmentListFragment.newInstance(AppointmentStatus.COMPLETED) // 已完成
            4 -> AppointmentListFragment.newInstance(AppointmentStatus.CANCELLED) // 已取消
            else -> AppointmentListFragment.newInstance(null)
        }
    }
}