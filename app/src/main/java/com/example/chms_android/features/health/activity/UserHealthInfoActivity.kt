package com.example.chms_android.features.health.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityUserHealthInfoBinding
import com.example.chms_android.features.health.fragment.UserHealthInfoViewFragment

class UserHealthInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserHealthInfoBinding

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"

        fun start(context: Context, userId: Int) {
            val intent = Intent(context, UserHealthInfoActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHealthInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)


        val userId = intent.getIntExtra(EXTRA_USER_ID, -1)
        if (userId == -1) return

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, UserHealthInfoViewFragment.newInstance(userId))
                .commit()
        }
    }

}