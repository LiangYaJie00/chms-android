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
import com.example.chms_android.databinding.FragmentMineBinding
import com.example.chms_android.features.mine.activity.ReservationActivity
import com.example.chms_android.login.activity.LoginActivity
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ImagePickerUtil
import com.example.chms_android.utils.ToastUtil

class MineFragment : Fragment(), ImagePickerUtil.ImagePickerCallback {
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerUtil: ImagePickerUtil

    // Fragment 注册活动结果启动器
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
        arguments?.let {
        }

        // 实例化工具类
        imagePickerUtil = ImagePickerUtil(requireActivity(), this, pickImageLauncher)
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
        // 头像的点击事件
        binding.civMineAvatar.setOnClickListener {
            imagePickerUtil.checkPermissionAndPickImage()
        }
        // 在线就诊
        binding.rcFmReservation.setOnClickListener {
            val intent = Intent(requireActivity(), ReservationActivity::class.java)
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
        binding.civMineAvatar.setImageURI(imageUri)
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