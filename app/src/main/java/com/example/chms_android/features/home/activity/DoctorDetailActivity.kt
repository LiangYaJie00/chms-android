package com.example.chms_android.features.home.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chms_android.R
import com.example.chms_android.data.Doctor
import com.example.chms_android.databinding.ActivityDoctorDetailBinding
import com.example.chms_android.dto.DoctorDTO
import com.example.chms_android.features.appointment.activity.CreateAppointmentActivity
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import com.bumptech.glide.Glide
import com.example.chms_android.repo.UserRepo

class DoctorDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorDetailBinding
    private var doctorId: Int = 0
    private var doctorDTO: DoctorDTO? = null
    private val TAG = "DoctorDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        // 获取传递的医生ID
        doctorId = intent.getIntExtra("DOCTOR_ID", 0)
        
        if (doctorId <= 0) {
            ToastUtil.show(this, "医生ID无效", Toast.LENGTH_SHORT)
            finish()
            return
        }

        // 加载医生数据
        loadDoctorData()
        
        // 设置按钮点击事件
        setupButtons()
    }

    private fun loadDoctorData() {
        // 显示加载进度
        // 如果有进度条，可以在这里显示
        
        // 从DoctorRepo获取医生数据
        DoctorRepo.getDoctorById(
            doctorId = doctorId,
            context = this,
            onSuccess = { doctor ->
                // 保存医生数据
                doctorDTO = doctor
                
                // 更新UI
                updateUI(doctor)
            },
            onError = { errorMsg ->
                ToastUtil.show(this, "加载医生数据失败: $errorMsg", Toast.LENGTH_SHORT)
                Log.e(TAG, "Error loading doctor: $errorMsg")
            }
        )
    }

    private fun updateUI(doctor: DoctorDTO) {
        // 设置医生姓名
        binding.tvActddName.text = doctor.name ?: "未知医生"
        
        // 设置医生专业
        binding.tvActddSpecialization.text = doctor.specialization ?: "未知专业"
        
        // 设置医生邮箱
        binding.tvActddEmail.text = doctor.email ?: "暂无邮箱"
        
        // 设置医生电话
        binding.tvActddPhone.text = doctor.phoneNumber ?: "暂无电话"
        
        // 设置医生所属社区
        binding.tvActddCommunityName.text = doctor.communityName ?: "未分配社区"
        
        // 设置医生描述（综合展示医生的各种信息）
        val descriptionBuilder = StringBuilder()

        // 添加个人简介
        if (!doctor.biography.isNullOrEmpty()) {
            descriptionBuilder.append("【个人简介】\n")
            descriptionBuilder.append(doctor.biography)
            descriptionBuilder.append("\n\n")
        }

        // 添加教育经历
        if (!doctor.education.isNullOrEmpty()) {
            descriptionBuilder.append("【教育经历】\n")
            doctor.education.forEach { education ->
                descriptionBuilder.append("• ${education.school ?: "未知学校"}")
                if (!education.degree.isNullOrEmpty()) {
                    descriptionBuilder.append(" - ${education.degree}")
                }
                if (!education.major.isNullOrEmpty()) {
                    descriptionBuilder.append(" - ${education.major}")
                }
                descriptionBuilder.append(" (${education.start ?: "未知"} - ${education.end ?: "至今"})\n")
            }
            descriptionBuilder.append("\n")
        }

        // 添加工作经验
        if (!doctor.experiences.isNullOrEmpty()) {
            descriptionBuilder.append("【工作经验】\n")
            doctor.experiences.forEach { experience ->
                descriptionBuilder.append("• ${experience.hospital ?: "未知医院"}")
                if (!experience.position.isNullOrEmpty()) {
                    descriptionBuilder.append(" - ${experience.position}")
                }
                descriptionBuilder.append(" (${experience.start ?: "未知"} - ${experience.end ?: "至今"})\n")
            }
            descriptionBuilder.append("\n")
        }

        // 添加证书
        if (!doctor.certificates.isNullOrEmpty()) {
            descriptionBuilder.append("【专业证书】\n")
            doctor.certificates.forEach { certificate ->
                descriptionBuilder.append("• ${certificate.name ?: "未知证书"}")
//                if (!certificate.issuingAuthority.isNullOrEmpty()) {
//                    descriptionBuilder.append(" - 颁发机构: ${certificate.issuingAuthority}")
//                }
                if (!certificate.getDate.isNullOrEmpty()) {
                    descriptionBuilder.append(" - 颁发日期: ${certificate.getDate}")
                }
                descriptionBuilder.append("\n")
            }
            descriptionBuilder.append("\n")
        }

        // 添加奖项
        if (!doctor.awards.isNullOrEmpty()) {
            descriptionBuilder.append("【荣誉奖项】\n")
            doctor.awards.forEach { award ->
                descriptionBuilder.append("• ${award.title ?: "未知奖项"}")
                if (!award.issuer.isNullOrEmpty()) {
                    descriptionBuilder.append(" - 颁发机构: ${award.issuer}")
                }
                if (!award.date.isNullOrEmpty()) {
                    descriptionBuilder.append(" - 颁发日期: ${award.date}")
                }
                descriptionBuilder.append("\n")
            }
            descriptionBuilder.append("\n")
        }

        // 添加出版物
        if (!doctor.publications.isNullOrEmpty()) {
            descriptionBuilder.append("【论文著作】\n")
            doctor.publications.forEach { publication ->
                descriptionBuilder.append("• ${publication.title ?: "未知出版物"}")
                if (!publication.journal.isNullOrEmpty()) {
                    descriptionBuilder.append(" - 期刊: ${publication.journal}")
                }
                if (!publication.publishDate.isNullOrEmpty()) {
                    descriptionBuilder.append(" - 发表日期: ${publication.publishDate}")
                }
                descriptionBuilder.append("\n")
            }
        }

        // 如果没有任何信息，显示默认文本
        if (descriptionBuilder.isEmpty()) {
            binding.tvActddDescription.text = "暂无医生详细信息"
        } else {
            binding.tvActddDescription.text = descriptionBuilder.toString()
        }
        
        // 通过userId获取用户数据，加载头像
        if (doctor.userId > 0) {
            UserRepo.getUserById(
                userId = doctor.userId,
                context = this,
                onSuccess = { user ->
                    // 如果用户有头像，则加载头像
                    if (!user.avatar.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(user.avatar)
                            .placeholder(R.mipmap.avatar_default)
                            .error(R.mipmap.avatar_default)
                            .into(binding.ivActddAvatar)
                    } else {
                        // 如果用户没有头像，则根据性别加载默认头像
                        val avatarResId = if (user.gender == 1) {
                            R.mipmap.avatar_doctor01 // 男性默认头像
                        } else {
                            R.mipmap.avatar_doctor02 // 女性默认头像
                        }
                        
                        Glide.with(this)
                            .load(avatarResId)
                            .into(binding.ivActddAvatar)
                    }
                },
                onError = { errorMsg ->
                    // 获取用户数据失败，使用默认头像
                    Log.e(TAG, "Error loading user data: $errorMsg")
                    
                    // 根据医生性别选择默认头像
                    val avatarResId = if (doctor.gender == 1) {
                        R.mipmap.avatar_doctor01 // 男性默认头像
                    } else {
                        R.mipmap.avatar_doctor02 // 女性默认头像
                    }
                    
                    Glide.with(this)
                        .load(avatarResId)
                        .into(binding.ivActddAvatar)
                }
            )
        } else {
            // 如果没有userId，使用默认头像
            Glide.with(this)
                .load(R.mipmap.avatar_default)
                .into(binding.ivActddAvatar)
        }
    }

    private fun setupButtons() {
        // 线下问诊预约按钮
        binding.btnActddOfflineAppointment.setOnClickListener {
            if (doctorDTO == null) {
                ToastUtil.show(this, "医生数据尚未加载完成", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            
            // 检查用户是否登录
            if (!AccountUtil(this).isUserLoggedIn()) {
                ToastUtil.show(this, "请先登录", Toast.LENGTH_SHORT)
                // 可以跳转到登录页面
                return@setOnClickListener
            }
            
            // 跳转到创建预约页面
            val intent = Intent(this, CreateAppointmentActivity::class.java).apply {
                putExtra("DOCTOR_ID", doctorId)
                putExtra("APPOINTMENT_TYPE", 0) // 0表示线下问诊
            }
            startActivity(intent)
        }
        
        // 视频问诊预约按钮
        binding.btnActddVideoAppointment.setOnClickListener {
            if (doctorDTO == null) {
                ToastUtil.show(this, "医生数据尚未加载完成", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            
            // 检查用户是否登录
            if (!AccountUtil(this).isUserLoggedIn()) {
                ToastUtil.show(this, "请先登录", Toast.LENGTH_SHORT)
                // 可以跳转到登录页面
                return@setOnClickListener
            }
            
            // 跳转到创建预约页面
            val intent = Intent(this, CreateAppointmentActivity::class.java).apply {
                putExtra("DOCTOR_ID", doctorId)
                putExtra("APPOINTMENT_TYPE", 1) // 1表示视频问诊
            }
            startActivity(intent)
        }
    }
}