package com.example.chms_android.features.home.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.chms_android.R
import com.example.chms_android.data.Community
import com.example.chms_android.repo.UserRepo
import com.example.chms_android.utils.AccountUtil
import kotlin.random.Random

class CommunityAdapter(
    val context: Context,
    private var itemList: List<Community>
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityAdapter.ViewHolder, position: Int) {
        val community = itemList[position]

        val bgResources = listOf(
            R.mipmap.bg_community03,
            R.mipmap.bg_community04,
            R.mipmap.bg_community05,
            R.mipmap.bg_community06
        )
        // 从列表中选择一个随机的资源 ID
        val randomBgResId = bgResources[Random.nextInt(bgResources.size)]
        Glide.with(context).load(randomBgResId)
            .into(holder.ivIcBg)

        holder.tvIcCommunityName.text = community.name
        holder.tvIcCommunityPosition.text = community.city + community.state

        holder.cdIcCommunityCard.setOnClickListener {
            showConfirmationDialog(community)
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    // 显示确认对话框
    private fun showConfirmationDialog(community: Community) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("确认")
        builder.setMessage("您是否要更新您的社区为: ${community.name}?")

        builder.setPositiveButton("确定") { dialog, _ ->
            updateUserCommunityName(community.name)
            dialog.dismiss()
        }

        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // 更新用户社区名称的逻辑
    private fun updateUserCommunityName(newCommunityName: String) {
        var user = AccountUtil(context).getUser()
        user?.community = newCommunityName
        if (user != null) {
            UserRepo.updateUser(user, context, onSuccess = { updatedUser ->
                Toast.makeText(context, "社区已更新为: $newCommunityName", Toast.LENGTH_SHORT).show()
            })
        }
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val ivIcBg: ImageView = view.findViewById(R.id.iv_ic_bg)
        val tvIcCommunityName: TextView = view.findViewById(R.id.tv_ic_community_name)
        val tvIcCommunityPosition: TextView = view.findViewById(R.id.tv_ic_community_position)
        val cdIcCommunityCard: CardView = view.findViewById(R.id.cd_ic_community_card)
    }
}