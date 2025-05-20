package com.example.chms_android.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chms_android.dto.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import java.sql.Timestamp
import java.util.Date

@Parcelize
@Entity(tableName = "doctor")
data class Doctor(
    @PrimaryKey(autoGenerate = false)
    var doctorId: Int,
    var userId: Int? = null,
    var name: String?,
    var gender: Int?,
    var dateOfBirth: Date?,
    var phoneNumber: String?,
    var email: String?,
    var specialization: String?,
    var communityName: String?,
    var address: String?,
    var biography: String? = null,
    var education: String? = null,
    var experiences: String? = null,
    var certificates: String? = null,
    var awards: String? = null,
    var publications: String? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
): Parcelable {
    // 将实体类转换为DTO
    fun toDTO(): DoctorDTO {
        val gson = Gson()
        return DoctorDTO(
            doctorId = doctorId,
            userId = userId ?: 0,
            name = name,
            gender = gender ?: 0,
            dateOfBirth = dateOfBirth,
            phoneNumber = phoneNumber,
            email = email,
            specialization = specialization,
            communityName = communityName,
            address = address,
            biography = biography,
            education = parseEducation(),
            experiences = parseExperiences(),
            certificates = parseCertificates(),
            awards = parseAwards(),
            publications = parsePublications(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    // 解析JSON字符串为对象列表
    fun parseEducation(): List<EducationDTO>? {
        if (education.isNullOrEmpty()) return null
        val gson = Gson()
        val type = object : TypeToken<List<EducationDTO>>() {}.type
        return gson.fromJson(education, type)
    }
    
    fun parseExperiences(): List<ExperienceDTO>? {
        if (experiences.isNullOrEmpty()) return null
        val gson = Gson()
        val type = object : TypeToken<List<ExperienceDTO>>() {}.type
        return gson.fromJson(experiences, type)
    }
    
    fun parseCertificates(): List<CertificateDTO>? {
        if (certificates.isNullOrEmpty()) return null
        val gson = Gson()
        val type = object : TypeToken<List<CertificateDTO>>() {}.type
        return gson.fromJson(certificates, type)
    }
    
    fun parseAwards(): List<AwardDTO>? {
        if (awards.isNullOrEmpty()) return null
        val gson = Gson()
        val type = object : TypeToken<List<AwardDTO>>() {}.type
        return gson.fromJson(awards, type)
    }
    
    fun parsePublications(): List<PublicationDTO>? {
        if (publications.isNullOrEmpty()) return null
        val gson = Gson()
        val type = object : TypeToken<List<PublicationDTO>>() {}.type
        return gson.fromJson(publications, type)
    }
}
