package com.example.chms_android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.databinding.ActivityMainBinding
import com.example.chms_android.login.activity.LoginActivity

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainActivityVM: MainActivityVM
    lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sp = getPreferences(Context.MODE_PRIVATE)
        val tvMain = sp.getString("tv_main", "Hello World!") ?: "Hello World!"
        mainActivityVM = ViewModelProvider(this, MainActivityVMFactory(tvMain)).get(MainActivityVM::class.java)
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

    private fun observeViewModel() {
        mainActivityVM.tvMain.observe(this, Observer { tvMain ->
            binding.tvMain.text = tvMain.toString()
            Toast.makeText(this, tvMain, Toast.LENGTH_SHORT).show()
        })
    }

    private fun initListeners() {
        binding.tvMain.setOnClickListener {
            mainActivityVM.changeTvMain()
        }

        binding.btnMainToLogin.setOnClickListener {
            try {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } catch (e: Error) {
                Log.e("MainActivity", e.toString())
            }
        }
    }

}