package com.example.chms_android

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainActivityVM: MainActivityVM
    lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        DoctorRepo.getAllNetDoctors(this)

//        // 设置窗口标志，扩展内容到状态栏和导航栏
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                )

        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        setContentView(binding.root)

        initNavView()

        sp = getPreferences(Context.MODE_PRIVATE)
        val tvMain = sp.getString("tv_main", "Hello World!") ?: "Hello World!"
        mainActivityVM = ViewModelProvider(this, MainActivityVMFactory(tvMain)).get(MainActivityVM::class.java)
        Log.d("MainActivity", "activity onCreate")

        initListeners()

        observeViewModel()

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

}