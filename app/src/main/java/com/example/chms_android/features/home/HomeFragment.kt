package com.example.chms_android.features.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.MainActivity
import com.example.chms_android.R
import com.example.chms_android.data.User
import com.example.chms_android.databinding.FragmentHomeBinding
import com.example.chms_android.features.home.activity.CommunityActivity
import com.example.chms_android.features.health.activity.DailyHealthReportActivity
import com.example.chms_android.features.home.activity.DeviceManagerActivity
import com.example.chms_android.features.home.activity.DoctorsActivity
import com.example.chms_android.features.home.adapter.DoctorShowsAdapter
import com.example.chms_android.features.home.vm.HomeFragmentVM
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeFragmentVM
    private var offlineBanner: View? = null
    private var isBannerVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeFragmentVM::class.java)

        initView()
        setListener()
        initDoctorRecyclerView()
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        // 观察离线模式状态
        viewModel.isOfflineMode.observe(viewLifecycleOwner) { isOffline ->
            Log.d("HomeFragment", "Offline mode changed: $isOffline, isBannerVisible=$isBannerVisible")
            if (isOffline && !isBannerVisible) {
                showOfflineBanner()
            } else if (!isOffline && isBannerVisible) {
                removeOfflineBanner()
            }
        }
        
        // 观察重试连接结果
        viewModel.connectionRetryResult.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                removeOfflineBanner()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 在视图完全创建后检查离线模式
        viewModel.checkOfflineMode(requireContext())
    }

    override fun onResume() {
        super.onResume()

        // 在Fragment恢复时也检查离线模式
        checkOfflineMode()
    }

    private fun checkOfflineMode() {
        try {
            val mainActivity = activity as MainActivity
            val isOffline = mainActivity.isInOfflineMode()

            // 记录当前检查结果
            Log.d("HomeFragment", "Checking offline mode: isOffline=$isOffline, isBannerVisible=$isBannerVisible")

            if (isOffline && !isBannerVisible) {
                // 需要显示横幅但当前未显示
                showOfflineBanner()
            } else if (!isOffline && isBannerVisible) {
                // 不需要显示横幅但当前正在显示
                removeOfflineBanner()
            }
            // 其他情况保持当前状态
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error checking offline mode: ${e.message}")
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(user: User) {
        // Handle the event
        initView()
    }

    private fun initView() {
        val user = AccountUtil(requireContext()).getUser()
        binding.tvFhUserName.text = user?.name
        if (user?.community == null) {
            binding.tvFhWelcome.text = "欢迎，"
            binding.tvFhCommunity.text = "请选择你的社区"
        } else {
            binding.tvFhWelcome.text = "欢迎来到" + user.community + ","
            binding.tvFhCommunity.text = user?.community
        }
    }

    private fun setListener() {
        binding.cdFhCommunityChose.setOnClickListener {
            val intent = Intent(context, CommunityActivity::class.java)
            startActivity(intent)
        }

        binding.cdManageDevices.setOnClickListener {
            val intent = Intent(context, DeviceManagerActivity::class.java)
            startActivity(intent)
        }

        binding.tvFhShowAll.setOnClickListener {
            val intent = Intent(context, DoctorsActivity::class.java)
            startActivity(intent)
        }

        binding.cdFhHealthInfo.setOnClickListener {
            val intent = Intent(context, DailyHealthReportActivity::class.java).apply {
                // 设置为从上报入口进入
                putExtra("entryMode", 0)
            }
            startActivity(intent)
        }
    }

    private fun initDoctorRecyclerView() {
        initDoctorList()
        binding.recyclerViewFhDoctor.layoutManager = LinearLayoutManager(requireContext())
        viewModel.doctorList.observe(requireActivity(), Observer { doctorList ->
            binding.recyclerViewFhDoctor.adapter = DoctorShowsAdapter(requireContext(), doctorList)
        })
    }

    private fun initDoctorList() {
        DoctorRepo.getFirstSevenDoctorsFromDb(
            onComplete = { doctors ->
                viewModel.setDoctorList(doctors)
            },
            onError = { error ->
                // 处理错误
                ToastUtil.show(requireContext(), "Error fetching doctors: ${error.message}", Toast.LENGTH_SHORT)
            }
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private fun showOfflineBanner() {
        try {
            Log.d("HomeFragment", "Showing offline banner")

            // 创建离线模式横幅
            offlineBanner = layoutInflater.inflate(R.layout.offline_mode_banner, null)

            // 获取容器
            val container = binding.fragmentHome

            if (container != null && container is LinearLayout) {
                // 在LinearLayout中，添加到索引1的位置（TitleBar之后）
                container.addView(offlineBanner, 1)

                // 设置重试按钮点击事件
                offlineBanner?.findViewById<Button>(R.id.btn_retry_connection)?.setOnClickListener {
                    val mainActivity = activity as MainActivity
                    mainActivity.checkServerAndReconnect { isConnected ->
                        if (isConnected) {
                            // 如果连接成功，移除横幅
                            removeOfflineBanner()
                        }
                    }
                }

                // 标记横幅已显示
                isBannerVisible = true
            } else {
                Log.e("HomeFragment", "Container view is null or not a LinearLayout")
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error showing offline banner: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun removeOfflineBanner() {
        try {
            Log.d("HomeFragment", "Removing offline banner")

            if (offlineBanner != null) {
                val container = binding.fragmentHome
                if (container != null && container is LinearLayout) {
                    container.removeView(offlineBanner)
                    offlineBanner = null

                    // 标记横幅已移除
                    isBannerVisible = false
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error removing offline banner: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 在视图销毁时重置状态
        offlineBanner = null
        isBannerVisible = false
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
