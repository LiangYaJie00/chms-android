package com.example.chms_android.features.home.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.chms_android.R
import com.example.chms_android.data.Doctor
import com.example.chms_android.data.User
import com.example.chms_android.features.home.activity.DoctorDetailActivity
import com.example.chms_android.repo.UserRepo
import com.example.chms_android.ui.components.CircularImageView

class DoctorShowsAdapter(
    val context: Context,
    private var doctorList: List<Doctor>,
    private val onDoctorClickListener: OnDoctorClickListener? = null
) : RecyclerView.Adapter<DoctorShowsAdapter.ViewHolder>(){

    // 定义点击事件接口
    interface OnDoctorClickListener {
        fun onDoctorClick(doctor: Doctor)
    }

    // 默认头像资源ID
    private val defaultAvatarResId = R.mipmap.avatar_default

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DoctorShowsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_show, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorShowsAdapter.ViewHolder, position: Int) {
        val doctor = doctorList[position]

        // 设置基本信息
        holder.name.text = doctor.name
        holder.communityName.text = doctor.communityName ?: "未分配社区"
        
        // 先设置默认头像
        Glide.with(context)
            .load(defaultAvatarResId)
            .transform(CircleCrop())
            .into(holder.avatar)
        
        // 通过userId获取用户数据
        if (doctor.userId!! > 0) {
            UserRepo.getUserById(
                userId = doctor.userId!!,
                context = context,
                onSuccess = { user ->
                    // 如果用户有头像，则加载头像
                    if (!user.avatar.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(user.avatar)
                            .placeholder(defaultAvatarResId)
                            .error(defaultAvatarResId)
                            .transform(CircleCrop())
                            .into(holder.avatar)
                    } else {
                        // 如果用户没有头像，则根据性别加载默认头像
                        val avatarResId = if (user.gender == 1) {
                            R.mipmap.avatar_doctor01 // 男性默认头像
                        } else {
                            R.mipmap.avatar_doctor02 // 女性默认头像
                        }
                        
                        Glide.with(context)
                            .load(avatarResId)
                            .transform(CircleCrop())
                            .into(holder.avatar)
                    }
                },
                onError = { errorMsg ->
                    // 获取用户数据失败，使用默认头像
                    Log.e("DoctorShowsAdapter", "Error loading user data: $errorMsg")
                    
                    // 根据医生性别选择默认头像
                    val avatarResId = if (doctor.gender == 1) {
                        R.mipmap.avatar_doctor01 // 男性默认头像
                    } else {
                        R.mipmap.avatar_doctor02 // 女性默认头像
                    }
                    
                    Glide.with(context)
                        .load(avatarResId)
                        .transform(CircleCrop())
                        .into(holder.avatar)
                }
            )
        }
        
        // 设置点击事件
        holder.idsItem.setOnClickListener {
            // 如果设置了点击监听器，则调用监听器方法
            onDoctorClickListener?.onDoctorClick(doctor)
            
            // 如果没有设置监听器，则使用默认行为（向后兼容）
            if (onDoctorClickListener == null) {
                val intent = Intent(context, DoctorDetailActivity::class.java).apply {
                    putExtra("DOCTOR_ID", doctor.doctorId)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return doctorList.size
    }

    // 更新医生列表数据
    fun updateDoctors(newDoctorList: List<Doctor>) {
        doctorList = newDoctorList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val avatar = view.findViewById<CircularImageView>(R.id.civ_ids_avatar)
        val name = view.findViewById<TextView>(R.id.tv_ids_name)
        val communityName = view.findViewById<TextView>(R.id.tv_ids_community_name)
        val idsItem = view.findViewById<LinearLayout>(R.id.ll_ids_item)
    }
}