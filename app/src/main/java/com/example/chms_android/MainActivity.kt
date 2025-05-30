package com.example.chms_android

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chms_android.databinding.ActivityMainBinding
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.TUIUtil
import com.example.chms_android.utils.WindowUtil
import com.example.chms_android.utils.NetworkUtil
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Button
import com.example.chms_android.common.Constants
import com.example.chms_android.features.analysis.activity.ReportAnalysisActivity
import com.example.chms_android.features.home.activity.DoctorDetailActivity
import com.example.chms_android.utils.JPushHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import org.json.JSONException
import com.example.chms_android.utils.NotificationPermissionUtil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainActivityVM: MainActivityVM
    lateinit var sp: SharedPreferences
    
    // 添加一个标志，表示是否处于离线模式
    var isOfflineMode = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 检查是否处于离线模式
        isOfflineMode = intent.getBooleanExtra("OFFLINE_MODE", false)
        
        TUIUtil.initTUI(this)
        
        // 检查通知权限
        checkNotificationPermission()
        
        initNavView()

        sp = getPreferences(Context.MODE_PRIVATE)
        val tvMain = sp.getString("tv_main", "Hello World!") ?: "Hello World!"
        mainActivityVM = ViewModelProvider(this, MainActivityVMFactory(tvMain, this)).get(MainActivityVM::class.java)
        Log.d("MainActivity", "activity onCreate")

        initListeners()

        observeViewModel()

        // 处理从通知启动的情况
        handleNotificationIntent(intent)
    }

    // 提供一个公共方法，让Fragment可以检查是否处于离线模式
    fun isInOfflineMode(): Boolean {
        return isOfflineMode
    }
    
    // 提供一个公共方法，用于检查服务器连接
    fun checkServerAndReconnect(callback: (Boolean) -> Unit) {
        // 显示加载中提示
        Toast.makeText(this, "正在尝试重新连接服务器...", Toast.LENGTH_SHORT).show()
        
        // 在后台线程中检查服务器连接
        Thread {
            val isServerReachable = NetworkUtil.checkServerReachable(Constants.TEST_URL)
            
            runOnUiThread {
                if (isServerReachable) {
                    // 服务器可达
                    isOfflineMode = false
                    Toast.makeText(this, "已恢复网络连接", Toast.LENGTH_SHORT).show()
                    
                    // 重新加载需要网络的数据
                    loadNetworkData()
                    
                    // 通知回调
                    callback(true)
                } else {
                    // 服务器仍然不可达
                    Toast.makeText(this, "服务器仍然不可用，继续使用离线模式", Toast.LENGTH_LONG).show()
                    
                    // 通知回调
                    callback(false)
                }
            }
        }.start()
    }

    private fun loadNetworkData() {
        // 加载需要网络连接的数据
        DoctorRepo.getAllNetDoctors(this)
        // 其他网络数据加载...
    }

    override fun onPause() {
        super.onPause()
        sp.edit().apply {
            putString("tv_main", mainActivityVM.tvMain.value ?: "")
            apply()
        }
        Log.d("MainActivity", "activity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivityVM.clear()
    }

    private fun initNavView() {
        // 安全地隐藏 ActionBar
        supportActionBar?.hide()

        val navController = findNavController(R.id.fragment_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_report,
                R.id.nav_knowledge,
                R.id.nav_mine,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navMainView.setupWithNavController(navController)

//        adaptStatusBar()

    }

    private fun adaptStatusBar() {
        val navController = findNavController(R.id.fragment_main)

        val statusBarHeight = WindowUtil(this).getStatusBarHeight()

        binding.navMainView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    setViewTopMargins(binding.mainPage, top = statusBarHeight)
                    WindowUtil.setStatusBarTextColor(this, false)
                    // 设置状态栏颜色
                    window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
                    // 设置布局扩展到导航栏下，但不扩展到状态栏下
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    navController.navigate(R.id.nav_home)
                    true // Return true to select this menu item
                }
                R.id.nav_report -> {
                    setViewTopMargins(binding.mainPage, top = statusBarHeight)
                    WindowUtil.setStatusBarTextColor(this, false)
                    // 设置状态栏颜色
                    window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
                    // 设置布局扩展到导航栏下，但不扩展到状态栏下
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    navController.navigate(R.id.nav_report)
                    true
                }
                R.id.nav_knowledge -> {
                    setViewTopMargins(binding.mainPage, top = statusBarHeight)
                    WindowUtil.setStatusBarTextColor(this, false)
                    // 设置状态栏颜色
                    window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
                    // 设置布局扩展到导航栏下，但不扩展到状态栏下
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    navController.navigate(R.id.nav_knowledge)
                    true
                }
                R.id.nav_mine -> {
                    setViewTopMargins(binding.mainPage, top = 0)
                    WindowUtil.setStatusBarTextColor(this, true)
                    // 设置窗口标志，扩展内容到状态栏和导航栏
                    window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    navController.navigate(R.id.nav_mine)
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        mainActivityVM.tvMain.observe(this, Observer { tvMain ->
//            binding.tvMain.text = tvMain.toString()
            Toast.makeText(this, tvMain, Toast.LENGTH_SHORT).show()
        })
    }

    private fun initListeners() {
//        binding.tvMain.setOnClickListener {
//            mainActivityVM.changeTvMain()
//        }
//
//        binding.btnMainToLogin.setOnClickListener {
//            try {
//                val intent = Intent(this, LoginActivity::class.java)
//                startActivity(intent)
//            } catch (e: Error) {
//                Log.e("MainActivity", e.toString())
//            }
//        }
    }

    private fun setViewTopMargins(view: View, top: Int) {
        // 获取当前的布局参数
        val params = view.layoutParams as ViewGroup.MarginLayoutParams

        // 设置新的 margin 值
        params.setMargins(0, top, 0, 0)

        // 将新的布局参数应用到视图
        view.layoutParams = params
    }

    private fun handleNotificationIntent(intent: Intent) {
        val extras = intent.getStringExtra("NOTIFICATION_EXTRAS")
        if (extras != null) {
            try {
                val extrasJson = JSONObject(extras)
                // 根据通知内容执行相应操作
                val type = extrasJson.optString("type")
                
                Log.d("MainActivity", "收到通知: type=$type")
                
                // 根据通知类型执行不同操作
                when (type) {
                    "healthReport" -> {
                        // 处理健康报告通知
                        val reportId = extrasJson.optString("reportId", "0").toInt()
                        // 导航到健康报告分析页面
                        val intent = Intent(this, ReportAnalysisActivity::class.java).apply {
                            putExtra("reportId", reportId.toLong())
                        }
                        startActivity(intent)
                    }
                    "advice" -> {
                        // 处理医生建议通知
                        val adviceId = extrasJson.optString("adviceId", "0").toInt()
                        // 导航到医生建议详情页面
                        val intent = Intent(this, DoctorDetailActivity::class.java).apply {
                            putExtra("adviceId", adviceId)
                        }
                        startActivity(intent)
                    }
                    "health_report" -> {
                        // 导航到健康报告页面
//                        navController.navigate(R.id.nav_report)
                    }
                    "doctor_message" -> {
                        // 导航到医生消息页面
//                        navController.navigate(R.id.nav_message)
                    }
                    // 其他类型...
                }
                
                // 清除角标
                JPushHelper.clearBadge(this)
            } catch (e: JSONException) {
                Log.e("MainActivity", "解析通知额外信息失败", e)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)  // 确保调用父类方法
        handleNotificationIntent(intent)
    }

    private fun checkPushPermissions() {
        val manufacturer = Build.MANUFACTURER.toLowerCase()
        
        if (manufacturer.contains("huawei") || manufacturer.contains("honor") || 
            manufacturer.contains("xiaomi") || manufacturer.contains("oppo") || 
            manufacturer.contains("vivo") || manufacturer.contains("meizu")) {
            
            AlertDialog.Builder(this)
                .setTitle("推送权限设置")
                .setMessage("为了确保您能及时收到消息通知，请允许应用自启动和后台运行。\n\n" +
                        "请前往设置中开启相关权限。")
                .setPositiveButton("去设置") { _, _ ->
                    try {
                        val intent = Intent()
                        when {
                            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                                intent.component = ComponentName("com.huawei.systemmanager",
                                    "com.huawei.systemmanager.optimize.process.ProtectActivity")
                            }
                            manufacturer.contains("xiaomi") -> {
                                intent.component = ComponentName("com.miui.securitycenter", 
                                    "com.miui.permcenter.autostart.AutoStartManagementActivity")
                            }
                            manufacturer.contains("oppo") -> {
                                intent.component = ComponentName("com.coloros.safecenter", 
                                    "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                            }
                            manufacturer.contains("vivo") -> {
                                intent.component = ComponentName("com.vivo.permissionmanager", 
                                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                            }
                            else -> {
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                intent.data = Uri.fromParts("package", packageName, null)
                            }
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to open settings", e)
                        // 如果特定页面打开失败，打开应用详情页
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    }
                }
                .setNegativeButton("稍后再说", null)
                .show()
        }
    }

    // 检查通知权限
    private fun checkNotificationPermission() {
        NotificationPermissionUtil.requestPermissionInActivity(
            activity = this,
            onGranted = {
                Log.d("MainActivity", "通知权限已授予")
                // 可以在这里执行需要通知权限的操作
            },
            onDenied = {
                Log.d("MainActivity", "用户拒绝了通知权限")
                // 可以在这里处理用户拒绝权限的情况
            }
        )
    }
}
