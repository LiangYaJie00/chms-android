package com.example.chms_android.features.mine.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityUserEditBinding
import com.example.chms_android.features.home.activity.CommunityActivity
import com.example.chms_android.features.mine.viewmodel.UserEditViewModel
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.ImagePickerUtil
import com.example.chms_android.utils.ToastUtil

class UserEditActivity : AppCompatActivity(), ImagePickerUtil.ImagePickerCallback {
    
    private lateinit var binding: ActivityUserEditBinding
    private lateinit var viewModel: UserEditViewModel
    private lateinit var imagePickerUtil: ImagePickerUtil
    private lateinit var loadingDialog: LoadingDialog
    
    // 注册活动结果启动器
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                onImagePicked(it)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 设置状态栏为透明，并让内容延伸到状态栏下方
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30)及以上使用新的API
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // 确保状态栏图标为白色（深色背景）
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = false // false表示状态栏图标为白色
                isAppearanceLightNavigationBars = true // true表示导航栏图标为黑色（白色背景）
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 (API 23)及以上
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR // 添加导航栏图标为黑色（白色背景）
            // 不添加 SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 标志，保持状态栏图标为白色
        } else {
            // 低版本Android
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.WHITE // 设置导航栏背景为白色

        // 初始化ViewBinding
        binding = ActivityUserEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(UserEditViewModel::class.java)
        
        // 初始化图片选择工具
        imagePickerUtil = ImagePickerUtil(this, this, pickImageLauncher)
        
        // 初始化加载对话框
        loadingDialog = LoadingDialog(this)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 设置观察者
        setupObservers()
        
        // 加载用户数据
        viewModel.loadUserInfo(this)
        
        // 设置监听器
        setupListeners()
    }
    
    private fun setupObservers() {
        // 观察用户数据变化
        viewModel.user.observe(this) { user ->
            if (user != null) {
                // 加载头像
                if (!user.avatar.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(user.avatar)
                        .placeholder(R.drawable.default_avatar_rect)
                        .error(R.drawable.default_avatar_rect)
                        .into(binding.ivUserAvatar)
                } else {
                    // 如果头像为null或空，显示默认头像
                    binding.ivUserAvatar.setImageResource(R.drawable.default_avatar_rect)
                }

                binding.tvName.setText(user.name)
                binding.tvEmail.setText(user.email)
                binding.tvCommunity.setText(user.community ?: "未选择社区")
                binding.tvCommunityLabel.setText(user.community ?: "未选择社区")

                // 填充表单字段
                binding.fieldName.setContent(user.name)
                binding.fieldAge.setContent(user.age.toString())
                binding.fieldGender.setContent(if (user.gender == 1) "男" else "女")
                binding.fieldEmail.setContent(user.email)
                binding.fieldPhone.setContent(user.phone.toString())
                binding.fieldWeight.setContent(user.weight.toString())
                binding.fieldHeight.setContent(user.height.toString())
            } else {
                // 用户数据为空，显示错误信息
                ToastUtil.show(this, "无法获取用户信息")
                finish() // 关闭页面，因为没有用户数据无法编辑
            }
        }
        
        // 观察上传状态
        viewModel.uploadStatus.observe(this) { status ->
            when (status) {
                is UserEditViewModel.UploadStatus.Loading -> {
                    // 显示加载中
                    loadingDialog.show()
                }
                is UserEditViewModel.UploadStatus.Success -> {
                    // 隐藏加载中
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    
                    // 显示成功信息
                    ToastUtil.show(this, "头像上传成功")
                }
                is UserEditViewModel.UploadStatus.UpdateSuccess -> {
                    // 隐藏加载中
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    
                    // 关闭当前页面
                    finish()
                }
                is UserEditViewModel.UploadStatus.Error -> {
                    // 隐藏加载中
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    
                    // 显示错误信息
                    ToastUtil.show(this, "操作失败: ${status.message}")
                }
            }
        }
    }
    
    private fun setupListeners() {
        // 设置保存按钮
        binding.tvSaveButton.setOnClickListener {
            saveUserInfo()
        }

        // 社区选择
        binding.llCommunity.setOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }
        
        // 设置头像点击事件
        binding.ivUserAvatar.setOnClickListener {
            imagePickerUtil.checkPermissionAndPickImage()
        }

        // 头像选择
        binding.ivUserAvatarEdit.setOnClickListener {
            imagePickerUtil.checkPermissionAndPickImage()
        }
    }
    
    private fun saveUserInfo() {
        // 获取编辑后的值
        val name = binding.fieldName.getContent()
        val ageStr = binding.fieldAge.getContent()
        val genderStr = binding.fieldGender.getContent()
        val phoneStr = binding.fieldPhone.getContent()
        val weightStr = binding.fieldWeight.getContent()
        val heightStr = binding.fieldHeight.getContent()
        
        // 验证输入
        if (name.isEmpty()) {
            ToastUtil.show(this, "用户名不能为空")
            return
        }
        
        // 调用ViewModel更新用户信息
        viewModel.updateUserInfo(
            this,
            name,
            ageStr,
            genderStr,
            phoneStr,
            weightStr,
            heightStr
        )
    }
    
    // 返回图片选择结果
    override fun onImagePicked(imageUri: Uri) {
        // 先显示本地图片（立即反馈）
        binding.ivUserAvatar.setImageURI(imageUri)
        
        // 上传图片到服务器
        viewModel.uploadAvatar(this, imageUri)
    }
    
    // 处理权限被拒绝的情况
    override fun onPermissionDenied() {
        ToastUtil.show(this, "存储权限请求失败")
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePickerUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 确保对话框关闭，避免内存泄漏
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}