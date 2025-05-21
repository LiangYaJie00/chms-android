package com.example.chms_android.features_doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.chms_android.R
import com.example.chms_android.data.User
import android.content.Intent
import com.example.chms_android.features_doctor.activity.ResidentDetailActivity

class ResidentAdapter(
    private val context: Context,
    private var residentList: List<User>,
    private val onResidentClickListener: OnResidentClickListener
) : RecyclerView.Adapter<ResidentAdapter.ViewHolder>(), Filterable {

    private var filteredResidentList: List<User> = residentList

    interface OnResidentClickListener {
        fun onResidentClick(resident: User)
        fun onViewHealthRecord(resident: User)
        fun onViewAppointments(resident: User)
        fun onViewHealthReports(resident: User)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.iv_resident_avatar)
        val name: TextView = view.findViewById(R.id.tv_resident_name)
        val community: TextView = view.findViewById(R.id.tv_resident_community)
        val gender: TextView = view.findViewById(R.id.tv_resident_gender)
        val phone: TextView = view.findViewById(R.id.tv_resident_phone)
        val more: ImageView = view.findViewById(R.id.iv_resident_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resident, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resident = filteredResidentList[position]

        // 加载头像
        if (resident.avatar != null) {
            Glide.with(context)
                .load(resident.avatar)
                .transform(CircleCrop())
                .into(holder.avatar)
        } else {
            Glide.with(context)
                .load(R.mipmap.avatar_default)
                .transform(CircleCrop())
                .into(holder.avatar)
        }

        holder.name.text = resident.name
        holder.gender.text = if (resident.gender == 1) "男" else if (resident.gender == 0) "女" else "性别未知"
        holder.community.text = "社区: ${resident.community ?: "未分配"}"
        holder.phone.text = "电话: ${resident.phone}"

        // 设置点击事件
        holder.itemView.setOnClickListener {
            onResidentClickListener.onResidentClick(resident)
        }

        // 设置更多操作菜单
        holder.more.setOnClickListener { view ->
            showPopupMenu(view, resident)
        }
    }

    private fun showPopupMenu(view: View, resident: User) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_resident_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_health_record -> {
                    onResidentClickListener.onViewHealthRecord(resident)
                    true
                }
                R.id.action_view_appointments -> {
                    onResidentClickListener.onViewAppointments(resident)
                    true
                }
                R.id.action_view_health_reports -> {
                    onResidentClickListener.onViewHealthReports(resident)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount(): Int = filteredResidentList.size

    fun updateResidents(newResidentList: List<User>) {
        residentList = newResidentList
        filteredResidentList = newResidentList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                filteredResidentList = if (charString.isEmpty()) {
                    residentList
                } else {
                    residentList.filter {
                        it.name?.contains(charString, ignoreCase = true) == true ||
                        it.phone?.toString()?.contains(charString) == true ||
                        it.email?.contains(charString, ignoreCase = true) == true
                    }
                }
                return FilterResults().apply { values = filteredResidentList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredResidentList = results?.values as? List<User> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }
}