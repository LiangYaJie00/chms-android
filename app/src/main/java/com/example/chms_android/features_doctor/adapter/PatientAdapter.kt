package com.example.chms_android.features_doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.chms_android.R
import com.example.chms_android.data.Patient
import com.example.chms_android.ui.components.CircularImageView
import java.util.Locale

class PatientAdapter(
    private val context: Context,
    private var patientList: List<Patient>,
    private val onPatientClickListener: OnPatientClickListener
) : RecyclerView.Adapter<PatientAdapter.ViewHolder>(), Filterable {

    private var filteredPatientList: List<Patient> = patientList

    interface OnPatientClickListener {
        fun onPatientClick(patient: Patient)
        fun onViewHealthRecord(patient: Patient)
        fun onViewAppointments(patient: Patient)
        fun onSendMessage(patient: Patient)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: CircularImageView = itemView.findViewById(R.id.iv_patient_avatar)
        val name: TextView = itemView.findViewById(R.id.tv_patient_name)
        val id: TextView = itemView.findViewById(R.id.tv_patient_id)
        val phone: TextView = itemView.findViewById(R.id.tv_patient_phone)
        val more: ImageView = itemView.findViewById(R.id.iv_patient_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patient = filteredPatientList[position]

        // 加载头像
        if (patient.avatarUrl != null) {
            Glide.with(context)
                .load(patient.avatarUrl)
                .transform(CircleCrop())
                .into(holder.avatar)
        } else {
            Glide.with(context)
                .load(R.mipmap.avatar_default)
                .transform(CircleCrop())
                .into(holder.avatar)
        }

        holder.name.text = patient.name
        holder.id.text = "ID: ${patient.id}"
        holder.phone.text = "电话: ${patient.phoneNumber}"

        // 设置点击事件
        holder.itemView.setOnClickListener {
            onPatientClickListener.onPatientClick(patient)
        }

        // 设置更多操作菜单
        holder.more.setOnClickListener { view ->
            showPopupMenu(view, patient)
        }
    }

    private fun showPopupMenu(view: View, patient: Patient) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_patient_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_health_record -> {
                    onPatientClickListener.onViewHealthRecord(patient)
                    true
                }
                R.id.action_view_appointments -> {
                    onPatientClickListener.onViewAppointments(patient)
                    true
                }
                R.id.action_send_message -> {
                    onPatientClickListener.onSendMessage(patient)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount(): Int = filteredPatientList.size

    fun updatePatients(newPatientList: List<Patient>) {
        patientList = newPatientList
        filteredPatientList = newPatientList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                filteredPatientList = if (charString.isEmpty()) {
                    patientList
                } else {
                    val filteredList = ArrayList<Patient>()
                    patientList.filter {
                        (it.name.lowercase(Locale.getDefault()).contains(charString.lowercase(Locale.getDefault())) ||
                                it.phoneNumber.contains(charString))
                    }.forEach { filteredList.add(it) }
                    filteredList
                }
                return FilterResults().apply { values = filteredPatientList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredPatientList = if (results?.values == null)
                    ArrayList()
                else
                    results.values as List<Patient>
                notifyDataSetChanged()
            }
        }
    }
}