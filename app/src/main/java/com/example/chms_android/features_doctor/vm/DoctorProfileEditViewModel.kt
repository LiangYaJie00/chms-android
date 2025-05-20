package com.example.chms_android.features_doctor.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.Doctor
import com.example.chms_android.dto.AwardDTO
import com.example.chms_android.dto.CertificateDTO
import com.example.chms_android.dto.DoctorDTO
import com.example.chms_android.dto.EducationDTO
import com.example.chms_android.dto.ExperienceDTO
import com.example.chms_android.dto.PublicationDTO
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.AccountUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DoctorProfileEditViewModel : ViewModel() {

    private val TAG = "DoctorProfileEditVM"

    private val _doctorDTO = MutableLiveData<DoctorDTO>()
    val doctorDTO: LiveData<DoctorDTO> = _doctorDTO

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // 加载医生信息（通过医生ID）
    fun loadDoctorById(doctorId: Int, context: Context) {
        _isLoading.value = true

        DoctorRepo.getDoctorById(
            doctorId = doctorId,
            context = context,
            onSuccess = { doctorDTO ->
                _doctorDTO.value = doctorDTO
                _isLoading.value = false
                Log.d(TAG, "Doctor loaded successfully: ${doctorDTO.name}")
            },
            onError = { errorMsg ->
                _error.value = errorMsg
                _isLoading.value = false
                Log.e(TAG, "Error loading doctor: $errorMsg")
            }
        )
    }

    // 加载医生信息（通过用户ID）
    fun loadDoctorByUserId(userId: Int, context: Context) {
        _isLoading.value = true

        DoctorRepo.getDoctorByUserId(
            userId = userId,
            context = context,
            onSuccess = { doctorDTO ->
                _doctorDTO.value = doctorDTO
                _isLoading.value = false
                Log.d(TAG, "Doctor loaded successfully by userId: ${doctorDTO.name}")
            },
            onError = { errorMsg ->
                // 如果没有找到医生信息，创建一个新的DTO
                if (errorMsg.contains("not found")) {
                    val user = AccountUtil(context).getUser()
                    _doctorDTO.value = DoctorDTO(
                        userId = userId,
                        name = user?.name ?: "",
                        gender = user?.gender ?: 0,
                        email = user?.email ?: "",
                        phoneNumber = user?.phone?.toString() ?: "",
                        communityName = user?.community ?: ""
                    )
                    Log.d(TAG, "Created new doctor DTO for userId: $userId")
                } else {
                    _error.value = errorMsg
                    Log.e(TAG, "Error loading doctor by userId: $errorMsg")
                }
                _isLoading.value = false
            }
        )
    }

    // 保存医生信息
    fun saveDoctor(context: Context) {
        _isLoading.value = true

        _doctorDTO.value?.let { doctor ->
            // 确保必填字段不为空
            val validatedDoctor = validateDoctorData(doctor)

            Log.d(TAG, "Saving doctor data: ${Gson().toJson(validatedDoctor)}")

            DoctorRepo.saveOrUpdateDoctor(
                doctorDTO = validatedDoctor,
                context = context,
                onSuccess = { updatedDoctorDTO ->
                    _doctorDTO.value = updatedDoctorDTO
                    _saveStatus.value = true
                    _isLoading.value = false
                    Log.d(TAG, "Doctor saved successfully: ${updatedDoctorDTO.name}")
                },
                onError = { errorMsg ->
                    _error.value = errorMsg
                    _saveStatus.value = false
                    _isLoading.value = false
                    Log.e(TAG, "Error saving doctor: $errorMsg")
                }
            )
        } ?: run {
            _error.value = "No doctor data to save"
            _saveStatus.value = false
            _isLoading.value = false
            Log.e(TAG, "Error: No doctor data to save")
        }
    }

    // 验证医生数据，确保必填字段不为空
    private fun validateDoctorData(doctor: DoctorDTO): DoctorDTO {
        return doctor.copy(
            name = doctor.name ?: "",
            gender = doctor.gender ?: 0,
            phoneNumber = doctor.phoneNumber ?: "",
            email = doctor.email ?: "",
            // 确保集合不为null
            education = doctor.education ?: emptyList(),
            experiences = doctor.experiences ?: emptyList(),
            certificates = doctor.certificates ?: emptyList(),
            awards = doctor.awards ?: emptyList(),
            publications = doctor.publications ?: emptyList()
        )
    }

    // 更新基本信息
    fun updateBasicInfo(
        name: String? = null,
        gender: Int,
        dateOfBirth: String?,
        phoneNumber: String? = null,
        email: String? = null,
        specialization: String?,
        communityName: String? = null,
        address: String?,
        biography: String?
    ) {
        val currentDoctor = _doctorDTO.value ?: return

        try {
            _doctorDTO.value = currentDoctor.copy(
                name = name ?: currentDoctor.name,
                gender = gender,
                dateOfBirth = if (dateOfBirth.isNullOrEmpty()) null else java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth),
                phoneNumber = phoneNumber ?: currentDoctor.phoneNumber,
                email = email ?: currentDoctor.email,
                specialization = specialization,
                communityName = communityName ?: currentDoctor.communityName,
                address = address,
                biography = biography
            )
            Log.d(TAG, "Basic info updated: ${name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating basic info: ${e.message}", e)
            _error.value = "更新基本信息失败: ${e.message}"
        }
    }

    // 更新教育经历
    fun updateEducation(educationList: List<EducationDTO>) {
        val currentDoctor = _doctorDTO.value ?: return
        _doctorDTO.value = currentDoctor.copy(education = educationList)
        Log.d(TAG, "Education updated: ${educationList.size} items")
    }

    // 更新工作经历
    fun updateExperiences(experienceList: List<ExperienceDTO>) {
        val currentDoctor = _doctorDTO.value ?: return
        _doctorDTO.value = currentDoctor.copy(experiences = experienceList)
        Log.d(TAG, "Experiences updated: ${experienceList.size} items")
    }

    // 更新证书
    fun updateCertificates(certificateList: List<CertificateDTO>) {
        val currentDoctor = _doctorDTO.value ?: return
        _doctorDTO.value = currentDoctor.copy(certificates = certificateList)
        Log.d(TAG, "Certificates updated: ${certificateList.size} items")
    }

    // 更新奖项
    fun updateAwards(awardList: List<AwardDTO>) {
        val currentDoctor = _doctorDTO.value ?: return
        _doctorDTO.value = currentDoctor.copy(awards = awardList)
        Log.d(TAG, "Awards updated: ${awardList.size} items")
    }

    // 更新出版物
    fun updatePublications(publicationList: List<PublicationDTO>) {
        val currentDoctor = _doctorDTO.value ?: return
        _doctorDTO.value = currentDoctor.copy(publications = publicationList)
        Log.d(TAG, "Publications updated: ${publicationList.size} items")
    }

    // 将Doctor实体转换为DTO
    private fun convertToDTO(doctor: Doctor): DoctorDTO? {
        val gson = Gson()

        // 解析JSON字符串为对象列表
        val educationList = if (!doctor.education.isNullOrEmpty()) {
            gson.fromJson<List<EducationDTO>>(doctor.education, object : TypeToken<List<EducationDTO>>() {}.type)
        } else null

        val experiencesList = if (!doctor.experiences.isNullOrEmpty()) {
            gson.fromJson<List<ExperienceDTO>>(doctor.experiences, object : TypeToken<List<ExperienceDTO>>() {}.type)
        } else null

        val certificatesList = if (!doctor.certificates.isNullOrEmpty()) {
            gson.fromJson<List<CertificateDTO>>(doctor.certificates, object : TypeToken<List<CertificateDTO>>() {}.type)
        } else null

        val awardsList = if (!doctor.awards.isNullOrEmpty()) {
            gson.fromJson<List<AwardDTO>>(doctor.awards, object : TypeToken<List<AwardDTO>>() {}.type)
        } else null

        val publicationsList = if (!doctor.publications.isNullOrEmpty()) {
            gson.fromJson<List<PublicationDTO>>(doctor.publications, object : TypeToken<List<PublicationDTO>>() {}.type)
        } else null

        return doctor.userId?.let {
            doctor.gender?.let { it1 ->
                DoctorDTO(
                    doctorId = doctor.doctorId,
                    userId = it,
                    name = doctor.name ?: "",
                    gender = it1,
                    dateOfBirth = doctor.dateOfBirth,
                    phoneNumber = doctor.phoneNumber ?: "",
                    email = doctor.email ?: "",
                    specialization = doctor.specialization,
                    communityName = doctor.communityName,
                    address = doctor.address,
                    biography = doctor.biography,
                    education = educationList ?: emptyList(),
                    experiences = experiencesList ?: emptyList(),
                    certificates = certificatesList ?: emptyList(),
                    awards = awardsList ?: emptyList(),
                    publications = publicationsList ?: emptyList(),
                    createdAt = doctor.createdAt,
                    updatedAt = doctor.updatedAt
                )
            }
        }
    }
}