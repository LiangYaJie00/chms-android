package com.example.chms_android.features_doctor.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.databinding.FragmentAppointmentListBinding
import com.example.chms_android.dto.AppointmentDTO
import com.example.chms_android.features.appointment.activity.AppointmentDetailActivity
import com.example.chms_android.features.appointment.adapter.AppointmentAdapter
import com.example.chms_android.features_doctor.activity.DoctorAvailableTimeActivity
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppointmentListFragment : Fragment(), AppointmentAdapter.AppointmentListener {

    private var _binding: FragmentAppointmentListBinding? = null
    private val binding get() = _binding!!
    private val allAppointments = mutableListOf<AppointmentDTO>()
    private val filteredAppointments = mutableListOf<AppointmentDTO>()
    private lateinit var adapter: AppointmentAdapter
    private var status: AppointmentStatus? = null
    private var selectedDate: Date? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    companion object {
        private const val ARG_STATUS = "status"

        fun newInstance(status: AppointmentStatus?): AppointmentListFragment {
            val fragment = AppointmentListFragment()
            val args = Bundle()
            if (status != null) {
                args.putSerializable(ARG_STATUS, status)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = it.getSerializable(ARG_STATUS) as? AppointmentStatus
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupStatusChips()
        setupDateFilter()
        setupAddButton()
        loadAppointments()
    }

    override fun onResume() {
        super.onResume()
        loadAppointments()
    }

    private fun setupRecyclerView() {
        adapter = AppointmentAdapter(filteredAppointments, this, isPatient = false)
        binding.recyclerViewAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AppointmentListFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterAppointments()
            }
        })
    }

    private fun setupStatusChips() {
        // 设置初始选中状态
        updateChipSelection(status)

        // 设置点击事件
        binding.chipAll.setOnClickListener {
            status = null
            updateChipSelection(null)
            filterAppointments()
        }

        binding.chipPending.setOnClickListener {
            status = AppointmentStatus.PENDING
            updateChipSelection(AppointmentStatus.PENDING)
            filterAppointments()
        }

        binding.chipConfirmed.setOnClickListener {
            status = AppointmentStatus.CONFIRMED
            updateChipSelection(AppointmentStatus.CONFIRMED)
            filterAppointments()
        }

        binding.chipCompleted.setOnClickListener {
            status = AppointmentStatus.COMPLETED
            updateChipSelection(AppointmentStatus.COMPLETED)
            filterAppointments()
        }

        binding.chipCancelled.setOnClickListener {
            status = AppointmentStatus.CANCELLED
            updateChipSelection(AppointmentStatus.CANCELLED)
            filterAppointments()
        }
    }

    private fun updateChipSelection(selectedStatus: AppointmentStatus?) {
        // 重置所有chip的样式
        binding.chipAll.setBackgroundResource(R.drawable.bg_chip_unselected)
        binding.chipAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        
        binding.chipPending.setBackgroundResource(R.drawable.bg_chip_unselected)
        binding.chipPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        
        binding.chipConfirmed.setBackgroundResource(R.drawable.bg_chip_unselected)
        binding.chipConfirmed.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        
        binding.chipCompleted.setBackgroundResource(R.drawable.bg_chip_unselected)
        binding.chipCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        
        binding.chipCancelled.setBackgroundResource(R.drawable.bg_chip_unselected)
        binding.chipCancelled.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))

        // 设置选中chip的样式
        val selectedChip = when (selectedStatus) {
            null -> binding.chipAll
            AppointmentStatus.PENDING -> binding.chipPending
            AppointmentStatus.CONFIRMED -> binding.chipConfirmed
            AppointmentStatus.COMPLETED -> binding.chipCompleted
            AppointmentStatus.CANCELLED -> binding.chipCancelled
            else -> binding.chipAll
        }

        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected)
        selectedChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setupDateFilter() {
        binding.tvDateFilter.setOnClickListener {
            showDatePicker()
        }

        // 初始化日期筛选文本
        updateDateFilterText()
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                selectedDate = calendar.time
                updateDateFilterText()
                filterAppointments()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }

    private fun updateDateFilterText() {
        if (selectedDate != null) {
            binding.tvDateFilter.text = "日期: ${dateFormat.format(selectedDate!!)}"
        } else {
            binding.tvDateFilter.text = "选择日期"
        }
    }

    private fun setupAddButton() {
        binding.fabAddAppointment.setOnClickListener {
            // 跳转到预约时间管理页面
            val intent = Intent(requireContext(), DoctorAvailableTimeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadAppointments() {
        val doctorId = AccountUtil(requireContext()).getUserId()
        if (doctorId == null) {
            ToastUtil.show(requireContext(), "获取用户ID失败", Toast.LENGTH_SHORT)
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        
        AppointmentRepo.getAppointmentsByDoctorId(
            doctorId = doctorId.toInt(),
            context = requireContext(),
            onSuccess = { appointmentList ->
                binding.progressBar.visibility = View.GONE
                
                allAppointments.clear()
                allAppointments.addAll(appointmentList)
                
                filterAppointments()
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                ToastUtil.show(requireContext(), "加载预约列表失败: $errorMsg", Toast.LENGTH_SHORT)
                
                updateEmptyView()
            }
        )
    }

    private fun filterAppointments() {
        // 清空当前筛选结果
        filteredAppointments.clear()
        
        // 应用所有筛选条件
        val searchQuery = binding.etSearch.text.toString().trim().lowercase()
        
        for (appointment in allAppointments) {
            // 1. 状态筛选
            val statusMatch = status == null || appointment.status == status
            
            // 2. 日期筛选
            val dateMatch = if (selectedDate != null) {
                val appointmentDate = appointment.appointmentDate?.let { 
                    try {
                        dateFormat.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (appointmentDate != null) {
                    dateFormat.format(appointmentDate) == dateFormat.format(selectedDate)
                } else {
                    false
                }
            } else {
                true
            }
            
            // 3. 搜索筛选
            val searchMatch = searchQuery.isEmpty() || 
                    appointment.patientName?.lowercase()?.contains(searchQuery) == true
            
            // 如果满足所有条件，添加到筛选结果中
            if (statusMatch && dateMatch && searchMatch) {
                filteredAppointments.add(appointment)
            }
        }
        
        // 更新适配器
        adapter.notifyDataSetChanged()
        
        // 更新空视图
        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (filteredAppointments.isEmpty()) {
            binding.tvEmptyView.visibility = View.VISIBLE
            binding.recyclerViewAppointments.visibility = View.GONE
        } else {
            binding.tvEmptyView.visibility = View.GONE
            binding.recyclerViewAppointments.visibility = View.VISIBLE
        }
    }

    override fun onViewAppointment(appointment: AppointmentDTO) {
        val intent = Intent(requireContext(), AppointmentDetailActivity::class.java)
        intent.putExtra("APPOINTMENT_ID", appointment.appointmentId)
        intent.putExtra("IS_PATIENT", false)
        startActivity(intent)
    }

    override fun onCancelAppointment(appointment: AppointmentDTO) {
        // 医生不能直接取消预约，而是在详情页面处理
        onViewAppointment(appointment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}