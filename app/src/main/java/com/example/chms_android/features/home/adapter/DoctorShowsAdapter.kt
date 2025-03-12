package com.example.chms_android.features.home.adapter

import android.content.Context
import android.content.Intent
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
import com.example.chms_android.features.home.activity.DoctorDetailActivity
import com.example.chms_android.ui.components.CircularImageView
import kotlin.random.Random

class DoctorShowsAdapter(
    val context: Context,
    private var doctorList: List<Doctor>
) : RecyclerView.Adapter<DoctorShowsAdapter.ViewHolder>(){

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

        val avatarResources = listOf(
            R.mipmap.avatar_doctor01, // 首先，请确保这些资源 ID 是正确的
            R.mipmap.avatar_doctor02,
            R.mipmap.avatar_doctor03,
            R.mipmap.avatar_doctor04
        )
        // 从列表中选择一个随机的资源 ID
        val randomAvatarResId = avatarResources[Random.nextInt(avatarResources.size)]
        Glide.with(context).load(randomAvatarResId)
            .transform(CircleCrop())
            .into(holder.avatar)

        holder.name.text = doctor.name
        holder.communityName.text = doctor.communityName
        holder.idsItem.setOnClickListener {
            val intent = Intent(context, DoctorDetailActivity::class.java).apply {
                putExtra("doctorData", doctor)
                putExtra("doctorAvatar", randomAvatarResId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return doctorList.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val avatar = view.findViewById<CircularImageView>(R.id.civ_ids_avatar)
        val name = view.findViewById<TextView>(R.id.tv_ids_name)
        val communityName = view.findViewById<TextView>(R.id.tv_ids_community_name)
        val idsItem = view.findViewById<LinearLayout>(R.id.ll_ids_item)

    }
}