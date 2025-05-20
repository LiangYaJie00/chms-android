package com.example.chms_android.features_doctor.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.R
import com.example.chms_android.databinding.FragmentDoctorProfileViewBinding
import com.example.chms_android.dto.AwardDTO
import com.example.chms_android.dto.CertificateDTO
import com.example.chms_android.dto.DoctorDTO
import com.example.chms_android.dto.EducationDTO
import com.example.chms_android.dto.ExperienceDTO
import com.example.chms_android.dto.PublicationDTO
import com.example.chms_android.features_doctor.vm.DoctorProfileEditViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class DoctorProfileViewFragment : Fragment() {
    private var _binding: FragmentDoctorProfileViewBinding? = null
    private val binding get() = _binding!!
    
    // 不使用 by 委托方式初始化 ViewModel
    private lateinit var viewModel: DoctorProfileEditViewModel
    
    companion object {
        fun newInstance() = DoctorProfileViewFragment()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorProfileViewBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 使用 ViewModelProvider 初始化 ViewModel，确保与 Activity 共享同一个实例
        viewModel = ViewModelProvider(requireActivity()).get(DoctorProfileEditViewModel::class.java)
        
        // 添加日志，打印 ViewModel 实例的哈希码，用于调试
        Log.d("DoctorProfileView", "Fragment ViewModel instance: ${viewModel.hashCode()}")
        
        // 观察医生数据变化
        viewModel.doctorDTO.observe(viewLifecycleOwner) { doctorDTO ->
            if (doctorDTO != null) {
                updateUI(doctorDTO)
            }
        }
        
        // 立即获取当前值并更新UI
        viewModel.doctorDTO.value?.let { 
            updateUI(it)
            Log.d("DoctorProfileView", "Initial UI update with existing data")
        }
        
        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }
    
    private fun updateUI(doctorDTO: DoctorDTO) {
        // 基本信息
        binding.tvDoctorName.text = doctorDTO.name ?: "未设置"
        binding.tvGender.text = when(doctorDTO.gender) {
            0 -> "女"
            1 -> "男"
            else -> "未设置"
        }
        binding.tvDateOfBirth.text = doctorDTO.dateOfBirth?.let { 
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) 
        } ?: "未设置"
        binding.tvPhone.text = doctorDTO.phoneNumber ?: "未设置"
        binding.tvEmail.text = doctorDTO.email ?: "未设置"
        binding.tvSpecialization.text = doctorDTO.specialization ?: "未设置"
        binding.tvCommunity.text = doctorDTO.communityName ?: "未设置"
        binding.tvAddress.text = doctorDTO.address ?: "未设置"
        binding.tvBiography.text = doctorDTO.biography ?: "未设置"
        
        // 教育经历
        updateEducationList(doctorDTO.education)
        
        // 工作经历
        updateExperienceList(doctorDTO.experiences)
        
        // 证书
        updateCertificateList(doctorDTO.certificates)
        
        // 奖项
        updateAwardList(doctorDTO.awards)
        
        // 出版物
        updatePublicationList(doctorDTO.publications)
    }
    
    private fun updateEducationList(educationList: List<EducationDTO>?) {
        binding.llEducation.removeAllViews()
        
        if (educationList.isNullOrEmpty()) {
            binding.tvNoEducation.visibility = View.VISIBLE
            return
        }
        
        binding.tvNoEducation.visibility = View.GONE
        
        for (education in educationList) {
            val educationView = layoutInflater.inflate(
                R.layout.item_education_view, 
                binding.llEducation, 
                false
            )
            
            // 设置教育经历信息
            educationView.findViewById<TextView>(R.id.tv_school).text =
                education.school ?: "未设置"
            educationView.findViewById<TextView>(R.id.tv_degree).text =
                education.degree ?: "未设置"
            educationView.findViewById<TextView>(R.id.tv_major).text =
                education.major ?: "未设置"
            educationView.findViewById<TextView>(R.id.tv_period).text =
                "${education.start ?: "未设置"} - ${education.end ?: "至今"}"
            try {
                binding.llEducation.addView(educationView)
            } catch (e: Exception) {
                Log.e("DoctorProfileView", "Error inflating education view: ${e.message}", e)
            }
        }
    }
    
    private fun updateExperienceList(experienceList: List<ExperienceDTO>?) {
        binding.llExperience.removeAllViews()
        
        if (experienceList.isNullOrEmpty()) {
            binding.tvNoExperience.visibility = View.VISIBLE
            return
        }
        
        binding.tvNoExperience.visibility = View.GONE
        
        for (experience in experienceList) {
            val experienceView = layoutInflater.inflate(
                R.layout.item_experience_view, 
                binding.llExperience, 
                false
            )
            
            // 设置工作经历信息
            experienceView.findViewById<TextView>(R.id.tv_hospital).text = 
                experience.hospital ?: "未设置"
            experienceView.findViewById<TextView>(R.id.tv_position).text = 
                experience.position ?: "未设置"
            experienceView.findViewById<TextView>(R.id.tv_period).text = 
                "${experience.start ?: "未设置"} - ${experience.end ?: "至今"}"
            
            binding.llExperience.addView(experienceView)
        }
    }
    
    private fun updateCertificateList(certificateList: List<CertificateDTO>?) {
        binding.llCertificates.removeAllViews()
        
        if (certificateList.isNullOrEmpty()) {
            binding.tvNoCertificates.visibility = View.VISIBLE
            return
        }
        
        binding.tvNoCertificates.visibility = View.GONE
        
        for (certificate in certificateList) {
            val certificateView = layoutInflater.inflate(
                R.layout.item_certificate_view, 
                binding.llCertificates, 
                false
            )
            
            // 设置证书信息
            certificateView.findViewById<TextView>(R.id.tv_certificate_name).text = 
                certificate.name ?: "未设置"
            certificateView.findViewById<TextView>(R.id.tv_get_date).text = 
                certificate.getDate ?: "未设置"
            
            binding.llCertificates.addView(certificateView)
        }
    }
    
    private fun updateAwardList(awardList: List<AwardDTO>?) {
        binding.llAwards.removeAllViews()
        
        if (awardList.isNullOrEmpty()) {
            binding.tvNoAwards.visibility = View.VISIBLE
            return
        }
        
        binding.tvNoAwards.visibility = View.GONE
        
        for (award in awardList) {
            val awardView = layoutInflater.inflate(
                R.layout.item_award_view, 
                binding.llAwards, 
                false
            )
            
            // 设置奖项信息
            awardView.findViewById<TextView>(R.id.tv_award_title).text = 
                award.title ?: "未设置"
            awardView.findViewById<TextView>(R.id.tv_award_date).text = 
                award.date ?: "未设置"
            awardView.findViewById<TextView>(R.id.tv_issuer).text = 
                award.issuer ?: "未设置"
            
            binding.llAwards.addView(awardView)
        }
    }
    
    private fun updatePublicationList(publicationList: List<PublicationDTO>?) {
        binding.llPublications.removeAllViews()
        
        if (publicationList.isNullOrEmpty()) {
            binding.tvNoPublications.visibility = View.VISIBLE
            return
        }
        
        binding.tvNoPublications.visibility = View.GONE
        
        for (publication in publicationList) {
            val publicationView = layoutInflater.inflate(
                R.layout.item_publication_view, 
                binding.llPublications, 
                false
            )
            
            // 设置出版物信息，使用正确的字段名称
            publicationView.findViewById<TextView>(R.id.tv_publication_title).text = 
                publication.title ?: "未设置"
            publicationView.findViewById<TextView>(R.id.tv_publisher).text = 
                publication.journal ?: "未设置"
            publicationView.findViewById<TextView>(R.id.tv_publication_date).text = 
                publication.publishDate ?: "未设置"
            
            binding.llPublications.addView(publicationView)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}