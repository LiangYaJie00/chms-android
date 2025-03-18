package com.example.chms_android.features.report

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chms_android.databinding.FragmentReportBinding
import com.example.chms_android.features.report.activity.DailyReportActivity

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportBinding.inflate(inflater, container, false)

        binding.cvDailyReport.setOnClickListener {
            val intent = Intent(context, DailyReportActivity::class.java).apply {
                putExtra("status", 0)
            }
            startActivity(intent)
        }

        binding.cvMonthReport.setOnClickListener {
            val intent = Intent(context, DailyReportActivity::class.java).apply {
                putExtra("status", 1)
            }
            startActivity(intent)
        }

        binding.cvWeeklyReport.setOnClickListener {

        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReportFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}