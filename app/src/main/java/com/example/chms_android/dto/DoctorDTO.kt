package com.example.chms_android.dto

import com.example.chms_android.data.Doctor
import com.google.gson.Gson
import java.sql.Timestamp
import java.util.Date

data class DoctorDTO(
    val doctorId: Int? = null,                  // 医生ID，主键
    val userId: Int = 0,                        // 关联的用户ID
    val name: String? = null,                   // 医生姓名
    val gender: Int = 0,                        // 性别：0-女，1-男
    val dateOfBirth: Date? = null,              // 出生日期
    val phoneNumber: String? = null,            // 电话号码
    val email: String? = null,                  // 电子邮箱
    val specialization: String? = null,         // 专业/专长领域
    val communityName: String? = null,          // 所属社区名称
    val address: String? = null,                // 地址
    val biography: String? = null,              // 个人简介/履历概述
    val education: List<EducationDTO>? = null,  // 教育经历列表
    val experiences: List<ExperienceDTO>? = null, // 工作经历列表
    val certificates: List<CertificateDTO>? = null, // 职业资格证书列表
    val awards: List<AwardDTO>? = null,         // 荣誉奖项列表
    val publications: List<PublicationDTO>? = null, // 论文/著作列表
    val createdAt: Timestamp? = null,           // 记录创建时间
    val updatedAt: Timestamp? = null            // 记录更新时间
) {
    // 将DTO转换为实体类
    fun toEntity(): Doctor {
        val gson = Gson()
        return Doctor(
            doctorId = doctorId ?: 0,
            userId = userId,
            name = name,
            gender = gender,
            dateOfBirth = dateOfBirth,
            phoneNumber = phoneNumber,
            email = email,
            specialization = specialization,
            communityName = communityName,
            address = address,
            biography = biography,
            education = education?.let { gson.toJson(it) },
            experiences = experiences?.let { gson.toJson(it) },
            certificates = certificates?.let { gson.toJson(it) },
            awards = awards?.let { gson.toJson(it) },
            publications = publications?.let { gson.toJson(it) },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}