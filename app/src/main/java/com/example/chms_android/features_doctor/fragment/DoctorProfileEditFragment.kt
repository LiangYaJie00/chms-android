package com.example.chms_android.features_doctor.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.databinding.FragmentDoctorProfileEditBinding
import com.example.chms_android.dto.*
import com.example.chms_android.features_doctor.activity.DoctorProfileEditActivity
import com.example.chms_android.features_doctor.adapter.*
import com.example.chms_android.features_doctor.dialog.AddAwardDialog
import com.example.chms_android.features_doctor.dialog.AddCertificateDialog
import com.example.chms_android.features_doctor.dialog.AddEducationDialog
import com.example.chms_android.features_doctor.dialog.AddExperienceDialog
import com.example.chms_android.features_doctor.dialog.AddPublicationDialog
import com.example.chms_android.features_doctor.vm.DoctorProfileEditViewModel
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class DoctorProfileEditFragment : Fragment() {

    private var _binding: FragmentDoctorProfileEditBinding? = null
    private val binding get() = _binding!!
    
    // 不使用 by 委托方式初始化 ViewModel
    private lateinit var viewModel: DoctorProfileEditViewModel
    
    // 适配器
    private lateinit var educationAdapter: EducationEditAdapter
    private lateinit var experienceAdapter: ExperienceEditAdapter
    private lateinit var certificateAdapter: CertificateEditAdapter
    private lateinit var awardAdapter: AwardEditAdapter
    private lateinit var publicationAdapter: PublicationEditAdapter

    companion object {
        fun newInstance() = DoctorProfileEditFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)
            
            // 使用 ViewModelProvider 初始化 ViewModel，确保与 Activity 共享同一个实例
            viewModel = ViewModelProvider(requireActivity()).get(DoctorProfileEditViewModel::class.java)
            
            // 添加日志，打印 ViewModel 实例的哈希码，用于调试
            Log.d("DoctorProfileEdit", "Fragment ViewModel instance: ${viewModel.hashCode()}")
            
            // 初始化RecyclerView
            setupRecyclerViews()
            
            // 设置添加按钮点击事件
            setupAddButtons()
            
            // 设置不可编辑字段
            setupReadOnlyFields()
            
            // 设置保存按钮
            binding.btnSave.setOnClickListener {
                saveProfile()
            }
            
            // 观察ViewModel中的数据变化
            observeViewModel()

            // 检查数据是否已加载
            checkAndLoadData()
        } catch (e: Exception) {
            Log.e("DoctorProfileEdit", "Error in onViewCreated: ${e.message}", e)
            ToastUtil.show(requireContext(), "初始化界面时出错: ${e.message}", Toast.LENGTH_SHORT)
        }
    }

    private fun checkAndLoadData() {
        // 检查数据是否已加载，如果没有则主动加载
        if (viewModel.doctorDTO.value == null) {
            Log.d("DoctorProfileEdit", "doctorDTO is null in Fragment, attempting to load data")
            // 从Activity获取用户ID或医生ID
            val activity = requireActivity() as? DoctorProfileEditActivity
            val userId = activity?.intent?.getIntExtra("USER_ID", -1) ?: -1
            val doctorId = activity?.intent?.getIntExtra("DOCTOR_ID", -1) ?: -1

            when {
                doctorId > 0 -> viewModel.loadDoctorById(doctorId, requireContext())
                userId > 0 -> viewModel.loadDoctorByUserId(userId, requireContext())
                else -> {
                    // 如果没有ID，尝试加载当前登录用户的医生信息
                    val currentUser = AccountUtil(requireContext()).getUser()
                    currentUser?.userId?.let { viewModel.loadDoctorByUserId(it, requireContext()) }
                }
            }
        } else {
            Log.d("DoctorProfileEdit", "doctorDTO already loaded in Fragment")
        }
    }

    private fun setupRecyclerViews() {
        try {
            // 教育经历
            binding.rvEducation.layoutManager = LinearLayoutManager(requireContext())
            educationAdapter = EducationEditAdapter(
                emptyList(), // 使用空列表初始化
                { viewModel.updateEducation(it) },
                { position -> educationAdapter.removeItem(position) }
            )
            binding.rvEducation.adapter = educationAdapter
        
            // 工作经历
            binding.rvExperience.layoutManager = LinearLayoutManager(requireContext())
            experienceAdapter = ExperienceEditAdapter(
                emptyList(), // 使用空列表初始化
                { viewModel.updateExperiences(it) },
                { position -> experienceAdapter.removeItem(position) }
            )
            binding.rvExperience.adapter = experienceAdapter
        
            // 证书
            binding.rvCertificates.layoutManager = LinearLayoutManager(requireContext())
            certificateAdapter = CertificateEditAdapter(
                emptyList(), // 使用空列表初始化
                { viewModel.updateCertificates(it) },
                { position -> certificateAdapter.removeItem(position) }
            )
            binding.rvCertificates.adapter = certificateAdapter
        
            // 奖项
            binding.rvAwards.layoutManager = LinearLayoutManager(requireContext())
            awardAdapter = AwardEditAdapter(
                emptyList(), // 使用空列表初始化
                { viewModel.updateAwards(it) },
                { position -> awardAdapter.removeItem(position) }
            )
            binding.rvAwards.adapter = awardAdapter
        
            // 出版物
            binding.rvPublications.layoutManager = LinearLayoutManager(requireContext())
            publicationAdapter = PublicationEditAdapter(
                emptyList(), // 使用空列表初始化
                { viewModel.updatePublications(it) },
                { position -> publicationAdapter.removeItem(position) }
            )
            binding.rvPublications.adapter = publicationAdapter
        
            // 打印日志确认适配器初始化
            Log.d("DoctorProfileEdit", "All adapters initialized.")
        } catch (e: Exception) {
            Log.e("DoctorProfileEdit", "Error in setupRecyclerViews: ${e.message}", e)
        }
    }
    
    private fun setupAddButtons() {
        // 添加教育经历
        binding.btnAddEducation.setOnClickListener {
            try {
                // 显示添加教育经历的对话框
                showEducationDialog { education ->
                    // 添加到适配器并更新ViewModel
                    educationAdapter.addItem(education)
                    // 安全滚动到新添加的项
                    safeScrollToPosition(binding.rvEducation, educationAdapter.itemCount - 1)
                    // 确保"无数据"提示隐藏
                    binding.tvNoEducation.visibility = View.GONE
                    binding.rvEducation.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("DoctorProfileEdit", "Error in btnAddEducation: ${e.message}", e)
            }
        }
        
        // 添加工作经历
        binding.btnAddExperience.setOnClickListener {
            // 显示添加工作经历的对话框
            showExperienceDialog { experience ->
                // 添加到适配器并更新ViewModel
                experienceAdapter.addItem(experience)
                // 安全滚动到新添加的项
                safeScrollToPosition(binding.rvExperience, experienceAdapter.itemCount - 1)
                // 确保"无数据"提示隐藏
                binding.tvNoExperience.visibility = View.GONE
                binding.rvExperience.visibility = View.VISIBLE
            }
        }
        
        // 添加证书
        binding.btnAddCertificate.setOnClickListener {
            // 显示添加证书的对话框
            showCertificateDialog { certificate ->
                // 添加到适配器并更新ViewModel
                certificateAdapter.addItem(certificate)
                // 安全滚动到新添加的项
                safeScrollToPosition(binding.rvCertificates, certificateAdapter.itemCount - 1)
                // 确保"无数据"提示隐藏
                binding.tvNoCertificates.visibility = View.GONE
                binding.rvCertificates.visibility = View.VISIBLE
            }
        }
        
        // 添加奖项
        binding.btnAddAward.setOnClickListener {
            // 显示添加奖项的对话框
            showAwardDialog { award ->
                // 添加到适配器并更新ViewModel
                awardAdapter.addItem(award)
                // 安全滚动到新添加的项
                safeScrollToPosition(binding.rvAwards, awardAdapter.itemCount - 1)
                // 确保"无数据"提示隐藏
                binding.tvNoAwards.visibility = View.GONE
                binding.rvAwards.visibility = View.VISIBLE
            }
        }
        
        // 添加出版物
        binding.btnAddPublication.setOnClickListener {
            // 显示添加出版物的对话框
            showPublicationDialog { publication ->
                // 添加到适配器并更新ViewModel
                publicationAdapter.addItem(publication)
                // 安全滚动到新添加的项
                safeScrollToPosition(binding.rvPublications, publicationAdapter.itemCount - 1)
                // 确保"无数据"提示隐藏
                binding.tvNoPublications.visibility = View.GONE
                binding.rvPublications.visibility = View.VISIBLE
            }
        }
    }

    // 显示添加教育经历的对话框
    private fun showEducationDialog(onConfirm: (EducationDTO) -> Unit) {
        val dialog = AddEducationDialog(requireContext())
        dialog.setOnConfirmListener { school, degree, major, start, end ->
            val education = EducationDTO(
                school = school,
                degree = degree,
                major = major,
                start = start,
                end = end
            )
            onConfirm(education)
        }
        dialog.show()
    }

    // 显示添加工作经历的对话框
    private fun showExperienceDialog(onConfirm: (ExperienceDTO) -> Unit) {
        val dialog = AddExperienceDialog(requireContext())
        dialog.setOnConfirmListener { hospital, position, start, end ->
            val experience = ExperienceDTO(
                hospital = hospital,
                position = position,
                start = start,
                end = end
            )
            onConfirm(experience)
        }
        dialog.show()
    }

    // 显示添加证书的对话框
    private fun showCertificateDialog(onConfirm: (CertificateDTO) -> Unit) {
        val dialog = AddCertificateDialog(requireContext())
        dialog.setOnConfirmListener { name, getDate ->
            val certificate = CertificateDTO(
                name = name,
                getDate = getDate
            )
            onConfirm(certificate)
        }
        dialog.show()
    }

    // 显示添加奖项的对话框
    private fun showAwardDialog(onConfirm: (AwardDTO) -> Unit) {
        val dialog = AddAwardDialog(requireContext())
        dialog.setOnConfirmListener { title, date, issuer ->
            val award = AwardDTO(
                title = title,
                date = date,
                issuer = issuer
            )
            onConfirm(award)
        }
        dialog.show()
    }

    // 显示添加出版物的对话框
    private fun showPublicationDialog(onConfirm: (PublicationDTO) -> Unit) {
        val dialog = AddPublicationDialog(requireContext())
        dialog.setOnConfirmListener { title, journal, publishDate ->
            val publication = PublicationDTO(
                title = title,
                journal = journal,
                publishDate = publishDate
            )
            onConfirm(publication)
        }
        dialog.show()
    }
    
    private fun setupReadOnlyFields() {
        // 设置与User实体重复的字段为不可编辑
        binding.etName.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.etCommunity.isEnabled = false
        
        // 添加提示信息
        binding.tvNameHint.visibility = View.VISIBLE
        binding.tvEmailHint.visibility = View.VISIBLE
        binding.tvPhoneHint.visibility = View.VISIBLE
        binding.tvCommunityHint.visibility = View.VISIBLE
    }
    
    private fun observeViewModel() {
        viewModel.doctorDTO.observe(viewLifecycleOwner) { doctorDTO ->
            if (doctorDTO != null) {
                updateUI(doctorDTO)
                Log.d("DoctorProfileEdit", "Data observed and UI updated")
            }
        }
        
        // 立即获取当前值并更新UI
        viewModel.doctorDTO.value?.let { 
            updateUI(it)
            Log.d("DoctorProfileEdit", "Initial UI update with existing data")
        }
        
        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                ToastUtil.show(requireContext(), "保存成功", Toast.LENGTH_SHORT)
            } else {
                ToastUtil.show(requireContext(), "保存失败", Toast.LENGTH_SHORT)
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // 可以添加加载指示器
            binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                ToastUtil.show(requireContext(), errorMsg, Toast.LENGTH_SHORT)
            }
        }
    }
    
    private fun updateUI(doctorDTO: DoctorDTO) {
        // 基本信息
        binding.etName.setText(doctorDTO.name ?: "")
        binding.etEmail.setText(doctorDTO.email ?: "")
        binding.etPhone.setText(doctorDTO.phoneNumber ?: "")
        // 处理日期格式
        binding.etDateOfBirth.setText(
            doctorDTO.dateOfBirth?.let { 
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(it) 
            } ?: ""
        )
        binding.etSpecialization.setText(doctorDTO.specialization ?: "")
        binding.etCommunity.setText(doctorDTO.communityName ?: "")
        binding.etAddress.setText(doctorDTO.address ?: "")
        binding.etBiography.setText(doctorDTO.biography ?: "")
        
        // 设置性别
        if (doctorDTO.gender == 1) {
            binding.rbMale.isChecked = true
        } else if (doctorDTO.gender == 0) {
            binding.rbFemale.isChecked = true
        }
        
        // 更新列表数据
        updateEducationList(doctorDTO)
        updateExperienceList(doctorDTO)
        updateCertificateList(doctorDTO)
        updateAwardList(doctorDTO)
        updatePublicationList(doctorDTO)
    }
    
    private fun updateEducationList(doctorDTO: DoctorDTO) {
        val educationList = doctorDTO.education ?: listOf()
        if (educationList.isEmpty()) {
            binding.tvNoEducation.visibility = View.VISIBLE
            binding.rvEducation.visibility = View.GONE
        } else {
            binding.tvNoEducation.visibility = View.GONE
            binding.rvEducation.visibility = View.VISIBLE
            educationAdapter.updateData(educationList)
        }
    }
    
    private fun updateExperienceList(doctorDTO: DoctorDTO) {
        val experienceList = doctorDTO.experiences ?: listOf()
        if (experienceList.isEmpty()) {
            binding.tvNoExperience.visibility = View.VISIBLE
            binding.rvExperience.visibility = View.GONE
        } else {
            binding.tvNoExperience.visibility = View.GONE
            binding.rvExperience.visibility = View.VISIBLE
            experienceAdapter.updateData(experienceList)
        }
    }
    
    private fun updateCertificateList(doctorDTO: DoctorDTO) {
        val certificateList = doctorDTO.certificates ?: listOf()
        if (certificateList.isEmpty()) {
            binding.tvNoCertificates.visibility = View.VISIBLE
            binding.rvCertificates.visibility = View.GONE
        } else {
            binding.tvNoCertificates.visibility = View.GONE
            binding.rvCertificates.visibility = View.VISIBLE
            certificateAdapter.updateData(certificateList)
        }
    }
    
    private fun updateAwardList(doctorDTO: DoctorDTO) {
        val awardList = doctorDTO.awards ?: listOf()
        if (awardList.isEmpty()) {
            binding.tvNoAwards.visibility = View.VISIBLE
            binding.rvAwards.visibility = View.GONE
        } else {
            binding.tvNoAwards.visibility = View.GONE
            binding.rvAwards.visibility = View.VISIBLE
            awardAdapter.updateData(awardList)
        }
    }
    
    private fun updatePublicationList(doctorDTO: DoctorDTO) {
        try {
            // 获取出版物列表，并创建一个新的列表副本
            val publicationList = doctorDTO.publications?.map { it.copy() } ?: listOf()
            Log.d("DoctorProfileEdit", "updatePublicationList: received ${publicationList.size} publications")
            
            if (publicationList.isEmpty()) {
                binding.tvNoPublications.visibility = View.VISIBLE
                binding.rvPublications.visibility = View.GONE
                Log.d("DoctorProfileEdit", "No publications, showing empty state")
            } else {
                binding.tvNoPublications.visibility = View.GONE
                binding.rvPublications.visibility = View.VISIBLE
                Log.d("DoctorProfileEdit", "Publications found, updating adapter")
                
                // 确保适配器已初始化
                if (::publicationAdapter.isInitialized) {
                    Log.d("DoctorProfileEdit", "Before updateData: publicationAdapter.itemCount = ${publicationAdapter.itemCount}")
                    // 传递列表副本给适配器
                    publicationAdapter.updateData(ArrayList(publicationList))
                    Log.d("DoctorProfileEdit", "After updateData: publicationAdapter.itemCount = ${publicationAdapter.itemCount}")
                } else {
                    Log.e("DoctorProfileEdit", "Publication adapter not initialized!")
                }
            }
        } catch (e: Exception) {
            Log.e("DoctorProfileEdit", "Error in updatePublicationList: ${e.message}", e)
        }
    }
    
    private fun saveProfile() {
        // 获取基本信息
        val gender = if (binding.rbMale.isChecked) 1 else 0
        val dateOfBirth = binding.etDateOfBirth.text.toString().trim()
        val specialization = binding.etSpecialization.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val biography = binding.etBiography.text.toString().trim()
        
        // 更新ViewModel中的数据
        viewModel.updateBasicInfo(
            gender = gender,
            dateOfBirth = dateOfBirth,
            specialization = specialization,
            address = address,
            biography = biography
        )
        
        // 保存数据
        viewModel.saveDoctor(requireContext())
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 添加一个安全滚动方法
    private fun safeScrollToPosition(recyclerView: RecyclerView, position: Int) {
        try {
            if (position >= 0 && recyclerView.adapter != null && position < (recyclerView.adapter?.itemCount ?: 0)) {
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(position)
                }
            }
        } catch (e: Exception) {
            Log.e("DoctorProfileEdit", "Error scrolling to position $position: ${e.message}", e)
        }
    }
}
