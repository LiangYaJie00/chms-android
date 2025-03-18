package com.example.chms_android

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
import com.example.chms_android.utils.WindowUtil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainActivityVM: MainActivityVM
    lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        setContentView(binding.root)

        initNavView()

        sp = getPreferences(Context.MODE_PRIVATE)
        val tvMain = sp.getString("tv_main", "Hello World!") ?: "Hello World!"
        mainActivityVM = ViewModelProvider(this, MainActivityVMFactory(tvMain, this)).get(MainActivityVM::class.java)
        Log.d("MainActivity", "activity onCreate")

        initListeners()

        observeViewModel()

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
        supportActionBar!!.hide()

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
}