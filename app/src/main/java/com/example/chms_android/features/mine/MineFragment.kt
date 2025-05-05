package com.example.chms_android.features.mine

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.chms_android.databinding.FragmentMineBinding
import com.example.chms_android.features.mine.activity.ReservationActivity
import com.example.chms_android.features.mine.activity.ReservationManageActivity
import com.example.chms_android.features.mine.activity.UserEditActivity
import com.example.chms_android.features.report.activity.ReportShowActivity
import com.example.chms_android.login.activity.LoginActivity
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ImagePickerUtil
import com.example.chms_android.utils.ToastUtil

class MineFragment : Fragment(), ImagePickerUtil.ImagePickerCallback {
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerUtil: ImagePickerUtil
    // 添加ViewModel和加载对话框
    private lateinit var viewModel: MineViewModel
    private lateinit var loadingDialog: LoadingDialog

    // Fragment 注册活动结果启动器
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                onImagePicked(it)
                ToastUtil.show(requireContext(), "头像已更新")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        // 实例化工具类
        imagePickerUtil = ImagePickerUtil(requireActivity(), this, pickImageLauncher)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(MineViewModel::class.java)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 观察上传状态
        viewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is MineViewModel.UploadStatus.Loading -> {
                    // 显示加载中
                    if (!::loadingDialog.isInitialized) {
                        loadingDialog = LoadingDialog(requireContext())
                    }
                    loadingDialog.show()
                }
                is MineViewModel.UploadStatus.Success -> {
                    // 隐藏加载中
                    if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    
                    // 使用Glide加载远程图片
                    Glide.with(requireContext())
                        .load(status.imageUrl)
                        .into(binding.civMineAvatar)
                    
                    ToastUtil.show(requireContext(), "头像上传成功")
                }
                is MineViewModel.UploadStatus.Error -> {
                    // 隐藏加载中
                    if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    
                    // 显示错误信息
                    ToastUtil.show(requireContext(), "上传失败: ${status.message}")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMineBinding.inflate(inflater, container, false)

        setListener()

        return binding.root
    }

    // 监听器设置
    private fun setListener() {
        // 个人资料编辑
        binding.ibMineEditUser.setOnClickListener {
            val intent = Intent(requireActivity(), UserEditActivity::class.java)
            startActivity(intent)
        }
        // 头像的点击事件
        binding.civMineAvatar.setOnClickListener {
            imagePickerUtil.checkPermissionAndPickImage()
        }
        // 在线就诊
        binding.rcFmReservation.setOnClickListener {
            val intent = Intent(requireActivity(), ReservationActivity::class.java)
            startActivity(intent)
        }
        // 预约管理
        binding.rcFmReservationManage.setOnClickListener {
            val intent = Intent(requireActivity(), ReservationManageActivity::class.java)
            startActivity(intent)
        }
        binding.rcFmReportManage.setOnClickListener {
            val intent = Intent(requireActivity(), ReportShowActivity::class.java)
            startActivity(intent)
        }
        // 退出登录
        binding.btnMineLogOut.setOnClickListener {
            AccountUtil(requireContext()).clearUser()
            AccountUtil(requireContext()).clearUserId()

            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context?.startActivity(intent)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MineFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    // 返回图片选择结果
    override fun onImagePicked(imageUri: Uri) {
        // 先显示本地图片（立即反馈）
        binding.civMineAvatar.setImageURI(imageUri)
        
        // 上传图片到服务器
        viewModel.uploadAvatar(requireContext(), imageUri)
    }

    // 处理权限被拒绝的情况
    override fun onPermissionDenied() {
        ToastUtil.show( requireActivity(),"存储权限请求失败")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePickerUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}