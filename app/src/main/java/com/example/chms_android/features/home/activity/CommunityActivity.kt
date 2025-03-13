package com.example.chms_android.features.home.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chms_android.R
import com.example.chms_android.data.User
import com.example.chms_android.databinding.ActivityCommunityBinding
import com.example.chms_android.features.home.adapter.CommunityAdapter
import com.example.chms_android.features.home.vm.CommunityActivityVM
import com.example.chms_android.repo.CommunityRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CommunityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommunityBinding
    private lateinit var viewModel: CommunityActivityVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()

        viewModel = ViewModelProvider(this).get(CommunityActivityVM::class.java)

        initRecyclerView()

    }

    private fun initView() {
        val user = AccountUtil(this).getUser()
        binding.tvAcCommunityName.text = user?.community
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(user: User) {
        // Handle the event
        initView()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun initRecyclerView() {
        initCommunityList()
        // 设置 GridLayoutManager，指定列数为2
        binding.recyclerViewAcCommunities.layoutManager = GridLayoutManager(this, 2)

        viewModel.communityList.observe(this, Observer { communityList ->
            // 设置适配器
            binding.recyclerViewAcCommunities.adapter = CommunityAdapter(this, communityList)
        })
    }

    private fun initCommunityList() {
        CommunityRepo.getAllCommunityFromDb(
            onComplete = { communityList ->
                viewModel.setCommunityList(communityList)
            },
            onError = { error ->
                ToastUtil.show(this, "Error fetching communities: ${error.message}", Toast.LENGTH_SHORT)
            }
        )
    }
}