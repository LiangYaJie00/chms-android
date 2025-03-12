package com.example.chms_android.features.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.data.Doctor
import com.example.chms_android.data.User
import com.example.chms_android.databinding.FragmentHomeBinding
import com.example.chms_android.features.home.activity.DeviceManagerActivity
import com.example.chms_android.features.home.activity.DoctorsActivity
import com.example.chms_android.features.home.adapter.DoctorShowsAdapter
import com.example.chms_android.features.home.vm.HomeFragmentVM
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.ToastUtil

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeFragmentVM

    private var doctorList: List<Doctor> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeFragmentVM::class.java)

        setListener()

        initDoctorRecyclerView()

        return binding.root
    }

    private fun setListener() {
        binding.cdManageDevices.setOnClickListener {
            val intent = Intent(context, DeviceManagerActivity::class.java)
            startActivity(intent)
        }

        binding.tvFhShowAll.setOnClickListener {
            val intent = Intent(context, DoctorsActivity::class.java)
            startActivity(intent)
        }
    }


    private fun initDoctorRecyclerView() {
        initDoctorList()
        binding.recyclerViewFhDoctor.layoutManager = LinearLayoutManager(requireContext())
        viewModel.doctorList.observe(requireActivity(), Observer { doctorList ->
            binding.recyclerViewFhDoctor.adapter = DoctorShowsAdapter(requireContext(), doctorList)
        })
    }

    private fun initDoctorList() {
        DoctorRepo.getFirstSevenDoctorsFromDb(
            onComplete = { doctors ->
                viewModel.setDoctorList(doctors)
            },
            onError = { error ->
                // 处理错误
                ToastUtil.show(requireContext(), "Error fetching doctors: ${error.message}", Toast.LENGTH_SHORT)
            }
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}