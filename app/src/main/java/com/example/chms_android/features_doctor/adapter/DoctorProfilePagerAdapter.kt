package com.example.chms_android.features_doctor.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chms_android.features_doctor.fragment.DoctorProfileEditFragment
import com.example.chms_android.features_doctor.fragment.DoctorProfileViewFragment

class DoctorProfilePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DoctorProfileViewFragment.newInstance()
            1 -> DoctorProfileEditFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}