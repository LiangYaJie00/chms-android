package com.example.chms_android.features_doctor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.data.Appointment
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.databinding.FragmentAppointmentListBinding
import com.example.chms_android.features_doctor.adapter.AppointmentAdapter
import com.example.chms_android.features_doctor.vm.DoctorAppointmentsViewModel
import com.example.chms_android.utils.ToastUtil

class AppointmentListFragment : Fragment(), AppointmentAdapter.OnAppointmentClickListener {

    private var _binding: FragmentAppointmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DoctorAppointmentsViewModel
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var status: AppointmentStatus? = null

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
        viewModel = ViewModelProvider(requireParentFragment()).get(DoctorAppointmentsViewModel::class.java)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter(requireContext(), emptyList(), this)
        binding.recyclerViewAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.appointments.observe(viewLifecycleOwner) { appointments ->
            val filteredAppointments = if (status == null) {
                appointments
            } else {
                appointments.filter { it.status == status }
            }
            appointmentAdapter.updateAppointments(filteredAppointments)
            updateEmptyView(filteredAppointments.isEmpty())
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

    override fun onAppointmentClick(appointment: Appointment) {
        // 打开预约详情界面
        ToastUtil.show(requireContext(), "查看预约: ${appointment.patientName}", Toast.LENGTH_SHORT)
    }

    override fun onAcceptAppointment(appointment: Appointment) {
        viewModel.updateAppointmentStatus(appointment.id, AppointmentStatus.CONFIRMED)
    }

    override fun onRejectAppointment(appointment: Appointment) {
        viewModel.updateAppointmentStatus(appointment.id, AppointmentStatus.CANCELLED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}