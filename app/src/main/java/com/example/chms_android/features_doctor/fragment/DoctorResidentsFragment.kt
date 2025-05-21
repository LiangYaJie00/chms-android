package com.example.chms_android.features_doctor.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.data.User
import com.example.chms_android.databinding.FragmentDoctorResidentsBinding
import com.example.chms_android.features.health.activity.HealthReportHistoryActivity
import com.example.chms_android.features.health.activity.UserHealthInfoActivity
import com.example.chms_android.features_doctor.activity.ResidentDetailActivity
import com.example.chms_android.features_doctor.adapter.ResidentAdapter
import com.example.chms_android.features_doctor.vm.DoctorResidentsViewModel
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class DoctorResidentsFragment : Fragment(), ResidentAdapter.OnResidentClickListener {

    private var _binding: FragmentDoctorResidentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DoctorResidentsViewModel
    private lateinit var residentAdapter: ResidentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorResidentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DoctorResidentsViewModel::class.java)

        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeViewModel()
        
        // 加载居民数据
        loadResidents()
    }

    private fun loadResidents() {
        // 获取当前医生的社区
        val currentUser = AccountUtil(requireContext()).getUser()
        val community = currentUser?.community
        
        if (community != null) {
            viewModel.loadResidents(requireContext(), community)
        } else {
            ToastUtil.show(requireContext(), "无法获取医生所属社区信息", Toast.LENGTH_SHORT)
        }
    }

    private fun setupRecyclerView() {
        residentAdapter = ResidentAdapter(requireContext(), emptyList(), this)
        binding.recyclerViewResidents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = residentAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                residentAdapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddResident.setOnClickListener {
            // 打开添加居民界面
            ToastUtil.show(requireContext(), "添加居民功能待实现", Toast.LENGTH_SHORT)
        }
    }

    private fun observeViewModel() {
        viewModel.residents.observe(viewLifecycleOwner) { residents ->
            residentAdapter.updateResidents(residents)
            updateEmptyView(residents.isEmpty())
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

    override fun onResidentClick(resident: User) {
        // 打开居民详情界面
        val intent = Intent(requireContext(), ResidentDetailActivity::class.java).apply {
            putExtra("resident", resident)
        }
        startActivity(intent)
    }

    override fun onViewHealthRecord(resident: User) {
        // 查看健康档案
        resident.userId?.let { userId ->
            UserHealthInfoActivity.start(requireContext(), userId)
        } ?: run {
            ToastUtil.show(requireContext(), "无法获取用户ID", Toast.LENGTH_SHORT)
        }
    }

    override fun onViewAppointments(resident: User) {
        // 查看预约记录
        resident.userId?.let { userId ->
            // TODO 预约界面跳转待添加
//            val intent = Intent(requireContext(), AppointmentHistoryActivity::class.java).apply {
//                putExtra("userId", userId)
//            }
//            startActivity(intent)
        } ?: run {
            ToastUtil.show(requireContext(), "无法获取用户ID", Toast.LENGTH_SHORT)
        }
    }

    override fun onViewHealthReports(resident: User) {
        // 查看健康日报
        resident.userId?.let { userId ->
            val intent = Intent(requireContext(), HealthReportHistoryActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(intent)
        } ?: run {
            ToastUtil.show(requireContext(), "无法获取用户ID", Toast.LENGTH_SHORT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}