package com.example.chms_android.features_doctor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.data.Patient
import com.example.chms_android.databinding.FragmentDoctorPatientsBinding
import com.example.chms_android.features_doctor.adapter.PatientAdapter
import com.example.chms_android.features_doctor.vm.DoctorPatientsViewModel
import com.example.chms_android.utils.ToastUtil

class DoctorPatientsFragment : Fragment(), PatientAdapter.OnPatientClickListener {

    private var _binding: FragmentDoctorPatientsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DoctorPatientsViewModel
    private lateinit var patientAdapter: PatientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DoctorPatientsViewModel::class.java)

        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeViewModel()
        
        // 加载患者数据
        viewModel.loadPatients()
    }

    private fun setupRecyclerView() {
        patientAdapter = PatientAdapter(requireContext(), emptyList(), this)
        binding.recyclerViewPatients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = patientAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                patientAdapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddPatient.setOnClickListener {
            // 打开添加患者界面
            ToastUtil.show(requireContext(), "添加患者功能待实现", Toast.LENGTH_SHORT)
        }
    }

    private fun observeViewModel() {
        viewModel.patients.observe(viewLifecycleOwner) { patients ->
            patientAdapter.updatePatients(patients)
            updateEmptyView(patients.isEmpty())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                ToastUtil.show(requireContext(), errorMsg, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        binding.tvEmptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onPatientClick(patient: Patient) {
        // 打开患者详情界面
        ToastUtil.show(requireContext(), "查看患者: ${patient.name}", Toast.LENGTH_SHORT)
    }

    override fun onViewHealthRecord(patient: Patient) {
        // 查看健康档案
        ToastUtil.show(requireContext(), "查看健康档案: ${patient.name}", Toast.LENGTH_SHORT)
    }

    override fun onViewAppointments(patient: Patient) {
        // 查看预约记录
        ToastUtil.show(requireContext(), "查看预约记录: ${patient.name}", Toast.LENGTH_SHORT)
    }

    override fun onSendMessage(patient: Patient) {
        // 发送消息
        ToastUtil.show(requireContext(), "发送消息给: ${patient.name}", Toast.LENGTH_SHORT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}